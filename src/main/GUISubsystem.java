import javax.swing.*;
import java.awt.*;
import java.net.SocketException;

public class GUISubsystem {
    private static final int GUI_PORT = 8000; // Must match what DroneSubsystem sends to.


    public static void main(String[] args) {
        // Create the view (the grid panel)
        DroneMapView mapView = new DroneMapView();

        // Create a JFrame to host the view
        JFrame frame = new JFrame("Firefighting Drone Swarm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane scrollPane = new JScrollPane(mapView);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);
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
