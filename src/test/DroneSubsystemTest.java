import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.lang.reflect.*;
import java.time.LocalTime;
import java.util.concurrent.CopyOnWriteArrayList;

public class DroneSubsystemTest {
    private DroneSubsystem subsystem;
    private Field dronesField;
    private Field pendingEventsField;
    private Field currentStateField;

    @BeforeEach
    void setUp() throws Exception {
        resetInputEventID();
        subsystem = new DroneSubsystem("TestDS", 3);

        dronesField = DroneSubsystem.class.getDeclaredField("drones");
        dronesField.setAccessible(true);

        pendingEventsField = DroneSubsystem.class.getDeclaredField("pendingEvents");
        pendingEventsField.setAccessible(true);

        currentStateField = DroneSubsystem.class.getDeclaredField("currentState");
        currentStateField.setAccessible(true);
    }

    private void resetInputEventID() throws Exception {
        Field idField = InputEvent.class.getDeclaredField("InputEventID");
        idField.setAccessible(true);
        idField.set(null, 1);
    }

    @AfterEach
    void tearDown() {
        try {
            if (subsystem != null) {
                closeSocket("schedulerSocket");
                closeSocket("droneSocket");
            }
        } catch (Exception e) {
            System.err.println("Error closing sockets: " + e.getMessage());
        }
    }

    private void closeSocket(String fieldName) throws Exception {
        Field field = DroneSubsystem.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        DatagramSocket socket = (DatagramSocket) field.get(subsystem);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Nested
    class SerializationTests {
        @Test
        void roundTripSerialization() throws Exception {
            // Create test zone
            Zone testZone = new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)); // Creates a new zone// object

            // Create test event
            InputEvent original = new InputEvent("10:00:00", 1, "FIRE_DETECTED", "High", Status.UNRESOLVED, null);

            original.setZone(testZone);
            original.setRemainingAgentNeeded(2);

            byte[] serialized = subsystem.serializeEvent(original);
            InputEvent deserialized = subsystem.deserializeEvent(serialized);

            assertAll(
                    () -> assertEquals(original.getEventID(), deserialized.getEventID()),
                    () -> assertEquals(original.getZone().getZoneID(), deserialized.getZone().getZoneID()),
                    () -> assertEquals(original.getRemainingAgentNeeded(), deserialized.getRemainingAgentNeeded()),
                    () -> assertEquals(original.getFaultType(), deserialized.getFaultType())
            );
        }
    }

    @Nested
    class EventScenarioTests {
        @Test
        void completeEventFlow() throws Exception {
            InputEvent testEvent = createTestEvent();
            testEvent.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)));

            pendingEventsField.set(subsystem, new CopyOnWriteArrayList<>(List.of(testEvent)));
            subsystem.handleReceivedEventState();
            subsystem.handleSendingConfirmationState();

            List<InputEvent> pending = (List<InputEvent>) pendingEventsField.get(subsystem);
            assertTrue(pending.isEmpty());
        }

        @Test
        void eventNeedsMoreAgents() throws Exception {
            InputEvent testEvent = createTestEvent();
            testEvent.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)));
            testEvent.setRemainingAgentNeeded(2);

            pendingEventsField.set(subsystem, new CopyOnWriteArrayList<>(List.of(testEvent)));
            subsystem.handleReceivedEventState();
            subsystem.handleSendingConfirmationState();

            List<InputEvent> pending = (List<InputEvent>) pendingEventsField.get(subsystem);
            assertEquals(0, pending.size());
        }

        @Test
        void eventWithFault() throws Exception {
            InputEvent testEvent = createTestEvent();
            testEvent.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)));
            testEvent.setFaultType(FaultType.STUCK);

            pendingEventsField.set(subsystem, new CopyOnWriteArrayList<>(List.of(testEvent)));
            subsystem.handleSendingConfirmationState();

            // Verify fault was persisted
            List<InputEvent> pending = (List<InputEvent>) pendingEventsField.get(subsystem);
            assertEquals(FaultType.STUCK, pending.get(0).getFaultType());
        }
    }

    @Nested
    class AlgorithmTests {
        @Test
        void selectsClosestDrone() throws Exception {
            List<Drone> drones = (List<Drone>) dronesField.get(subsystem);

            // Configure drones with coordinates
            setDroneCoordinates(drones.get(0), 0, 0);
            setDroneCoordinates(drones.get(1), 10, 10);
            setDroneCoordinates(drones.get(2), 5, 5);

            // Create event with zone
            InputEvent event = createTestEvent();
            event.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)));

            Drone selected = subsystem.chooseDroneAlgorithm(event);
            assertEquals(drones.get(1).getName(), selected.getName());
        }

        private void setDroneCoordinates(Drone drone, int x, int y) throws Exception {
            Field coordField = Drone.class.getDeclaredField("currentCoordinates");
            coordField.setAccessible(true);
            coordField.set(drone, new Coordinate(x, y));
        }
    }

    private InputEvent createTestEvent() {
        InputEvent event = new InputEvent( LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), 1, "FIRE_DETECTED", "High", Status.UNRESOLVED, null);
        event.setZone(new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)));
        return event;
    }
}