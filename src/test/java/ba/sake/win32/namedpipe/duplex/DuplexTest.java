package ba.sake.win32.namedpipe.duplex;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.Executors;
import org.junit.Test;

public class DuplexTest {

    private static String PIPE_NAME = DuplexTest.class.getName();

    @Test
    public void testDuplexCommunication() throws Exception {

        var pool = Executors.newFixedThreadPool(2);

        int serverSendMessages = 8;
        var server = new DuplexServer(PIPE_NAME, serverSendMessages);
        pool.execute(() -> server.startAndAwait());
        Thread.sleep(1000); // wait for pipe to be instantiated

        int clientSendMessages = 7;
        var client = new DuplexClient(PIPE_NAME, clientSendMessages);
        pool.execute(() -> client.startAndAwait());

        Thread.sleep((Math.max(serverSendMessages, clientSendMessages) + 1) * 1000);
        pool.shutdown();

        assertEquals(serverSendMessages, client.receiver.receivedMessages);
        assertEquals(clientSendMessages, server.receiver.receivedMessages);
    }
}
