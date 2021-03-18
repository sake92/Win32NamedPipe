package ba.sake.win32.namedpipe.blocking;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

class Win32NamedPipeOutputStream extends OutputStream {

    private static final Kernel32 API = Kernel32.INSTANCE;

    private final HANDLE handle;

    Win32NamedPipeOutputStream(HANDLE handle) {
        this.handle = handle;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) (0xFF & b)});
    }

    @Override
    public void write(byte[] originalData, int off, int len) throws IOException {

        byte[] data = Arrays.copyOfRange(originalData, off, off + len);
        IntByReference bytesWrittenRef = new IntByReference();
        boolean success = API.WriteFile(handle, data, data.length, bytesWrittenRef, null);

        if (!success) {
            int lastError = API.GetLastError();
            throw new IOException("WriteFile() failed: " + lastError);
        }
    }

    @Override
    public void flush() throws IOException {
        API.FlushFileBuffers(handle);
    }
}
