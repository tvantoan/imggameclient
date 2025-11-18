package imggame.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
	private final Socket socket;
	private final ObjectOutputStream output;
	private final ObjectInputStream input;
	private ResponseHandler responseHandler;
	private Thread listenerThread;

	public Client(String host, int port) throws Exception {
		socket = new Socket(host, port);
		output = new ObjectOutputStream(socket.getOutputStream());
		output.flush();
		input = new ObjectInputStream(socket.getInputStream());
	}

	public synchronized void send(Object packet) throws Exception {
		output.writeObject(packet);
		output.flush();
	}

	public void setResponseHandler(ResponseHandler handler) {
		this.responseHandler = handler;
		if (listenerThread == null) {
			listenerThread = new Thread(() -> {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						Object response = input.readObject();
						if (responseHandler != null) {
							responseHandler.handleResponse(response);
						}
					}
				} catch (Exception e) {
					// Handle exception or log it
				}
			});
			listenerThread.start();
		}
	}

}
