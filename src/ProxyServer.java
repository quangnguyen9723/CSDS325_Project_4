import java.io.*;
import java.net.*;

public class ProxyServer {

    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            new ProxyServerThread(serverSocket.accept()).start();
        }
    }



    public static void main(String[] args) throws IOException {
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.start(8080);
    }
}
