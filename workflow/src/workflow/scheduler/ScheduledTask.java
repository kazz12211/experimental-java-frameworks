package workflow.scheduler;


import java.util.ArrayList;
import java.util.List;

public abstract class ScheduledTask {

	private String id;
	private String name;
	private List<TaskCompleteListener> listeners = new ArrayList<TaskCompleteListener>();
	private Schedule schedule;
	private boolean runsOnBusinessDay;
	
	protected ScheduledTask() {
	}
		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Schedule getSchedule() {
		return schedule;
	}
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public boolean runsOnBusinessDay() {
		return runsOnBusinessDay;
	}
	public void setRunsOnBusinessDay(boolean runsOnBusinessDay) {
		this.runsOnBusinessDay = runsOnBusinessDay;
	}
	
	public abstract void start();
	
	protected void complete() {
		for(TaskCompleteListener listener : listeners) {
			listener.taskCompleted(this, null);
		}
		this.listeners.clear();
	}
	
	protected void fail(Throwable error) {
		for(TaskCompleteListener listener : listeners) {
			listener.taskCompleted(this, error);
		}
		this.listeners.clear();
	}
	
	public void addTaskCompleteListener(TaskCompleteListener listener) {
		this.listeners.add(listener);
	}
	public void removeTaskCompleteListener(TaskCompleteListener listener) {
		this.listeners.remove(listener);
	}
	
}
