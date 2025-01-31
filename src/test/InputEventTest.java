import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        event = new InputEvent("11:30:00", 3, "FIRE_DETECTED", "High");
    }

    /**
     * Tests the getSeverity method to ensure it returns the correct severity level of the event.
     */
    @Test
    void getSeverity() {
        assertEquals(Severity.High, event.getSeverity());
    }

    /**
     * Tests the setZone method to confirm that the zone can be correctly set and retrieved.
     */
    @Test
    void setZone(){
        // Create a new Zone and set it on the event
        Zone zone = new Zone(1, new Coordinate(0,0), new Coordinate(700,600));
        event.setZone(zone);
        // Assert that the zone set is the same as the one retrieved
        assertEquals(zone, event.zone);
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