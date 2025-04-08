import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;


public class DroneStateMachineTest {
    private Drone drone;
    private Field stateField;
    private Field socketField;

    @BeforeEach
    void setUp() throws Exception {
        drone = new Drone();
        drone.setLocalTime(LocalTime.parse(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

        // Reflection setup for state manipulation
        stateField = Drone.class.getDeclaredField("droneState");
        stateField.setAccessible(true);

        // Disable actual network operations
        socketField = Drone.class.getDeclaredField("sendReceiveSocket");
        socketField.setAccessible(true);
        socketField.set(drone, new MockDatagramSocket());
    }



    @Test
    void ascendingStateWithStuckFault() throws Exception {
        // Setup
        InputEvent event = createTestEvent(FaultType.STUCK);
        drone.setAssignedEvent(event);
        stateField.set(drone, new AscendingState());

        // Execute
        ((DroneStateMachine) stateField.get(drone)).handle(drone);

        // Verify
        assertTrue(stateField.get(drone) instanceof StuckState);
    }

    @Test
    void corruptStateClearsFault() throws Exception {
        // Setup
        InputEvent event = createTestEvent(FaultType.CORRUPT);
        drone.setAssignedEvent(event);
        stateField.set(drone, new CorruptState());

        // Execute
        ((DroneStateMachine) stateField.get(drone)).handle(drone);

        // Verify
        assertNull(event.getFaultType());
        assertTrue(stateField.get(drone) instanceof AvailableState);
    }

    @Test
    void cruisingStateUpdatesPosition() throws Exception {
        // Setup
        InputEvent event = createTestEvent();
        drone.setAssignedEvent(event);
        stateField.set(drone, new CruisingState());

        double initialX = drone.getCurrentCoordinates().getX();

        // Execute
        ((DroneStateMachine) stateField.get(drone)).handle(drone);

        // Verify
        assertTrue(drone.getCurrentCoordinates().getX() > initialX);
        assertTrue(stateField.get(drone) instanceof DropAgentState);
    }


    @Test
    void returningToBaseResetsPosition() throws Exception {
        // Setup
        drone.setCurrentCoordinates(new Coordinate(50, 50));
        stateField.set(drone, new ReturningToBaseState());

        // Execute
        ((DroneStateMachine) stateField.get(drone)).handle(drone);

        // Verify
        assertEquals(0, drone.getCurrentCoordinates().getX());
        assertEquals(0, drone.getCurrentCoordinates().getY());
        assertTrue(stateField.get(drone) instanceof RefillState);
    }

    @Test
    void refillStateBasedOnBattery() throws Exception {
        // Setup
        drone.setBatteryLevel(30);  // Below 80% threshold
        stateField.set(drone, new RefillState());

        // Execute
        ((DroneStateMachine) stateField.get(drone)).handle(drone);

        // Verify
        assertEquals(Drone.MAX_WATER_CAPACITY, drone.getWaterLevel());
        assertTrue(stateField.get(drone) instanceof BatteryRechargingState);
    }

    private InputEvent createTestEvent() {
        return createTestEvent(null);
    }

    private InputEvent createTestEvent(FaultType fault) {
        InputEvent event = new InputEvent(
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                1,
                "FIRE_DETECTED",
                "High",
                Status.UNRESOLVED,
                fault
        );
        event.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(100, 100)));
        return event;
    }

    private DatagramPacket createTestPacket(InputEvent event) throws IOException {
        byte[] data = drone.serializeEvent(event);
        return new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 6000);
    }

    static class MockDatagramSocket extends DatagramSocket {
        private boolean closed = false;

        public MockDatagramSocket() throws SocketException {
        }

        @Override public void send(DatagramPacket p) {}
        @Override public void close() { closed = true; }
        @Override public boolean isClosed() { return closed; }
    }
}
