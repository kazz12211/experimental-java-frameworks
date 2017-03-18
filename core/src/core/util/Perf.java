package core.util;

public class Perf {

	private String _message;
	private long _start;

	protected Perf() {
	}
	
	public static Perf newPerf(String message) {
		Perf p = new Perf();
		return p.start(message);
	}
	
	public Perf start(String message) {
		_message = message;
		_start = System.currentTimeMillis();
		return this;
	}
	public void stop() {
		Log.corePerf.debug("[PERF] " + _message + " in " + (System.currentTimeMillis() - _start) + " ms"); 
	}

	public void stop(String string) {
		Log.corePerf.debug("[PERF] " + _message + " in " + (System.currentTimeMillis() - _start) + " ms " + string);
	}
}
