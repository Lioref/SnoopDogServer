package SnoopDogServer;

import java.io.*;
import java.net.Socket;
import java.util.Queue;

/* Listens for input from clients and parses them, adding tasks to taskQueue when necessary */

public class ServerConnectionThread extends Thread {

    private volatile boolean shouldRun = true;


    boolean disconnectFlag = false;
    Socket socket;
    private Server server;
    private BufferedReader inFromClient;
    DataOutputStream outToClient;
    private Queue<ServerTask> taskQueue;
    private Queue<byte[]> soundQueue;


    public ServerConnectionThread(Socket socket, Server server, Queue<ServerTask> taskQueue, Queue<byte[]> soundQueue) {
        super("ServerConnectionThread");
        this.socket = socket;
        this.server = server;
        this.taskQueue = taskQueue;
        this.soundQueue = soundQueue;
    }

    public void kill() {
        this.shouldRun = false;
    }

    @Override
    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());
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

                if (textIn.equals("send audio")) { /* User sent a "send audio" request */
                    System.out.println("Received: " + textIn);
                    server.soundThread.setSaveAudio(true);

                    /*
                    AudioStreamerThread audioStreamerThread = new AudioStreamerThread(this);
                    audioStreamerThread.start();
                    ServerTask newTask = new ServerTask("Send audio", this, false);
                    taskQueue.add(newTask);
                    */
                }
                else if (textIn.equals("stop audio")) { /* User sent a "stop audio" request */
                    System.out.println("Received: " + textIn);


                    /*
                    ServerTask newTask = new ServerTask("Stop audio", this, false);
                    taskQueue.add(newTask);
                    */
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
