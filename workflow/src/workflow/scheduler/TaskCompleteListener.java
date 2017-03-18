package workflow.scheduler;


public interface TaskCompleteListener {

	public void taskCompleted(ScheduledTask task, Throwable error);

}
