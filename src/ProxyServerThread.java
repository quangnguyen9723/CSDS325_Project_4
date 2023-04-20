import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class ProxyServerThread extends Thread {
    private final Socket clientSocket;
    private boolean isRedirected = false;
    private static int count = 1;

    public ProxyServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("connected, count=" + count++);

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String requestLine = clientReader.readLine();

            String[] requestTokens = requestLine.split(" ");
            String method = requestTokens[0];
            String url = requestTokens[1];

            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(method);

            BufferedReader serverReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            OutputStream clientOutput = clientSocket.getOutputStream();
            PrintWriter clientWriter = new PrintWriter(clientOutput, true);

            String responseLine = "HTTP/1.1 " + connection.getResponseCode() + " " + connection.getResponseMessage();
            clientWriter.println(responseLine);

            System.out.println(responseLine);

            String headerLine;
            boolean isConnected = true;

//            while (isConnected) {
//                headerLine = serverReader.readLine();
//                if (headerLine != null) System.out.println(headerLine);
//            }

            while ((headerLine = serverReader.readLine()) != null) {
                clientWriter.println(headerLine);
                System.out.println(headerLine);
            }

            clientWriter.println();
            String content;
            while ((content = serverReader.readLine()) != null) {
                clientWriter.println(content);
            }
            System.out.println("finished");

            clientWriter.close();
            clientOutput.close();
            clientReader.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}