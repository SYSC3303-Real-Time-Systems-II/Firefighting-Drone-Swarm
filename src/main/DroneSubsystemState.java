/**
 * This class deals with the states of the drone subsystem which there will be two for the state machine, it will
 * have one of the states as receiving an event from the scheduler and sending an event to the scheduler.
 *
 * @author Rami Ayoub
 * @version 2.0
 */

public enum DroneSubsystemState {
    WAITING, RECEIVED_EVENT_FROM_SCHEDULER, SENDING_EVENT_TO_SCHEDULER
}
