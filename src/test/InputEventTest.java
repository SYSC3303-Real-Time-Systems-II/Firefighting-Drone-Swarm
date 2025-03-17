import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the InputEvent class to ensure correct initialization and behavior of its public methods.
 */
class InputEventTest {
    private InputEvent event;

    /**
     * Sets up the environment for each test.
     * This method initializes an InputEvent with predefined values to be used in the tests.
     */
    @BeforeEach
    void setUp() {
        event = new InputEvent("11:30:00", 3, "FIRE_DETECTED", "High", Status.UNRESOLVED);
    }

    /**
     * Tests the retrieval of the event type.
     */
    @Test
    void getEventType() {
        assertEquals(EventType.FIRE_DETECTED, event.getEventType());
    }

    /**
     * Tests the ability to set and then retrieve a zone.
     */
    @Test
    void setAndGetZone(){
        // Create a new Zone and set it on the event
        Zone zone = new Zone(1, new Coordinate(0,0), new Coordinate(700,600));
        event.setZone(zone);
        // Assert that the zone set is the same as the one retrieved
        assertEquals(zone, event.getZone());
    }

    /**
     * Tests the retrieval of the event time.
     */
    @Test
    void getTime() {
        assertEquals(LocalTime.parse("11:30:00"), event.getTime());
    }

    /**
     * Tests the retrieval of the zone ID.
     */
    @Test
    void getZoneId() {
        assertEquals(3, event.getZoneId());
    }

    /**
     * Tests the retrieval of the event status.
     */
    @Test
    void getStatus() {
        assertEquals(Status.UNRESOLVED, event.getStatus());
    }

    /**
     * Tests the ability to update and retrieve the event time.
     */
    @Test
    void setTime() {
        LocalTime time = LocalTime.parse("12:30:00");
        event.setTime(time);
        assertEquals(time, event.getTime());
    }

    /**
     * Tests the ability to update and retrieve the zone ID.
     */
    @Test
    void setZoneId() {
        Zone zone = new Zone(1, new Coordinate(0,0), new Coordinate(700,600));
        event.setZone(zone);
        assertEquals(zone, event.getZone());
    }

    /**
     * Tests the ability to update and retrieve the event type.
     */
    @Test
    void setEvent_type() {
        event.setEventType(EventType.DRONE_REQUEST);
        assertEquals(EventType.DRONE_REQUEST, event.getEventType());
    }

    /**
     * Tests the ability to update and retrieve the event status.
     */
    @Test
    void setStatus() {
        event.setStatus(Status.COMPLETE);
        assertEquals(Status.COMPLETE, event.getStatus());
    }

    /**
     * Tests the getSeverity method to ensure it returns the correct severity level of the event.
     */
    @Test
    void getSeverity() {
        assertEquals(Severity.High, event.getSeverity());
    }

    /**
     * Tests the ability to update and retrieve the event severity.
     */
    @Test
    void setSeverity() {
        Severity severity = Severity.High;
        event.setSeverity(severity);
        assertEquals(severity, event.getSeverity());
    }

    /**
     * Tests the toString method to ensure it returns a correctly formatted string based on the event's properties.
     */
    @Test
    void testToString() {
        String expected = "Time: 11:30 Zone: 3 Event Type: FIRE_DETECTED Severity: High";
        assertEquals(expected, event.toString());
    }

}