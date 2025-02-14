import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * The InputEvent class represents a fire incident event read by the FireIncidentSubsystem.
 * It contains details such as the time of the event, the zone where it occurred,
 * the type of event, its severity, and its current status.
 */
public class InputEvent {
    // Formatter for parsing and displaying time
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private LocalTime time;                 // Time of the event
    private int zoneId;                     // ID of the zone where the event occurred
    private Zone zone;                      // Zone object associated with the event
    private EventType event_type;           // Type of the event (e.g., DRONE_REQUEST, FIRE_DETECTED)
    private Severity severity;              // Severity of the event (e.g., High, Moderate, Low)
    private Status status;                  // Current status of the event (e.g., UNRESOLVED, COMPLETE)

    /**
     * Constructs an InputEvent object.
     *
     * @param time      The time of the event in "HH:mm:ss" format.
     * @param zone_id   The ID of the zone where the event occurred.
     * @param event_type The type of the event (e.g., FIRE, SMOKE).
     * @param severity  The severity of the event (e.g., High, Moderate, Low).
     * @param status    The current status of the event (e.g., UNRESOLVED, COMPLETE).
     */
    public InputEvent(String time, int zone_id, String event_type, String severity, Status status){
        this.time = LocalTime.parse(time, TIME_FORMATTER);
        this.zoneId = zone_id;
        this.zone = null;
        this.event_type = EventType.valueOf(event_type);
        this.severity = Severity.valueOf(severity);
        this.status = status;
    }
    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public EventType getEventType() {
        return event_type;
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
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public EventType getEvent_type() {
        return event_type;
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
     * @param event_type The new event type.
     */
    public void setEvent_type(EventType event_type) {
        this.event_type = event_type;
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
     * Returns a string representation of the event.
     *
     * @return A string containing the event's time, zone ID, event type, and severity.
     */
    @Override
    public String toString() {
        return "Time: " + time + " Zone: " + zoneId + " Event Type: " + event_type + " Severity: " + severity;
    }
}
