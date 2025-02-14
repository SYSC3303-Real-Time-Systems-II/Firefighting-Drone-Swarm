import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will mainly check that at all drone methods such as taking the input event from the fire incident subsystem through the scheduler and
 * adding a relaying message are implemented correctly.
 *
 * @author Rami Ayoub
 * @version 1.0
 */

class DroneSubsystemTest {

    /**
     * Tests the method to calculate the total travel time of the drone based on the zone, speed, distance, etc.
     */
    @Test
    void calculateTotalTravelTime(){

        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)); // Creates a new zone object that will be assigned to the event object

        EventBuffer buffer = new EventBuffer(); // Creates an event buffer

        DroneSubsystem droneSubsystem = new DroneSubsystem("drone", buffer); // Creates an object of the drone subsystem

        InputEvent inputEvent = new InputEvent("14:10:00", 3, "FIRE_DETECTED", "Low", Status.UNRESOLVED); // Created the input event which will be used to test the method

        inputEvent.setZone(zone); // Sets the zone of the event

        assertEquals(22.162366483877133, droneSubsystem.calculateZoneTravelTime(inputEvent)); // Checks that the calculated drone subsystem zone travel time is the expected value

        assertEquals(22.21336648387713, droneSubsystem.calculateArrivalZoneTime(inputEvent)); // Checks that the calculated drone subsystem arrival time for the zone is the expected value
    }
}