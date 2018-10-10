package SnoopDogServer;

public class SoundLib {

    static final int SOUNDLOWERBOUND = 25;

    public static int calculateRMSLevel(byte[] audioData)
    {
        long lSum = 0;
        for(int i=0; i < audioData.length; i++)
            lSum = lSum + audioData[i];

        double dAvg = lSum / audioData.length;
        double sumMeanSquare = 0d;

        for(int j=0; j < audioData.length; j++)
            sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / audioData.length;

        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
    }

    public static boolean isLoud(int RMSLevel) {
        if (RMSLevel > SOUNDLOWERBOUND) {
            return true;
        }
        return false;
    }

}


