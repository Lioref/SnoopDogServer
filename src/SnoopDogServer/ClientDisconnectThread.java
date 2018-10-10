package SnoopDogServer;


/* A ServerConnectionThread is added to the lostConnections queue when a SocketException happens or when a client asks to disconnect
 * The ClientDisconnectThread keeps the server's connections arraylist up to date */

public class ClientDisconnectThread extends Thread {

    Server server;

    public ClientDisconnectThread(Server server) {
        super("ClientDisconnectThread");
        this.server = server;
    }

    @Override
    public void run() {
        while (server.lostConnections.isEmpty()) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (ServerConnectionThread sc : server.lostConnections) {
            server.connections.remove(sc); /* causes no exception if object has already been removed */
        }
    }
}
