import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class to ensure correct behavior of its scheduling and event handling capabilities.
 */
class SchedulerTest {
    private Scheduler scheduler;

    /**
     * Sets up the test environment before each test.
     * This method initializes a Scheduler and two different priority InputEvents.
     */
    @BeforeEach
    void setUp() {
        RelayBuffer relayBuffer = new RelayBuffer();
        EventBuffer eventBuffer = new EventBuffer();
        scheduler = new Scheduler("Scdlr", relayBuffer, eventBuffer);
    }

    /**
     * Tests the addZones method to ensure zones are correctly added to the Scheduler.
     */
    @Test
    void addZones() {
        ArrayList<Zone> zonesList = new ArrayList<>();
        zonesList.add(new Zone(1, new Coordinate(0,0), new Coordinate(700,600)));
        zonesList.add(new Zone(2, new Coordinate(0,600), new Coordinate(650,1500)));

        scheduler.addZones(zonesList, Systems.Scheduler, "Test");

        assertEquals(2, scheduler.getZones().size());

    }

    /**
     * These test cases will test if the scheduler initializes to the WAITING state, and switches between states properly
     */
    @Test
    void testSchedulerState(){
        ArrayList<Zone> zonesList = new ArrayList<>();
        zonesList.add(new Zone(1, new Coordinate(0,0), new Coordinate(700,600)));
        zonesList.add(new Zone(2, new Coordinate(0,600), new Coordinate(650,1500)));

        Zone zone = zonesList.get(1); // Creates a new zone object that will be assigned to the event object
        InputEvent inputEvent = new InputEvent("14:10:00", 3, "FIRE_DETECTED", "Low", Status.UNRESOLVED); // Created the input event which will be used to test the method
        inputEvent.setZone(zone); // Sets the zone of the event

        RelayPackage relayPackage = new RelayPackage("1", Systems.Scheduler, inputEvent, zonesList);

        assertEquals(scheduler.getSchedulerState(), SchedulerState.WAITING); // Check that the initial state of the scheduler is WAITING
        scheduler.handleSchedulerState(inputEvent, "test", relayPackage); // Update the state
        assertEquals(scheduler.getSchedulerState(), SchedulerState.RECEIVED_EVENT_FROM_FIS); // Check if the state updated to the next state properly
        scheduler.handleSchedulerState(inputEvent, "test", relayPackage); // Update the state
        assertEquals(scheduler.getSchedulerState(), SchedulerState.SENT_EVENT_TO_DRONE_SUBSYSTEM); // Check if the state updated to the next state properly
        scheduler.handleSchedulerState(inputEvent, "test", relayPackage); // Update the state
        assertEquals(scheduler.getSchedulerState(), SchedulerState.SEND_EVENT_TO_FIS); // Check if the state updated to the next state properly
        scheduler.handleSchedulerState(inputEvent, "test", relayPackage); // Update the state
        assertEquals(scheduler.getSchedulerState(), SchedulerState.WAITING); // Check if the state looped back to the first state
    }
}