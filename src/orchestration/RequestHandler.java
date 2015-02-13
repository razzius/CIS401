package orchestration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//
//
// Request Handler
//
// Stores Individual Tenant information
public class RequestHandler {
    
    public RequestHandler() throws IOException {
       startServer();
    }
    
    private void startServer() throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/requests", new RequestServer());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server running on port " + port);
    }

    class RequestServer implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {
            String response = "hello world";
            System.out.println("RECIEVED: " + t.getRequestMethod());
            processRequest(t);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        public void processRequest(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            String[] params = out.toString().split("&");
            System.out.println("Recieved Parameters:");
            for (String param : params) {
                System.out.println(param);
            }
            //System.out.println(out.toString());   //Prints the string content read from input stream
            reader.close();

        }


    }
}