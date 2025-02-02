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

        DroneSubsystem droneSubsystem = new DroneSubsystem("drone", scheduler); // Creates an object of the drone subsystem

        InputEvent inputEvent = new InputEvent("14:10:00", 3, "FIRE_DETECTED", "Low"); // Created the input event which will be used to test the method

        droneSubsystem.getScheduler().addInputEvent(inputEvent, Systems.FireIncidentSubsystem, "FIS"); // Add the created input event into the scheduler where this is actually what the fire incident subsystem does, but we assume that there exists an event already

        InputEvent event = droneSubsystem.getScheduler().takeInputEvent(Systems.DroneSubsystem, "drone"); // The drone takes the created input event

        assertEquals(inputEvent, event); // Check that the event that was sent to the scheduler is the same as the event that was received from the scheduler

    }

    /**
     * Tests the method of adding the relay message back to the scheduler for the fire incident system to receive
     * the message.
     */
    @Test
    void addRelayMessage(){
        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object

        DroneSubsystem droneSubsystem = new DroneSubsystem("drone", scheduler); // Creates an object of the drone subsystem

        InputEvent inputEvent = new InputEvent("14:10:00", 3, "FIRE_DETECTED", "Low"); // Created the input event which will be used to test the method

        droneSubsystem.getScheduler().addRelayMessageEvents(inputEvent, Systems.DroneSubsystem, "drone"); // Adds the message that the drone is relaying back to the fire incident system through the scheduler

        assertEquals(inputEvent.toString(), droneSubsystem.getScheduler().getRelayMessageEvents().peek().toString()); // Checks that the event that was ent as a relay message to the scheduler was correct
    }

    /**
     * Tests the method to calculate the total travel time of the drone based on the zone, speed, distance, etc.
     */
    @Test
    void calculateTotalTravelTime(){

        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)); // Creates a new zone object that will be assigned to the event object

        Scheduler scheduler = new Scheduler("scheduler"); // Creates a scheduler object

        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv", scheduler);

        DroneSubsystem droneSubsystem = new DroneSubsystem("drone", scheduler); // Creates an object of the drone subsystem

        InputEvent inputEvent = new InputEvent("14:10:00", 3, "FIRE_DETECTED", "Low"); // Created the input event which will be used to test the method

        inputEvent.setZone(zone); // Sets the zone of the event

        assertEquals(5558, droneSubsystem.calulateTotalTravelTime(inputEvent)); // Checks that the calculated drone subsystem travel time is the expected value
    }
}
