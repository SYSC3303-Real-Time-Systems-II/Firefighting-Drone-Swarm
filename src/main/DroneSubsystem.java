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
    private final List<Drone> availableDrones = new ArrayList<>();
    private final ConcurrentHashMap<Integer, Drone> workingDrones = new ConcurrentHashMap<>();

    private Queue<InputEvent> pendingEvents = new LinkedList<>();
    private InputEvent currentEvent;


    public DroneSubsystem(String name, int numDrones) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;

        // Initialize drone fleet
        for(int i = 0; i < numDrones; i++) {
            Drone drone = new Drone();
            drones.add(drone);
            availableDrones.add(drone);
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
            System.out.println("["+this.name + "] received event: " + event);
            pendingEvents.add(event);
            currentState = SubsystemState.RECEIVED_EVENT;
            return true;
        }catch (SocketTimeoutException e) {
            currentState = SubsystemState.RECEIVED_EVENT;
            return false;
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleReceivedEventState() {
        // Convert queue to a list to avoid ConcurrentModificationException
        List<InputEvent> eventsToProcess = new ArrayList<>(pendingEvents);

        for (InputEvent event : eventsToProcess) {
            Drone selectedDrone = chooseDroneAlgorithm(event);
            if (selectedDrone != null && availableDrones.remove(selectedDrone)) {
                selectedDrone.setAssignedEvent(event);
                workingDrones.put(selectedDrone.getID(), selectedDrone);
                System.out.println("[" + this.name + "] Assigned " + selectedDrone.getName() + " to event");

                pendingEvents.remove(event); // Remove event from queue since it's assigned
            } else {
                System.out.println("NO DRONE ");
            }
        }
        currentState = SubsystemState.SENDING_CONFIRMATION;
    }

    private void handleSendingConfirmationState() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println( "start " +  workingDrones);
        // Convert entry set to a list to avoid ConcurrentModificationException
        List<Map.Entry<Integer, Drone>> entries = new ArrayList<>(workingDrones.entrySet());

        // Process each working drone once
        for (Map.Entry<Integer, Drone> entry : entries) {
            Drone workingDrone = entry.getValue();

            if (workingDrone != null) {
                InputEvent completedEvent = workingDrone.getCompletedEvent();
                if (completedEvent != null) {
                    workingDrone.setCompletedEvent(null); //reset since it has been completed
                    System.out.println("[" + this.name + "] " + workingDrone.getName() + ": COMPLETED EVENT");
                    sendConfirmation(completedEvent);
                    availableDrones.add(workingDrone);
                    workingDrones.remove(entry.getKey()); // Remove from working drones
                }
            }
        }

        System.out.println( "end  " +  workingDrones);
        // Move to next state after processing
        currentState = SubsystemState.WAITING;
    }

    private Drone chooseDroneAlgorithm(InputEvent event) {
        Coordinate eventCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;
        for (Drone drone : drones) {
            if (drone.getDroneState() instanceof AvailableState | drone.getDroneState() instanceof AscendingState) {
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
            System.out.println("["+this.name + "] SENDING EVENT TO SCHEDULER --> " + event.toString()); // Sends the message back to the Scheduler
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
        DroneSubsystem subsystem = new DroneSubsystem("DSS", 1);
        new Thread(subsystem).start();
    }
}