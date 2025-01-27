import java.util.ArrayList;
import java.util.Dictionary;

public class DroneSubsystem implements Runnable{

    private String name;
    private Systems systemType;
    public MessageRelay relay;

    public DroneSubsystem(String name,MessageRelay relay) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.relay = relay;
    }

    public RelayPackage createRelayPackage(String packageID,InputEvent inputEvent, Boolean finalEvent){
        return new RelayPackage(packageID,Systems.Scheduler,inputEvent, finalEvent);
    }

    @Override
    public void run() {
        ArrayList<RelayPackage> outgoingPackages = new ArrayList<RelayPackage>();

        //listen for packages from Scheduler
        while (true) {
            RelayPackage receivedPackage = relay.get(name, systemType);
            System.out.println("[" + name + "] --"+receivedPackage);
            outgoingPackages.add(receivedPackage);
            System.out.println("[" + name + "] -- Processing event package: " + receivedPackage);
            if (receivedPackage.lastPackage){
                break;
            }
        }

        //send packages to Scheduler
        for (int i = 0; i < outgoingPackages.size(); i++) {
            String packageID = name+"_"+i;
            RelayPackage sendingPackge = outgoingPackages.get(i);
            RelayPackage relayPackage = createRelayPackage( packageID, sendingPackge.event, sendingPackge.lastPackage);
            System.out.println("[" + name + "] -- created " + packageID +" to send to Scheduler");
            relay.put(name, relayPackage);
        }

    }
}
