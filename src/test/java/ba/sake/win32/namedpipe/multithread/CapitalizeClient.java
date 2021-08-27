package ba.sake.win32.namedpipe.multithread;

import java.io.PrintWriter;
import java.util.Scanner;
import ba.sake.win32.namedpipe.Win32NamedPipeClientSocket;

public class CapitalizeClient {

    public static void main(String[] args) throws Exception {
        try (var socket = new Win32NamedPipeClientSocket("CapitalizePipeTest");
                var scanner = new Scanner(System.in);
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);) {

            System.out.println("Enter line to be capitalized:");
            while (scanner.hasNextLine()) {
                out.println(scanner.nextLine());
                System.out.println(in.nextLine());
            }
        }
    }
}
