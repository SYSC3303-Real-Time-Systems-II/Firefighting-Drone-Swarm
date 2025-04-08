import java.io.Serializable;


/**
 * Represents the status of a drone, including its name, state, and position.
 * This class implements {@link Serializable} so that it can be transmitted over a network.
 */
public class DroneStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String droneName;
    private final String state;
    private final double x;
    private final double y;


    /**
     * Constructs a new DroneStatus instance with the specified values.
     *
     * @param droneName The name of the drone.
     * @param state     The current state of the drone.
     * @param x         The x-coordinate (position) of the drone.
     * @param y         The y-coordinate (position) of the drone.
     */
    public DroneStatus(String droneName, String state, double x, double y) {
        this.droneName = droneName;
        this.state = state;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the name of the drone.
     *
     * @return The drone's name.
     */
    public String getDroneName() {
        return droneName;
    }


    /**
     * Returns the x-coordinate of the drone.
     *
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }


    /**
     * Returns the y-coordinate of the drone.
     *
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the current state of the drone.
     *
     * @return A string representing the drone's state.
     */
    public String getState() {
        return state;
    }

    /**
     * Returns a string representation of the DroneStatus.
     *
     * @return A formatted string containing the drone's name and coordinates.
     */
    @Override
    public String toString() {
        return String.format("DroneStatus[name=%s, x=%.2f, y=%.2f]", droneName, x, y);
    }
}
