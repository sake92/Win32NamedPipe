package ba.sake.win32.namedpipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

class Win32NamedPipeInputStream extends InputStream {

    private static final Kernel32 API = Kernel32.INSTANCE;

    private static Set<Integer> PERMITTED_ERRORS = new HashSet<>();
    static {
        PERMITTED_ERRORS.add(WinError.ERROR_MORE_DATA);
        // if server disconnects while we're waiting for ReadFile
        PERMITTED_ERRORS.add(WinError.ERROR_PIPE_NOT_CONNECTED);
    }

    private final HANDLE handle;

    Win32NamedPipeInputStream(HANDLE handle) {
        this.handle = handle;
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

        byte[] data = new byte[len];
        IntByReference bytesReadRef = new IntByReference();
        boolean success = API.ReadFile(handle, data, len, bytesReadRef, null);
        
        
        int lastError = API.GetLastError();
        if (!success && !PERMITTED_ERRORS.contains(lastError)) {
            throw new IOException("ReadFile() failed: " + lastError);
        }

        int bytesRead = bytesReadRef.getValue();
        System.arraycopy(data, 0, readBuffer, off, bytesRead);

        if (bytesRead == 0 || !success)
            return -1;
        else
            return bytesRead;
    }
}
