import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class to ensure correct behavior of its scheduling and event handling capabilities.
 */
class SchedulerTest {
    private Scheduler scheduler;
    private InputEvent highPriorityEvent, lowPriorityEvent;

    /**
     * Sets up the test environment before each test.
     * This method initializes a Scheduler and two different priority InputEvents.
     */
    @BeforeEach
    void setUp() {
        scheduler = new Scheduler("Scdlr");
        highPriorityEvent = new InputEvent("11:30:00", 3, "FIRE_DETECTED", "High");
        lowPriorityEvent = new InputEvent("11:30:00", 2, "FIRE_DETECTED", "Low");
    }

    /**
     * Tests the addInputEvent method to ensure events are correctly added to the priority queue.
     */
    @Test
    void addInputEvent() {
        scheduler.addInputEvent(highPriorityEvent, Systems.Scheduler, "Test");
        scheduler.addInputEvent(lowPriorityEvent, Systems.Scheduler, "Test");
        assertEquals(2, scheduler.getInputEvents().size());
    }

    /**
     * Tests the takeInputEvent method to ensure it correctly retrieves and removes events based on priority.
     */
    @Test
    void takeInputEvent() {
        scheduler.addInputEvent(lowPriorityEvent, Systems.Scheduler, "Test");
        scheduler.addInputEvent(highPriorityEvent, Systems.Scheduler, "Test");

        //Higher priority event should be first in queue
        assertEquals(highPriorityEvent, scheduler.takeInputEvent(Systems.Scheduler, "Test"));
        assertEquals(lowPriorityEvent, scheduler.takeInputEvent(Systems.Scheduler, "Test"));

        // Queue should be empty now
        assertTrue(scheduler.getInputEvents().isEmpty());
    }

    /**
     * Tests the addRelayMessageEvents method to confirm that it properly queues events.
     */
    @Test
    void addRelayMessageEvents() {
        scheduler.addRelayMessageEvents(highPriorityEvent, Systems.Scheduler, "Test");
        scheduler.addRelayMessageEvents(lowPriorityEvent, Systems.Scheduler, "Test");
        assertEquals(2, scheduler.getRelayMessageEvents().size());
    }

    /**
     * Tests the getRelayMessageEvent method to ensure it retrieves and removes events from the relay message queue.
     */
    @Test
    void getRelayMessageEvent() {
        InputEvent event3 = new InputEvent("12:30:00", 1, "FIRE_DETECTED", "Low");
        scheduler.addRelayMessageEvents(highPriorityEvent, Systems.Scheduler, "Test");
        scheduler.addRelayMessageEvents(lowPriorityEvent, Systems.Scheduler, "Test");
        scheduler.addRelayMessageEvents(event3, Systems.Scheduler, "Test");

        // Check that events are retrieved in FIFO order
        assertEquals(highPriorityEvent, scheduler.getRelayMessageEvent(Systems.Scheduler, "Test", true));
        assertEquals(lowPriorityEvent, scheduler.getRelayMessageEvent(Systems.Scheduler, "Test", false));
        assertEquals(event3, scheduler.getRelayMessageEvent(Systems.Scheduler, "Test", false));

        // Queue should be empty now
        assertTrue(scheduler.getRelayMessageEvents().isEmpty());
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
}