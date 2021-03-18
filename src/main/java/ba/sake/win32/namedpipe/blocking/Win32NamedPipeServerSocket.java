package ba.sake.win32.namedpipe.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * Adapted from https://docs.microsoft.com/en-us/windows/win32/ipc/multithreaded-pipe-server
 * @author Sakib
 */
public class Win32NamedPipeServerSocket extends ServerSocket {

    private static final String PIPE_PREFIX = "\\\\.\\pipe\\";
    
    private static final Kernel32 API = Kernel32.INSTANCE;
    
    private final String pipeName;
    private final int maxInstances;

    public Win32NamedPipeServerSocket(String pipeName) throws IOException {
        this(pipeName, WinBase.PIPE_UNLIMITED_INSTANCES);
    }

    public Win32NamedPipeServerSocket(String pipeName, int maxInstances) throws IOException {
        this.pipeName = pipeName.startsWith(PIPE_PREFIX) ? pipeName : PIPE_PREFIX + pipeName;
        this.maxInstances = maxInstances;
    }

    @Override
    public Socket accept() throws IOException {
        return new Win32NamedPipeServerSocketImpl(pipeName, maxInstances);
    }
    
    @Override
    public synchronized void close() throws IOException {}

    private static class Win32NamedPipeServerSocketImpl extends Socket {

        private static final int BUFFER_SIZE = 8192;

        private final HANDLE pipeHandle;

        private final InputStream is;
        private final OutputStream os;

        Win32NamedPipeServerSocketImpl(String pipeName, int maxInstances) throws IOException {
            this.pipeHandle = API.CreateNamedPipe(
                    pipeName,
                    WinBase.PIPE_ACCESS_DUPLEX | WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE,
                    WinBase.PIPE_WAIT,
                    maxInstances,
                    BUFFER_SIZE,
                    BUFFER_SIZE, 
                    2000, // wait max 2 seconds for a client
                    null
            );

            if (WinBase.INVALID_HANDLE_VALUE.equals(pipeHandle)) {
                throw new IOException("Invalid named pipe: " + API.GetLastError());
            }
            boolean success = API.ConnectNamedPipe(pipeHandle, null);
            int connectError = API.GetLastError();
            if (success || connectError == WinError.ERROR_PIPE_CONNECTED) {
                // OK
            } else {
                throw new IOException("Could not accept a client: " + connectError);
            }
            this.is = new Win32NamedPipeInputStream(pipeHandle);
            this.os = new Win32NamedPipeOutputStream(pipeHandle);
        }

        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public OutputStream getOutputStream() {
            return os;
        }

        @Override
        public synchronized void close() throws IOException {
            API.FlushFileBuffers(pipeHandle);
            API.DisconnectNamedPipe(pipeHandle);
            API.CloseHandle(pipeHandle);
        }
    }
}
