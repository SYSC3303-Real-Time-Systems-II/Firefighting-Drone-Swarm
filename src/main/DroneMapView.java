import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Swing panel that displays a grid and the positions of multiple drones.
 */
public class DroneMapView extends JPanel {
    private static final int CELL_SIZE = 25;
    private int rows = 24; // Default rows
    private int cols = 28; // Default columns

    private List<DroneStatus> droneStatuses; // The latest statuses from the subsystem
    private List<Zone> zones = new ArrayList<>();

    public DroneMapView() {
        setBackground(Color.WHITE);
    }

    public void updateDisplay(List<DroneStatus> statuses) {
        this.droneStatuses = statuses;
        repaint(); // triggers paintComponent
    }

    public void setZones(List<Zone> newZones) {
        this.zones = newZones;
        updateGridSize();
    }

    private void updateGridSize() {
        int maxRow = 0, maxCol = 0;
        for (Zone zone : zones) {
            System.out.println(zone.getZoneID() + " + "+zone.getZoneStart()+ "+"+ zone.getZoneEnd());
            maxRow = Math.max(maxRow, (int) (zone.getZoneEnd().getY() / CELL_SIZE));
            maxCol = Math.max(maxCol, (int) (zone.getZoneEnd().getX() / CELL_SIZE));
        }
        rows = maxRow;
        cols = maxCol;

        // Update the preferred size and revalidate the component layout
        setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        revalidate(); // This tells the layout manager to redo the layout based on the new size
        repaint(); // Redraw the component with new dimensions
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawZones(g);
        drawDrones(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawZones(Graphics g) {
        g.setColor(Color.RED);
        for (Zone zone : zones) {
            int xStart = (int) (zone.getZoneStart().getX() / CELL_SIZE) * CELL_SIZE;
            int yStart = (int) (zone.getZoneStart().getY() / CELL_SIZE) * CELL_SIZE;
            int xEnd = (int) (zone.getZoneEnd().getX() / CELL_SIZE) * CELL_SIZE;
            int yEnd = (int) (zone.getZoneEnd().getY() / CELL_SIZE) * CELL_SIZE;
            g.drawRect(xStart, yStart, xEnd - xStart, yEnd - yStart);
            g.drawString("Zone " + zone.getZoneID(), xStart + 5, yStart + 15);
        }
    }
    private void drawDrones(Graphics g) {
        if (droneStatuses != null) {
            for (DroneStatus status : droneStatuses) {
                System.out.println(status.getDroneName()+" "+status.getX()+","+status.getY());
                int cellX = (int) (status.getX() / CELL_SIZE);
                int cellY = (int) (status.getY() / CELL_SIZE);
                Color color = getDroneColour(status.getState());
                g.setColor(color);
                g.fillOval(cellX * CELL_SIZE + 4, cellY * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
                g.setColor(Color.BLACK);
                g.drawString(status.getDroneName(), cellX * CELL_SIZE + 4, cellY * CELL_SIZE + 20);
            }
        }
    }

    private Color getDroneColour(String state) {
        switch (state) {
            case "AvailableState":
                return Color.BLUE;
            case "AscendingState":
                return Color.ORANGE;
            case "CruisingState":
                return Color.CYAN;
            case "DropAgentState":
                return Color.GREEN;
            case "ReturningToBaseState":
                return Color.MAGENTA;
            default:
                return Color.BLACK;
        }
    }
}
