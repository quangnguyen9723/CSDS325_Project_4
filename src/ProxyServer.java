import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            port = DEFAULT_PORT;
        } catch (NumberFormatException e) {
            System.out.println("Please give port number as integer.");
            System.exit(-1);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Proxy server listening on port " + port);

            while (true) {
                new ProxyServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Error creating listening socket: " + e);
        }
    }
}
