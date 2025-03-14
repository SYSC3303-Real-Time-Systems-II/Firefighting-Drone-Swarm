import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Coordinate current_coords;
    private DatagramSocket receiveAndSendDDSSocket; // The socket that will be used to send and receive drones
    private Drone drone; // The drone that will be used to send out to the zones for fire

    public DroneSubsystem(String name) {
        try{
            this.name = name;
            this.systemType = Systems.DroneSubsystem;
            this.current_coords = new Coordinate(0, 0);
            this.drone = new Drone();
            this.receiveAndSendDDSSocket = new DatagramSocket(6000);
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /**
     * This is a method used to serialize an input event to be sent to the scheduler as a confirmation. This will help in keeping the object
     * and its attributes.
     *
     * @param inputEvent the input event being serialized.
     * @return the array of bytes for the serialized input event
     */
    private byte[] serializeInputEvent(InputEvent inputEvent) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // Creates a byte aray object
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) { // Wraps it around an output object
            objectStream.writeObject(inputEvent);  // Write the RelayPackage object to the stream
            objectStream.flush(); // Flushes the object stream
        }
        return byteStream.toByteArray();  // Return the byte array
    }

    /**
     * This is a method used to deserialize an input event from the drone subsystem. This is again helpful in keeping the
     * object and its attributes that was sent.
     * @param receivePacket The datagram packet that is to be deserialized and returned as an InputEvent Object
     * @return InputEvent that was received from the drone subsystem.
     */
    private InputEvent deserializeInputEvent(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength()); // Creates an array input stream from the datagram packet
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) { // Creates an inout stream object from the array of input stream bytes as a wrapper
            return (InputEvent) objectStream.readObject();  // Read the object from the byte array
        }
    }

    /**
     * A method that deals with receiving a message from the scheduler.
     * It also returns the input event that was received from the scheduler.
     * @return the input event from the scheduler.
     *
     */
    private InputEvent receiveUDPMessageSCHD() {
        try {
            byte[] receiveData = new byte[6000]; // Creates an array of bytes for the received packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // Receive a packet from the Scheduler
            receiveAndSendDDSSocket.receive(receivePacket); // Receives the packet from the Scheduler

            // Deserialize the byte array into an InputEvent object
            InputEvent inputEvent = deserializeInputEvent(receivePacket);

            System.out.println(name + ": RECEIVED EVENT FROM SCHEDULER --> " + inputEvent.toString()); // Prints a message saying that the drone subsystem has received an event from the scheduler
            System.out.println(name + ": HANDLING EVENT: " + inputEvent); // Prints a message saying that the drone subsystem will handel the event

            /// TODO NEED TO SAVE INPUT EVENT OR HAVE FUNCTION THAT CHECKS FOR DRONE AVAILABILITY HERE

            return inputEvent;

        }catch (IOException | ClassNotFoundException  e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A method to send the confirmation back to the scheduler.
     * @param inputEvent the input event being sent back.
     */
    private void sendUPDMessageSCHD(InputEvent inputEvent) {
        try {
            byte[] message = serializeInputEvent(inputEvent);  // Serializes the input event by passing it to the method
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), 5001); // The packet that will be sent to the scheduler which has a port of 5001
            System.out.println(this.name + ": SENDING THE EVENT --> " + inputEvent.toString() + " TO: " + Systems.DroneSubsystem); // Prints a message that its being sent
            receiveAndSendDDSSocket.send(sendPacket); // Sends the input event
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }


    /**
     * A method used to calculate the travel time to a zone and can also be used to calculate the travel time back from the zone.
     * @param event The event that is sent to the zone.
     * @return the travel time of a zone.
     */
    public double calculateZoneTravelTime(InputEvent event){
        Coordinate fire_coords = event.getZone().getZoneCenter();
        return Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2)) / drone.getTOP_SPEED();
    }

    /**
     * Returns the arrival time of drone to arrive at a zone,
     * @param event The event sent to the drone subsystem.
     * @return the arrival time.
     */
    public double calculateArrivalZoneTime(InputEvent event) {
        return calculateZoneTravelTime(event) + drone.getACCELERATION_TIME(); // Convert to minutes
    }

    @Override
    public void run() {

        System.out.println(this.name + " subsystem started..."); // Prints out a message that the drone subsystem has started

        while (true) {
            // Checks for an event sent from scheduler
            InputEvent event = receiveUDPMessageSCHD();

            if (event != null) { // Checks if the event is not null and that the drone is available

                // Simulate handling the fire meaning that the drone will begin to handle the events, checks for its state first
                if(drone.getDroneState() == DroneState.AVAILABLE) { // If the drone is available
                    System.out.println(drone.getName() + ": AVAILABLE TO HANDLE --> : " + event); // Prints that the drone that was found available to handle the event
                    drone.setLocalTime(event.getTime()); // Sets the event as the local time for the drone
                    drone.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as on route to the zone
                    drone.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as arrived
                }

                // Step 4: Check if the drone has arrived at the zone to message back to the scheduler
                if(drone.getDroneState() == DroneState.ARRIVED) {
                    event.setStatus(Status.COMPLETE); // Makes the status complete
                    event.setTime(event.getTime().plusSeconds((long) calculateArrivalZoneTime(event))); // Update time
                    System.out.println(drone.getName() + ": COMPLETED EVENT (ARRIVED AT ZONE): " + event); // Prints out the time that the drone arrived at zone
                    System.out.println(name + ": SENDING EVENT TO SCHEDULER --> " + event.toString()); // Sends the message back to the Scheduler
                    drone.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as arrived
                    sendUPDMessageSCHD(event);
                }

                // Step 5: Heads back to home base
                drone.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as dropping water

                // Step 6: Arrives back at the home base and now ready to be sent to the next zone
                drone.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as travelling back to the home base

            } else {
                System.out.println("[" + systemType + " - " + name + "] No event to handle, retrying...");

            }

        }
    }

    /**
     * A main method that will be used to run the thread.
     */

    public static void main(String[] args) {
        //Initialize and create thread for the DroneSubsystem
        DroneSubsystem droneSubsystem = new DroneSubsystem("DS");
        Thread drone_t1 = new Thread(droneSubsystem);
        drone_t1.start();
    }


}