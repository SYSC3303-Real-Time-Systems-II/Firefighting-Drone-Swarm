import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;

/**
 * The controller class that listens for drone updates on a UDP port
 * and tells the view (DroneMapView) to refresh.
 */
public class DroneMapController {

    private DroneMapView view;
    private DatagramSocket socket;
    private boolean running = true;

    public DroneMapController(DroneMapView view, int port) throws SocketException {
        this.view = view;
        this.socket = new DatagramSocket(port);
    }

    /**
     * Starts a new thread that continuously listens for incoming packets
     * and updates the view.
     */
    public void start() {
        new Thread(() -> {
            System.out.println("[DroneMapController] Listening for drone data on port " + socket.getLocalPort());
            while (running) {
                try {
                    byte[] buffer = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // blocks until a packet is received

                    // Deserialize the list of DroneStatus
                    List<DroneStatus> statuses = deserializeDroneStatuses(packet.getData());

                    // Update the UI on the EDT
                    SwingUtilities.invokeLater(() -> view.updateDisplay(statuses));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        socket.close();
    }

    @SuppressWarnings("unchecked")
    private List<DroneStatus> deserializeDroneStatuses(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (List<DroneStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
