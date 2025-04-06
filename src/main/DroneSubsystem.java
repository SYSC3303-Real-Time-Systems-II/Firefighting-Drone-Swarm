import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DroneSubsystem implements Runnable {


    private final String name;
    private final DatagramSocket schedulerSocket; // For Scheduler on port 6000
    private final DatagramSocket droneSocket;     // For Drones on port 6001
    private DroneSubsystemState currentState = DroneSubsystemState.WAITING;

    // Drone management
    private final List<Drone> drones = new CopyOnWriteArrayList<>();
    private List<InputEvent> pendingEvents = new ArrayList<>();
    private DroneModel droneModel;

    public DroneSubsystem(String name, int numDrones) {
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.STARTING, null, null);

        this.name = name;
        // Initialize drone fleet
        for(int i = 0; i < numDrones; i++) {
            Drone drone = new Drone();
            drones.add(drone);
            new Thread(drone).start();
        }

        //start drone model thread after its given the
        droneModel = new DroneModel(drones);
        new Thread(droneModel).start();

        try {
            this.schedulerSocket = new DatagramSocket(6000);
            this.schedulerSocket.setSoTimeout(2000);
            this.droneSocket = new DatagramSocket(6001);
            this.droneSocket.setSoTimeout(2000);

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize UDP socket", e);
        }
    }


    @Override
    public void run() {
        System.out.println("["+this.name + "] SUBSYSTEM STARTED WITH " + drones.size() + " DRONES.");

        while(true) {
            switch(currentState) {
                case WAITING:
                    handleWaitingState();
                    break;

                case RECEIVED_EVENT_FROM_SCHEDULER:
                    handleReceivedEventState();
                    break;

                case SENDING_EVENT_TO_SCHEDULER:
                    handleSendingConfirmationState();
                    break;
            }
        }
    }

    public void handleWaitingState() {

        try {
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            schedulerSocket.receive(packet); // Receives a packet from the Scheduler
            InputEvent event = deserializeEvent(packet.getData()); // Deserializes the data
            System.out.println("["+this.name + "] RECEIVED EVENT --> " + "INPUT_EVENT_" + event.getEventID() + " (" +  event + ")" + " FROM: " + "SCHEDULER"); // Prints a message that it has received the data
            pendingEvents.add(event);
            MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.RECEIVED_EVENT, event, null);
            currentState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER;

        }catch (SocketTimeoutException e) {
            currentState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER;

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleReceivedEventState() {
        //iterate the pending events and send them out
        for (int i=0; i < pendingEvents.size(); i++) {
            InputEvent currentEvent = pendingEvents.get(i);
            try {
                Drone selectedDrone = chooseDroneAlgorithm(currentEvent);
                if (selectedDrone != null ) {
                    byte[] data = serializeEvent(currentEvent); // Serializes the event
                    // Sends it to that specific drone
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), selectedDrone.getPortID());
                    droneSocket.send(packet); // Sends the damn packet
                    selectedDrone.setAssignedEvent(currentEvent);
                    pendingEvents.remove(currentEvent);
                    System.out.println("[" + this.name + "] ASSIGNED INPUT_EVENT_" + currentEvent.getEventID() + " TO: " + selectedDrone.getName()); // Prints the name of the drone that was assigned the event
                }
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Moves to the next state

    }

    public void handleSendingConfirmationState() {
        boolean reQueueEvent = false;
        try {
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            droneSocket.receive(packet); // Gets the event from the drone
            InputEvent receivedEvent = deserializeEvent(packet.getData()); // Deserializes the data
            if (receivedEvent.getFaultType() == null) {

                if(receivedEvent.getRemainingAgentNeeded() <= 0) {
                    System.out.println("[" + this.name + "]" + " COMPLETED INPUT_EVENT_" + receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");

                } else if (receivedEvent.getRemainingAgentNeeded() > 0 ) {
                    System.out.println("[" + name + "] RE-QUEUED EVENT " + receivedEvent.getEventID() + " (" + receivedEvent.getRemainingAgentNeeded() + "L remaining)");
                    pendingEvents.add(receivedEvent);
                    reQueueEvent = true;
                }

            } else if(receivedEvent.getFaultType() != null) {
                System.out.println("[" + this.name + "]" + " FAILED TO COMPLETE INPUT_EVENT_" + receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");

            }
            if (!reQueueEvent) {
                sendConfirmation(receivedEvent); // Sends the event back to the scheduler
            }

            currentState = DroneSubsystemState.WAITING;

        } catch (SocketTimeoutException e){
            currentState = DroneSubsystemState.WAITING;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Drone chooseDroneAlgorithm(InputEvent event) {
        Coordinate eventCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        // Iterate over ACTUALLY available drones from DroneModel
        for (Drone drone : droneModel.getAvailableDrones()) {
            Coordinate droneCoords = drone.getCurrentCoordinates();
            double distance = calculateDistance(eventCoords, droneCoords);

            if (distance < minDistance) {
                minDistance = distance;
                closestDrone = drone;
            }
        }
        return closestDrone;
    }

    public double calculateDistance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) +
                Math.pow(a.getY() - b.getY(), 2));
    }

    private void sendConfirmation(InputEvent event) {
        try {
            byte[] data = serializeEvent(event);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 5001);
            schedulerSocket.send(packet);
            System.out.println("["+this.name + "] SENDING EVENT TO SCHEDULER --> " + "INPUT_EVENT_" + event.getEventID() + " (" +event.toString() + ")"); // Sends the message back to the Scheduler
        } catch (IOException e) {
            System.err.println("Failed to send confirmation: " + e.getMessage());
        }
    }

    public byte[] serializeEvent(InputEvent event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(event);
        return bos.toByteArray();
    }

    public InputEvent deserializeEvent(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (InputEvent) ois.readObject();
    }

    public void startGUIUpdates() {
        new Thread(() -> {
            try (DatagramSocket guisocket = new DatagramSocket()) {
                while (true) {
                    // Gather statuses from droneModel
                    List<DroneStatus> statuses = new ArrayList<>();
                    Map<String, Coordinate> coords = droneModel.getCoordinates();
                    Map<String, DroneStateMachine> states = droneModel.getStates();

                    for (String droneName: coords.keySet()) {
                        Coordinate coord = coords.get(droneName);
                        DroneStateMachine stateMachine = states.get(droneName);
                        // Convert 'stateMachine' to a short string
                        String stateName = stateMachine.getClass().getSimpleName();
                        statuses.add(new DroneStatus(droneName, stateName, coord.getX(), coord.getY()));
                    }
                    // Send to GUI
                    byte[] data = serialize(statuses);
                    DatagramPacket statusPacket = new DatagramPacket(
                            data, data.length,
                            InetAddress.getLocalHost(), 8000
                    );
                    guisocket.send(statusPacket);


                    // Gather and send metrics

                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("droneResponseTime", MetricAnalysisLogger.getDroneResponseTime());
                    metrics.put("fireExtinguishedResponseTime", MetricAnalysisLogger.getFireExtinguishedResponseTime());
                    metrics.put("throughput", MetricAnalysisLogger.getThroughput());
                    metrics.put("utilizations", MetricAnalysisLogger.getDronesUtilization());

                    byte[] metricsData = serialize(metrics);
                    DatagramPacket metricsPacket = new DatagramPacket(
                            metricsData, metricsData.length, InetAddress.getLocalHost(), 8000
                    );
                    guisocket.send(metricsPacket);

                    Thread.sleep(2000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    public static void main(String[] args) {
        try {
            DroneSubsystem subsystem = new DroneSubsystem("DS", 10);
            new Thread(subsystem).start();
            subsystem.startGUIUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}