package SnoopDogServer;

import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;

public class SourceThread extends Thread {
    private SourceDataLine sourceLine;
    private ByteArrayOutputStream out;
    private volatile boolean shouldRun = true;

    public SourceThread(SourceDataLine sourceLine, ByteArrayOutputStream out) {
        super("SourceThread");
        this.sourceLine = sourceLine;
        this.out = out;
    }

    public void startPlaying() {
        System.out.println("Start playing...");
        sourceLine.start();
        while (shouldRun) {
            sourceLine.write(out.toByteArray(), 0, out.size());
        }
    }

    public void stopPlaying() {
        System.out.println("Stop playing...");
        sourceLine.stop();
    }

    @Override
    public void run() {
        startPlaying();
    }

    public void kill() {
        this.shouldRun = false;
    }
}

