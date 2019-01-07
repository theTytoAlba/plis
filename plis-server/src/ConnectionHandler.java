import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
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
        String responseBody = handleRequest(new JSONObject(requestBody));

        // Send the response back.
        t.sendResponseHeaders(200, responseBody.length());
        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes(), 0, responseBody.getBytes().length);
        os.close();
    }


    private String handleRequest(JSONObject request) {
        JSONObject result = new JSONObject();

        // Check if it is a protein or ligand.
        JSONObject resultObject;
        if (request.getString("queryType").equals("Protein")
                && (resultObject = DataHandler.getProtein(request.getString("query"))) != null) {
            result.put("result", resultObject);
        } else if (request.getString("queryType").equals("Ligand")
                && (resultObject = DataHandler.getLigand(request.getString("query"))) != null) {
            result.put("result", resultObject);
        } else {
            return "{}";
        }

        JSONArray resultInteractions = new JSONArray();
        // Add the interaction info.
        JSONArray interactions = DataHandler.getInteractions(request.getString("query"));
        System.out.println("Interaction count " + interactions.length());
        for (int i = 0; i < interactions.length(); i++) {
            JSONObject interactionObject = new JSONObject();
            JSONArray affinities = new JSONArray();
            // If query is protein, interactions will be ligands.
            if (request.getString("queryType").equals("Protein")) {
                // Info for interaction
                interactionObject = DataHandler.getLigand(interactions.get(i).toString());
                // Affinities
                JSONObject retreivalAffinity = new JSONObject();
                retreivalAffinity.put("Retreival",
                        DataHandler.getKibaAffinity(interactions.get(i).toString(), request.getString("query")));
                System.out.println("Retreival affinity " + retreivalAffinity.toString());
                affinities.put(retreivalAffinity);
                interactionObject.put("affinities", affinities);
            } else {
                // Info for interaction
                interactionObject = DataHandler.getProtein(interactions.get(i).toString());
                // Affinities
                JSONObject retreivalAffinity = new JSONObject();
                retreivalAffinity.put("Retreival",
                        DataHandler.getKibaAffinity(request.getString("query"), interactions.get(i).toString()));
                affinities.put(retreivalAffinity);
                System.out.println("Retreival affinity " + retreivalAffinity.toString());
                interactionObject.put("affinities", affinities);
            }
            System.out.println(interactionObject.toString());
            resultInteractions.put(interactionObject);
        }
        result.put("interactions", resultInteractions);
        return result.toString();
    }
}
