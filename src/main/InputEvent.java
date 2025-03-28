import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * The InputEvent class represents a fire incident event read by the FireIncidentSubsystem.
 * It contains details such as the time of the event, the zone where it occurred,
 * the type of event, its severity, and its current status.
 */
public class InputEvent implements Serializable {
    // Formatter for parsing and displaying time
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int InputEventID = 1;

    private LocalTime time;                 // Time of the event
    private int zoneId;                     // ID of the zone where the event occurred
    private Zone zone;                      // Zone object associated with the event
    private EventType eventType;           // Type of the event (e.g., DRONE_REQUEST, FIRE_DETECTED)
    private Severity severity;              // Severity of the event (e.g., High, Moderate, Low)
    private Status status;                  // Current status of the event (e.g., UNRESOLVED, COMPLETE)
    private int eventID;                    // The ID of the event
    private FaultType faultType;            // The fault time that is associated with the input event

    /**
     * Constructs an InputEvent object.
     *
     * @param time      The time of the event in "HH:mm:ss" format.
     * @param zoneId   The ID of the zone where the event occurred.
     * @param eventType The type of the event (e.g., FIRE, SMOKE).
     * @param severity  The severity of the event (e.g., High, Moderate, Low).
     * @param status    The current status of the event (e.g., UNRESOLVED, COMPLETE).
     * @param faultType The fault type that is associated with the event
     */
    public InputEvent(String time, int zoneId, String eventType, String severity, Status status, FaultType faultType) {
        this.time = LocalTime.parse(time, TIME_FORMATTER);
        this.zoneId = zoneId;
        this.zone = null;
        this.eventType = EventType.valueOf(eventType);
        this.severity = Severity.valueOf(severity);
        this.status = status;
        this.eventID = InputEventID;
        this.faultType = faultType;
        InputEventID ++;
    }

    /**
     * Returns the id of the event.
     *
     * @return The event id.
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Returns the severity of the event.
     *
     * @return The event severity.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the zone associated with the event.
     *
     * @param zone The zone to associate with the event.
     */
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    /**
     * Returns the time of the event.
     *
     * @return The event time.
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Returns the ID of the zone where the event occurred.
     *
     * @return The zone ID.
     */
    public int getZoneId() {
        return zoneId;
    }

    /**
     * Returns the zone associated with the event.
     *
     * @return The zone object.
     */
    public Zone getZone() {
        return zone;
    }

    /**
     * Returns the current status of the event.
     *
     * @return The event status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the time of the event.
     *
     * @param time The new time for the event.
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    /**
     * Sets the ID of the zone where the event occurred.
     *
     * @param zoneId The new zone ID.
     */
    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The new event type.
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Sets the current status of the event.
     *
     * @param status The new status for the event.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets the severity of the event.
     *
     * @param severity The new severity for the event.
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Gets the fault type of the event.
     * @return the fault type of the event.
     */
    public FaultType getFaultType() {
        return faultType;
    }

    /**
     * Sets the fault type of the event.
     */
    public void setFaultType(FaultType faultType) {
        this.faultType = faultType;
    }

    /**
     * Returns a string representation of the event.
     *
     * @return A string containing the event's time, zone ID, event type, and severity.
     */
    @Override
    public String toString() {
        return "Time: " + time + " Zone: " + zoneId + " Event Type: " + eventType + " Severity: " + severity;
    }
}
