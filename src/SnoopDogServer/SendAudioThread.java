package SnoopDogServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Queue;

/* If audio appears in the soundQueue, sends it to (single) client
 * This thread is created for every connected client */

public class SendAudioThread extends Thread {

    static final int AUDIO = 1;

    private volatile boolean shouldRun;
    private Server server;
    private Queue<byte[]> soundQueue;
    private ServerConnectionThread sc;

    public SendAudioThread(ServerConnectionThread sc, Server server, Queue<byte[]> soundQueue) {
        super("SendAudioThread");
        this.server = server;
        this.soundQueue = soundQueue;
        this.sc = sc;

    }

    public void sendAudioToClient() {
        System.out.println("Sending audio to client");

        DataOutputStream outToClient;
        try {
            outToClient = new DataOutputStream(sc.socket.getOutputStream());
            while(!soundQueue.isEmpty()) {
                byte[] audio = soundQueue.remove();
                System.out.println("Sound being sent");
                outToClient.writeInt(AUDIO); /* AUDIO is an indicator to the client that audio is being sent */
                outToClient.writeInt(audio.length);
                outToClient.write(audio, 0, audio.length);
            }
        } catch (SocketException e) {
            /* If client has disconnected - kill thread */
            server.lostConnections.add(sc);
            this.shouldRun = false;
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        shouldRun = true;

        try {
            while(shouldRun) {
                while (soundQueue.isEmpty()) {
                    sleep(1);
                }
                if (!soundQueue.isEmpty()) {
                    sendAudioToClient();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        this.shouldRun = false;
    }
}
