import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class to ensure correct behavior of its scheduling and event handling capabilities.
 */
class SchedulerTest {

    /**
     * Tests for the scheduler
     */
    @Test
    void schedulerTests() {
        Scheduler scheduler = new Scheduler("SCHD"); // Creates a scheduler object

        Zone zone1 = new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600)); // Creates a new zone object

        ArrayList<Zone> zones = new ArrayList<>(); // Creates an array list of zones

        zones.add(zone1); // Adds zone1

        scheduler.addZones(zones, Systems.Scheduler, "SCHD"); // adds the zones

        assertEquals(1, scheduler.getZones().size()); // Checks the expected and actual size

    }

}