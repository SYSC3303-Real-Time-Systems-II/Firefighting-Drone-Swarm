public class Main {
    public static void main(String[] args) {

        RelayBuffer relayBuffer = new RelayBuffer();
        EventBuffer eventBuffer = new EventBuffer();

        FireIncidentSubsystem fis1 = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv",relayBuffer);
        Thread fis1_t1 = new Thread(fis1);

        Scheduler scheduler = new Scheduler("Scdlr", relayBuffer,eventBuffer);
        Thread scheduler_t1 = new Thread(scheduler);


        DroneSubsystem drone = new DroneSubsystem("Drone", eventBuffer);
        Thread drone_t1 = new Thread(drone);

        fis1_t1.start();
        scheduler_t1.start();
        drone_t1.start();
    }

}
