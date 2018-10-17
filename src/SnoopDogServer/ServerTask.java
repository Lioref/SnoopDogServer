package SnoopDogServer;


public class ServerTask {
    public String task;
    public ServerConnectionThread sc;
    public boolean broadCast;


    public ServerTask(String task, ServerConnectionThread sc, boolean broadCast) {
        this.task = task;
        this.sc = sc;
        this.broadCast = broadCast;
    }

}
