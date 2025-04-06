import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Swing panel that displays a grid and the positions of multiple drones,
 * along with events such as fires and faults.
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

    /**
     * Creates the DroneMapView panel with a legend panel on the east side
     * and a scrollable map panel in the center.
     */
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

    /**
     * Updates the list of drone statuses (positions, states, etc.) and triggers a repaint.
     *
     * @param statuses A list of {@link DroneStatus} objects providing the latest info on drones.
     */
    public void updateDisplay(List<DroneStatus> statuses) {
        this.droneStatuses = statuses;
        repaint(); // triggers paintComponent
    }

    /**
     * Replaces the list of zones and updates the grid size accordingly.
     *
     * @param newZones A list of {@link Zone} objects to be displayed.
     */
    public void setZones(List<Zone> newZones) {
        this.zones = newZones;
        updateGridSize();
    }

    /**
     * Displays a new or updated event (e.g. fire, fault, completion) on the map.
     * This method also starts a timer to remove fault messages after 5 seconds.
     *
     * @param event The {@link InputEvent} representing the new or updated event status.
     */
    public void displayEvent(InputEvent event) {
        if (event.getStatus() == Status.COMPLETE) {
            //completedEvents.put(event.getZoneId(), event);
            System.out.println("event completed...");
            fireEvents.remove(event.getEventID());
            System.out.println("new fire events size: "+fireEvents.size());
        }
        if (event.getStatus() == Status.UNRESOLVED) {
            fireEvents.put(event.getEventID(), event);
        }

        if (event.getFaultType() != null) {
            failedEvents.put(event.getEventID(), event);
            System.out.println("event failed... showing fault for 5s...");

            // Start a one-shot timer that removes the event from failedEvents after 5 seconds
            Timer faultTimer = new Timer(5000, e -> {
                failedEvents.remove(event.getEventID());
                // Repaint so the fault icon/label disappears
                SwingUtilities.invokeLater(this::repaint);
            });
            faultTimer.setRepeats(false);
            faultTimer.start();
        } else {
            // If the event's fault is cleared, remove from failedEvents
            failedEvents.remove(event.getEventID());
        }
        repaint();
    }

    /**
     * Updates the grid size based on the maximum extents of all zones.
     * Revalidates and repaints the map panel accordingly.
     */
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

    /**
     * Creates and configures a panel containing metrics (response time, throughput, etc.).
     *
     * @return A configured {@link JPanel} that displays drone-related metrics.
     */
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

    /**
     * Updates the metrics displayed on the metrics panel.
     *
     * @param metrics A map containing metric keys and values (e.g. "droneResponseTime", "throughput").
     */
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

    /**
     * Rounds a double value to two decimal places, returning a string representation.
     *
     * @param decimals The double value to be rounded.
     * @return A string formatted to two decimal places.
     */
    private String round2Decimals(double decimals) {
        return String.format("%.2f", decimals);
    }

    /**
     * Creates the legend panel that shows what each drone state color indicates.
     *
     * @return A {@link JPanel} with color + text entries describing each drone state.
     */
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

    /**
     * Creates a single legend entry (a colored box and the corresponding text).
     *
     * @param color The color displayed in the small box.
     * @param text  The label text next to the color box.
     * @return A {@link Component} that can be added to the legend panel.
     */
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

    /**
     * Creates the main map panel which houses the grid, zones, drones, fires, etc.
     *
     * @return A {@link JPanel} that overrides paintComponent to draw everything.
     */
    private JPanel createMapPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
                drawZones(g);
                drawFires(g);
                drawDrones(g);
                drawFaults(g);
            }
        };
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        panel.setLayout(null); // Use absolute positioning
        return panel;
    }

    /**
     * Draws the light gray grid lines across the panel.
     *
     * @param g The {@link Graphics} context to draw onto.
     */
    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Draws the zones in red, each outlined by a rectangle with a small "Zone X" label.
     *
     * @param g The {@link Graphics} context to draw onto.
     */
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

    /**
     * Draws the drones (as colored circles + an icon), labeling each with its name.
     *
     * @param g The {@link Graphics} context to draw onto.
     */
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


    /**
     * Draws a fault icon + red text below the drone if an event is found in the failedEvents map.
     *
     * @param g The {@link Graphics} context to draw onto.
     */
    private void drawFaults(Graphics g) {
        Image faultImage = loadImage("error.png");
        if (failedEvents.isEmpty() || droneStatuses == null) return;

        // For each fault, find the matching drone's coordinates, then draw a red "FAULT"
        for (InputEvent faultEvent : failedEvents.values()) {
            String droneName = faultEvent.getHandlingDrone();
            if (droneName == null) continue;

            // Find the drone's position from the statuses
            DroneStatus faultedDrone = findDroneStatusByName(droneName);
            if (faultedDrone == null) {
                continue; // no matching drone found
            }

            int cellX = (int) (faultedDrone.getX() / CELL_SIZE);
            int cellY = (int) (faultedDrone.getY() / CELL_SIZE);

            // Slightly below the cell
            int labelX = cellX * CELL_SIZE;
            int labelY = (cellY + 1) * CELL_SIZE + 12;

            int iconSize = 16;
            int iconOffsetY = labelY - (iconSize - 4);

            g.drawImage(faultImage, labelX, iconOffsetY, iconSize, iconSize, this);

            g.setColor(Color.RED);

            String faultMessage = new String();

            if (faultEvent.getFaultType() == FaultType.JAMMED) {
                faultMessage = "NOZZLE IS JAMMED";
            }
            if (faultEvent.getFaultType() == FaultType.CORRUPT) {
                faultMessage = "MESSAGE RECEIVED IS CORRUPTED";
            }
            if (faultEvent.getFaultType() == FaultType.STUCK){
                faultMessage = "GOT STUCK MID-FLIGHT - GOING OFFLINE";
            }
            String faultLabel = droneName + " FAULT: " + faultMessage;
            g.drawString(faultLabel, labelX + iconSize + 4, labelY);
        }
    }


    /**
     * Finds and returns the {@link DroneStatus} matching the given drone name.
     *
     * @param name The name of the drone, as given by {@link DroneStatus#getDroneName()}.
     * @return The matching {@link DroneStatus} if found, otherwise null.
     */
    private DroneStatus findDroneStatusByName(String name) {
        for (DroneStatus ds : droneStatuses) {
            if (ds.getDroneName().equals(name)) {
                return ds;
            }
        }
        return null;
    }

    /**
     * Draws fire icons either full-cell if there's a single fire, or in quadrants if multiple fires exist in the same zone.
     *
     * @param g The {@link Graphics} context to draw onto.
     */
    private void drawFires(Graphics g) {
        Image fireImage = loadImage("fire.png");
        if (fireEvents.isEmpty()) {
            return;
        }
        // Group fires by zone
        Map<Integer, List<InputEvent>> firesByZone = new HashMap<>();
        for (InputEvent fireEvent : fireEvents.values()) {
            int zoneId = fireEvent.getZone().getZoneID();
            firesByZone.computeIfAbsent(zoneId, k -> new ArrayList<>()).add(fireEvent);
        }

        // For each zone with fires, place them
        for (Map.Entry<Integer, List<InputEvent>> zoneEntry : firesByZone.entrySet()) {
            List<InputEvent> zoneFires = zoneEntry.getValue();
            if (zoneFires.isEmpty()) {
                continue;
            }

            // All events in zoneFires share the same zone
            Zone zone = zoneFires.get(0).getZone();
            Coordinate center = zone.getZoneCenter();

            // The cell (row,col) that contains the zone center
            int centerCellX = (int) (center.getX() / CELL_SIZE);
            int centerCellY = (int) (center.getY() / CELL_SIZE);

            // The top-left pixel of that cell
            int cellPixelX = centerCellX * CELL_SIZE;
            int cellPixelY = centerCellY * CELL_SIZE;

            // If there's only 1 fire in this zone, fill the entire cell
            if (zoneFires.size() == 1) {
                InputEvent singleFireEvent = zoneFires.get(0);
                g.drawImage(fireImage, cellPixelX, cellPixelY, CELL_SIZE, CELL_SIZE, this);
                g.setColor(Color.BLACK);
                String severityLetter = singleFireEvent.getSeverity().toString().substring(0, 1);
                g.drawString(severityLetter, cellPixelX, cellPixelY - 2);

            } else {
                // Quadrant logic for multiple fires
                int fireIconSize = CELL_SIZE / 2; // half cell size
                List<Point> quadrantOffsets = new ArrayList<>();
                // Quadrants: top-left, top-right, bottom-left, bottom-right
                quadrantOffsets.add(new Point(0, 0));
                quadrantOffsets.add(new Point(CELL_SIZE - fireIconSize, 0));
                quadrantOffsets.add(new Point(0, CELL_SIZE - fireIconSize));
                quadrantOffsets.add(new Point(CELL_SIZE - fireIconSize, CELL_SIZE - fireIconSize));

                // Place each fire in a quadrant (reuse the 4th quadrant for 5+ fires)
                for (int i = 0; i < zoneFires.size(); i++) {
                    InputEvent fireEvent = zoneFires.get(i);
                    int quadrantIndex = Math.min(i, quadrantOffsets.size() - 1);
                    Point offset = quadrantOffsets.get(quadrantIndex);

                    int drawX = cellPixelX + offset.x;
                    int drawY = cellPixelY + offset.y;

                    // Draw each fire image at half size
                    g.drawImage(fireImage, drawX, drawY, fireIconSize, fireIconSize, this);
                    g.setColor(Color.BLACK);
                    String severityLetter = fireEvent.getSeverity().toString().substring(0,1);
                    g.drawString(severityLetter, drawX, drawY - 2);
                }
            }
        }
    }

    /**
     * Determines the color used to represent a drone on the map, based on its state name.
     *
     * @param state The string representing the drone's current state.
     * @return A {@link Color} corresponding to the given state.
     */
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

    /**
     * Loads an image from the resources folder.
     *
     * @param fileName The name of the image file to load (e.g., "fire.png", "error.png").
     * @return An {@link Image} if loaded successfully, otherwise null.
     */
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
