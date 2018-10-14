package SnoopDogServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//TODO: Rewrite sendAudioThread

public class Server {

    private volatile boolean shouldRun = true;
    private static final int PORTNUM = 3333; /* change here if a change of port number is necessary */


    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected SoundThread soundThread;
    protected PlayCommandThread commandThread;
    protected ClientDisconnectThread clientDisconnectThread;
    protected SendAudioThread sendAudioThread;

    /* Data-Structures */
    protected ArrayList<ServerConnectionThread> connections = new ArrayList<>();
    protected Queue<ServerConnectionThread> lostConnections;
    protected Queue<ServerTask> messagingTaskQueue;
    protected Queue<byte[]> soundQueue;



    public static void main(String[] args) {
        new Server();
    }

    public synchronized void stopServer() {
        this.shouldRun = false;
    }


    public Server() {

        /* Init data structures */
        this.messagingTaskQueue = new ConcurrentLinkedQueue<>();
        this.soundQueue = new ConcurrentLinkedQueue<>();
        this.lostConnections = new ConcurrentLinkedQueue<>();


        try {

            /* Start messaging thread to send bark messages */
            MessagingThread messagingThread = new MessagingThread(messagingTaskQueue, this);
            messagingThread.start();

            /* Start listen for barking thread */
            this.soundThread = new SoundThread(this, out, messagingTaskQueue, soundQueue);
            soundThread.start();

            /* Start the client disconnect thread */
            this.clientDisconnectThread = new ClientDisconnectThread(this);
            clientDisconnectThread.start();

            /* Start the play command thread */
            this.commandThread = new PlayCommandThread(this);
            commandThread.start();

            /* Accept client connections */
            ServerSocket serverSocket = new ServerSocket(PORTNUM);
            while (shouldRun) {
                /* accept connections - Blocking */
                Socket clientSocket = serverSocket.accept(); /* This line is blocking by design */
                ServerConnectionThread sc = new ServerConnectionThread(clientSocket, this, messagingTaskQueue, soundQueue);
                sc.start();
                connections.add(sc);

                /* Start send audio thread for the accepted connection */
                sendAudioThread = new SendAudioThread(sc, this , soundQueue);
                sendAudioThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
