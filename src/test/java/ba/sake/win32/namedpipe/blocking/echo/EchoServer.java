package ba.sake.win32.namedpipe.blocking.echo;

import java.net.*;
import ba.sake.win32.namedpipe.blocking.Win32NamedPipeServerSocket;
import java.io.*;

public class EchoServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(String pipeName) {
        try {
            serverSocket = new Win32NamedPipeServerSocket(pipeName);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if ("STOP".equals(inputLine)) {
                    out.println("bye");
                    break;
                }
                out.println(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
