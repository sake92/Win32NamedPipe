package ba.sake.win32.namedpipe.duplex;

import java.io.IOException;
import java.util.concurrent.Executors;
import ba.sake.win32.namedpipe.Win32NamedPipeClientSocket;

public class DuplexClient {

    private final String pipeName;
    private final int sendMessages;

    public Sender sender;
    public Receiver receiver;

    public DuplexClient(String pipeName, int sendMessages) {
        this.pipeName = pipeName;
        this.sendMessages = sendMessages;
    }

    public void startAndAwait() {
        var pool = Executors.newFixedThreadPool(2);

        try (var socket = new Win32NamedPipeClientSocket(pipeName)) {
            sender = new Sender("client", socket, sendMessages);
            receiver = new Receiver("client", socket);
            pool.execute(sender);
            pool.execute(receiver);

            Thread.sleep(sendMessages * 1000);
            pool.shutdownNow();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        var client = new DuplexClient("testDuplex", 2);
        client.startAndAwait();
        System.out.println("Server has received messages: " + client.receiver.receivedMessages);
    }

}
