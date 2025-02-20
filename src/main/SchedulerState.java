/**
 * This class deals with the states of the scheduler which there will be four for the state machine, it will
 * have one of the states as receiving an event from the fire incident subsystem, sending an event to the drone subsystem, receiving an
 * event from the drone subsystem and sending the event back to the fire incident subsystem.
 *
 * @author Rami Ayoub
 * @version 2.0
 */

public enum SchedulerState {
    WAITING, RECEIVED_EVENT_FROM_FIS, SENT_EVENT_TO_DRONE_SUBSYSTEM, RECEIVE_EVENT_FROM_DRONE_SUB, SEND_EVENT_TO_FIS
}
