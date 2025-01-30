import java.nio.charset.CoderResult;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class DroneSubsystem implements Runnable{

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private Coordinate current_coords;


    private final Double ACCELERATION_TIME = 0.51;
    private final Double DECELERATION_TIME = 0.75;
    private final Double TOP_SPEED = 20.8; //meter per sec
    private final Double DROP_WATER_TIME = 10.0; //UPDATE THE ITER0
    private ArrayList<InputEvent> inputEvents;

    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.scheduler = scheduler;
        this.inputEvents = new ArrayList<>();
        this.current_coords = new Coordinate(0,0);
    }

    public int calulateTotalTravelTime(InputEvent event){
        Coordinate fire_coords = event.zone.getZoneCenter();
        double distance = Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2));
        double travelTime = 2*(distance / TOP_SPEED); //both ways
        //rounded value
        int totalTravelTime = (int) Math.round((travelTime+ACCELERATION_TIME+DECELERATION_TIME+DROP_WATER_TIME) * 100);
        return totalTravelTime;
    }

    @Override
    public void run() {
        int i = 0;
        while(i < 10){
            InputEvent event = this.scheduler.takeInputEvent(systemType, name);
            if (event != null) {
                int travelTime = calulateTotalTravelTime(event);
                System.out.println("["+ systemType + " - " + name + "] HANDLING FIRE: " + event);
                try {
                    sleep(travelTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("["+ systemType + " - " + name + "] RETURNING: " + event);

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
