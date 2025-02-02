import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

/**
 * FireIncidentSubsystem class is responsible for reading fire incident data and zone information
 * from input files, and sending this data to the Scheduler via a RelayBuffer.
 *
 */
public class FireIncidentSubsystem implements Runnable {

    private String name;                            // Name of the subsystem
    private Systems systemType;                     // Type of the system (FireIncidentSubsystem)
    private Queue<InputEvent> inputEvents;          // Queue of input events to be processed
    private ArrayList<Zone> zonesList;              // List of zones read from the input file
    private LocalTime current_time;                 // Current time for simulating event timing
    private RelayBuffer relayBuffer;                // Buffer for communication with the Scheduler

    /**
     * Constructs a FireIncidentSubsystem object.
     *
     * @param name               The name of the subsystem.
     * @param inputEventFileName The name of the file containing input events.
     * @param inputZoneFileName  The name of the file containing zone information.
     * @param relayBuffer        The RelayBuffer used for communication with the Scheduler.
     */
    public FireIncidentSubsystem(String name, String inputEventFileName, String inputZoneFileName,RelayBuffer relayBuffer){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInputEvents(inputEventFileName);
        this.zonesList = readZones(inputZoneFileName);
        this.relayBuffer = relayBuffer;
        this.current_time = null;
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
     * The run method is executed when the thread starts.
     * It sends zone information and input events to the Scheduler via the RelayBuffer.
     * It also reads the RelayBuffer to receive acknowledgments from the Scheduler regarding the events.
     * Additionally, it simulates time delays between events to mimic real-world timing.
     */
    @Override
    public void run() {
        int i = 0;

        //Send the zone package to the Scheduler
        RelayPackage zonePackage = new RelayPackage("ZONE_PKG_1", Systems.Scheduler, null, zonesList);
        relayBuffer.addReplayPackage(zonePackage);
        System.out.println(this.name + ": SENDING --> " + zonePackage.getRelayPackageID() + " TO: " + zonePackage.getReceiverSystem());

            //Loop to send Input Events to Schduler and receive acknowledgments back
        while (i < 10) {

            //Send input events to the Scheduler (if available)
            if (!this.inputEvents.isEmpty()) {

                InputEvent inputEvent = inputEvents.remove();
                // Simulate time passing if this is not the first event
                if (current_time == null) {
                    current_time = inputEvent.getTime();
                } else if (!current_time.equals(inputEvent.getTime())) {
                    Duration duration = Duration.between(current_time, inputEvent.getTime());
                    current_time = inputEvent.getTime();
                    try {
                        Thread.sleep(duration.toMinutes() * 100); // Simulate time delay
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                //Place ReplayPackage in relayBuffer for Scheduler
                RelayPackage inputEventPackage = new RelayPackage("INPUT_EVENT_" + i, Systems.Scheduler, inputEvent, null);
                System.out.println(this.name + ": SENDING --> " + inputEventPackage.getRelayPackageID() + " TO: " + inputEventPackage.getReceiverSystem());
                relayBuffer.addReplayPackage(inputEventPackage);

            // Check ReplayPackage acknowledgments from Scheduler
            } else {
                RelayPackage receivedPackage = relayBuffer.getRelayPackage(this.systemType);
                if (receivedPackage != null) {
                    System.out.println(this.name + ": Received <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.Scheduler);
                } else {
                    System.out.println(this.name + ": No item for FireIncidentSubsystem, retrying...");
                    i--; // Retry the same iteration
                }
            }
            i++;
        }
    }

}


