import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DroneSubsystem implements Runnable {

    private enum SubsystemState { WAITING, RECEIVED_EVENT, SENDING_CONFIRMATION }

    private final String name;
    private final Systems systemType;
    private final DatagramSocket socket;
    private SubsystemState currentState = SubsystemState.WAITING;

    // Drone management
    private final List<Drone> drones = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Integer, Drone> workingDrones = new ConcurrentHashMap<>();
    private Queue<InputEvent> pendingEvents = new LinkedList<>();

    public DroneSubsystem(String name, int numDrones) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;

        // Initialize drone fleet
        for(int i = 0; i < numDrones; i++) {
            Drone drone = new Drone();
            drones.add(drone);
            new Thread(drone).start();
        }

        try {
            this.socket = new DatagramSocket(6000);
            this.socket.setSoTimeout(2000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize UDP socket", e);
        }
    }

    @Override
    public void run() {
        System.out.println("["+this.name + "] subsystem started with " + drones.size() + " drones");

        while(true) {
            switch(currentState) {
                case WAITING:
                    handleWaitingState();
                    break;

                case RECEIVED_EVENT:
                    handleReceivedEventState();
                    break;

                case SENDING_CONFIRMATION:
                    handleSendingConfirmationState();
                    break;
            }
        }
    }

    private boolean handleWaitingState() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            InputEvent event = deserializeEvent(packet.getData());
            System.out.println("[" + this.name + "] RECEIVED EVENT: " + event.getEventID());
            pendingEvents.add(event);
            currentState = SubsystemState.RECEIVED_EVENT;
            return true;
        } catch (SocketTimeoutException e) {
            currentState = SubsystemState.RECEIVED_EVENT;
            return false;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleReceivedEventState() {
        List<InputEvent> eventsToProcess = new ArrayList<>(pendingEvents);
        for (InputEvent event : eventsToProcess) {
            Drone selectedDrone = chooseDroneAlgorithm22(event);

            //if done was selected
            if (selectedDrone != null) {

                // if Switching the drone's task
                InputEvent selectedDroneCurrentEvent = selectedDrone.getCurrentEvent();
                if(selectedDroneCurrentEvent != null){
                    // Add old task back only if not already in pendingEvents
                    if (!pendingEvents.contains(selectedDroneCurrentEvent)) {
                        pendingEvents.add(selectedDroneCurrentEvent);
                    }
                    System.out.println("[" + this.name + "] SWITCHING TASK FOR " + selectedDrone.getName());
                    selectedDrone.setChangedEvent(true);    //set to true stating that the drone will not take any new tasks until completed current task
                }

                System.out.println("[" + this.name + "] ASSIGNED " + selectedDrone.getName().toUpperCase() + " TO " + event.getEventID()+" ["+ event +"]");
                selectedDrone.setAssignedEvent(event);
                workingDrones.put(selectedDrone.getID(), selectedDrone);
                pendingEvents.remove(event);
            } else {
                System.out.println("NO AVAILABLE DRONES FOR EVENT");
            }
        }
        currentState = SubsystemState.SENDING_CONFIRMATION;
    }

    private void handleSendingConfirmationState() {
        try {
            Thread.sleep(3000); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Map.Entry<Integer, Drone>> entries = new ArrayList<>(workingDrones.entrySet());

        for (Map.Entry<Integer, Drone> entry : entries) {
            Drone workingDrone = entry.getValue();
            if (workingDrone != null) {
                InputEvent completedEvent = workingDrone.getCompletedEvent();
                if (completedEvent != null) {
                    workingDrone.setCompletedEvent(null);
                    System.out.println("[" + this.name + "] " + workingDrone.getName().toUpperCase() + ": COMPLETED " + completedEvent);
                    sendConfirmation(completedEvent);
                    workingDrones.remove(entry.getKey());
                }
            }
        }

        currentState = SubsystemState.WAITING;
    }

    private Drone chooseDroneAlgorithm22(InputEvent event) {
        Coordinate eventCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            // Skip drones that have already changed tasks
            if (drone.isChangedEvent()) {
                continue;
            }

            // Existing state check
            if (drone.getDroneState() instanceof AvailableState || drone.getDroneState() instanceof AscendingState || drone.getDroneState() instanceof CruisingState) {

                InputEvent currentEvent = drone.getCurrentEvent();
                boolean isEligible = false;

                // Existing priority logic
                if (currentEvent == null) {
                    isEligible = true;
                } else {
                    int currentPriority = currentEvent.getSeverity().getValue();
                    int newPriority = event.getSeverity().getValue();

                    if (newPriority > currentPriority) {
                        isEligible = true;
                    } else if (newPriority == currentPriority) {
                        isEligible = true;
                    }
                }

                if (isEligible) {
                    Coordinate droneCoords = drone.getCurrentCoordinates();
                    double distance = calculateDistance(eventCoords, droneCoords);

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestDrone = drone;
                    }
                }
            }
        }

        return closestDrone;
    }

    private Drone chooseDroneAlgorithm(InputEvent event) {
        Coordinate eventCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            // Check both state and working status
            if (drone.getDroneState() instanceof AvailableState && !workingDrones.containsKey(drone.getID())) {

                Coordinate droneCoords = drone.getCurrentCoordinates();
                double distance = calculateDistance(eventCoords, droneCoords);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestDrone = drone;
                }
            }
        }
        return closestDrone;
    }

    // Rest of the helper methods remain unchanged
    private double calculateDistance(Coordinate a, Coordinate b) {
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
            System.out.println("["+this.name + "] SENDING CONFIRMATION TO SCHEDULER --> " + event.getEventID());
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

    public static void main(String[] args) {
        DroneSubsystem subsystem = new DroneSubsystem("DSS", 3);
        new Thread(subsystem).start();
    }
}