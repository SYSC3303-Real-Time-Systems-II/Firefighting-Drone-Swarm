import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a test for the zone to ensure that the methods are functioning correctly.
 * @author Rami Ayoub
 * @version 1.0
 */

class ZoneTest {
    /**
     * Here we are creating a Zone object and testing the methods.
     */
    @Test
    void zoneTest(){

        Zone zone = new Zone(1, new Coordinate(0, 0), new Coordinate(700,600)); // Creates a new zone// object

        assertEquals(1, zone.getZoneID()); // Checks that the zone ID matches
        assertEquals("(0.0, 0.0)", zone.getZoneStart().toString()); // Checks that the start coordinate matches
        assertEquals("(700.0, 600.0)", zone.getZoneEnd().toString()); // Checks that the end coordinate matches

        assertEquals("(0.0, 0.0)", zone.parseCoordinates("0;0").toString()); // Checks that the parsing method works correctly
    }
}