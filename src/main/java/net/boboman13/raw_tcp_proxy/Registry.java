package net.boboman13.raw_tcp_proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author boboman13
 */
public class Registry implements Runnable {

    private Proxy proxy;
    private boolean isRunning = true;

    private Socket outsocket;
    private Socket insocket;

    public InputStream serverIn;
    public OutputStream serverOut;

    public InputStream clientIn;
    public OutputStream clientOut;

    private SocketListener socketListener;

    /**
     * Creates a Registry instance; Registry represents a client.
     *
     * @param outSocket The socket to the server.
     * @param inSocket The socket to the client.
     * @throws IOException Upon getting an exception, the program will throw an exception.
     */
    public Registry(Proxy proxy, Socket outSocket, Socket inSocket) throws IOException {
        this.proxy = proxy;

        this.outsocket = outSocket;
        this.insocket = inSocket;

        // Initiate the in and out fields.
        this.clientIn = this.insocket.getInputStream();
        this.clientOut = this.insocket.getOutputStream();

        this.serverIn = this.outsocket.getInputStream();
        this.serverOut = this.outsocket.getOutputStream();

        // Start up the SocketListener.
        this.socketListener = new SocketListener(this);
        Thread thread = new Thread(this.socketListener);
        thread.start();
    }

    /**
     * Ran as a separate thread.
     */
    public void run() {
        try {
            String line = null;

            final byte[] request = new byte[1024];

            int bytesRead;

            // Try and read lines from the client.
            while (isRunning && (bytesRead = this.clientIn.read(request)) != -1) {
                this.serverOut.write(request, 0, bytesRead);
                this.serverOut.flush();

                String data = new String(request, "UTF-8");
                proxy.debug("C -> S: "+data);
            }

            // Client disconnected.
            this.kill();

        } catch (IOException ex) {
            if(ex instanceof SocketTimeoutException) {
                // The socket simply timed out. Kill, then exit.
                this.kill();
                return;
            }

            //ex.printStackTrace();
            this.kill();
        }
    }

    /**
     * Gets the Proxy of the Registry.
     *
     * @return proxy
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * Kills the Registry, this happens when either the client or server disconnects.
     */
    public void kill() {
        this.socketListener.kill();
        isRunning = false;

        try {
            if(this.outsocket != null) this.outsocket.close();
            if(this.insocket != null) this.insocket.close();
        } catch (IOException ex) {
            // Do nothing.
        }

        proxy.debug("Client "+insocket.getInetAddress().getHostAddress()+" has disconnected.");
    }

}