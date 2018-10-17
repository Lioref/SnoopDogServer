package SnoopDogServer;


import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;

public class TargetThread extends Thread {
    private volatile boolean shouldRun = true;


    private TargetDataLine targetLine;
    private ByteArrayOutputStream out;

    public TargetThread(TargetDataLine targetLine, ByteArrayOutputStream out) {
        super("TargetThread");
        this.targetLine = targetLine;
        this.out = out;
    }

    private void resumeListening() {
        targetLine.flush();
        targetLine.start();
    }

    public void startRecording() {
        //System.out.println("Start recording...");
        targetLine.start();
        byte[] data = new byte[targetLine.getBufferSize() / 5];
        int readBytes;
        while (shouldRun) {
            readBytes = targetLine.read(data, 0, data.length);
            out.write(data, 0, readBytes);
        }

    }

    public void stopRecording() {
        //System.out.println("Stop recording...");
        targetLine.stop();
    }

    @Override
    public void run() {
        startRecording();
    }

    public void kill() {
        this.shouldRun = false;
    }
}
