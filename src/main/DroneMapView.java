import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Swing panel that displays a grid and the positions of multiple drones.
 */
public class DroneMapView extends JPanel {
    private static final int CELL_SIZE = 25;
    private int rows = 24; // Default rows
    private int cols = 28; // Default columns

    private List<DroneStatus> droneStatuses; // The latest statuses from the subsystem
    private List<Zone> zones = new ArrayList<>();
    private Map<Integer, InputEvent> fireEvents = new HashMap<>(); // Store fire events by zone ID for quick access
    private Map<Integer, InputEvent> completedEvents = new HashMap<>();
    private Map<Integer, InputEvent> failedEvents = new HashMap<>();
    private JPanel mapPanel;

    // Metrics labels
    private JLabel droneResponseTimeLabel;
    private JLabel fireExtinguishedTimeLabel;
    private JLabel throughputLabel;
    private JPanel utilizationPanel;

    public DroneMapView() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.add(createLegendPanel());
        eastPanel.add(createMetricsPanel());
        add(eastPanel, BorderLayout.EAST);

        mapPanel = createMapPanel();
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateDisplay(List<DroneStatus> statuses) {
        this.droneStatuses = statuses;
        repaint(); // triggers paintComponent
    }

    public void setZones(List<Zone> newZones) {
        this.zones = newZones;
        updateGridSize();
    }

    public void displayEvent(InputEvent event) {
        if (event.getStatus() == Status.COMPLETE) {
            //completedEvents.put(event.getZoneId(), event);
            System.out.println("event completed...");
            fireEvents.remove(event.getZoneId());
            System.out.println("new fire events size: "+fireEvents.size());
        }
        if (event.getStatus() == Status.UNRESOLVED) {
            fireEvents.put(event.getZoneId(), event);
        }

        if (event.getFaultType() != null) {
            failedEvents.put(event.getZoneId(), event);
            System.out.println("event failed...");
        }
        repaint();
    }

