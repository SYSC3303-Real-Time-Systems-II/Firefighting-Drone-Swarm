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
     * These test cases will test if the drone initializes to the AVAILABLE state, and switches between states properly
     */
    @Test
    void testDroneState(){
        Drone drone = new Drone();
        drone.setLocalTime(LocalTime.MIDNIGHT); //The drone class needs a local time. It doesn't have to be midnight, this is just used for the test.
        assertEquals(drone.getDroneState(), DroneState.AVAILABLE); //Make sure the drone starts at the AVAILABLE state
        drone.handleDroneState(5, 2); //Update the state
        assertEquals(drone.getDroneState(), DroneState.ON_ROUTE); //Check if the state updated to the next state properly
        drone.handleDroneState(5, 2); //Update the state
        assertEquals(drone.getDroneState(), DroneState.ARRIVED); //Check if the state updated to the next state properly
        drone.handleDroneState(5, 2); //Update the state
        assertEquals(drone.getDroneState(), DroneState.DROPPING_WATER); //Check if the state updated to the next state properly
        drone.handleDroneState(5, 2); //Update the state
        assertEquals(drone.getDroneState(), DroneState.RETURNING_TO_BASE); //Check if the state updated to the next state properly
        drone.handleDroneState(5, 2); //Update the state
        assertEquals(drone.getDroneState(), DroneState.AVAILABLE); //Check if the state looped back to the first state
    }

    /**
     * Test if the drone objects are initialized properly
     */
    @Test
    void testDroneAttributes(){
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
    }
}