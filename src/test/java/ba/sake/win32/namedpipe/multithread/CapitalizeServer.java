package ba.sake.win32.namedpipe.multithread;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import ba.sake.win32.namedpipe.Win32NamedPipeServerSocket;

/**
 * When a client connects, a new thread is started to handle it. Receiving
 * client data, capitalizing it, and sending the response back is all done on
 * the thread.
 */
public class CapitalizeServer {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        try (var serverSocket = new Win32NamedPipeServerSocket("CapitalizePipeTest")) {
            System.out.println("CapitalizeServer is running...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                var socket = serverSocket.accept(); // wait for new client
                pool.execute(new Capitalizer(socket));
            }
        }
    }

    private static class Capitalizer implements Runnable {

        private final Socket socket;

        Capitalizer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + socket.hashCode());
            try (socket;
                    var in = new Scanner(socket.getInputStream());
                    var out = new PrintWriter(socket.getOutputStream(), true);) {

                while (in.hasNextLine()) {
                    out.println(in.nextLine().toUpperCase());
                }
            } catch (IOException e) {
                System.out.println("Error:" + socket);
            }
            System.out.println("Client disconnected: " + socket.hashCode());
        }
    }
}
