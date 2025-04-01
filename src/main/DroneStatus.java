import java.io.Serializable;

public class DroneStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String droneName;
    private final String state;
    private final double x;
    private final double y;

    public DroneStatus(String droneName, String state, double x, double y) {
        this.droneName = droneName;
        this.state = state;
        this.x = x;
        this.y = y;
    }

    public String getDroneName() {
        return droneName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("DroneStatus[name=%s, x=%.2f, y=%.2f]", droneName, x, y);
    }
}
