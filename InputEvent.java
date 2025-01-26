public class InputEvent {

    public String time;
    public String zone;
    public String event_type;
    public String severity;

    public InputEvent(String time, String zone, String event_type, String severity){
        this.time = time;
        this.zone = zone;
        this.event_type = event_type;
        this.severity = severity;
    }


    @Override
    public String toString() {
        return "Time: "+ time +" Zone: " + zone + " Event Type: " + event_type + " Severity: " +severity;
    }
}
