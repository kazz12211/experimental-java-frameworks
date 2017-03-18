package core.util;

public class FatalAssertionException extends RuntimeException {

    public FatalAssertionException ()
    {
    }
    public FatalAssertionException (Throwable t)
    {
        super(t);
    }
    public FatalAssertionException (String message)
    {
        super(message);
    }

}
