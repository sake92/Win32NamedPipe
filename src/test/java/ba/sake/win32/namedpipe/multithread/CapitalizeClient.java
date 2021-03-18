package ba.sake.win32.namedpipe.multithread;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import ba.sake.win32.namedpipe.Win32NamedPipeClientSocket;

public class CapitalizeClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Win32NamedPipeClientSocket("CapitalizePipeTest");
                Scanner scanner = new Scanner(System.in);) {
            System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");

            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (scanner.hasNextLine()) {
                out.println(scanner.nextLine());
                System.out.println(in.nextLine());
            }
        }
    }
}
