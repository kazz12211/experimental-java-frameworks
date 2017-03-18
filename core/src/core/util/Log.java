package core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Level;

import ariba.util.log.Logger;

public class Log {

    public static Level DebugLevel = Level.DEBUG;
    public static Level InfoLevel = Level.INFO;
    public static Level WarnLevel = Level.WARN;
    public static Level ErrorLevel = Level.ERROR;

    public static Level toLevel (String level)
    {
        return Level.toLevel(level);
    }
    
    public static final Logger coreUtil = (Logger)Logger.getLogger("core.util");
    public static final Logger corePerf = (Logger)Logger.getLogger("core.perf");
	public static final Logger coreUtilDelegate = (Logger)Logger.getLogger("core.util.delegate");
	public static final Logger coreUtilSelector = (Logger)Logger.getLogger("core.util.selector");

    protected Log() {}
    
    static {
    	Logger.convertEarlyChecks();
    }
    
    public static void logStack(Logger category, String msg) {
    	Exception e = new Exception(msg);
    	logException(category, e);
    }
    
    public static void logException(Logger category, Exception e) {
    	Writer result = new StringWriter();
    	PrintWriter printWriter = new PrintWriter(result);
    	e.printStackTrace(printWriter);
    	category.debug(result.toString());
    }

}
