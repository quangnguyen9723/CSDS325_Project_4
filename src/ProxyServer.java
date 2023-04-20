import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("Proxy server listening on port " + DEFAULT_PORT);

            while (true) {
                new ProxyServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("error creating listening socket");
            e.printStackTrace();
        }
    }
}
