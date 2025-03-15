import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

/**
 * FireIncidentSubsystem class is responsible for reading fire incident data and zone information
 * from input files, and sending this data to the Scheduler via a RelayBuffer.
 */
public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Queue<InputEvent> inputEvents;
    private ArrayList<Zone> zonesList;
    private LocalTime current_time;
    private DatagramSocket sendReceiveSocket;
    private FireIncidentSubsystemState currentState = FireIncidentSubsystemState.SENDING_DATA;
    private boolean zonesSent = false;
    private int eventCounter = 0;
    private long lastSendTime = 0;

    /**
     * Constructs a FireIncidentSubsystem object.
     *
     * @param name               The name of the subsystem.
     * @param inputEventFileName The name of the file containing input events.
     * @param inputZoneFileName  The name of the file containing zone information.
     */
    public FireIncidentSubsystem(String name, String inputEventFileName, String inputZoneFileName) {
        try {
            this.name = name;
            this.systemType = Systems.FireIncidentSubsystem;
            this.inputEvents = readInputEvents(inputEventFileName);
            this.zonesList = readZones(inputZoneFileName);
            this.current_time = null;
            this.sendReceiveSocket = new DatagramSocket(4000);
            this.sendReceiveSocket.setSoTimeout(4000); // 4-second timeout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads zone information from a CSV file and returns a list of Zone objects.
     *
     * @param inputZoneFileName The name of the file containing zone information.
     * @return An ArrayList of Zone objects.
     */
    public ArrayList<Zone> readZones(String inputZoneFileName){
        ArrayList<Zone> zones = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("data/" + inputZoneFileName))) {
            // Skip the first line (header)
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(","); // Split by comma for CSV

                //Create zone object and add to the zone ArrayList
                Zone zone = new Zone(Integer.parseInt(parts[0]), Zone.parseCoordinates(parts[1]), Zone.parseCoordinates(parts[2]));
                zones.add(zone);
            }
            return zones;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: Ensure the correct file is used");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads input events from a CSV file and returns a queue of InputEvent objects.
     *
     * @param inputEventFileName The name of the file containing input events.
     * @return A Queue of InputEvent objects.
     */
    public Queue<InputEvent> readInputEvents(String inputEventFileName) {
        Queue<InputEvent> inputEvents = new LinkedList<>();
        try (Scanner scanner = new Scanner(new File("data/" + inputEventFileName))) {
            // Skip the first line (header)
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(","); // Split by comma for CSV

                //Create InputEvent object and add to the inputEvents ArrayList
                InputEvent event = new InputEvent(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3],Status.UNRESOLVED);
                inputEvents.add(event);
            }
            return inputEvents;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: Ensure the correct file is used");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the queue of input events that the fire incident subsystem want to send to the drone through the scheduler
     * @return the input events
     */
    public Queue<InputEvent> getInputEvents() {
        return inputEvents;
    }

    /**
     * Returns the array list of the zone events.
     * @return zone events
     */
    public ArrayList<Zone> getZonesList() {
        return zonesList;
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
            objectStream.flush(); // Flushes after it writes the serialized array of bytes
        }
        return byteStream.toByteArray();  // Return the byte array
    }

    /**
     * This is a method used to deserialize a relay package from the Scheduler. This is again helpful in keeping the
     * object and its attributes that was sent.
     * @param receivePacket The packets to be received and deserialized into an InputEvent object.
     * @return the relay package that was received from scheduler.
     *
     */
    private RelayPackage deserializeRelayPackage(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (RelayPackage) objectStream.readObject();  // Read the object from the byte array
        }
    }

    /**
     * A method that is used to handle sending a package to the Scheduler that has been polled.
     * @param inputEventPackage The relay package being sent to the Scheduler.
     */
    private void sendUDPMessage(RelayPackage inputEventPackage){
        try{
            byte[] message = serializeRelayPackage(inputEventPackage);  // Serializes the RelayPackage by passing it to the method

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), 5000); // The packet that will be sent to the scheduler which has a port of 5000
            System.out.println("["+this.name + "] SENDING --> " + inputEventPackage.getRelayPackageID() + " TO: " + Systems.Scheduler);
            sendReceiveSocket.send(sendPacket); // Sends the packet to the scheduler
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * A method that is used to handle when a package has been sent by the scheduler for acknowledgment.
     */
    private void receiveUDPMessage(){
        try{
            byte[] receiveData = new byte[6000];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // The received data packet that weill be received from teh scheduler
            sendReceiveSocket.receive(receivePacket); // Receives the packet

            // Deserialize the byte array into a RelayPackage object
            RelayPackage receivedPackage = deserializeRelayPackage(receivePacket);

            System.out.println("["+this.name + "] Received <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.Scheduler); // Prints out the confirmation message that it got the data from the scheduler
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }


    /**
     * The run method is executed when the thread starts.
     * It sends zone information and input events to the Scheduler via the RelayBuffer.
     * It also reads the RelayBuffer to receive acknowledgments from the Scheduler regarding the events.
     * Additionally, it simulates time delays between events to mimic real-world timing.
     */
    @Override
    public void run() {
        System.out.println("["+this.name + "] subsystem started...");

        // Initial zone package
        sendZonePackage();

        while (true) {
            switch (currentState) {
                case SENDING_DATA:
                    handleSendingState();
                    break;

                case WAITING_CONFIRMATION:
                    handleWaitingState();
                    break;

                case IDLE:
                    handleIdleState();
                    break;
            }
        }
    }

    private void sendZonePackage() {
        RelayPackage zonePackage = new RelayPackage(
                "ZONE_PKG_1",
                Systems.Scheduler,
                null,
                zonesList
        );
        sendUDPMessage(zonePackage);
        zonesSent = true;
    }

    public void handleSendingState() {
        if (!zonesSent) {
            sendZonePackage();
        } else if (!inputEvents.isEmpty()) {
            InputEvent event = inputEvents.remove();
            RelayPackage pkg = new RelayPackage(
                    "INPUT_EVENT_" + eventCounter++,
                    Systems.Scheduler,
                    event,
                    null
            );
            sendUDPMessage(pkg);
            simulateTimeDelay(event);
        }

        lastSendTime = System.currentTimeMillis();
        currentState = FireIncidentSubsystemState.WAITING_CONFIRMATION;
    }

    private void handleWaitingState() {
        try {
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            sendReceiveSocket.receive(packet);

            RelayPackage received = deserializeRelayPackage(packet);
            System.out.println("["+this.name + "] Received confirmation for " + received.getRelayPackageID());

            // Only switch to sending if we have more events
            currentState = inputEvents.isEmpty() ? FireIncidentSubsystemState.IDLE : FireIncidentSubsystemState.SENDING_DATA;

        } catch (SocketTimeoutException e) {
            // Resend if we have pending events and 4 seconds have passed
            if (!inputEvents.isEmpty() && (System.currentTimeMillis() - lastSendTime) > 4000) {
                currentState = FireIncidentSubsystemState.SENDING_DATA;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void handleIdleState() {
        try {
            // Still listen for potential late confirmations
            byte[] buffer = new byte[6000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            sendReceiveSocket.receive(packet);

            RelayPackage received = deserializeRelayPackage(packet);
            System.out.println("["+this.name + "] Received confirmation for " + received.getRelayPackageID());

        } catch (SocketTimeoutException e) {
            // Expected in idle state
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void simulateTimeDelay(InputEvent event) {
        if (current_time == null) {
            current_time = event.getTime();
            return;
        }

        Duration duration = Duration.between(current_time, event.getTime());
        if (!duration.isZero()) {
            try {
                // Scale real-world time (1 minute = 100ms)
                Thread.sleep(duration.toMinutes() * 100);
                current_time = event.getTime();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Main method to run the thread.
     */
    public static void main(String[] args) {

        // Initialize and create thread for the FireIncidentSubsystem
        FireIncidentSubsystem fis1 = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv");
        Thread fis1_t1 = new Thread(fis1);
        fis1_t1.start();
    }
}


