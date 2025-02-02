import java.util.ArrayList;

public class RelayPackage {
    private String RelayPackageID;
    private Systems receiverSystem;
    private InputEvent event;
    private ArrayList<Zone> zone;

    public RelayPackage(String relayPackageID, Systems receiverSystem, InputEvent event, ArrayList<Zone> zone){
        this.RelayPackageID = relayPackageID;
        this.receiverSystem = receiverSystem;
        this.event = event;
        this.zone = zone;
    }

    public String getRelayPackageID() {
        return RelayPackageID;
    }

    public Systems getReceiverSystem() {
        return receiverSystem;
    }

    public void setReceiverSystem(Systems receiverSystem) {
        this.receiverSystem = receiverSystem;
    }

    public InputEvent getEvent() {
        return event;
    }

    public void setEvent(InputEvent event) {
        this.event = event;
    }

    public ArrayList<Zone> getZone() {
        return zone;
    }

    public void setZone(ArrayList<Zone> zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "receiverSystem: " + this.receiverSystem + " event: [" + this.event +"] Zone: " + zone ;
    }
}
