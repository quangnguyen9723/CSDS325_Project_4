import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class ProxyServerThread extends Thread {
    /**
     * things to do
     * 1. receive GET request from wget
     * 2. handle GET request until getting to the resource or 4xx response
     * 3. forward the response back to wget
     */
    private final Socket clientSocket;

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
            System.out.println(requestLine); //---------------------

            // Parse the request and get the URL
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String url = requestParts[1];

            // Send the request to the destination server using HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);

            // Read headers from client and set them to the HttpURLConnection
            String line;
            while (!(line = clientReader.readLine()).isEmpty()) {
                System.out.println(line); //---------------------
                int index = line.indexOf(":");
                if (index != -1) {
                    connection.setRequestProperty(line.substring(0, index), line.substring(index + 1).trim());
                }
            }

            // Get the server's response
            InputStream serverIn = connection.getInputStream();

            // Write the status line and headers back to the client
            OutputStream clientOut = clientSocket.getOutputStream();
            PrintWriter clientWriter = new PrintWriter(new OutputStreamWriter(clientOut));
            clientWriter.println("HTTP/1.1 " + connection.getResponseCode() + " " + connection.getResponseMessage());
            System.out.println();
            System.out.println("HTTP/1.1 " + connection.getResponseCode() + " " + connection.getResponseMessage()); //---------------------
            for (String key : connection.getHeaderFields().keySet()) {
                if (key != null) {
                    System.out.println(key + ": " + connection.getHeaderField(key)); //---------------------
                    clientWriter.println(key + ": " + connection.getHeaderField(key));
                }
            }
            clientWriter.println();
            clientWriter.flush();

            // Send the server's response back to the client
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = serverIn.read(buffer)) != -1) {
                System.out.println(new String(buffer)); //---------------------
                clientOut.write(buffer, 0, bytesRead);
            }
            clientOut.flush();

            // Close all connections
            connection.disconnect();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}