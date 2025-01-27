public class RelayPackage {

    public String packageID;
    public Systems receiverSystem;
    public InputEvent event;
    public Boolean lastPackage;


    public RelayPackage(String packageID, Systems receiverSystem, InputEvent event,Boolean lastPackage){
        this.packageID = packageID;
        this.receiverSystem = receiverSystem;
        this.event = event;
        this.lastPackage = lastPackage;
    }


    @Override
    public String toString() {
        return "PackageID: " + packageID;
    }
}
