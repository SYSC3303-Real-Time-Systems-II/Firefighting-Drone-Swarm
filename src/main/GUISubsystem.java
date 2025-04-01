import javax.swing.*;
import java.awt.*;
import java.net.SocketException;

public class GUISubsystem {
    private static final int GUI_PORT = 8000; // Must match what DroneSubsystem sends to.
    private static JPanel createLegendPanel() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createTitledBorder("Legend"));
        legend.setPreferredSize(new Dimension(150, 150)); // Adjust size if needed

        // Adjust the vertical spacing between legend entries
        legend.add(Box.createVerticalStrut(5)); // Small vertical space at the top

        legend.add(createLegendEntry(Color.BLUE, "Available"));
        legend.add(createLegendEntry(Color.ORANGE, "Ascending"));
        legend.add(createLegendEntry(Color.CYAN, "Cruising"));
        legend.add(createLegendEntry(Color.GREEN, "Dropping Agent"));
        legend.add(createLegendEntry(Color.MAGENTA, "Returning to Base"));
        legend.add(createLegendEntry(Color.BLACK, "Other States"));

        return legend;
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
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        entry.add(colorLabel);
        entry.add(Box.createHorizontalStrut(5)); // Small space between color box and text
        entry.add(textLabel);

        return entry;
    }

    public static void main(String[] args) {
        // Create the view (the grid panel)
        DroneMapView mapView = new DroneMapView();

        // Create a JFrame to host the view
        JFrame frame = new JFrame("Drone GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane scrollPane = new JScrollPane(mapView);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel legendPanel = createLegendPanel();
        frame.add(legendPanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(800, 500);
        frame.setVisible(true);

        // Create the controller, which listens for updates on port 8000
        try {
            DroneMapController controller = new DroneMapController(mapView, GUI_PORT);
            controller.start(); // begin listening thread
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


}
