import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

/**
 * The controller class that listens for both drone updates (List<DroneStatus>)
 * and zone updates (List<Zone>) on a UDP port, then tells the DroneMapView to refresh.
 */
public class DroneMapController {

    private DroneMapView view;
    private DatagramSocket socket;
    private boolean running = true;


    /**
     * Constructs a DroneMapController with the given DroneMapView and UDP port.
     *
     * @param view The {@link DroneMapView} instance that will be updated with new data.
     * @param port The UDP port on which the controller listens for incoming packets.
     * @throws SocketException If the socket could not be opened or the socket could not
     *                         bind to the specified port.
     */
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
                    else if (receivedObject instanceof Map){
                        Map<?, ?> map = (Map<?, ?>) receivedObject;
                        if (!map.isEmpty()) {
                            SwingUtilities.invokeLater(()->view.updateMetrics(map));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Stops the controller from listening for further packets and closes the socket.
     */
    public void stop() {
        running = false;
        socket.close();
    }

    /**
     * Deserializes a byte array into an Object using standard Java serialization.
     *
     * @param data The byte array containing the serialized object data.
     * @return The deserialized {@link Object}, or {@code null} if an error occurred.
     */
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
