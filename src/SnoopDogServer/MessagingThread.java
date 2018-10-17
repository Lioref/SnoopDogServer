package SnoopDogServer;

import java.io.IOException;
import java.net.SocketException;
import java.util.Queue;

/* Sends the bark indicator to all users if the task appears in the taskQueue */

public class MessagingThread extends Thread {

    static final int BARK = 0;
    static int SLEEPTIME = 1;

    private volatile boolean shouldRun = true;
    private Queue<ServerTask> messagingTaskQueue;
    private Server server;


    public MessagingThread(Queue<ServerTask> messagingTaskQueue, Server server) {
        super("MessagingThread");
        this.messagingTaskQueue = messagingTaskQueue;
        this.server = server;
    }

    public void sendBarkToClient(ServerConnectionThread sc) {
        try {
            synchronized (sc.outToClient) {
                sc.outToClient.writeInt(BARK); /* Sending int indicator for barking */
            }
        } catch (SocketException e) {
            server.lostConnections.add(sc); /* client disconnected */
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendBarkToAllClients() {
        for (ServerConnectionThread sc : server.connections) {
            if (!sc.isStreaming) {
                sendBarkToClient(sc);
            }
        }

    }

    @Override
    public void run() {
        while(shouldRun) {
            while (messagingTaskQueue.isEmpty()) {
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            messagingTaskQueue.remove();
            sendBarkToAllClients();
        }
    }
}

