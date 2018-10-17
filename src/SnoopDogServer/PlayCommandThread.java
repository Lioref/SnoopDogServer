package SnoopDogServer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class PlayCommandThread extends Thread {

    private volatile static int commandID; /* Will indicate which command to play: 0 means play nothing */
    private volatile boolean shouldRun = true;
    Server server;

    public PlayCommandThread(Server server) {
        super("PlayCommandThread");
        this.server = server;
    }

    public void setPlayCommand(int commandNum) {
        commandID = commandNum;
    }

    private void playSoundResource(String name) {
        try {
            URL resourceURL = ResourceLoader.load("Audio/" + name);
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(resourceURL);
            Clip clip = AudioSystem.getClip();
            clip.open(inputStream);
            clip.start();
            Thread.sleep(clip.getMicrosecondLength()/1000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (shouldRun) {
            while (commandID == 0) { /* Audio should not be played */
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            switch (commandID) {
                case 1:
                    System.out.println("Playing command");
                    playSoundResource("Rec1.wav");
                    commandID = 0;
                    break;
                case 2:
                    /* Insert code for other recording */
                    commandID = 0;
                    break;
            }
        }

    }

    public void kill() {
        this.shouldRun = false;
    }
}
