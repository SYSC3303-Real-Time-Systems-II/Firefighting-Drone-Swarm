import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will check if the initialization and operation of the drone class works properly.
 * @author Louis Pantazopoulos
 * @version 1.0
 */

class DroneTest {

    private Drone drone;
    /**
     * Initializes a new Drone instance before each test.
     */
    @BeforeEach
    public void setUp() {
        drone = new Drone();
    }

    /**
     * Tests that the drone's name is generated correctly based on its ID.
     */
    @Test
    public void testGetNameAndID() {
        int id = drone.getID();
        assertEquals("Drone" + id, drone.getName());
    }

    /**
     * Tests that the drone initializes with coordinates (0,0).
     */
    @Test
    public void testInitialCoordinates() {
        // The drone should initialize at (0,0)
        assertEquals(0, drone.getCurrentCoordinates().getX(), 1e-9);
        assertEquals(0, drone.getCurrentCoordinates().getY(), 1e-9);
    }

    /**
     * Tests setting and retrieving the local time for the drone.
     */
    @Test
    public void testSetAndGetLocalTime() {
        LocalTime now = LocalTime.now();
        drone.setLocalTime(now);
        assertEquals(now, drone.getLocalTime());
    }


    /**
     * Tests the methods for managing water level.
     * Verifies that the water level is initially at maximum capacity,
     * can be set to a different value, and is restored upon refill.
     */
    @Test
    public void testWaterLevelMethods() {
        // Initially, water level should equal MAX_WATER_CAPACITY
        assertEquals(drone.MAX_WATER_CAPACITY, drone.getWaterLevel(), 1e-9);

        // Change water level and then refill
        drone.setWaterLevel(10.0);
        assertEquals(10.0, drone.getWaterLevel(), 1e-9);
        drone.refillWater();
        assertEquals(drone.MAX_WATER_CAPACITY, drone.getWaterLevel(), 1e-9);
    }

    /**
     * Tests the battery management methods.
     * Verifies that the battery starts at maximum capacity,
     * decreases correctly when drained, and resets upon charging.
     */
    @Test
    public void testBatteryMethods() {
        // Initially, battery level should equal MAX_BATTERY_CAPACITY
        assertEquals(drone.MAX_BATTERY_CAPACITY, drone.getBatteryLevel(), 1e-9);

        // Drain battery: drainBattery(seconds) reduces battery by seconds * BATTERY_DRAIN_RATE
        double initialBattery = drone.getBatteryLevel();
        drone.drainBattery(5); // expected drain: 5 * 0.1 = 0.5
        assertEquals(initialBattery - 0.5, drone.getBatteryLevel(), 1e-9);

        // Recharge battery
        drone.chargeBattery();
        assertEquals(drone.MAX_BATTERY_CAPACITY, drone.getBatteryLevel(), 1e-9);
    }

    /**
     * Tests the calculation of travel time from the drone's current position to an event's zone.
     * The zone's center is computed as the midpoint between its start and end coordinates.
     */
    @Test
    public void testCalculateZoneTravelTime() {
        // Create a Zone whose center is computed as the midpoint of its start and end coordinates.
        // For example, let the zone start at (0,0) and end at (20,0) so that the center is (10,0).
        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(20, 0));
        InputEvent event = new InputEvent("14:00:00", 1, "FIRE_DETECTED", "Low", Status.UNRESOLVED, null);
        event.setZone(zone);
        // With currentCoordinates = (0,0), distance from (0,0) to (10,0) is 10.
        // Travel time = distance / TOP_SPEED = 10 / 20.8.
        double expectedTime = 10.0 / drone.TOP_SPEED;
        double travelTime = drone.calculateZoneTravelTime(event);
        assertEquals(expectedTime, travelTime, 1e-9);
    }

    /**
     * Tests that draining the battery over a specified period decreases the battery level as expected.
     */
    @Test
    public void testDrainBattery() {
        double initialBattery = drone.getBatteryLevel();
        drone.drainBattery(10); // Expected drain: 10 * 0.1 = 1.0
        assertEquals(initialBattery - 1.0, drone.getBatteryLevel(), 1e-9);
    }

    /**
     * Tests that the sleepFor method causes the thread to pause for approximately the specified duration.
     */
    @Test
    public void testSleepFor() {
        // Test that sleepFor approximately sleeps for the given duration.
        long start = System.currentTimeMillis();
        drone.sleepFor(0.5); // Sleep for 0.5 seconds.
        long elapsed = System.currentTimeMillis() - start;
        // Allow some tolerance for thread scheduling delays.
        assertTrue(elapsed >= 500);
    }

    /**
     * Tests that the drone enters the correct state when it encounters a fault
     */
//    @Test
//    public void testDroneFaultState() {
//        drone.setAssignedEvent(new InputEvent("12:00:00", 4, "DRONE_REQUEST", "Low", Status.UNRESOLVED, FaultType.STUCK));
//
//        if (drone.getAssignedEvent().getFaultType() == FaultType.STUCK) {
//            DroneStateMachine DSM = new DroneStateMachine() {
//                @Override
//                public void handle(Drone context) {
//                    //
//                }
//            };
//            DSM.handle(drone);
//            assertTrue(drone.getDroneState() instanceof AvailableState);
//        }
//        else if (drone.getAssignedEvent().getFaultType() == FaultType.JAMMED) {
//
//        }
//        else if (drone.getAssignedEvent().getFaultType() == FaultType.CORRUPT) {
//
//        }
//    }
}