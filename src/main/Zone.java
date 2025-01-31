import java.time.ZoneId;

public class Zone {
    private final int zoneID;
    private final Coordinate zoneStart;
    private final Coordinate zoneEnd;
    private final Coordinate zoneCenter;

    public Zone(int zoneID, Coordinate zoneStart, Coordinate zoneEnd){
        this.zoneID = zoneID;
        this.zoneStart = zoneStart;
        this.zoneEnd = zoneEnd;
        this.zoneCenter = calculateZoneCenter();
    }

    private Coordinate calculateZoneCenter(){
        double cx = (this.zoneStart.getX() + this.zoneEnd.getX()) / 2.0;
        double cy = (this.zoneStart.getY() + this.zoneEnd.getY()) / 2.0;

        return new Coordinate(cx, cy);
    }

    public int getZoneID() {
        return zoneID;
    }

    public Coordinate getZoneCenter() {
        return zoneCenter;
    }

    public static Coordinate parseCoordinates(String coordinateString) {
        // Remove parentheses and split by semicolon
        String[] parts = coordinateString.replace("(", "").replace(")", "").split(";");
        double x = Double.parseDouble(parts[0]); // Extract x
        double y = Double.parseDouble(parts[1]); // Extract y
        return new Coordinate(x, y);
    }

    @Override
    public String toString() {
        return "Zone ID: "+ this.zoneID +" Zone Start: " + this.zoneStart + " Zone End: " + this.zoneEnd + " Zone Center: " + this.zoneCenter;
    }
}

