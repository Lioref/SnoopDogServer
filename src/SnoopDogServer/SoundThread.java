package SnoopDogServer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.Queue;

/* Records from microphone, analyzes sounds volume using the SoundLib methods
 * adds task to send "bark" message to the task queue when necessary */

public class SoundThread extends Thread {

    private static final int CLIPTIME = 1000; /* The length of each recording should be changed here */
    
    private volatile boolean shouldRun;
    private boolean saveAudio;

    private Queue<ServerTask> taskQueue;
    private Queue<byte[]> soundQueue;

    private ByteArrayOutputStream out;
    private SourceDataLine sourceLine;
    private TargetDataLine targetLine;
    private Server server;


    public SoundThread(Server server, ByteArrayOutputStream out, Queue<ServerTask> taskQueue) {
        super("SoundThread");
        try {
            /* Init targetDataLine - a DataLine from which audio data can be read*/
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);

            /* Init source data line - a data line to which data may be written */
            /*
            info = new DataLine.Info(SourceDataLine.class, format);
            final SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            this.sourceLine = sourceLine;
            */

            this.targetLine = targetLine;
            this.taskQueue = taskQueue;
            this.soundQueue = soundQueue;
            this.server = server;



        } catch(Exception e) {
            e.printStackTrace();
        }
        this.out = out;
    }

    @Override
    public void run() {
        shouldRun = true;
        saveAudio = false;

        TargetThread targetThread;

        try {
            targetLine.open();
            /*
            sourceLine.open();
            */

            while (shouldRun) {

                /* Init targetThread to record, once done, kill thread */
                targetThread = new TargetThread(targetLine, out);
                /*
                sourceThread = new SourceThread(sourceLine, out);
                */
                targetThread.start();
                Thread.sleep(CLIPTIME);
                targetThread.stopRecording();

                /* Calculate loudness */
                byte[] audioData = out.toByteArray();
                if (SoundLib.isLoud(SoundLib.calculateRMSLevel(audioData))) {
                    /* Add a broadCast task to send message */
                    ServerTask newTask = new ServerTask("Send message", null, true);
                    taskQueue.add(newTask);
                    System.out.println("added task to send message");
                }

                /* Clean out */
                out.flush();
                targetLine.flush();
                out.reset();

                targetThread.kill();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        this.shouldRun = false;
        this.targetLine.close();
        /*
        this.sourceLine.close();
        */
    }

}

