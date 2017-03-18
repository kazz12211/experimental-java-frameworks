package core.util;

import ariba.util.core.ThreadDebugState;

public final class Assert {

    private Assert () {
    }

    public static void fail(String message) {
    	assertFatal(message);
    }
    
    public static void fail(Throwable t, String message) {
    	assertFatal(message, t);
    }
    
    public static void that(boolean b, String message) {
    	if(!b)
    		assertFatal(message);
    }
    public static void assertNonFatal(boolean b, String message) {
    	if(!b)
    		assertNonFatal(message);
    }
    private static void assertNonFatal(String message) {
    	assertNonFatal(message, null);
    }
    private static void assertNonFatal(String message, Throwable t) {
    	Log.coreUtil.warning(2811, message, Thread.currentThread().getName(), ThreadDebugState.makeString(),
    			t != null ? SystemUtils.stackTrace(t) : SystemUtils.stackTrace());
    }
    private static void assertFatal(String message) {
    	assertFatal(message, null);
    }
    private static void assertFatal(String message, Throwable t) {
    	assertNonFatal(message, t);
    	FatalAssertionException e = new FatalAssertionException(message);
    	if(t != null)
    		e.initCause(t);
    	throw e;
    }
}
