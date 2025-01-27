import java.io.File;
import java.io.FileNotFoundException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.ArrayList;

public class FireIncidentSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private ArrayList<InputEvent> inputEvents;
    public MessageRelay relay;


    public FireIncidentSubsystem(String name, String inputFileName,MessageRelay relay){
        this.name = name;
        this.systemType = Systems.FireIncidentSubsystem;
        this.inputEvents = readInput(inputFileName);
        this.relay = relay;
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

    public RelayPackage createRelayPackage(String packageID,InputEvent inputEvent, Boolean finalEvent){
        return new RelayPackage(packageID,Systems.Scheduler,inputEvent, finalEvent);
    }

    @Override
    public void run() {

        ArrayList<RelayPackage> acknowledgePackages = new ArrayList<RelayPackage>();
        for (int i = 0; i < inputEvents.size(); i++) {

            Boolean finalEvent = false;

            // Check to see if last Event from input file
            if (i == inputEvents.size() - 1) {
                finalEvent = true;
            }

            String packageID = name+"_"+i;
            RelayPackage relayPackage = createRelayPackage( packageID,inputEvents.get(i), finalEvent);
            System.out.println("[" + name + "] -- created " + packageID +" to send to scheduler");
            relay.put(name, relayPackage);
        }

        while (true) {
            RelayPackage receivedPackage = relay.get(name, systemType);
            acknowledgePackages.add(receivedPackage);
            System.out.println("[" + name + "] -- Processing event package: " + receivedPackage);

            if (receivedPackage.lastPackage) {
                break;
            }
        }

        System.out.println("[" + name + "] --" + acknowledgePackages);

    }

}
