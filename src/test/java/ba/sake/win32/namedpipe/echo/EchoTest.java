package ba.sake.win32.namedpipe.echo;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.junit.Test;

public class EchoTest {

    private static String PIPE = "test_pipe_echo_123";

    @Test
    public void givenClient_whenServerEchosMessage_thenCorrect() {
        withServerAndClient(client -> {
            String resp1 = client.sendMessage("hello");
            String resp2 = client.sendMessage("wo rld");
            String resp3 = client.sendMessage("!");
            String resp4 = client.sendMessage("STOP");
            assertEquals("hello", resp1);
            assertEquals("wo rld", resp2);
            assertEquals("!", resp3);
            assertEquals("bye", resp4);
        });
    }

    static void withServerAndClient(Consumer<EchoClient> code) {
        EchoServer server = new EchoServer();
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            System.out.println("Starting EchoServer");
            server.start(PIPE);
        });

        try {
            Thread.sleep(2000);// wait a bit for pipe to be ready
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EchoClient client = new EchoClient();
        client.startConnection(PIPE);

        code.accept(client); // do test code

        client.stopConnection();
        server.stop();
        System.out.println("Stopped EchoServer");
    }
}
