import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The Scheduler class is responsible for managing and prioritizing input events received from the FireIncidentSubsystem.
 * It sends the highest-priority events to the DroneSubsystem and handles confirmation messages back to the FireIncidentSubsystem.
 */
public class Scheduler implements Runnable {

    private String name;                                // Name of the scheduler
    private Systems systemType;                         // Type of the system (Scheduler)
    private PriorityQueue<InputEvent> inputEvents;      // Priority queue of input events, ordered by severity
    private Queue<RelayPackage> confirmationPackages;   // Queue of confirmation packages to send back to the FireIncidentSubsystem
    private Map<Integer, Zone> zones;                   // Map of zones, keyed by zone ID
    private DatagramSocket receiveAndSendFISSocket, receiveAndSendDSSSocket; // Socket for receiving and sending communication with the FireIncidentSubsystem and DroneSubsystem

    /**
     * Constructs a Scheduler object.
     *
     * @param name        The name of the scheduler.
     */
    public Scheduler(String name) {
        // Comparator to prioritize events based on severity (High > Moderate > Low)
        Comparator<InputEvent> priorityComparator = Comparator.comparingInt(inputEvent -> {
            switch (inputEvent.getSeverity()) {
                case High: return 1; // Highest priority
                case Moderate: return 2;
                case Low: return 3; // Lowest priority
                default: throw new IllegalArgumentException("Unknown priority level");
            }
        });

        try {
            this.name = name;
            this.systemType = Systems.Scheduler;
            this.inputEvents = new PriorityQueue<>(priorityComparator);
            this.confirmationPackages = new LinkedList<>();
            this.zones = new HashMap<>();
            this.receiveAndSendFISSocket = new DatagramSocket(5000); // Has a port of 5000
            this.receiveAndSendDSSSocket = new DatagramSocket(5001); // Has a port of 5001

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds zones to the scheduler's zone map.
     *
     * @param zonesList   The list of zones to add.
     * @param systemType  The system type sending the zones.
     * @param name        The name of the system sending the zones.
     */
    public synchronized void addZones(ArrayList<Zone> zonesList, Systems systemType, String name) {
        for (Zone zone : zonesList) {
            this.zones.put(zone.getZoneID(), zone);
        }
        System.out.println(this.name + ": Added zones: " + this.zones);
    }

    /**
     * Retrieves a map of all zones currently managed by the scheduler.
     *
     * @return A map of zone IDs to Zone objects.
     */
    public Map<Integer, Zone> getZones() {
        return zones;
    }

    /**
     * This is a method used to serialize a relay package to be sent to the Scheduler. This will help in keeping the object
     * and its attributes.
     *
     * @param relayPackage the relay package being serialized.
     * @return the array of bytes for the serialized relay package
     */
    private byte[] serializeRelayPackage(RelayPackage relayPackage) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // Creates a byte aray object
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) { // Wraps it around an output object
            objectStream.writeObject(relayPackage);  // Write the RelayPackage object to the stream
            objectStream.flush(); // Flushes the object stream
        }
        return byteStream.toByteArray();  // Return the byte array
    }

