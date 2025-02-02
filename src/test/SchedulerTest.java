import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class to ensure correct behavior of its scheduling and event handling capabilities.
 */
class SchedulerTest {
    private Scheduler scheduler;

    /**
     * Sets up the test environment before each test.
     * This method initializes a Scheduler and two different priority InputEvents.
     */
    @BeforeEach
    void setUp() {
        RelayBuffer relayBuffer = new RelayBuffer();
        EventBuffer eventBuffer = new EventBuffer();
        scheduler = new Scheduler("Scdlr", relayBuffer, eventBuffer);
    }

    /**
     * Tests the addZones method to ensure zones are correctly added to the Scheduler.
     */
    @Test
    void addZones() {
        ArrayList<Zone> zonesList = new ArrayList<>();
        zonesList.add(new Zone(1, new Coordinate(0,0), new Coordinate(700,600)));
        zonesList.add(new Zone(2, new Coordinate(0,600), new Coordinate(650,1500)));

        scheduler.addZones(zonesList, Systems.Scheduler, "Test");

        assertEquals(2, scheduler.getZones().size());

    }
}