import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is intended to test out the Event Buffer class and all of its main methods.
 * @author Rami Ayoub
 * @version 1.0
 */
class EventBufferTest {

    /**
     * Tests out the class by creating an object and checking the results of the methods.
     */
    @Test
    void eventBufferTest() {

        EventBuffer eventBuffer = new EventBuffer(); // Creates an event buffer object

        eventBuffer.addInputEvent(new InputEvent("14:00:15",1, "FIRE_DETECTED", "High", Status.UNRESOLVED), Systems.FireIncidentSubsystem); //Adds a new

        InputEvent inputEvent = eventBuffer.getBuffer().get(Systems.FireIncidentSubsystem).get(0); // Gets the input event that was just added to the buffer simulates as the fire incident subsystem

        assertEquals("Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High", inputEvent.toString()); // Checks that the events match

        InputEvent inputEvent2 = eventBuffer.getInputEvent(Systems.FireIncidentSubsystem); // Gets the newly added event from the buffer

        assertEquals("Time: 14:00:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High", inputEvent2.toString()); // Checks that the event that was received is new

        assertEquals(0, eventBuffer.getBuffer().get(Systems.FireIncidentSubsystem).size()); // Checks that the array lists of events for the system is now empty after the event was dealt with

    }
}