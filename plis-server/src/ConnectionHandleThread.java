import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ConnectionHandleThread extends Thread {
    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    public ConnectionHandleThread(Socket socket) {
        // Get the socket
        this.socket = socket;
    }

    @Override
    public void run() {
        // Initialize streams.
        initStreams();

        // Get JSONObject.
        String line = "";
        String newLine;
        try {
            // Skip the metadata. The last line contains the needed data.
            while ((newLine = in.readLine()) != null) {
                line = newLine;
            }
        } catch (IOException e1) {
            System.out.println("Failed to read the line.");
            e1.printStackTrace();
        }
        System.out.println("Received message: " + line);
        try {
            JSONObject obj = new JSONObject(line);
            handleConnection(obj);
        } catch (JSONException e) {
            System.out.println("Could not parse JSON. Leaving");
        }
        // Finalize.
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiates the in and out streams.
     */
    private void initStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("ConnectionHandleThread: Failed to create input stream. Leaving.");
            e.printStackTrace();
            this.stop();
        }
        try {
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("ConnectionHandleThread: Failed to create output stream. Leaving.");
            e.printStackTrace();
            this.stop();
        }
    }

    private void handleConnection(JSONObject obj) {
        // TODO: Handle connection.
        System.out.println("Handling");
    }
}
