package imggame.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameClient implements AutoCloseable {
  private final Socket socket;
  private final ObjectOutputStream output;
  private final ObjectInputStream input;

  public GameClient(String host, int port) throws IOException {
    socket = new Socket(host, port);
    output = new ObjectOutputStream(socket.getOutputStream());
    input = new ObjectInputStream(socket.getInputStream());
  }

    public synchronized void sendPacket(Object packet) throws IOException {
        try {
            System.out.println("sending: " + packet.getClass().getName());
            output.writeObject(packet);
            output.flush();
            System.out.println("packet sent OK");
        } catch (Exception e) {
            System.out.println("Error while sending packet: " + e.getClass() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


  public synchronized Object receivePacket() throws IOException, ClassNotFoundException {
    return input.readObject();
  }

  @Override
  public void close() {
    try {
      input.close();
    } catch (IOException ignored) {
    }
    try {
      output.close();
    } catch (IOException ignored) {
    }
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }
}
