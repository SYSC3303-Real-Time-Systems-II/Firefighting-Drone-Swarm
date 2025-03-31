import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is intended to test out the Relay Package class and all of its main methods.
 * @author Rami Ayoub
 * @version 1.0
 */

class RelayPackageTest {

    /**
     * Tests out the class by creating an object and checking the results of the methods.
     */
    @Test
    void relayPackageTest() {

        ArrayList<Zone> zonesList = new ArrayList<>(); // Creates and initializes and arraylist of zones

        zonesList.add(new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600))); // Adds the first zone to the list
        zonesList.add(new Zone(2, new Coordinate(0, 600), new Coordinate(650, 1500))); // Adds the first zone to the list

        RelayPackage zonePackage = new RelayPackage("ZONE_PKG_1", Systems.Scheduler, null, zonesList); // Creates a relay package object

        assertEquals("ZONE_PKG_1", zonePackage.getRelayPackageID()); // Checks to makes sure that the ID matches
        assertEquals(Systems.Scheduler, zonePackage.getReceiverSystem()); // Checks to see that system matches

        zonePackage.setReceiverSystem(Systems.DroneSubsystem); // Changes the type of system

        assertEquals(Systems.DroneSubsystem, zonePackage.getReceiverSystem()); // Checks to see that system matches after the change

        zonePackage.setReceiverSystem(Systems.Scheduler); // Changes it back to a scheduler

        assertEquals(null, zonePackage.getEvent()); // Checks that there is no event

        InputEvent inputEvent = new InputEvent("14:00:15",1, "FIRE_DETECTED", "High", Status.UNRESOLVED, null); // Creates an event object

        zonePackage.setEvent(inputEvent); // Sets the newly created event

        assertEquals(inputEvent.toString(), zonePackage.getEvent().toString()); // Checks that the events are the same

        assertEquals(zonesList.get(0).toString(), zonePackage.getZone().get(0).toString()); // Checks that the zones are the same
        assertEquals(zonesList.get(1).toString(), zonePackage.getZone().get(1).toString()); // Checks that the zones are the same

        ArrayList<Zone> zonesList2 = new ArrayList<>(); // Creates a new zone array list object

        zonesList2.add(new Zone(1, new Coordinate(0, 0), new Coordinate(700, 600))); // Adds a new zone to the zone list

        zonePackage.setZone(zonesList2); // Sets the new zone list of the package

        assertEquals(zonesList.get(0).toString(), zonePackage.getZone().get(0).toString()); // Makes sure that the new zones match

    }

}