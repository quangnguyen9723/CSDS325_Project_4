import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyServerThread extends Thread {
    /**
     * things to do
     * 1. receive GET request from wget
     * 2. forward GET request to server
     * 3. forward the response back to wget
     */

    private final static String CRLF = "\r\n";
    private static final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();
    private final Socket clientSocket;

    public ProxyServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Read client request
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Receive output
            byte[] response = handleRequest(fromClient);

            // Send the cached response back to the client
            OutputStream clientOut = clientSocket.getOutputStream();
            clientOut.write(response);
            clientOut.flush();

            // Close the client connection
            clientSocket.close();
            fromClient.close();
            clientOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] handleRequest(BufferedReader fromClient) throws IOException {
        // Parse the request and get the URL
        String requestLine = fromClient.readLine();
        String[] requestParts = requestLine.split(" ");
        String method = requestParts[0];
        String url = requestParts[1];

        // Check if the response is already cached
        byte[] response = cache.get(url);

        if (response != null) {
            return response;
        }

        // Send the request to the destination server using HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);

        // Read headers from client and set them to the HttpURLConnection
        String line;
        while (!(line = fromClient.readLine()).isEmpty()) {
            int index = line.indexOf(":");
            if (index == -1) continue;

            String key = line.substring(0, index);
            String value = line.substring(index + 1).trim();
            connection.setRequestProperty(key, value);
        }

        response = getResponse(url, connection);

        // Close HttpURLConnection
        connection.disconnect();

        return response;
    }

    private static byte[] getResponse(String url, HttpURLConnection connection) throws IOException {
        byte[] response;
        // Get the server's response
        InputStream serverIn = connection.getInputStream();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

        // Write the status line and headers to responseStream
        String statusLine = "HTTP/1.1 " + connection.getResponseCode() + " " + connection.getResponseMessage() + CRLF;
        responseStream.write(statusLine.getBytes());

        for (String key : connection.getHeaderFields().keySet()) {
            if (key == null) continue;

            String headerLine = key + ": " + connection.getHeaderField(key) + CRLF;
            responseStream.write(headerLine.getBytes());
        }

        responseStream.write(CRLF.getBytes());

        // Read the response body into responseStream
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = serverIn.read(buffer)) != -1) {
            responseStream.write(buffer, 0, bytesRead);
        }

        // Cache the response
        response = responseStream.toByteArray();
        cache.put(url, response);
        return response;
    }
}