import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputEventTest {
    private InputEvent event;

    @BeforeEach
    void setUp() {
        event = new InputEvent("11:30:00", 3, "FIRE_DETECTED", "High");
    }

    @Test
    void getSeverity() {
        assertEquals(Severity.High, event.getSeverity());
    }

    @Test
    void setZone(){
        Zone zone = new Zone(1, new Coordinate(0,0), new Coordinate(700,600));
        event.setZone(zone);
        assertEquals(zone, event.zone);
    }

    @Test
    void testToString() {
        String expected = "Time: 11:30 Zone: 3 Event Type: FIRE_DETECTED Severity: High";
        assertEquals(expected, event.toString());
    }
}