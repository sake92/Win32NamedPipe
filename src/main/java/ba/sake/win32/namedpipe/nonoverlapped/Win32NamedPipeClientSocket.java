package ba.sake.win32.namedpipe.nonoverlapped;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * Adapted from https://docs.microsoft.com/en-us/windows/win32/ipc/named-pipe-client
 * @author Sakib
 */
public class Win32NamedPipeClientSocket extends Socket {

    private static final String PIPE_PREFIX = "\\\\.\\pipe\\";
    
    private static final Kernel32 API = Kernel32.INSTANCE;

    private final String pipeName;
    private final HANDLE pipeHandle;
    private final InputStream is;
    private final OutputStream os;

    public Win32NamedPipeClientSocket(String name) throws IOException {
        this.pipeName = name.startsWith(PIPE_PREFIX) ? name : PIPE_PREFIX + name;
        this.pipeHandle = API.CreateFile(
                pipeName,
                WinNT.GENERIC_READ | WinNT.GENERIC_WRITE,
                0, // no sharing
                null, // default security attributes
                WinNT.OPEN_EXISTING, 0, // default attributes
                null
        );
        
        int connectError = API.GetLastError();
        if (WinBase.INVALID_HANDLE_VALUE.equals(pipeHandle)) {
            throw new IOException("Invalid named pipe: '" + pipeName + "' Error: " + connectError);
        }

        if (connectError != WinError.DS_S_SUCCESS) {
            if (connectError == WinError.ERROR_PIPE_BUSY) {
                System.out.println("Named pipe[" + pipeName + "] is busy. Waiting for 5 seconds...");
                if (!API.WaitNamedPipe(pipeName, 5_000)) {
                    throw new IOException("Could not open named pipe after 5 seconds timeout " + connectError);
                }
            } else {
                throw new IOException("Could not open named pipe: " + connectError);
            }
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
        API.CloseHandle(pipeHandle);
    }

    @Override
    public void shutdownInput() throws IOException {}

    @Override
    public void shutdownOutput() throws IOException {}

}
