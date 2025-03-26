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
    private final BlockingQueue<InputEvent> completedEvents = new LinkedBlockingQueue<>();

    private InputEvent currentEvent;

    public DroneSubsystem(String name, int numDrones) {
        this.name = name;
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
        System.out.println("["+this.name + "] subsystem started with " + drones.size() + " drones");

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

    public boolean handleWaitingState() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            InputEvent event = deserializeEvent(packet.getData());
            System.out.println("["+this.name + "] received event: " + event);
            currentEvent = event;
            currentState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER;
            return true;
        }catch (SocketTimeoutException e) {
            currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER;
            return false;
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleReceivedEventState() {
        if(currentEvent != null) {
            Drone selectedDrone = chooseDroneAlgorithm(currentEvent);

            if (selectedDrone != null && availableDrones.remove(selectedDrone)) {
                selectedDrone.setAssignedEvent(currentEvent);
                workingDrones.put(selectedDrone.getID(), selectedDrone);
                System.out.println("["+this.name + "] Assigned " + selectedDrone.getName() + " to event");
            }
        }
        currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER;
    }

    public void handleSendingConfirmationState() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Use entrySet iterator for safe removal
        Iterator<Map.Entry<Integer, Drone>> iterator = workingDrones.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Drone> entry = iterator.next();
            Drone workingDrone = entry.getValue();

            if (workingDrone != null) {
                InputEvent completedEvent = workingDrone.getCompletedEvent();
                if (completedEvent != null) {
                    System.out.println("["+this.name + "] " + workingDrone.getName() + ": COMPLETED EVENT");
                    sendConfirmation(completedEvent);
                    availableDrones.add(workingDrone);
                    iterator.remove(); // Safe removal using iterator
                }
            }
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
        DroneSubsystem subsystem = new DroneSubsystem("DS", 5);
        new Thread(subsystem).start();
    }
}