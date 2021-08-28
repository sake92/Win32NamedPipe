





If you want to read/write **simultaneously** to/from a named pipe (from different threads),  
you **have to use Overlapped I/O** !

Explanation from the [docs](https://docs.microsoft.com/en-us/previous-versions/ms810467(v=msdn.10)?redirectedfrom=MSDN#nonoverlapped-io) for "Nonoverlapped I/O":

It is the responsibility of the application to serialize access to the port correctly.  
If one thread is blocked waiting for its I/O operation to complete, 
*all other threads that subsequently call a communications API will be blocked* until the original operation completes.  
For instance, if one thread were waiting for a `ReadFile` function to return,  
any other thread that issued a `WriteFile` function would be blocked.





