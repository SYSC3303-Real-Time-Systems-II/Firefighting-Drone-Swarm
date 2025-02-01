import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InputEvent {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");;
    public LocalTime time;
    public int zone_id;
    public Zone zone;
    public EventType event_type;
    public Severity severity;

    public InputEvent(String time, int zone_id, String event_type, String severity){
        this.time = LocalTime.parse(time, TIME_FORMATTER);
        this.zone_id = zone_id;
        this.zone = null;
        this.event_type = EventType.valueOf(event_type);
        this.severity = Severity.valueOf(severity);
    }

    public String getTime(){
        return time;
    }

    public int getZoneId(){
        return zone_id;
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

    @Override
    public String toString() {
        return "Time: "+ time +" Zone: " + zone_id + " Event Type: " + event_type + " Severity: " +severity;
    }
}
