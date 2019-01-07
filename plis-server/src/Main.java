import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        // Import core dataset from kiba files.
        DataHandler.prepareCoreDataset();
        // Fetch or import details of kiba chemicals.
        DataHandler.prepareCoreDatasetDetails();
        // Prepare extraction dataset.
        DataHandler.prepareExtractionDataset();
        // Create and start server.
        HttpServer server = HttpServer.create(new InetSocketAddress(60015), 0);
        server.createContext("/", new ConnectionHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}