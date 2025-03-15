import java.io.Serializable;
import java.util.ArrayList;

/**
 * The RelayPackage class represents a package used for communication between FireIncidentSubsystem and Scheduler.
 * It contains a unique ID, the receiver system, an optional event, and an optional list of zones.
 */
public class RelayPackage implements Serializable {

    private static final long serialVersionUID = 1L; // Add this to avoid compatibility issues
    private String RelayPackageID;              // Unique identifier for the package
    private Systems receiverSystem;             // The system that should receive this package
    private InputEvent event;                   // The event associated with the package (optional)
    private ArrayList<Zone> zone;               // The list of zones associated with the package (optional)

    /**
     * Constructs a RelayPackage object.
     *
     * @param relayPackageID The unique identifier for the package.
     * @param receiverSystem The system that should receive this package.
     * @param event          The event associated with the package (can be null).
     * @param zone           The list of zones associated with the package (can be null).
     */
    public RelayPackage(String relayPackageID, Systems receiverSystem, InputEvent event, ArrayList<Zone> zone){
        this.RelayPackageID = relayPackageID;
        this.receiverSystem = receiverSystem;
        this.event = event;
        this.zone = zone;
    }

    /**
     * Returns the unique identifier of the package.
     *
     * @return The package ID.
     */
    public String getRelayPackageID() {
        return RelayPackageID;
    }

    /**
     * Returns the system that should receive this package.
     *
     * @return The receiver system.
     */
    public Systems getReceiverSystem() {
        return receiverSystem;
    }

    /**
     * Sets the system that should receive this package.
     *
     * @param receiverSystem The receiver system.
     */
    public void setReceiverSystem(Systems receiverSystem) {
        this.receiverSystem = receiverSystem;
    }

    /**
     * Returns the event associated with the package.
     *
     * @return The event (can be null).
     */
    public InputEvent getEvent() {
        return event;
    }

    /**
     * Sets the event associated with the package.
     *
     * @param event The event to associate with the package.
     */
    public void setEvent(InputEvent event) {
        this.event = event;
    }

    /**
     * Returns the list of zones associated with the package.
     *
     * @return The list of zones (can be null).
     */
    public ArrayList<Zone> getZone() {
        return zone;
    }

    /**
     * Sets the list of zones associated with the package.
     *
     * @param zone The list of zones to associate with the package.
     */
    public void setZone(ArrayList<Zone> zone) {
        this.zone = zone;
    }

    /**
     * Returns a string representation of the RelayPackage.
     *
     * @return A string containing the receiver system, event, and zones.
     */
    @Override
    public String toString() {
        return "receiverSystem: " + this.receiverSystem + " event: [" + this.event + "] Zone: " + zone;
    }
}
