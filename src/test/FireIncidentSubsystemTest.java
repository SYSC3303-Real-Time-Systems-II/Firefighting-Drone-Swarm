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
     * Tests to makes sure that the event input file which traced all the input events was successful.
     */
    @Test
    void readInputEvents() {

        String[] time  = {"14:03:15", "14:10:00"}; // The time inputs from the file
        int[] zone = {1, 2}; // The zone inputs from the file
        EventType[] event_type = {EventType.FIRE_DETECTED, EventType.DRONE_REQUEST}; // The type of event
        Severity[] severity = {Severity.High, Severity.Moderate}; // The severity of the fire

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv", scheduler); // Creates a fire incident subsystem object

        int i = 0; // Will be used for indexing

        for (InputEvent q: fireIncidentSubsystem.getInputEvents()){ // Traverses through each input event that was received from the file
            assertEquals(time[i], q.getTime()); // Checks for the time to be the same
            assertEquals(zone[i], q.getZoneId()); // Checks for the zone to be the same
            assertEquals(event_type[i], q.getEventType()); // Checks for the event to be the same
            assertEquals(severity[i], q.getSeverity()); // Checks for the severity to be the same
            i++; // Increments to the next
        }
    }

    /**
     * A function that tests the reading of the zones file to check that all the inputs were traced successfully.
     */
    @Test
    void readZoneEvents(){
        int[] zoneId = {1, 2}; // The zone ids in the input file
        Coordinate[] zoneStart = {new Coordinate(0, 0), new Coordinate(0, 600)}; // The start zone coordinates from the input file
        Coordinate[] zoneEnd = {new Coordinate(700, 600), new Coordinate(650, 1500)}; // The end zone coordinates from the input file

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv", scheduler); // Creates a fire incident subsystem object

        int i = 0; // Will be used for indexing

        for (Zone z: fireIncidentSubsystem.getZonesList()){
            assertEquals(zoneId[i], z.getZoneID()); // Checks the zone id values are the same
            assertEquals(zoneStart[i].toString(), z.getZoneStart().toString()); // Checks the zone start coordinates are the same
            assertEquals(zoneEnd[i].toString(), z.getZoneEnd().toString()); // Checks that the zone start coordinates are the same
            i++;
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
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv" , scheduler); // Creates a fire incident subsystem object

        fireIncidentSubsystem.getScheduler().addInputEvent(fireIncidentSubsystem.getInputEvents().peek(), Systems.FireIncidentSubsystem, "FIS"); // Takes the first event of the input events in the fire incident subsystem and adds it to the scheduler

        assertEquals("Time: 14:03:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High", fireIncidentSubsystem.getInputEvents().peek().toString()); // Checks to see if the event that was added to the scheduler events was correct
    }

    /**
     * Tests the relaying message method t ensure that the message sent from the drone through the scheduler has been sent to
     * fire incident subsystem successfully.
     */
    @Test
    void getRelayMessageEvent() {
        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv" , scheduler); // Creates a fire incident subsystem object

        fireIncidentSubsystem.getScheduler().addRelayMessageEvents(fireIncidentSubsystem.getInputEvents().peek(), Systems.DroneSubsystem, "Drone"); // A message has been sent by the drone to the scheduler in which the fire incident subsystem wants to receive

        InputEvent inputEvent = fireIncidentSubsystem.getScheduler().getRelayMessageEvent(Systems.FireIncidentSubsystem, "FIS", false); // Gets the input event from the relayed messages that are in the scheduler to be received by the fire incident subsystem

        assertEquals("Time: 14:03:15 Zone: 1 Event Type: FIRE_DETECTED Severity: High", inputEvent.toString()); // Checks that the message that was sent by drone through the scheduler was the original message that was sent
    }

}