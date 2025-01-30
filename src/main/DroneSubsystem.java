import java.time.LocalTime;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class DroneSubsystem implements Runnable{

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private Coordinate current_coords;


    private final double ACCELERATION_TIME = 0.051;
    private final double DECELERATION_TIME = 0.075;
    private final double TOP_SPEED = 20.8; //meter per sec
    private final double DROP_WATER_TIME = 20.0;
    private ArrayList<InputEvent> inputEvents;

    public DroneSubsystem(String name, Scheduler scheduler) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.scheduler = scheduler;
        this.inputEvents = new ArrayList<>();
        this.current_coords = new Coordinate(0,0);
    }

    //returns the time taken to put out fire in minutes
    public double calulateTotalTravelTime(InputEvent event){
        Coordinate fire_coords = event.zone.getZoneCenter();
        double distance = Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2));
        double travelTime = 2*(distance / TOP_SPEED); //both ways
        double totalSeconds = travelTime+ACCELERATION_TIME+DECELERATION_TIME+DROP_WATER_TIME;
        return totalSeconds/60;
    }

    @Override
    public void run() {
        int i = 0;
        while(i < 10){
            InputEvent event = this.scheduler.takeInputEvent(systemType, name);
            if (event != null) {
                double travelTime = calulateTotalTravelTime(event);
                System.out.println("[" +event.time +"]["+ systemType + " - " + name + "] HANDLING FIRE: " + event);
                try {
                    Thread.sleep((int) (travelTime * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                event.time = event.time.plusMinutes((long) travelTime); //update time of replay package
                System.out.println("["+event.time +"]["+ systemType + " - " + name + "] RETURNING: " + event);
                this.inputEvents.add(event);
                this.scheduler.addRelayMessageEvents(event, systemType, name);
            } else {
                this.scheduler.addRelayMessageEvents(null, systemType, name);
            }
            i++;
        }
    }
}
