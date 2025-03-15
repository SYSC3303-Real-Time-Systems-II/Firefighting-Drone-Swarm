import java.io.Serializable;

/**
 * The Zone class represents a  zone defined by its ID, start and end coordinates, and a calculated center coordinate.
 * It provides methods to calculate the center and parse coordinate strings.
 */
public class Zone implements Serializable {

    private final int zoneID;               // Unique identifier for the zone
    private final Coordinate zoneStart;     // Starting coordinate of the zone
    private final Coordinate zoneEnd;       // Ending coordinate of the zone
    private final Coordinate zoneCenter;    // Center coordinate of the zone (calculated)

    /**
     * Constructs a Zone object with the specified ID, start, and end coordinates.
     * The center coordinate is automatically calculated upon construction.
     *
     * @param zoneID    The unique identifier for the zone.
     * @param zoneStart The starting coordinate of the zone.
     * @param zoneEnd   The ending coordinate of the zone.
     */
    public Zone(int zoneID, Coordinate zoneStart, Coordinate zoneEnd){
        this.zoneID = zoneID;
        this.zoneStart = zoneStart;
        this.zoneEnd = zoneEnd;
        this.zoneCenter = calculateZoneCenter();
    }

    /**
     * Calculates the center coordinate of the zone based on the start and end coordinates.
     *
     * @return The center coordinate of the zone.
     */
    public Coordinate calculateZoneCenter(){
        double cx = (this.zoneStart.getX() + this.zoneEnd.getX()) / 2.0;
        double cy = (this.zoneStart.getY() + this.zoneEnd.getY()) / 2.0;

        return new Coordinate(cx, cy);
    }

    /**
     * Returns the unique identifier of the zone.
     *
     * @return The zone ID.
     */
    public int getZoneID() {
        return zoneID;
    }

    /**
     * Returns the starting coordinate of the zone.
     *
     * @return The starting coordinate.
     */
    public Coordinate getZoneStart() {
        return zoneStart;
    }

    /**
     * Returns the ending coordinate of the zone.
     *
     * @return The ending coordinate.
     */
    public Coordinate getZoneEnd() {
        return zoneEnd;
    }

    /**
     * Returns the center coordinate of the zone.
     *
     * @return The center coordinate.
     */
    public Coordinate getZoneCenter() {
        return zoneCenter;
    }

    /**
     * Parses a coordinate string in the format "(x;y)" and returns a Coordinate object.
     *
     * @param coordinateString The coordinate string to parse.
     * @return A Coordinate object representing the parsed coordinates.
     */
    public static Coordinate parseCoordinates(String coordinateString) {
        // Remove parentheses and split by semicolon
        String[] parts = coordinateString.replace("(", "").replace(")", "").split(";");
        double x = Double.parseDouble(parts[0]); // Extract x
        double y = Double.parseDouble(parts[1]); // Extract y
        return new Coordinate(x, y);
    }

    /**
     * Returns a string representation of the zone, including its ID, start, end, and center coordinates.
     *
     * @return A string representation of the zone.
     */
    @Override
    public String toString() {
        return "Zone ID: "+ this.zoneID +" Zone Start: " + this.zoneStart + " Zone End: " + this.zoneEnd + " Zone Center: " + this.zoneCenter;
    }
}