    private void updateGridSize() {
        int maxRow = 0, maxCol = 0;
        for (Zone zone : zones) {
            maxRow = Math.max(maxRow, (int) (zone.getZoneEnd().getY() / CELL_SIZE));
            maxCol = Math.max(maxCol, (int) (zone.getZoneEnd().getX() / CELL_SIZE));
        }
        rows = maxRow;
        cols = maxCol;

        // Update the preferred size and revalidate the component layout
        mapPanel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        mapPanel.revalidate(); // This tells the layout manager to redo the layout based on the new size
        repaint(); // Redraw the component with new dimensions
    }
    private JPanel createMetricsPanel() {
        JPanel metricsPanel = new JPanel();
        metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.Y_AXIS));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Metrics"));
        metricsPanel.setPreferredSize(new Dimension(250, rows * CELL_SIZE));


        droneResponseTimeLabel = new JLabel("Drones Average Response Time: N/A");
        fireExtinguishedTimeLabel = new JLabel("Fire Extinguished Response Time: N/A");
        throughputLabel = new JLabel("Throughput (Fires Extinguished/Min): N/A");
        JLabel utilizationLabel = new JLabel("Drone Utilization:");
        utilizationLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        metricsPanel.add(droneResponseTimeLabel);
        metricsPanel.add(fireExtinguishedTimeLabel);
        metricsPanel.add(throughputLabel);

        utilizationPanel = new JPanel();
        utilizationPanel.setLayout(new BoxLayout(utilizationPanel, BoxLayout.Y_AXIS));
        utilizationPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        JScrollPane scrollPane = new JScrollPane(utilizationPanel);
        scrollPane.setBorder(null);
        metricsPanel.add(utilizationLabel);
        metricsPanel.add(scrollPane);

        return metricsPanel;
    }

    public void updateMetrics(Map<?, ?> metrics) {
        SwingUtilities.invokeLater(() -> {
            droneResponseTimeLabel.setText("Drones Average Response Time: " + round2Decimals((double) metrics.get("droneResponseTime")) + " ms");
            fireExtinguishedTimeLabel.setText("Fire Extinguished Response Time: " + round2Decimals((double) metrics.get("fireExtinguishedResponseTime")) + " s");
            throughputLabel.setText("Throughput (Fires Extinguished/Min): " + round2Decimals((double) metrics.get("throughput")));

            utilizationPanel.removeAll();
            Map<String, Double> utilizations = (Map<String, Double>) metrics.get("utilizations");
            utilizations.forEach((name, utilization) -> {
                JLabel label = new JLabel(name + ": " + round2Decimals(utilization)+"%");
                utilizationPanel.add(label);
            });
            utilizationPanel.revalidate();
            utilizationPanel.repaint();
        });
    }

    private String round2Decimals(double decimals) {
        return String.format("%.2f", decimals);
    }
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));
        //legendPanel.setPreferredSize(new Dimension(150, 150));

        // Adjust the vertical spacing between legend entries
        legendPanel.add(Box.createVerticalStrut(5)); // Small vertical space at the top

        legendPanel.add(createLegendEntry(Color.BLUE, "Available"));
        legendPanel.add(createLegendEntry(Color.ORANGE, "Ascending"));
        legendPanel.add(createLegendEntry(Color.CYAN, "Cruising"));
        legendPanel.add(createLegendEntry(Color.GREEN, "Dropping Agent"));
        legendPanel.add(createLegendEntry(Color.MAGENTA, "Returning to Base"));
        legendPanel.add(createLegendEntry(Color.RED, "Recharging Battery"));
        legendPanel.add(createLegendEntry(Color.YELLOW, "Refilling Agent Tank"));
        return legendPanel;

    }

    private static Component createLegendEntry(Color color, String text) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.X_AXIS));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); // Limit height

        JLabel colorLabel = new JLabel();
        colorLabel.setMinimumSize(new Dimension(10, 10));
        colorLabel.setPreferredSize(new Dimension(10, 10));
        colorLabel.setMaximumSize(new Dimension(10, 10));
        colorLabel.setOpaque(true);
        colorLabel.setBackground(color);

        JLabel textLabel = new JLabel(" " + text);

        entry.add(colorLabel);
        entry.add(Box.createHorizontalStrut(5)); // Small space between color box and text
        entry.add(textLabel);

        return entry;
    }

    private JPanel createMapPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
                drawZones(g);
                drawFires(g);
                drawDrones(g);
            }
        };
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        panel.setLayout(null); // Use absolute positioning
        return panel;
    }
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        drawGrid(g);
//        drawZones(g);
//        drawFires(g);
//        drawDrones(g);
//
//    }

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
            g.drawString("Zone " + zone.getZoneID(), xStart + 30, yStart + 20);
        }
    }
    private void drawDrones(Graphics g) {
        Image droneImage = loadImage("drone.png");
        if (droneStatuses != null) {
            for (DroneStatus status : droneStatuses) {
                int cellX = (int) (status.getX() / CELL_SIZE);
                int cellY = (int) (status.getY() / CELL_SIZE);
                Color color = getDroneColour(status.getState());
                g.setColor(color);
                g.fillOval(cellX * CELL_SIZE + 2, cellY * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                g.drawImage(droneImage, cellX * CELL_SIZE + 3, cellY * CELL_SIZE + 3, CELL_SIZE - 6, CELL_SIZE - 6, this);
                g.setColor(Color.BLACK);
                g.drawString(status.getDroneName(), cellX * CELL_SIZE, cellY * CELL_SIZE);
            }
        }
    }

    private void drawFires(Graphics g) {
        Image fireImage = loadImage("fire.png");
        if (!fireEvents.isEmpty()) {
            System.out.println("Fire events count: " + fireEvents.size());
            System.out.println("fireevents not null");
            for (InputEvent fireEvent : fireEvents.values()) {
                Zone zone = fireEvent.getZone();
                Coordinate fireCoords = zone.getZoneCenter();
                int x = (int) fireCoords.getX() / CELL_SIZE;
                int y = (int) fireCoords.getY() / CELL_SIZE;
                //g.setColor(Color.RED);
                //g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.drawImage(fireImage, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE, this);
                g.setColor(Color.BLACK);
                g.drawString(fireEvent.getSeverity().toString().substring(0,1), x * CELL_SIZE, y * CELL_SIZE -4);
            }
        }
    }


    private Color getDroneColour(String state) {
        return switch (state) {
            case "AvailableState" -> Color.BLUE;
            case "AscendingState" -> Color.ORANGE;
            case "CruisingState" -> Color.CYAN;
            case "DropAgentState" -> Color.GREEN;
            case "ReturningToBaseState" -> Color.MAGENTA;
            case "BatteryRechargingState" -> Color.RED;
            case "RefillState" -> Color.YELLOW;
            default -> new Color(255, 255, 255, 0);
        };
    }

    public Image loadImage(String fileName) {
        try {
            URL imageUrl = getClass().getResource("/"+fileName);
            return new ImageIcon(imageUrl).getImage();
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }

}
