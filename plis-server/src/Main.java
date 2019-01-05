import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        new Thread(() -> {
            // Initialize the server socket.
            initServer();
            // Accept connections
            while (true) {
                acceptConnections();
            }
        }).start();
    }

    /**
     * Initializes the server socket.
     */
    private static void initServer() {
        try {
            serverSocket  = new ServerSocket(60015);
        } catch (IOException e) {
            System.out.println("Failed to create connection.");
        }
    }

    /**
     * Accepts a new connection from serverSocket.
     * Starts a new ConnectionHandleThread to handle the request.
     */
    private static void acceptConnections() {
        try {
            Socket socket = serverSocket.accept();
            new ConnectionHandleThread(socket).start();
        } catch (IOException e) {
            System.out.println("Could not accept connection request to serverSocket.");
            e.printStackTrace();
        }
    }
}
