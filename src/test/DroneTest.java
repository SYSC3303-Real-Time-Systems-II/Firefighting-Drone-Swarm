import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class will check if the initialization and operation of the drone class works properly.
 * @author Louis Pantazopoulos
 * @version 1.0
 */

class DroneTest {

    /**
     * Test if the drone objects are initialized properly
     */
    @Test
    void testDroneAndAttributes(){
        //Three drones will be used to test this
        Drone drone1 = new Drone();
        Drone drone2 = new Drone();
        Drone drone3 = new Drone();

        //The Id of each drone should increment as new ones are created, so that is tested below:
        assertEquals(drone1.getID(), 1);
        assertEquals(drone2.getID(), 2);
        assertEquals(drone3.getID(), 3);

        //Each drone should contain a name containing their ID number, this is tested below:
        assertEquals(drone1.getName(), "Drone1");
        assertEquals(drone2.getName(), "Drone2");
        assertEquals(drone3.getName(), "Drone3");

        //Test the coordinates
        assertEquals(new Coordinate(0, 0).toString(), drone1.getCurrent_coords().toString());

        //Test acceleration time
        assertEquals(0.051, drone1.getACCELERATION_TIME());

        //Test declaration time
        assertEquals(0.075, drone1.getDECELERATION_TIME());

        //Test top speed
        assertEquals(20.8, drone1.getTOP_SPEED());

        //Test drop water time
        assertEquals(20.0, drone1.getDROP_WATER_TIME());

    }
}