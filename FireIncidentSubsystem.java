import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private ArrayList<InputEvent> inputEvents;

    public FireIncidentSubsystem(String name, String inputFileName){
        this.name = name;
        this.inputEvents = readInput(inputFileName);

        for(int i=0; i < inputEvents.size(); i++){
            InputEvent a = inputEvents.get(i);
            System.out.println(a.toString());
        }

    }


    public ArrayList<InputEvent> readInput(String inputFileName){
        ArrayList<InputEvent> inputEvents = new ArrayList<InputEvent>();
        try {
            File myObj = new File("data/"+inputFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] parts = data.split(", ");
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
        for (int i = 0; i < 5; i++) {
            System.out.println("["+name +"] - "+ i);
        }
    }
}
