package workflow.scheduler;

import java.util.Date;

public class Schedule {
	public static final int INITED = 0;
	public static final int RUNNING = 1;
	public static final int SUSPENDED = 2;
	public static final int FINISHED = 3;
	public static final int ERROR = 4;
	
	private String id;
	private String name;
	private Date fireTime;
	private String taskClass;
	private int status;
	private String errorString;
	private Date startTime;
	private Date stopTime;
	private boolean runsOnBusinessDay;
	
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
	public Date getFireTime() {
		return fireTime;
	}
	public void setFireTime(Date fireTime) {
		this.fireTime = fireTime;
	}
	public String getTaskClass() {
		return taskClass;
	}
	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}
	
	public boolean isSuspended() {
		return status == SUSPENDED;
	}
	public boolean isRunning() {
		return status == RUNNING;
	}
	public boolean isFinished() {
		return status == FINISHED;
	}
	public boolean isError() {
		return status == ERROR;
	}
	public boolean isInited() {
		return status == INITED;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getErrorString() {
		return errorString;
	}
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getStopTime() {
		return stopTime;
	}
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	public boolean runsOnBusinessDay() {
		return runsOnBusinessDay;
	}
	public void setRunsOnBusinessDay(boolean flag) {
		this.runsOnBusinessDay = flag;
	}
	
	/*
	private void getDateComponents(Date date, int ymdhms[]) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		ymdhms[0] = cal.get(Calendar.YEAR);
		ymdhms[1] = cal.get(Calendar.MONTH) + 1;
		ymdhms[2] = cal.get(Calendar.DATE);
		ymdhms[3] = cal.get(Calendar.HOUR_OF_DAY);
		ymdhms[4] = cal.get(Calendar.MINUTE);
		ymdhms[5] = cal.get(Calendar.SECOND);
	}
	
	private int compareDateComponents(int comp1[], int comp2[]) {
		if(comp1[0] > comp2[0]) {
			return 1;
		} else if(comp1[0] < comp2[0]) {
			return -1;
		} else {
			if(comp1[1] > comp2[1]) {
				return 1;
			} else if(comp1[1] < comp2[1]) {
				return -1;
			} else {
				if(comp1[2] > comp2[2]) {
					return 1;
				} else if(comp1[2] < comp2[2]) {
					return -1;
				} else {
					if(comp1[3] > comp2[3]) {
						return 1;
					} else if(comp1[3] < comp2[3]) {
						return -1;
					} else {
						if(comp1[4] > comp2[4]) {
							return 1;
						} else if(comp1[4] < comp2[4]) {
							return -1;
						} else {
							if(comp1[5] > comp2[5]) {
								return 1;
							} else if(comp1[5] < comp2[5]) {
								return -1;
							} else {
								return 0;
							}
						}
					}
				}
			}
		}
	}
	*/
	
	public boolean isEqualToSchedule(Schedule another) {
		/*
		if(!this.getTaskClass().equals(another.getTaskClass()))
			return false;
		
		int ymdhms1[] = {0,0,0,0,0,0};
		int ymdhms2[] = {0,0,0,0,0,0};
		this.getDateComponents(this.fireTime, ymdhms1);
		this.getDateComponents(another.getFireTime(), ymdhms2);
		if(this.compareDateComponents(ymdhms1, ymdhms2) != 0) {
			return false;
		}
		*/
		return this.getId().equals(another.getId());
	}
}
