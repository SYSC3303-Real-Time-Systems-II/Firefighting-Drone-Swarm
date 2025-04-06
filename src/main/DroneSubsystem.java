import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * The DroneSubsystem class is responsible for managing the communication between the Scheduler and the Drones.
 * It listens for incoming events from the Scheduler, assigns events to drones, and sends confirmations back.
 * It also gathers status updates and metrics to be sent to the GUI.
 */
public class DroneSubsystem implements Runnable {

    private final String name;
    private final DatagramSocket schedulerSocket; // For Scheduler on port 6000
    private final DatagramSocket droneSocket;     // For Drones on port 6001
    private DroneSubsystemState currentState = DroneSubsystemState.WAITING;

    // Drone management
    private final List<Drone> drones = new CopyOnWriteArrayList<>();
    private List<InputEvent> pendingEvents = new ArrayList<>();
    private DroneModel droneModel;


    /**
     * Constructs a DroneSubsystem with the given name and number of drones.
     * Initializes the drone fleet, starts the drone model thread, and sets up UDP sockets.
     *
     * @param name      The name of this DroneSubsystem.
     * @param numDrones The number of drones to initialize.
     * @throws RuntimeException if UDP sockets cannot be initialized.
     */
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

    /**
     * Continuously listens for incoming UDP packets and processes them based on the current subsystem state.
     * The method cycles through waiting for events, handling received events, and sending confirmations.
     */
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

    /**
     * Handles the waiting state by listening for an incoming event from the Scheduler.
     * Upon receiving an event, the event is deserialized, added to pending events, and the state changes.
     */
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

    /**
     * Processes all pending events by assigning each event to an available drone based on a selection algorithm.
     * After processing, transitions the state to SENDING_EVENT_TO_SCHEDULER.
     */
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

    /**
     * Waits for a confirmation packet from a drone, processes the received event,
     * re-queues it if necessary, and sends a confirmation back to the Scheduler.
     * Finally, transitions the state back to WAITING.
     */
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

    /**
     * Chooses the best available drone for the given event based on the shortest Euclidean distance
     * between the drone's current coordinates and the event's zone center.
     *
     * @param event The {@link InputEvent} for which a drone is needed.
     * @return The closest available {@link Drone}, or null if no drone is available.
     */
    private Drone chooseDroneAlgorithm(InputEvent event) {

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

    /**
     * Calculates the Euclidean distance between two coordinates.
     *
     * @param a The first {@link Coordinate}.
     * @param b The second {@link Coordinate}.
     * @return The Euclidean distance as a double.
     */
    public double calculateDistance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) +
                Math.pow(a.getY() - b.getY(), 2));
    }

    /**
     * Sends a confirmation message back to the Scheduler for the given event.
     *
     * @param event The {@link InputEvent} to confirm.
     */
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


    /**
     * Serializes an {@link InputEvent} into a byte array.
     *
     * @param event The {@link InputEvent} to serialize.
     * @return A byte array representing the serialized event.
     * @throws IOException If an I/O error occurs during serialization.
     */
    private byte[] serializeEvent(InputEvent event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(event);
        return bos.toByteArray();
    }


    /**
     * Deserializes a byte array into an {@link InputEvent}.
     *
     * @param data The byte array containing the serialized event.
     * @return The deserialized {@link InputEvent}.
     * @throws IOException            If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    private InputEvent deserializeEvent(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (InputEvent) ois.readObject();
    }

    /**
     * Starts a separate thread that periodically gathers drone statuses and metrics from the drone model,
     * then sends these updates to the GUI via UDP packets.
     */
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

    /**
     * Serializes an object into a byte array using Java serialization.
     *
     * @param obj The object to serialize.
     * @return A byte array representing the serialized object.
     * @throws IOException If an I/O error occurs during serialization.
     */
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * The entry point for the DroneSubsystem application.
     * Initializes the subsystem with a specified number of drones and starts GUI updates.
     *
     * @param args Command-line arguments (not used).
     */
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