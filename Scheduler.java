import java.util.ArrayList;
import java.util.Dictionary;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    public MessageRelay relay;

    public Scheduler(String name, MessageRelay relay) {
        this.name = name;
        this.systemType = Systems.Scheduler;
        this.relay = relay;
    }

    public RelayPackage createRelayPackage(String packageID, Systems receiver, InputEvent inputEvent, Boolean finalEvent){
        return new RelayPackage(packageID,receiver,inputEvent, finalEvent);
    }

    public void run() {
        ArrayList<RelayPackage> outgoingPackages = new ArrayList<RelayPackage>();

        //listen for packages from FIS
        while (true) {
            RelayPackage receivedPackage = relay.get(name, systemType);
            outgoingPackages.add(receivedPackage);
            System.out.println("[" + name + "] -- Processing event package: " + receivedPackage);

            if (receivedPackage.lastPackage) {
                break;
            }
        }

        //send packages to drone
        for (int i = 0; i < outgoingPackages.size(); i++) {
            String packageID = name+"_"+i;
            RelayPackage sendingPackge = outgoingPackages.get(i);
            RelayPackage relayPackage = createRelayPackage( packageID,Systems.DroneSubsystem,sendingPackge.event, sendingPackge.lastPackage);
            System.out.println("[" + name + "] -- created " + packageID +" to send to drone");
            relay.put(name, relayPackage);
        }

        outgoingPackages.clear();
        //listen for packages from Drone
        while (true) {
            RelayPackage receivedPackage = relay.get(name, systemType);
            outgoingPackages.add(receivedPackage);
            System.out.println("[" + name + "] -- Processing event package: " + receivedPackage);

            if (receivedPackage.lastPackage) {
                break;
            }
        }

        //send packages to FIS
        for (int i = 0; i < outgoingPackages.size(); i++) {
            String packageID = name+"_"+i;
            RelayPackage sendingPackge = outgoingPackages.get(i);
            RelayPackage relayPackage = createRelayPackage( packageID,Systems.FireIncidentSubsystem,sendingPackge.event, sendingPackge.lastPackage);
            System.out.println("[" + name + "] -- created " + packageID +" to send to drone");
            relay.put(name, relayPackage);
        }



    }
}
