import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a test class for the coordinate system to ensure that the functions and methods
 * are functioning as expected.
 * @author Rami Ayoub
 * @version 1.0
 */


class CoordinateTest {

    /**
     * Here we are creating a coordinate object and testing the methods.
     */
    @Test
    void coordinateTest() {
        Coordinate c1 = new Coordinate(1, 2); // Create a coordinate object
        Coordinate c2 = new Coordinate(3, 4); // Create another coordinate object

        assertEquals(1, c1.getX()); // Checks to see if the x coordinate is correct
        assertEquals(2, c1.getY()); // Checks to see if the y coordinate is correct

        assertEquals(3, c2.getX()); // Checks to see if the x coordinate is correct
        assertEquals(4, c2.getY()); // Checks to see if the y coordinate is correct

        assertEquals("(1, 2)", c1.toString()); // Checks for the to string method
        assertEquals("(3, 4)", c2.toString()); // Checks for the to string method
    }
}