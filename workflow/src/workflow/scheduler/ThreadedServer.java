package workflow.scheduler;

import workflow.util.Logging;

public abstract class ThreadedServer implements Runnable {

	private boolean complete;
	private Thread workerThreads[];
	protected ScheduleQueue queue;
	private String name;
	
	public ThreadedServer(String name, int numThreads) {
		this.name = name;
		queue = new ScheduleQueue();
		complete = true;
		workerThreads = new Thread[numThreads];
	}
	
	public void start() {
		if(this.isRunning())
			return;
		
		Logging.custom.info("Starting " + name + " with " + getThreadCount() + " worker thread(s)...");
		complete = false;
		for(int i = 0; i < this.getThreadCount(); i++) {
			workerThreads[i] = new Thread(this);
		}
		for(int i = 0; i < this.getThreadCount(); i++) {
			workerThreads[i].start();
		}
		Logging.custom.info("Started " + name + ".");
	}
	
	public void stop() {
		if(!this.isRunning())
			return;
		
		Logging.custom.info("Stopping " + name + "...");
		
		synchronized(queue) {
			queue.notifyAll();
			complete = true;
		}
		
		for(int i = this.getThreadCount() - 1; i >= 0; i--) {
			workerThreads[i].interrupt();
		}
		
		Logging.custom.info("Stopped " + name + ".");
	}
	
	public boolean isRunning() {
		return !complete;
	}
	
	public int getThreadCount() {
		return workerThreads.length;
	}
	
	public String getName() {
		return name;
	}
	
	public ScheduleQueue queue() {
		return queue;
	}
	
	protected void finalize() throws Throwable {
		this.stop();
		super.finalize();
	}
	

}
