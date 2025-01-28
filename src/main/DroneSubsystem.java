import java.util.ArrayList;

public class DroneSubsystem implements Runnable{

    private String name;
    private Systems systemType;

    private Scheduler scheduler;

    private ArrayList<InputEvent> inputEvents;

    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.scheduler = scheduler;
        this.inputEvents = new ArrayList<>();
    }

    @Override
    public void run() {
        while(true){
            this.inputEvents.add(this.scheduler.takeInputEvent());
        }
    }
}
