package ba.sake.win32.namedpipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import com.sun.jna.Memory;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

/*
 * https://docs.microsoft.com/en-us/windows/win32/ipc/synchronous-and-overlapped-input-and-output
 */
class Win32NamedPipeInputStream extends InputStream {
    
    private static final Kernel32API API = Kernel32API.INSTANCE;

    private static Set<Integer> PERMITTED_ERRORS = new HashSet<>();
    static {
        // if server disconnects while we're waiting for ReadFile
        PERMITTED_ERRORS.add(WinError.ERROR_PIPE_NOT_CONNECTED);
    }

    private final HANDLE pipeHandle;

    Win32NamedPipeInputStream(HANDLE handle) {
        this.pipeHandle = handle;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b) == -1) {
            return -1;
        } else {
            return 0xFF & b[0];
        }
    }

    @Override
    public int read(byte[] readBuffer, int off, int len) throws IOException {
        if (len <= 0)
            return 0;

        Memory data = new Memory(len);
        OVERLAPPED overlapped = new OVERLAPPED();
        overlapped.hEvent = API.CreateEvent(null, true, false, null);
        overlapped.write();
        boolean immediate = API.ReadFile(pipeHandle, data, len, null, overlapped.getPointer());

        if (!immediate) {
            int lastError = API.GetLastError();
            if (lastError != WinError.ERROR_IO_PENDING) {
                throw new IOException("ReadFile() failed: " + lastError);
            }
        }

        // wait for result blockingly
        IntByReference bytesReadRef = new IntByReference();
        boolean success = API.GetOverlappedResult(pipeHandle, overlapped.getPointer(), bytesReadRef, true);
        int lastError = API.GetLastError();

        if (success || PERMITTED_ERRORS.contains(lastError)) {
            int bytesRead = bytesReadRef.getValue();
            System.arraycopy(data.getByteArray(0, bytesRead), 0, readBuffer, off, bytesRead);

            if (bytesRead == 0)
                return -1;
            else
                return bytesRead;
        } else {
            throw new IOException("GetOverlappedResult() failed for ReadFile(): " + lastError);
        }

    }
}
