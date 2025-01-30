import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private Queue<InputEvent> inputEvents;
    private ArrayList<Zone> zonesList;


    public FireIncidentSubsystem(String name, String inputEventFileName, String inputZoneFileName,Scheduler scheduler){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInputEvents(inputEventFileName);
        this.zonesList = readZones(inputZoneFileName);
        this.scheduler = scheduler;
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
                InputEvent event = new InputEvent(parts[0], parts[1], parts[2], parts[3]);
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

        if (!this.zonesList.isEmpty()){
            this.scheduler.addZones(this.zonesList, this.systemType, this.name);
        }

        while (i < 10) {
            if (!this.inputEvents.isEmpty()) {
                this.scheduler.addInputEvent(this.inputEvents.remove(), this.systemType, this.name);
            }
            this.scheduler.getRelayMessageEvent(this.systemType, this.name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
    }

}
