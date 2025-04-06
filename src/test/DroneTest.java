import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
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
     * Tests serialization and deserialization of events.
     */
    @Test
    public void testSerializeDeserializeEvent() throws IOException, ClassNotFoundException, IOException {
        // Create test event
        InputEvent originalEvent = new InputEvent("14:00:00", 1, "FIRE_DETECTED", "Low", Status.UNRESOLVED, null);
        Zone zone = new Zone(1, new Coordinate(5, 5), new Coordinate(15, 15));
        originalEvent.setZone(zone);

        // Serialize
        byte[] serializedData = drone.serializeEvent(originalEvent);
        assertNotNull(serializedData);
        assertTrue(serializedData.length > 0);

        // Deserialize
        InputEvent deserializedEvent = drone.deserializeEvent(serializedData);
        assertNotNull(deserializedEvent);
        assertEquals(originalEvent.getEventID(), deserializedEvent.getEventID());
        assertEquals(originalEvent.getEventType(), deserializedEvent.getEventType());
        assertEquals(originalEvent.getStatus(), deserializedEvent.getStatus());
    }

    /**
     * Tests setting and getting the current coordinates.
     */
    @Test
    public void testSetAndGetCurrentCoordinates() {
        Coordinate newCoord = new Coordinate(10.5, 20.5);
        drone.setCurrentCoordinates(newCoord);
        assertEquals(10.5, drone.getCurrentCoordinates().getX(), 1e-9);
        assertEquals(20.5, drone.getCurrentCoordinates().getY(), 1e-9);
    }

    /**
     * Tests the port ID initialization and retrieval.
     */
    @Test
    public void testGetPortID() {
        int id = drone.getID();
        // Since nextID is incremented after assignment, the portID should be 8000 + id + 1
        assertEquals(8000 + id + 1, drone.getPortID());
    }

    /**
     * Tests calculation of return travel time to base (0,0).
     */
    @Test
    public void testCalculateReturnTravelTime() {
        // Set drone coordinates to a non-zero position
        drone.setCurrentCoordinates(new Coordinate(30, 40));

        // Calculate expected time: distance to origin / top speed
        double distance = Math.sqrt(30 * 30 + 40 * 40); // Distance to (0,0)
        double expectedTime = distance / drone.TOP_SPEED;

        assertEquals(expectedTime, drone.calculateReturnTravelTime(), 1e-9);
    }

    /**
     * Tests location updating when returning to base.
     */
    @Test
    public void testUpdateReturnLocation() {
        // Set initial position
        drone.setCurrentCoordinates(new Coordinate(50, 0));

        // Update for 1 second
        drone.updateReturnLocation(1.0);

        // After 1 second, should move TOP_SPEED meters toward origin
        assertEquals(50 - drone.TOP_SPEED, drone.getCurrentCoordinates().getX(), 1e-9);
        assertEquals(0, drone.getCurrentCoordinates().getY(), 1e-9);

        // Test snapping to origin when very close
        drone.setCurrentCoordinates(new Coordinate(0.05, 0.05));
        drone.updateReturnLocation(1.0);

        assertTrue(0 > drone.getCurrentCoordinates().getX());
        assertTrue(0> drone.getCurrentCoordinates().getY());
    }

    /**
     * Tests location updating when traveling to a zone.
     */
    @Test
    public void testUpdateLocation() {
        // Create test event with zone
        InputEvent event = new InputEvent("14:00:00", 1, "FIRE_DETECTED", "Low", Status.UNRESOLVED, null);
        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(20, 0));
        event.setZone(zone);

        // Set assigned event
        drone.setAssignedEvent(event);

        // Initial position at (0,0)
        // Update for 1 second - should move toward zone center at (10,0)
        drone.updateLocation(1.0);

        // Should move TOP_SPEED meters toward zone center
        assertEquals(drone.TOP_SPEED, drone.getCurrentCoordinates().getX(), 1e-9);
        assertEquals(0, drone.getCurrentCoordinates().getY(), 1e-9);
    }

    /**
     * Tests the drop completion status setting and retrieval.
     */
    @Test
    public void testDropCompletedStatus() {
        // Initially should be false
        assertFalse(drone.isDropCompleted());

        // Set to true
        drone.setDropCompleted(true);
        assertTrue(drone.isDropCompleted());

        // Set back to false
        drone.setDropCompleted(false);
        assertFalse(drone.isDropCompleted());
    }

    /**
     * Tests calculating travel time to a zone and back (home zone time).
     */
    @Test
    public void testCalculateHomeZoneTime() {
        // Set initial position
        drone.setCurrentCoordinates(new Coordinate(0, 0));

        // Target coordinate
        Coordinate target = new Coordinate(30, 40);

        // Expected time is twice the one-way time (for round trip)
        double distance = Math.sqrt(30 * 30 + 40 * 40);
        double expectedTime = (distance / drone.TOP_SPEED) * 2;

        assertEquals(expectedTime, drone.calculateHomeZoneTime(target), 1e-9);
    }

    /**
     * Tests that the state machine is properly initialized.
     */
    @Test
    public void testInitialDroneState() {
        assertTrue(drone.getDroneState() instanceof AvailableState);
    }

    /**
     * Tests setting and getting the assigned event.
     */
    @Test
    public void testSetAndGetAssignedEvent() {
        // Create test event
        InputEvent event = new InputEvent("14:00:00", 1, "FIRE_DETECTED", "Low", Status.UNRESOLVED, null);

        // Initially null
        assertNull(drone.getAssignedEvent());

        // Set and verify
        drone.setAssignedEvent(event);
        assertEquals(event, drone.getAssignedEvent());
        assertEquals(1, drone.getAssignedEvent().getEventID());
        assertEquals(EventType.FIRE_DETECTED, drone.getAssignedEvent().getEventType());
    }
}