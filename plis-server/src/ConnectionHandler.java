import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class ConnectionHandler implements HttpHandler {

    public void handle(HttpExchange t) throws IOException {
        // Get the request body.
        BufferedReader in = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        String requestBody = in.readLine();
        System.out.println("Received request: " + requestBody);

        // Headers needed to bypass CORS.
        Headers h = t.getResponseHeaders();
        h.add("Content-Type", "text");
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Credentials", "true");
        h.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        h.add("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");

        // Prepare response.
        String responseBody = handleRequest(requestBody);

        // Send the response back.
        t.sendResponseHeaders(200, responseBody.length());
        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes(), 0, responseBody.getBytes().length);
        os.close();
    }


    private String handleRequest(String request) {
        // TODO: actually handle request.
        return "{\"heard\":\"you\"}";
    }
}