import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

import static java.lang.Thread.sleep;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private Queue<InputEvent> inputEvents;
    private ArrayList<Zone> zonesList;
    private LocalTime current_time;




    public FireIncidentSubsystem(String name, String inputEventFileName, String inputZoneFileName,Scheduler scheduler){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInputEvents(inputEventFileName);
        this.zonesList = readZones(inputZoneFileName);
        this.scheduler = scheduler;
        this.current_time = null;
    }

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
                InputEvent event = new InputEvent(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]);
                inputEvents.add(event);
            }
            return inputEvents;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: Ensure the correct file is used");
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public void run() {
        int i = 0;
        //send zone info to the scheduler
        if (!this.zonesList.isEmpty()){
            this.scheduler.addZones(this.zonesList, this.systemType, this.name);
        }
        //send input events to the scheduler and check for acknowledgement
        while (i < 10) {
            if (!this.inputEvents.isEmpty()) {
                InputEvent event = inputEvents.remove();
                //if first package set time, else sleep to simulate time passing
                if (current_time == null){
                    current_time = event.time;
                } else if (current_time != event.time){
                    Duration duration = Duration.between(current_time, event.time);
                    current_time = event.time;
                    try {
                        Thread.sleep(duration.toMinutes()*100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.scheduler.addInputEvent(event, this.systemType, this.name);
            }
            this.scheduler.getRelayMessageEvent(this.systemType, this.name, this.inputEvents.isEmpty());
            i++;
        }
    }
}
