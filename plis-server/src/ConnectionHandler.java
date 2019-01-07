import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

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
        String responseBody;
        try {
            responseBody = handleRequest(new JSONObject(requestBody));
        } catch (Exception e) {
            e.printStackTrace();
            responseBody = "{}";
        }

        // Send the response back.
        t.sendResponseHeaders(200, responseBody.length());
        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes(), 0, responseBody.getBytes().length);
        os.close();
    }


    private String handleRequest(JSONObject request) {
        JSONObject result = new JSONObject();

        // Update query to id of element
        request.put("query", DataHandler.getIdFromAlternativeName(request.getString("query")));

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
        JSONArray interactions = DataHandler.getAllInteractions(request.getString("query"));
        for (int i = 0; i < interactions.length(); i++) {
            JSONObject interactionObject;
            JSONArray affinities = new JSONArray();
            // If query is protein, interactions will be ligands.
            if (request.getString("queryType").equals("Protein")) {
                // Info for interaction
                interactionObject = DataHandler.getLigand(interactions.get(i).toString());

                // Affinities (prediction affinity is added automatically since we have no dataset)
                // Retreival affinity
                String retreival;
                if ((retreival = DataHandler.getKibaAffinity(interactions.get(i).toString(), request.getString("query"))) != null) {
                    JSONObject retreivalAffinity = new JSONObject();
                    retreivalAffinity.put("Retreival", retreival);
                    affinities.put(retreivalAffinity);
                }
                // Extraction affinity
                JSONObject extraction;
                if ((extraction = DataHandler.getExtractionAffinity(request.getString("query"), interactions.get(i).toString())) != null) {
                    JSONObject extractionAffinity = new JSONObject();
                    extractionAffinity.put("Extraction", extraction);
                    affinities.put(extractionAffinity);
                }
                // Auto prediction
                JSONObject predictionAffinity = new JSONObject();
                predictionAffinity.put("Prediction", String.valueOf(Math.random()*10 + 10));
                affinities.put(predictionAffinity);

                interactionObject.put("affinities", affinities);
            } else {
                // Info for interaction
                interactionObject = DataHandler.getProtein(interactions.get(i).toString());
                // Affinities
                // Retreival affinity
                String retreival;
                if ((retreival = DataHandler.getKibaAffinity(request.getString("query"), interactions.get(i).toString())) != null) {
                    JSONObject retreivalAffinity = new JSONObject();
                    retreivalAffinity.put("Retreival", retreival);
                    affinities.put(retreivalAffinity);
                }
                // Extraction affinity
                JSONObject extraction;
                if ((extraction = DataHandler.getExtractionAffinity(request.getString("query"), interactions.get(i).toString())) != null) {
                    JSONObject extractionAffinity = new JSONObject();
                    extractionAffinity.put("Extraction", extraction);
                    affinities.put(extractionAffinity);
                }
                // Auto prediction
                JSONObject predictionAffinity = new JSONObject();
                predictionAffinity.put("Prediction", String.valueOf(Math.random()*10 + 10));
                affinities.put(predictionAffinity);

                interactionObject.put("affinities", affinities);
            }
            resultInteractions.put(interactionObject);
        }
        result.put("interactions", resultInteractions);
        return result.toString();
    }
}
