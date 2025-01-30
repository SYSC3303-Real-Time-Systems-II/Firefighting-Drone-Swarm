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
        int i = 0;
        while(i < 10){
            InputEvent event = this.scheduler.takeInputEvent(systemType, name);
            if (event != null) {
                this.inputEvents.add(event);
                /// work todo:
                this.scheduler.addRelayMessageEvents(event, systemType, name);
            } else {
                this.scheduler.addRelayMessageEvents(null, systemType, name);
            }
            i++;
        }
    }
}
