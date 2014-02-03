package net.boboman13.raw_tcp_proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author boboman13
 */
public class ProxyServer {

    private Proxy proxy;
    private ServerSocket server;

    /**
     * Creates the ProxyServer.
     *
     * @param proxy
     */
    public ProxyServer(Proxy proxy) {
        this.proxy = proxy;

        try {
            server = new ServerSocket(proxy.getListeningPort());

            // Keep listening for new clients!
            while(true) {
                Socket client = server.accept();

                this.proxy.debug("Accepted new client: "+client.getInetAddress().getHostAddress());

                Registry registry = new Registry(this.proxy, new Socket(this.proxy.getHost(), this.proxy.getPort()), client);

                // Start the thread.
                Thread thread = new Thread(registry);
                thread.start();
            }
        } catch (IOException ex) {
            // Bad boy, we got an error.
            ex.printStackTrace();
            return;
        }

    }

}