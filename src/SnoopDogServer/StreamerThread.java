package SnoopDogServer;

import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;

public class StreamerThread extends Thread {

    public volatile boolean shouldStream = true;
    private ServerConnectionThread sc;
    private DataOutputStream outputStream;
    private static final int AUDIO = 1;
    private Server server;

    private TargetDataLine line;

    public StreamerThread(Server server, ServerConnectionThread sc) {
        super("StreamerThread");
        this.sc = sc;
        this.outputStream = sc.outToClient;
        this.server = server;

        try {
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            this.line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    public void kill() {
        this.shouldStream = false;
    }


    @Override
    public void run() {
        try {
            int numBytesRead;
            byte[] data = new byte[line.getBufferSize() / 10];
            line.start();
            while (shouldStream) {
                synchronized (outputStream) {
                    outputStream.writeInt(AUDIO);
                    numBytesRead = line.read(data, 0, data.length);
                    outputStream.writeInt(numBytesRead);
                    outputStream.write(data, 0, numBytesRead);
                }
            }
        } catch (SocketException e) {
            server.lostConnections.add(this.sc);
            this.shouldStream = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

