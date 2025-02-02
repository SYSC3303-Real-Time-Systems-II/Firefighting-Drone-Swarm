/**
 * The Main class serves as the entry point for the application.
 * Initializes and starts the subsystems
 */
public class Main {

    /**
     * The main method where the program execution begins.
     * It initializes the shared buffers and subsystems, creates threads for each subsystem,
     * and starts them to run concurrently.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {

        //Create shared buffers for communication between subsystems
        RelayBuffer relayBuffer = new RelayBuffer();
        EventBuffer eventBuffer = new EventBuffer();

        // Initialize and create thread for the FireIncidentSubsystem
        FireIncidentSubsystem fis1 = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv",relayBuffer);
        Thread fis1_t1 = new Thread(fis1);

        // Initialize and create thread for the Scheduler
        Scheduler scheduler = new Scheduler("Scdlr", relayBuffer,eventBuffer);
        Thread scheduler_t1 = new Thread(scheduler);

        // Initialize and create thread for the DroneSubsystem
        DroneSubsystem drone = new DroneSubsystem("Drone", eventBuffer);
        Thread drone_t1 = new Thread(drone);

        // Start all threads
        fis1_t1.start();
        scheduler_t1.start();
        drone_t1.start();
    }

}
