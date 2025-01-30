import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will mainly check that at all information from the input file is received correctly into the priority queue. It will aos test the
 * methods of the fire incident subsystem such adding the input event to the scheduler and getting the relay message of the drone from the scheduler.
 * @author Rami Ayoub
 * @version 1.0
 */

class FireIncidentSubsystemTest {

    /**
     * Tests to makes sure that the input file which traced all the input events was successful.
     */
    @Test
    void readInput() {

        String[] time  = {"11:30", "4:45", "5:30", "6:00", "9:34"}; // The time inputs from the file
        String[] zone = {"2", "1", "3", "1", "4"}; // The zone inputs from the file
        EventType[] event_type = {EventType.FIRE_DETECTED, EventType.DRONE_REQUEST, EventType.FIRE_DETECTED, EventType.DRONE_REQUEST, EventType.FIRE_DETECTED}; // The type of event
        Severity[] severity = {Severity.Low, Severity.High, Severity.Low, Severity.Low, Severity.High}; // The severity of the fire

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "test.txt", scheduler); // Creates a fire incident subsystem object

        int i = 0; // Will be used for indexing

        for (InputEvent q: fireIncidentSubsystem.getInputEvents()){ // Traverses through each input event that was received from the file
            assertEquals(time[i], q.getTime()); // Checks for the time to be the same
            assertEquals(zone[i], q.getZone()); // Checks for the zone to be the same
            assertEquals(event_type[i], q.getEventType()); // Checks for the event to be the same
            assertEquals(severity[i], q.getSeverity()); // Checks for the severity to be the same
            i++; // Increments to the next
        }
    }

    /**
     * Tests the method of the fire incident subsystem to send the input event to the scheduler after it has read it from an input
     * file. Makes sure that the message sent from the fire incident subsystem is the same as the one from the input file through the addInputEvent
     * method.
     */
    @Test
    void addInputEvent() {

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "test.txt", scheduler); // Creates a fire incident subsystem object

        scheduler.addInputEvent(fireIncidentSubsystem.getInputEvents().peek(), Systems.FireIncidentSubsystem, "FIS"); // Takes the first element from the queue

        assertEquals(fireIncidentSubsystem.getInputEvents().peek().toString(), scheduler.getInputEvents().peek().toString()); // Checks to see if the two values are equal

    }

    /**
     * Tests the relaying message method t ensure that the message sent from the drone through the scheduler has been sent to
     * fire incident subsystem successfully.
     */
    @Test
    void getRelayMessageEvent() {

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "test.txt", scheduler); // Creates a fire incident subsystem object

        scheduler.addRelayMessageEvents(fireIncidentSubsystem.getInputEvents().peek(), Systems.DroneSubsystem, "Drone"); // A message has been sent by the drone to the scheduler in which the fire incident subsystem wants to receive
        InputEvent inputEvent = scheduler.getRelayMessageEvent(Systems.FireIncidentSubsystem, "FIS"); // Gets the input message that was sent by the drone through the scheduler in which the fire incident subsystem will receive

        assertEquals(inputEvent, fireIncidentSubsystem.getInputEvents().peek()); // Checks that the message that was sent by drone through the scheduler was the original message that was sent
    }

}