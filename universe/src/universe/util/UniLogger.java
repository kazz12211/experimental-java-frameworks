package universe.util;

import ariba.util.log.Logger;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniLogger {
	
    public static final Logger universe = (Logger)Logger.getLogger("universe");
    public static final Logger universe_dev = (Logger)Logger.getLogger("universe.dev");
    public static final Logger universe_connection = (Logger)Logger.getLogger("universe.connection");
    public static final Logger universe_command = (Logger)Logger.getLogger("universe.command");
    public static final Logger universe_test = (Logger)Logger.getLogger("universe.test");
    public static final Logger universe_snapshot = (Logger)Logger.getLogger("universe.snapshot");
    public static final Logger universe_cache = (Logger)Logger.getLogger("universe.cache");
    public static final Logger universe_perf = (Logger)Logger.getLogger("universe.perf");

}
