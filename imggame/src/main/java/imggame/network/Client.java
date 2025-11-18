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
		startListenerThread();
	}

	private void startListenerThread() {
		listenerThread = new Thread(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Object response = input.readObject();
					System.out.println("Received response: " + response.getClass().getSimpleName());
					if (responseHandler != null) {
						responseHandler.handleResponse(response);
					}
				}
			} catch (Exception e) {
				if (!Thread.currentThread().isInterrupted()) {
					System.err.println("Listener thread error: " + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		listenerThread.setDaemon(true);
		listenerThread.start();
	}

	public synchronized void send(Object packet) {
		try {
			output.writeObject(packet);
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setResponseHandler(ResponseHandler handler) {
		this.responseHandler = handler;
	}

	public void close() throws Exception {
		System.out.println("Closing client connection...");
		if (listenerThread != null && listenerThread.isAlive()) {
			listenerThread.interrupt();
			try {
				listenerThread.join(1000); // Wait max 1 second for thread to finish
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			listenerThread = null;
		}
		if (input != null) {
			try {
				input.close();
			} catch (Exception e) {
				// Ignore close errors
			}
		}
		if (output != null) {
			try {
				output.close();
			} catch (Exception e) {
				// Ignore close errors
			}
		}
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (Exception e) {
				// Ignore close errors
			}
		}
		System.out.println("Client connection closed.");
	}

}
