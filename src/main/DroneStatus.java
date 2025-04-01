import java.io.Serializable;

public class DroneStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String droneName;
    private final double x;
    private final double y;

    public DroneStatus(String droneName, double x, double y) {
        this.droneName = droneName;
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

    @Override
    public String toString() {
        return String.format("DroneStatus[name=%s, x=%.2f, y=%.2f]", droneName, x, y);
    }
}
