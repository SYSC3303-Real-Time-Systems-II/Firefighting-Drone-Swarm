import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Scheduler scheduler;
    private ArrayList<InputEvent> inputEvents;


    public FireIncidentSubsystem(String name, String inputFileName, Scheduler scheduler){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInput(inputFileName);
        this.scheduler = scheduler;
    }

    public ArrayList<InputEvent> readInput(String inputFileName){
        ArrayList<InputEvent> inputEvents = new ArrayList<InputEvent>();
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
        while (true) {
            if (!inputEvents.isEmpty()) {

            }
        }
    }

}
