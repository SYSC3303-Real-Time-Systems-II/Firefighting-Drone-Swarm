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

    private InputEvent currentEvent;

    public DroneSubsystem(String name, int numDrones) {
        this.name = name;
        // Initialize drone fleet
        for(int i = 0; i < numDrones; i++) {
            Drone drone = new Drone();
            drones.add(drone);
            new Thread(drone).start();
        }
        try {
            this.socket = new DatagramSocket(6000);
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
            currentEvent = event; // Saves the current event
            currentState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER; // Sets the next state

        }catch (SocketTimeoutException e) {
            currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER;

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleReceivedEventState() {
        if(currentEvent != null) {
            try {
                Drone selectedDrone = chooseDroneAlgorithm(currentEvent); // Selects the closest Drone bases on the event coordinates/zone

                while(selectedDrone == null) { // Keep trying to get a drone that is available
                    selectedDrone = chooseDroneAlgorithm(currentEvent);
                }

                byte[] data = serializeEvent(currentEvent); // Serializes the event

                DatagramPacket packet = new DatagramPacket( // Sends it to that specific drone
                        data, data.length,
                        InetAddress.getLocalHost(), selectedDrone.getPortID()
                );

                socket.send(packet); // Sends the damn packet

                selectedDrone.setAssignedEvent(currentEvent); // Assigns the event to the drone
                System.out.println("["+this.name + "] ASSIGNED INPUT_EVENT_" + currentEvent.getEventID() + " TO: " + selectedDrone.getName()); // Prints the name of the drone that was assigned the event
                currentState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Moves to the next state

            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public void handleSendingConfirmationState() {
        try {
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet); // Gets the event from the drone

            InputEvent receivedEvent = deserializeEvent(packet.getData()); // Deserializes the data

            if (receivedEvent.getFaultType() == null) { // If the event went through successfully and there were no faults
                System.out.println("[" + this.name + "]" + " COMPLETED INPUT_EVENT_" + receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");
            }
            else { // There was a fault in the event, and it needs to be sent to the scheduler to be scheduler to be rescheduled
                System.out.println("[" + this.name + "]" + " FAILED TO COMPLETE INPUT_EVENT_" + receivedEvent.getEventID() + " (" + receivedEvent.toString() + ")");
            }

            sendConfirmation(receivedEvent); // Sends the event back to the scheduler

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
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

    public static void main(String[] args) {
        DroneSubsystem subsystem = new DroneSubsystem("DS", 5);
        new Thread(subsystem).start();
    }
}