import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MultipleDroneTest {

    private List<Drone> drones;

    @BeforeEach
    public void setUp() {
        drones = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            drones.add(new Drone());
        }
    }

    /**
     * Test that each drone has a unique ID and name.
     */
    @Test
    public void testUniqueDroneIDsAndNames() {
        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (Drone d : drones) {
            assertNotNull(d.getName());
            assertTrue(d.getName().startsWith("Drone"));
            assertFalse(names.contains(d.getName()));
            assertFalse(ids.contains(d.getID()));
            names.add(d.getName());
            ids.add(d.getID());
        }

        assertEquals(5, ids.size());
        assertEquals(5, names.size());
    }

    /**
     * Test that all drones start at (0, 0) and are fully charged and filled with water.
     */
    @Test
    public void testInitialStateOfAllDrones() {
        for (Drone d : drones) {
            assertEquals(0.0, d.getCurrentCoordinates().getX(), 1e-9);
            assertEquals(0.0, d.getCurrentCoordinates().getY(), 1e-9);
            assertEquals(d.MAX_BATTERY_CAPACITY, d.getBatteryLevel(), 1e-9);
            assertEquals(d.MAX_WATER_CAPACITY, d.getWaterLevel(), 1e-9);
        }
    }

    /**
     * Test that battery draining works independently for each drone.
     */
    @Test
    public void testIndependentBatteryDrain() {
        drones.get(0).drainBattery(5); // drains 0.5
        drones.get(1).drainBattery(10); // drains 1.0

        assertEquals(drones.get(0).MAX_BATTERY_CAPACITY - 0.5, drones.get(0).getBatteryLevel(), 1e-9);
        assertEquals(drones.get(1).MAX_BATTERY_CAPACITY - 1.0, drones.get(1).getBatteryLevel(), 1e-9);

        // Others should be untouched
        for (int i = 2; i < drones.size(); i++) {
            assertEquals(drones.get(i).MAX_BATTERY_CAPACITY, drones.get(i).getBatteryLevel(), 1e-9);
        }
    }

    /**
     * Test that water usage and refill works independently for each drone.
     */
    @Test
    public void testIndependentWaterUseAndRefill() {
        drones.get(2).setWaterLevel(5.0);
        assertEquals(5.0, drones.get(2).getWaterLevel(), 1e-9);

        drones.get(2).refillWater();
        assertEquals(drones.get(2).MAX_WATER_CAPACITY, drones.get(2).getWaterLevel(), 1e-9);

        // Others should remain full
        for (int i = 0; i < drones.size(); i++) {
            if (i != 2) {
                assertEquals(drones.get(i).MAX_WATER_CAPACITY, drones.get(i).getWaterLevel(), 1e-9);
            }
        }
    }

    /**
     * Test that each drone can store and retrieve its own local time independently.
     */
    @Test
    public void testSetTimeIndependently() {
        LocalTime[] times = {
                LocalTime.of(8, 0),
                LocalTime.of(9, 30),
                LocalTime.of(10, 45),
                LocalTime.of(13, 15),
                LocalTime.of(16, 0)
        };

        for (int i = 0; i < drones.size(); i++) {
            drones.get(i).setLocalTime(times[i]);
        }

        for (int i = 0; i < drones.size(); i++) {
            assertEquals(times[i], drones.get(i).getLocalTime());
        }
    }


    /**
     * Test that each drone's initial state is AvailableState
     */
    @Test
    public void testInitialStateIsAvailable() {
        for (Drone d : drones) {
            assertTrue(d.getDroneState() instanceof AvailableState);
        }
    }
}
