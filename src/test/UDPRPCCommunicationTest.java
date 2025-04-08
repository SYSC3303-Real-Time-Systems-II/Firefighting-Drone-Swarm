import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This will be used to test out the overall communication between the classes.
 */
class UDPRPCCommunicationTest {

    /**
     * Tests for the communication.
     */
    @Test
    void communicationTest() {

        // Creates the fire incident subsystem object
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem("FIS", "Sample_event_file.csv", "sample_zone_file.csv");

        Scheduler scheduler = new Scheduler("SCHD"); // Creates a scheduler object

        DroneSubsystem droneSubsystem = new DroneSubsystem("DSS", 5); // Creates a drone subsystem object

        fireIncidentSubsystem.handleSendingState(); // Sends the zones to the scheduler

        scheduler.handleReceiveFromFIS(); // Receives the message that was sent by the FIS

        assertEquals(5, scheduler.getZones().size()); // Checks that both zones were successfully added

        fireIncidentSubsystem.handleSendingState(); // Sends the input event to the scheduler

        scheduler.handleReceiveFromFIS(); // Receives the event from the FIS

        assertEquals(1, scheduler.getInputEvent().size()); // Checks that the input event was added

        scheduler.handleSendToDSS(); // Sends it to the drone subsystem

        assertEquals(0, scheduler.getInputEvent().size()); // Checks that the input event was sent no longer in queue

        droneSubsystem.handleWaitingState(); // Gets the input event from scheduler

        droneSubsystem.handleReceivedEventState(); // Handles the received event

        droneSubsystem.handleSendingConfirmationState(); // Sends the confirmation back to teh scheduler

        scheduler.handleCheckDSSResponse(); // Checks for the response from the drone subsystem

        assertEquals(0, scheduler.getConfirmationPackage().size()); // Should now have the confirmation package in queue and ready to be sent

        scheduler.handleSendConfirmation(); // Sends the confirmation back to the FIS

        assertEquals(0, scheduler.getConfirmationPackage().size()); // Should be empty now

        fireIncidentSubsystem.handleIdleState(); // Receives the conformation

    }
}