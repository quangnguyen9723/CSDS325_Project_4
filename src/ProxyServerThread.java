import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyServerThread extends Thread {
    /**
     * things to do
     * 1. receive GET request from wget
     * 2. handle GET request until getting to the resource or 4xx response
     * 3. forward the response back to wget
     */
    private final Socket clientSocket;
    private static final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();


    public ProxyServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Read client request
            InputStream clientIn = clientSocket.getInputStream();
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientIn));
            String requestLine = clientReader.readLine();

            // Parse the request and get the URL
            String[] requestParts = requestLine.split(" ");
            String url = requestParts[1];

            // Check if the response is already cached
            byte[] response = cache.get(url);

            if (response == null) {
                // Send the request to the destination server using HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod(requestParts[0]);

                // Read headers from client and set them to the HttpURLConnection
                String line;
                while (!(line = clientReader.readLine()).isEmpty()) {
                    int index = line.indexOf(":");
                    if (index != -1) {
                        connection.setRequestProperty(line.substring(0, index), line.substring(index + 1).trim());
                    }
                }

                // Get the server's response
                InputStream serverIn = connection.getInputStream();
                ByteArrayOutputStream serverResponse = new ByteArrayOutputStream();

                // Write the status line and headers to serverResponse
                String statusLine = "HTTP/1.1 " + connection.getResponseCode() + " " + connection.getResponseMessage() + "\r\n";
                serverResponse.write(statusLine.getBytes());

                for (String key : connection.getHeaderFields().keySet()) {
                    if (key != null) {
                        String headerLine = key + ": " + connection.getHeaderField(key) + "\r\n";
                        serverResponse.write(headerLine.getBytes());
                    }
                }
                serverResponse.write("\r\n".getBytes());

                // Read the response body into serverResponse
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = serverIn.read(buffer)) != -1) {
                    serverResponse.write(buffer, 0, bytesRead);
                }

                // Cache the response
                response = serverResponse.toByteArray();
                cache.put(url, response);

                // Close HttpURLConnection
                connection.disconnect();
            }

            // Send the cached response back to the client
            OutputStream clientOut = clientSocket.getOutputStream();
            clientOut.write(response);
            clientOut.flush();

            // Close the client connection
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}