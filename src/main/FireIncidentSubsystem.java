import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private Queue<InputEvent> inputEvents;


    public FireIncidentSubsystem(String name, String inputFileName, Scheduler scheduler){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInput(inputFileName);
        this.scheduler = scheduler;
    }

    public Queue<InputEvent> readInput(String inputFileName){
        Queue<InputEvent> inputEvents = new LinkedList<>();
        try {
            File myObj = new File("data/"+inputFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] parts = data.split(" ");
                InputEvent event = new InputEvent(parts[0],parts[1],parts[2],parts[3]);
                inputEvents.add(event);
            }
            myReader.close();
            return inputEvents;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: Ensure the Correct file is used");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
            if (!this.inputEvents.isEmpty()) {
                this.scheduler.addInputEvent(this.inputEvents.remove(), this.systemType, this.name);
            }
            this.scheduler.getRelayMessageEvent(systemType, name);

            i++;
        }
    }

}
