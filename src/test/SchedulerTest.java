import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class to ensure correct behavior of its scheduling and event handling capabilities.
 */
class SchedulerTest {

    private Scheduler scheduler;

    @BeforeEach
    public void setUp() {
        // Create a Scheduler instance.
        scheduler = new Scheduler("TestScheduler");
    }

    /**
     * Test that immediately after construction, the zones map is empty.
     */
    @Test
    public void testInitialZonesEmpty() {
        Map<Integer, Zone> zones = scheduler.getZones();
        assertNotNull(zones, "Zones map should not be null");
        assertTrue(zones.isEmpty(), "Zones map should be initially empty");
    }

    /**
     * Test that addZones properly adds zones to the scheduler.
     */
    @Test
    public void testAddZones() {
        ArrayList<Zone> zonesList = new ArrayList<>();
        Zone zone1 = new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600));
        Zone zone2 = new Zone(2, new Coordinate(0, 600), new Coordinate(650, 1500));
        zonesList.add(zone1);
        zonesList.add(zone2);

        scheduler.addZones(zonesList, Systems.Scheduler, "TestScheduler");
        Map<Integer, Zone> zones = scheduler.getZones();

        assertNotNull(zones, "Zones map should not be null after adding zones");
        assertEquals(2, zones.size(), "Zones map should contain 2 zones");
        assertEquals(zone1, zones.get(1), "Zone with ID 1 should be present and match zone1");
        assertEquals(zone2, zones.get(2), "Zone with ID 2 should be present and match zone2");
    }

    /**
     * Test that calling addZones multiple times merges the zones correctly.
     */
    @Test
    public void testAddZonesMultipleCalls() {
        // First call: add zone with ID 1.
        ArrayList<Zone> zonesList1 = new ArrayList<>();
        Zone zone1 = new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600));
        zonesList1.add(zone1);
        scheduler.addZones(zonesList1, Systems.Scheduler, "TestScheduler");

        // Second call: add zone with ID 2.
        ArrayList<Zone> zonesList2 = new ArrayList<>();
        Zone zone2 = new Zone(2, new Coordinate(0, 600), new Coordinate(650, 1500));
        zonesList2.add(zone2);
        scheduler.addZones(zonesList2, Systems.Scheduler, "TestScheduler");

        Map<Integer, Zone> zones = scheduler.getZones();
        assertNotNull(zones, "Zones map should not be null after multiple calls");
        assertEquals(2, zones.size(), "Zones map should contain 2 zones after multiple calls");
        assertEquals(zone1, zones.get(1), "Zone with ID 1 should be present and match zone1");
        assertEquals(zone2, zones.get(2), "Zone with ID 2 should be present and match zone2");
    }
}