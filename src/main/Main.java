public class Main {
    public static void main(String[] args) {

        Scheduler scheduler = new Scheduler("Scdlr");
        Thread scheduler_t1 = new Thread(scheduler);

        FireIncidentSubsystem fis1 = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv",scheduler);
        Thread fis1_t1 = new Thread(fis1);

        DroneSubsystem drone = new DroneSubsystem("Drone", scheduler);
        Thread drone_t1 = new Thread(drone);

        fis1_t1.start();
        scheduler_t1.start();
        drone_t1.start();
    }

}
