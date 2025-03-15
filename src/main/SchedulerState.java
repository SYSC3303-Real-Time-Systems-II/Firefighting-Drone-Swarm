/**
 * This class deals with the states of the scheduler which there will be four for the state machine, it will
 * have one of the states as receiving an event from the fire incident subsystem, sending an event to the drone subsystem, receiving an
 * event from the drone subsystem and sending the event back to the fire incident subsystem.
 *
 * @author Rami Ayoub
 * @version 2.0
 */

public enum SchedulerState {
    RECEIVE_FROM_FIS,
    SEND_TO_DSS,
    CHECK_DSS_RESPONSE,
    SEND_CONFIRMATION
}
