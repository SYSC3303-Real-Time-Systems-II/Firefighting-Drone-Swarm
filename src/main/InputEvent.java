public class InputEvent {

    public String time;
    public String zone;
    public EventType event_type;
    public Severity severity;

    public InputEvent(String time, String zone, String event_type, String severity){
        this.time = time;
        this.zone = zone;
        this.event_type = EventType.valueOf(event_type);;
        this.severity = Severity.valueOf(severity);
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "Time: "+ time +" Zone: " + zone + " Event Type: " + event_type + " Severity: " +severity;
    }
}
