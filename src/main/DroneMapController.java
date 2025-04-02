import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;

/**
 * The controller class that listens for both drone updates (List<DroneStatus>)
 * and zone updates (List<Zone>) on a UDP port, then tells the DroneMapView to refresh.
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
            System.out.println("[DroneMapController] Listening on port " + socket.getLocalPort());
            while (running) {
                try {
                    byte[] buffer = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // blocks until a packet is received

                    // Deserialize the object
                    Object receivedObject = deserialize(packet.getData());

                    // Update the UI based on the type of the received object
                    if (receivedObject instanceof List) {
                        List<?> list = (List<?>) receivedObject;
                        if (!list.isEmpty()) {
                            if (list.get(0) instanceof DroneStatus) {
                                SwingUtilities.invokeLater(() -> view.updateDisplay((List<DroneStatus>) list));
                            } else if (list.get(0) instanceof Zone) {
                                SwingUtilities.invokeLater(() -> view.setZones((List<Zone>) list));
                            }
                        }
                    }
                    else if (receivedObject instanceof InputEvent){
                        SwingUtilities.invokeLater(()-> view.displayEvent((InputEvent) receivedObject));
                    }
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

    private Object deserialize(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
