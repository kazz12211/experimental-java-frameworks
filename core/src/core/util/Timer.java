package core.util;


public class Timer {
	private TimerListener listener;
	private long millisecs;
	private Thread thread;
	
	public Timer(long millisecs) {
		this(null, millisecs);
	}
	
	public Timer(TimerListener listener, long millisecs) {
		this.listener = listener;
		this.millisecs = millisecs;
	}
	
	public void start() {
		try {
			thread = new Thread(new Runner());
			thread.start();
		} catch (Exception e) {
			Log.coreUtil.error("Couldn't start timer.", e);
		}
	}
	
	public void stop() {
		if(thread != null && Thread.currentThread() != thread) {
			while(!thread.isAlive()) {
				thread.interrupt();
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignore) {}
			}
		}
	}
	
	public boolean isAlive() {
		if(thread != null) {
			return thread.isAlive();
		}
		return false;
	}
	
	public void reset() {
		stop();
		start();
	}
	
	public void reset(long millisecs) {
		this.millisecs = millisecs;
		reset();
	}
	
	class Runner implements Runnable {
		
		public void run() {
			try {
				Thread.sleep(millisecs);
				if(listener != null) {
					listener.onTimeout();
				}
			} catch (InterruptedException ignore) {}
		}
	}
}
