import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneStatusTest {
    private static DroneStatus status;

    @BeforeAll
    static void setUp() {
        status = new DroneStatus("Drone1", "Available", 100.0, 200.0);
    }
    @Test
    void getDroneName() {
        assertEquals("Drone1", status.getDroneName());
    }

    @Test
    void getX() {
        assertEquals(100.0, status.getX());
    }

    @Test
    void getY() {
        assertEquals(200.0, status.getY());
    }

    @Test
    void getState() {
        assertEquals("Available", status.getState());
    }

    @Test
    void testToString() {
        String expected = "DroneStatus[name=Drone1, x=100.00, y=200.00]";
        assertEquals(expected, status.toString());

    }
}