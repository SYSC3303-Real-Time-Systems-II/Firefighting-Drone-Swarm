import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DroneSubsystem implements Runnable {


    private final String name;
    private final DatagramSocket socket;
    private DroneSubsystemState currentState = DroneSubsystemState.WAITING;

    // Drone management
    private final List<Drone> drones = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedQueue<Drone> availableDrones = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Integer, Drone> workingDrones = new ConcurrentHashMap<>();

    private InputEvent currentEvent;
    private DroneModel droneModel;

    public DroneSubsystem(String name, int numDrones) {
        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.STARTING, null, null);

        this.name = name;
        // Initialize drone fleet
        for(int i = 0; i < numDrones; i++) {
            Drone drone = new Drone();
            drones.add(drone);
            availableDrones.add(drone);
            new Thread(drone).start();
        }

        //start drone model thread after its given the
        droneModel = new DroneModel(drones);
        new Thread(droneModel).start();

        try {
            this.socket = new DatagramSocket(6000);
            this.socket.setSoTimeout(2000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize UDP socket", e);
        }
    }

    /**
     * Gets the input event for the drone subsystem which can be received from the scheduler or sent to the
     * scheduler. FOR TESTING PURPOSES
     */
    public InputEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Set the input event for the drone subsystem. FOR TESTING PURPOSES.
     */
    public void setCurrentEvent(InputEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * Gets the hashmap for the working drones. FOR TESTING PURPOSES.
     */
    public ConcurrentHashMap<Integer, Drone> getWorkingDrones() {
        return workingDrones;
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

            socket.receive(packet); // Receives a packet from the Scheduler

            InputEvent event = deserializeEvent(packet.getData()); // Deserializes the data
            System.out.println("["+this.name + "] RECEIVED EVENT --> " + "INPUT_EVENT_" + event.getEventID() + " (" +  event + ")" + " FROM: " + "SCHEDULER"); // Prints a message that it has received the data
            currentEvent = event;
            MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.RECEIVED_EVENT, this.currentEvent, null);
            currentState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER;

        }catch (SocketTimeoutException e) {
            currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER;

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleReceivedEventState() {
        if(currentEvent != null) {
            Drone selectedDrone = chooseDroneAlgorithm(currentEvent); // Selects the closest Drone bases on the event coordinates/zone

            if (selectedDrone != null && availableDrones.remove(selectedDrone)) {
                selectedDrone.setAssignedEvent(currentEvent); // Assigns the event to the drone
                workingDrones.put(selectedDrone.getID(), selectedDrone); // Puts the drone in a working drone hashmap
                System.out.println("["+this.name + "] ASSIGNED INPUT_EVENT_" + currentEvent.getEventID() + " TO: " + selectedDrone.getName()); // Prints the name of the drone that was assigned the event
            }
        }
        currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Moves to the next state
    }

    public void handleSendingConfirmationState() {
        try {
            Thread.sleep(3000);  // Wait a bit longer to allow the drop to finish.
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Iterate safely over the working drones.
        Iterator<Map.Entry<Integer, Drone>> iterator = workingDrones.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Drone> entry = iterator.next();
            Drone workingDrone = entry.getValue();

            // Only process the drone if either:
            // 1) It’s in a fault state, OR
            // 2) Its water drop has completed (dropCompleted is true).
            if (!(workingDrone.isDropCompleted() ||
                    workingDrone.getDroneState() instanceof StuckState ||
                    workingDrone.getDroneState() instanceof JammedState ||
                    workingDrone.getDroneState() instanceof CorruptState)) {
                continue;  // Skip this drone for now
            }

            InputEvent receivedEvent;
            if (workingDrone.getDroneState() instanceof StuckState ||
                    workingDrone.getDroneState() instanceof JammedState ||
                    workingDrone.getDroneState() instanceof CorruptState) {
                receivedEvent = workingDrone.getHandledEvent(); // Gets the event in fault cases.
            } else {
                receivedEvent = workingDrone.getCurrentEvent();
            }
            if (receivedEvent != null) {
                if (receivedEvent.getFaultType() == null) {
                    System.out.println("[" + this.name + "] " + workingDrone.getName() + ": COMPLETED INPUT_EVENT_" +
                            receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");
                } else {
                    System.out.println("[" + this.name + "] " + workingDrone.getName() + ": FAILED TO COMPLETE INPUT_EVENT_" +
                            receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");
                }
            }
            // Add the drone back to available pool if it is not in a fault state.
            if (!(workingDrone.getDroneState() instanceof StuckState ||
                    workingDrone.getDroneState() instanceof JammedState)) {
                availableDrones.add(workingDrone); // Adds back to the list of available drones list
            }
            sendConfirmation(receivedEvent); // Sends the event back to the scheduler
            iterator.remove();  // Safe removal using iterato
        }
        currentState = DroneSubsystemState.WAITING;
    }

    private Drone chooseDroneAlgorithm(InputEvent event) {
        Coordinate eventCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            if (drone.getDroneState() instanceof AvailableState) {
                Coordinate droneCoords = drone.getCurrentCoordinates();
                double distance = calculateDistance(eventCoords, droneCoords);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestDrone = drone;
                }
            }
        }
        if (closestDrone == null) {
            System.out.println("There are no drones to be scheduled.");
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
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getLocalHost(), 5001
            );
            socket.send(packet);
            System.out.println("["+this.name + "] SENDING EVENT TO SCHEDULER --> " + "INPUT_EVENT_" + event.getEventID() + " (" +event.toString() + ")"); // Sends the message back to the Scheduler
        } catch (IOException e) {
            System.err.println("Failed to send confirmation: " + e.getMessage());
        }
    }

    private byte[] serializeEvent(InputEvent event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(event);
        return bos.toByteArray();
    }

    private InputEvent deserializeEvent(byte[] data) throws IOException, ClassNotFoundException {
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