    /**
     * This is a method used to serialize an input event to be sent to the drone subsystem. This will help in keeping the object
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
     * @param receivePacket The packets to be received and deserialized into an InputEvent object
     * @return InputEvent that was received from the drone subsystem.
     */
    private InputEvent deserializeInputEvent(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (InputEvent) objectStream.readObject();  // Read the object from the byte array
        }
    }

    /**
     * This is a method used to deserialize a relay package from the Scheduler. This is again helpful in keeping the
     * object and its attributes that was sent.
     *  @param receivePacket The packets to be received and deserialized into a RelayPackage object
     * @return relay package that was received from the FIS.
     */
    private RelayPackage deserializeRelayPackage(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (RelayPackage) objectStream.readObject();  // Read the object from the byte array

        }
    }

    /**
     * A method that deals with receiving a message from the FIS.
     */
    private void receiveUDPMessageFIS(){
        try{
            byte[] receiveData = new byte[6000];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // Receive a packet from the FireIncidentSubsystem
            receiveAndSendFISSocket.receive(receivePacket); // Receives the packet from the FIS

            RelayPackage receivedPackage = deserializeRelayPackage(receivePacket);

            System.out.println(receivedPackage.getRelayPackageID());  // THE PROBLEM IS FUCKING HERE

            // Check for RelayPackage from FireIncidentSubsystem
            if (receivedPackage.getRelayPackageID().contains("ZONE_PKG")) { // If a zone package was received from the fire incident subsystem
                this.addZones(receivedPackage.getZone(), this.systemType, this.name);
            }
            else { // If we have received an event package
                System.out.println(this.name + ": RECEIVED AN EVENT <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.FireIncidentSubsystem); // Prints out a message that the event was received
                // Process the event and add it to the inputEvents queue
                receivedPackage.getEvent().setZone(zones.get(receivedPackage.getEvent().getZoneId())); // Set the zone for the event
                this.inputEvents.add(receivedPackage.getEvent()); // Adds the input events to the list of input events for the drone subsystem
            }

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    /**
     * A method that is used to send a confirmation relay package back to the fire incident subsystem.
     * @param relayPackage the relay package being sent back to the fire incident subsystem.
     */
    private void sendUDPMessageFIS(RelayPackage relayPackage){
        try {
            byte[] message = serializeRelayPackage(relayPackage);  // Serializes the relay package by passing it to the method
            DatagramPacket sendPacket = new DatagramPacket(message, message.length,InetAddress.getLocalHost(), 4000); // The packet that will be sent to the fire incident subsystem which has a port of 4000
            System.out.println(this.name + ": SENDING CONFIRMATION FOR --> " + relayPackage.getEvent().toString() + " TO: " + relayPackage.getReceiverSystem());
            receiveAndSendFISSocket.send(sendPacket); // Send the relay package
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * A method used to send an input event to the drone subsystem.
     * @param inputEvent the input event being sent to the drone subsystem.
     */
    private void sendUDPMessageDSS(InputEvent inputEvent){
        try {
            byte[] message = serializeInputEvent(inputEvent);  // Serializes the input event by passing it to the method
            DatagramPacket sendPacket = new DatagramPacket(message, message.length,InetAddress.getLocalHost(), 6000); // The packet that will be sent to the drone subsystem which has a port of 6000
            System.out.println(this.name + ": SENDING THE EVENT --> " + inputEvent.toString() + " TO: " + Systems.DroneSubsystem); // Prints a message that its being sent
            receiveAndSendDSSSocket.send(sendPacket); // Sends the input event
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * A method that is used to receive the input event from the drone subsystem.
     */
    private void receiveUDPMessageDSS(){
        try {
            byte[] receiveData = new byte[6000];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // The packet to be received from the drone subsystem
            receiveAndSendDSSSocket.receive(receivePacket); // Receives the packet from the drone subsystem

            // Deserialize the byte array into a InputEvent object
            InputEvent receivedInput = deserializeInputEvent(receivePacket);

            System.out.println(this.name + ": RECEIVED EVENT <-- " + receivedInput.toString() + " FROM: " + Systems.DroneSubsystem);

            // Create a confirmation package and place in confirmationPackages queue
            RelayPackage sendingPackage = new RelayPackage("DRONE_CONFIRMATION", Systems.FireIncidentSubsystem, receivedInput, null);

            this.confirmationPackages.add(sendingPackage);

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }


    /**
     * The run method is executed when the thread starts.
     * It reads events from the RelayBuffer, prioritizes them, and sends them to the DroneSubsystem.
     * It also handles confirmation messages from the DroneSubsystem and sends them back to the FireIncidentSubsystem.
     */
    @Override
    public void run() {

        System.out.println(this.name + " subsystem started..."); // Prints out a message that the FIS has started

        // Loop to Read Input Events from FireIncidentSubsystem, sends Event to DroneSubsystem, and handles confirmation messages passing
        while (true) {

            // Check for ReplayPackage that was sent from the FIS
            receiveUDPMessageFIS();

            //Send the highest-priority event to the Drone
            if (!this.inputEvents.isEmpty()) {

                sendUDPMessageDSS(this.inputEvents.poll()); // Sends the event to the drone sub-system with the highest priority event

                receiveUDPMessageDSS(); //Check if there is any event back from the Drone
            }

            //Send confirmation packages back to the FireIncidentSubsystem
            if (!this.confirmationPackages.isEmpty()) {
                sendUDPMessageFIS(this.confirmationPackages.poll()); // Calls a method to send the confirmation
            }
        }
    }

    /**
     *  Main method to run the thread.
     */

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler("Scdlr");
        Thread scheduler_t1 = new Thread(scheduler);
        scheduler_t1.start();
    }

}