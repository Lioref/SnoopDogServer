package SnoopDogServer;

import java.io.*;
import java.net.Socket;
import java.util.Queue;

/* Listens for input from clients and parses them, adding tasks to taskQueue when necessary */

public class ServerConnectionThread extends Thread {

    private volatile boolean shouldRun = true;
    public volatile boolean isStreaming = false;


    private boolean disconnectFlag = false;
    Socket socket;
    private Server server;
    private BufferedReader inFromClient;
    DataOutputStream outToClient;


    public ServerConnectionThread(Socket socket, Server server) {
        super("ServerConnectionThread");
        this.socket = socket;
        this.server = server;
    }

    public void kill() {
        this.shouldRun = false;
    }

    @Override
    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());
            StreamerThread streamerThread = new StreamerThread(server, this);
            String textIn = "";

            while (shouldRun) {
                while (!inFromClient.ready()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                textIn = inFromClient.readLine().toLowerCase();

                // Check that it's not already in send audio mode
                if (textIn.equals("send audio")) { /* User sent a "send audio" request */
                    System.out.println("Received: " + textIn);
                    if (!this.isStreaming) {
                        this.isStreaming = true;
                        streamerThread.start();
                    }
                }
                else if (textIn.equals("stop audio")) { /* User sent a "stop audio" request */
                    System.out.println("Received: " + textIn);
                    streamerThread.kill();
                    if (this.isStreaming) {
                        this.isStreaming = false;
                        streamerThread = new StreamerThread(server, this);
                    }
                }
                else if (textIn.equals("play command")) {
                    server.commandThread.setPlayCommand(1);
                }
                else if (textIn.equals("disconnect")) { /* User sent a "disconnect" request */
                    server.lostConnections.add(this);
                    socket.close();
                    disconnectFlag = true;
                    break;
                }

                else if (textIn.equals("stop server")) { /* Lets the client stop the server */
                    server.stopServer();
                }
            if (disconnectFlag) { /* break to kill thread (one exists for each client) */
                break;
                }
            }

            /* clean up */
            outToClient.close();
            inFromClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
