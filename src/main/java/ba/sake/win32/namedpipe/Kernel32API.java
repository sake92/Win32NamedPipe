package ba.sake.win32.namedpipe;

import java.nio.ByteBuffer;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/*
 * Some APIs are missing from Kernel32. <br>
 * Some functions need ByteBuffer instead of simple arrays.
 */
public interface Kernel32API extends StdCallLibrary {

    Kernel32API INSTANCE = Native.load("kernel32", Kernel32API.class, W32APIOptions.DEFAULT_OPTIONS);

    int GetLastError();

    HANDLE CreateFile(String lpFileName, int dwDesiredAccess, int dwShareMode,
            WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes,
            HANDLE hTemplateFile);

   // boolean ReadFile(HANDLE hFile, ByteBuffer lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead,
    //        Pointer lpOverlapped);
    
    boolean ReadFile(HANDLE hFile, Memory lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead,
                    Pointer lpOverlapped);

    boolean WriteFile(HANDLE hFile, ByteBuffer lpBuffer, int nNumberOfBytesToWrite,
            IntByReference lpNumberOfBytesWritten, Pointer lpOverlapped);

    HANDLE CreateNamedPipe(String lpName, int dwOpenMode, int dwPipeMode, int nMaxInstances, int nOutBufferSize,
            int nInBufferSize, int nDefaultTimeOut, SECURITY_ATTRIBUTES lpSecurityAttributes);

    boolean ConnectNamedPipe(HANDLE hNamedPipe, Pointer lpOverlapped);

    boolean DisconnectNamedPipe(HANDLE hNamedPipe);

    boolean WaitNamedPipe(String lpNamedPipeName, int nTimeOut);

    boolean CloseHandle(HANDLE hObject);

    boolean FlushFileBuffers(HANDLE hFile);

    boolean GetOverlappedResult(HANDLE hFile, Pointer lpOverlapped, IntByReference lpNumberOfBytesTransferred,
            boolean wait);

    HANDLE CreateEvent(WinBase.SECURITY_ATTRIBUTES lpEventAttributes, boolean bManualReset, boolean bInitialState,
            String lpName);

    int WaitForSingleObject(HANDLE hHandle, int dwMilliseconds);
}
