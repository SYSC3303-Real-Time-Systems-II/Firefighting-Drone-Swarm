import javax.swing.*;
import java.net.SocketException;

public class GUISubsystem {
    private static final int GUI_PORT = 8000; // Must match what DroneSubsystem sends to.

    public static void main(String[] args) {
        // 1) Create the view (the grid panel)
        DroneMapView mapView = new DroneMapView();

        // 2) Create a JFrame to host the view
        JFrame frame = new JFrame("Drone GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mapView);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 3) Create the controller, which listens for updates on port 8000
        try {
            DroneMapController controller = new DroneMapController(mapView, GUI_PORT);
            controller.start(); // begin listening thread
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
