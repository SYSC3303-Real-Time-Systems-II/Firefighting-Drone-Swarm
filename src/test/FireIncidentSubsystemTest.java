import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.DatagramSocket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will mainly check that at all information from the input file is received correctly into the priority queue. It will aos test the
 * methods of the fire incident subsystem such adding the input event to the scheduler and getting the relay message of the drone from the scheduler.
 * @author Rami Ayoub
 * @version 1.0
 */

class FireIncidentSubsystemTest {
    private FireIncidentSubsystem fireIncidentSubsystem;

    @BeforeEach
    void setUp() {
        fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv");
    }

    /**
     * Tests to makes sure that the event input file which traced all the input events was successful.
     */
    @Test
    void readInputEvents() {

        String[] time  = {"13:55:05", "13:58:54", "14:00:15", "14:06:42", "14:12:17", "14:22:57"}; // The time inputs from the file
        int[] zone = {1, 1, 2, 2, 2, 1}; // The zone inputs from the file
        EventType[] event_type = {EventType.DRONE_REQUEST, EventType.DRONE_REQUEST, EventType.FIRE_DETECTED, EventType.FIRE_DETECTED, EventType.DRONE_REQUEST, EventType.DRONE_REQUEST}; // The type of event
        Severity[] severity = {Severity.Low, Severity.Moderate, Severity.High, Severity.Low, Severity.Moderate, Severity.High}; // The severity of the fire
        FaultType[] faultTypes = {FaultType.CORRUPT, null, FaultType.JAMMED, null, FaultType.STUCK, null};

        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv"); // Creates a fire incident subsystem object

        int i = 0; // Will be used for indexing

        for (InputEvent q: fireIncidentSubsystem.getInputEvents()){ // Traverses through each input event that was received from the file
            assertEquals(time[i].toString(), q.getTime().toString()); // Checks for the time to be the same
            assertEquals(zone[i], q.getZoneId()); // Checks for the zone to be the same
            assertEquals(event_type[i], q.getEventType()); // Checks for the event to be the same
            assertEquals(severity[i], q.getSeverity()); // Checks for the severity to be the same
            assertEquals(faultTypes[i], q.getFaultType());
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

        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv"); // Creates a fire incident subsystem object

        int i = 0; // Will be used for indexing

        for (Zone z: fireIncidentSubsystem.getZonesList()){
            assertEquals(zoneId[i], z.getZoneID()); // Checks the zone id values are the same
            assertEquals(zoneStart[i].toString(), z.getZoneStart().toString()); // Checks the zone start coordinates are the same
            assertEquals(zoneEnd[i].toString(), z.getZoneEnd().toString()); // Checks that the zone start coordinates are the same
            i++;
        }
    }
}