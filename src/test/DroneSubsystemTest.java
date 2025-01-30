import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will mainly check that at all drone methods such as taking the input event from the fire incident subsystem through the scheduler and
 * adding a relaying message are implemented correctly.
 *
 * @author Rami Ayoub
 * @version 1.0
 */

class DroneSubsystemTest {
    /**
     * Tests the method of taking the input event from the scheduler which is one of the methods that the
     * drone uses from the scheduler.
     */
    @Test
    void takeInputEvent(){
        Scheduler scheduler = new Scheduler("scheduler"); // Creates a schedule object

        InputEvent inputEvent = new InputEvent("9:00", "3", "FIRE_DETECTED", "Low"); // Created the input event which will be used to test the method

        scheduler.addInputEvent(inputEvent, Systems.FireIncidentSubsystem, "FIS"); // Add the created input event into the scheduler

        InputEvent event = scheduler.takeInputEvent(Systems.DroneSubsystem, "drone"); // The drone takes the created input event

        assertEquals(inputEvent, event); // Check that the event that was sent to the scheduler is the same as the event that was received from the scheduler

    }

    /**
     * Tests the method of adding the relay message back to the scheduler for the fire incident system to receive
     * the message.
     */
    @Test
    void addRelayMessage(){
        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object

        InputEvent inputEvent = new InputEvent("9:00", "3", "FIRE_DETECTED", "Low"); // Created the input event which will be used to test the method

        scheduler.addRelayMessageEvents(inputEvent, Systems.DroneSubsystem, "drone"); // Adds the message that the drone is relaying bck to the fire incident system through the scheduler

        InputEvent event = scheduler.getRelayMessageEvents().peek(); // Gets the relay message that was just added by the drone to the scheduler to be sent to the fire incident system

        assertEquals(inputEvent, event); // Checks that the event that was ent as a relay message to the scheduler was correct
    }
}
