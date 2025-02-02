import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InputEvent {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");;
    private LocalTime time;
    private int zoneId;
    private Zone zone;
    private EventType event_type;
    private Severity severity;
    private Status status;

    public InputEvent(String time, int zone_id, String event_type, String severity, Status status){
        this.time = LocalTime.parse(time, TIME_FORMATTER);
        this.zoneId = zone_id;
        this.zone = null;
        this.event_type = EventType.valueOf(event_type);
        this.severity = Severity.valueOf(severity);
        this.status = status;
    }

    public EventType getEventType(){
        return event_type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setZone(Zone zone){
        this.zone=zone;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getZoneId() {
        return zoneId;
    }

    public Zone getZone() {
        return zone;
    }

    public EventType getEvent_type() {
        return event_type;
    }

    public Status getStatus() {
        return status;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public void setEvent_type(EventType event_type) {
        this.event_type = event_type;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "Time: "+ time +" Zone: " + zoneId + " Event Type: " + event_type + " Severity: " +severity;
    }
}
