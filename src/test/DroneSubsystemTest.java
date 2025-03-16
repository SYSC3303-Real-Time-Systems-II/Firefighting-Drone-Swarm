import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.util.List;

public class DroneSubsystemTest {

    private DroneSubsystem ds;

    @BeforeEach
    public void setUp() {
        // Create a DroneSubsystem with 3 drones.
        ds = new DroneSubsystem("TestDS", 3);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Close the private DatagramSocket (named "socket") using reflection,
        // to free the port for subsequent tests.
        Field socketField = DroneSubsystem.class.getDeclaredField("socket");
        socketField.setAccessible(true);
        DatagramSocket socket = (DatagramSocket) socketField.get(ds);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Test that the calculateDistance method returns the correct Euclidean distance.
     */
    @Test
    public void testCalculateDistance() {
        // For two points (0,0) and (3,4), the expected distance is 5.0.
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(3, 4);
        double distance = ds.calculateDistance(a, b);
        assertEquals(5.0, distance, 1e-9);
    }

    /**
     * Test that the DroneSubsystem constructor properly initializes the drone fleet.
     */
//    @Test
//    public void testDroneFleetInitialization() throws Exception {
//        // Access the private 'drones' field using reflection.
//        Field dronesField = DroneSubsystem.class.getDeclaredField("drones");
//        dronesField.setAccessible(true);
//        List<Drone> drones = (List<Drone>) dronesField.get(ds);
//        assertNotNull(drones);
//        assertEquals(3, drones.size());
//    }
}
