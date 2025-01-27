public class Main {
    public static void main(String[] args) {

        MessageRelay relay = new MessageRelay();

        FireIncidentSubsystem fis1 = new FireIncidentSubsystem("FIS", "test.txt",relay);
        Thread fis1_t1 = new Thread(fis1);

        Scheduler scheduler = new Scheduler("Scdlr",relay);
        Thread scheduler_t1 = new Thread(scheduler);

        DroneSubsystem drone = new DroneSubsystem("Drone", relay);
        Thread drone_t1 = new Thread(drone);

        fis1_t1.start();
        scheduler_t1.start();
        drone_t1.start();
    }

}
