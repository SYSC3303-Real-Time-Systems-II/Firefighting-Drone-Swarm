import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will mainly check that at all drone methods such as taking the input event from the fire incident subsystem through the scheduler and
 * adding a relaying message are implemented correctly.
 *
 * @author Rami Ayoub, Louis Pantazopoulos
 * @version 2.0
 */

class DroneSubsystemTest {

    /**
     * Tests the method to calculate the handling of a received state. Also,
     * Tests the ability for calculating the distance between teh drone and the zone.
     */
    @Test
    void testDSS() {
        DroneSubsystem dss = new DroneSubsystem("DSS1", 5); // Creates a new drone subsystem object

        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600)); // Creates a new zone object

        InputEvent inputEvent = new InputEvent("14:00:15", 1, "FIRE_DETECTED", "High", Status.UNRESOLVED); // Creates a new inpur event object

        inputEvent.setZone(zone); // Sets the zoen for the input event

        dss.setCurrentEvent(inputEvent); // Creates and assigns the input event to teh drone subsystem

        assertEquals(0, dss.getWorkingDrones().size()); // No drone would be serviced to do anything yet

        dss.handleReceivedEventState(); // gets a drone to do handle the event

        assertEquals(1, dss.getWorkingDrones().size()); // one drone would be serviced to do something

        Coordinate c1 = new Coordinate(30, 90); // Creates a coordinate object

        Coordinate c2 = new Coordinate(20, 50); // Creates another coordinate object

        assertEquals(41.23105625617661, dss.calculateDistance(c1, c2)); // Compares the expected and actual value

    }
}

