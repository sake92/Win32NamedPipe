package ba.sake.win32.namedpipe;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

class Win32NamedPipeOutputStream extends OutputStream {

    private static final Kernel32API API = Kernel32API.INSTANCE;

    private final HANDLE handle;

    Win32NamedPipeOutputStream(HANDLE handle)  {
        this.handle = handle;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) (0xFF & b)});
    }

    @Override
    public void write(byte[] originalData, int off, int len) throws IOException {

        ByteBuffer data = ByteBuffer.wrap(originalData, off, len);
        OVERLAPPED overlapped = new OVERLAPPED();
        overlapped.hEvent = API.CreateEvent(null, true, false, null);
        overlapped.write();
        boolean immediate = API.WriteFile(handle, data, len, null, overlapped.getPointer());
        
        if (!immediate) {
            int lastError = API.GetLastError();
            if (lastError != WinError.ERROR_IO_PENDING) {
                throw new IOException("WriteFile() failed: " + lastError);
            }
        }

        IntByReference writtenRef = new IntByReference();
        boolean success = API.GetOverlappedResult(handle, overlapped.getPointer(), writtenRef, true);
        if (!success) {
            int lastError = API.GetLastError();
            throw new IOException("GetOverlappedResult() failed for WriteFile(): " + lastError);
        }
    }

    @Override
    public void flush() throws IOException {
        API.FlushFileBuffers(handle);
    }
}
