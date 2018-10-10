package SnoopDogServer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.Queue;

/* Records from microphone, analyzes sounds volume using the SoundLib methods
 * adds task to send "bark" message to the task queue when necessary */

public class SoundThread extends Thread {

    private static final int CLIPTIME = 1000; /* The length of each recording should be changed here */
    
    private volatile boolean shouldRun;
    private Queue<ServerTask> taskQueue;
    private Queue<byte[]> soundQueue;

    private ByteArrayOutputStream out;
    private SourceDataLine sourceLine;
    private TargetDataLine targetLine;
    private Server server;
    private boolean saveAudio;


    public SoundThread(Server server, ByteArrayOutputStream out, Queue<ServerTask> taskQueue, Queue<byte[]> soundQueue) {
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
        int loudnessInt;

        TargetThread targetThread;
        SourceThread sourceThread;

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
                loudnessInt = SoundLib.calculateRMSLevel(audioData); /* For debug */
                System.out.println(loudnessInt); /* For debug */

                if (SoundLib.isLoud(SoundLib.calculateRMSLevel(audioData))) {
                    /* Add a broadCast task to send message */
                    ServerTask newTask = new ServerTask("Send message", null, true);
                    taskQueue.add(newTask);
                    System.out.println("added task to send message");
                }

                if (saveAudio) { /* Indicator for saving audio after user request */
                    soundQueue.add(audioData);
                    System.out.println("Added audio");
                }

                /* If all connections are lost - empty the soundQueue (to avoid sound delays) */
                if (server.connections.isEmpty()) {
                    saveAudio = false;
                    while(!soundQueue.isEmpty()) {
                        soundQueue.remove();
                    }
                }

                /* Init sourceThread to play, once done, kill thread */
                /*
                sourceThread.start();
                sleep(CLIPTIME);
                sourceThread.stopPlaying();
                sourceLine.flush();
                sourceThread.kill();
                */

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

    public void setSaveAudio(boolean bool) {
        this.saveAudio = bool;
    }

}

