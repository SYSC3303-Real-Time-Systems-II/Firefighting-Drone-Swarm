import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * The Swing panel that displays a grid and the positions of multiple drones.
 */
public class DroneMapView extends JPanel {
    private static final int CELL_SIZE = 25; // e.g. each cell is 25x25 pixels
    private static final int ROWS = 24;      // e.g. 24 rows
    private static final int COLS = 28;      // e.g. 28 columns

    // The latest statuses from the subsystem
    private List<DroneStatus> droneStatuses;

    public DroneMapView() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        setBackground(Color.WHITE);
    }

    /**
     * Called by the controller to refresh the display based on new drone data.
     */
    public void updateDisplay(List<DroneStatus> statuses) {
        this.droneStatuses = statuses;
        repaint(); // triggers paintComponent
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1) Draw grid lines
        g.setColor(Color.LIGHT_GRAY);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 2) Draw drones
        if (droneStatuses != null) {
            g.setColor(Color.RED);
            for (DroneStatus status : droneStatuses) {
                // Convert real coords to grid cells
                int cellX = (int)(status.getX() / CELL_SIZE);
                int cellY = (int)(status.getY() / CELL_SIZE);

                if (cellX >= 0 && cellX < COLS && cellY >= 0 && cellY < ROWS) {
                    int padding = 4;
                    int diameter = CELL_SIZE - padding;
                    int drawX = cellX * CELL_SIZE + padding / 2;
                    int drawY = cellY * CELL_SIZE + padding / 2;
                    g.fillOval(drawX, drawY, diameter, diameter);
                }
            }
        }
    }
}
