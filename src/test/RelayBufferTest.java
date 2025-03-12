import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


/**
 * This class is intended to test out the Relay Buffer class and all of its main methods.
 * @author Rami Ayoub
 * @version 1.0
 */

class RelayBufferTest {

    /**
     * Tests out the class by creating an object and checking the results of the methods.
     */
    @Test
    void relayBufferTest() {

        RelayBuffer relayBuffer = new RelayBuffer(); // Creates a relay buffer object

        ArrayList<Zone> zonesList = new ArrayList<>(); // Creates an array list of zones

        zonesList.add(new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600))); // Adds the first zone to the list
        zonesList.add(new Zone(2, new Coordinate(0, 600), new Coordinate(650, 1500))); // Adds the first zone to the list

        RelayPackage relayPackage = new RelayPackage("ZONE_PKG_1", Systems.Scheduler, null, zonesList); // Creates a relay package object

        relayBuffer.addReplayPackage(relayPackage); //Adds a new relay package to the relay buffer

        assertEquals(relayPackage.toString(), relayBuffer.getBuffer().get(Systems.Scheduler).get(0).toString()); //Makes sure that the added package buffer is the same

        assertEquals(relayPackage.toString(), relayBuffer.getRelayPackage(Systems.Scheduler).toString()); // Gets and checks the package

        assertEquals(0, relayBuffer.getBuffer().get(Systems.Scheduler).size()); // Checks that it was successfully removed

    }
}