package workflow.scheduler;


public class ScheduleDescription {

	private String year; 			// [0] 9999
	private String month; 			// [1] 1,2,3,4,5,6,7,8,9,10,11,12
	private String day; 			// [2] 1,2,3,.......,31
	private String hour;			// [3] 0,1,2,3,......,23
	private String minute;			// [4] 0,1,2,3,......,59
	private String second;			// [5] 0,1,2,3,......,59
	private String dayOfWeek;		// [6] 1,2,3,4,5,6,7  1=Sunday
	private String id;				// [7]
	private String runsOnBusinessDay;	// [8]
	private String taskClassname;	// [9] full qualified class name
	private String name;			// [10] human readable task name
	
	public String getTaskClassname() {
		return taskClassname;
	}
	public void setTaskClassname(String taskClassname) {
		this.taskClassname = taskClassname;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getMinute() {
		return minute;
	}
	public void setMinute(String minute) {
		this.minute = minute;
	}
	public String getSecond() {
		return second;
	}
	public void setSecond(String second) {
		this.second = second;
	}
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRunsOnBusinessDay() {
		return runsOnBusinessDay;
	}
	public void setRunsOnBusinessDay(String runsOnBusinessDay) {
		this.runsOnBusinessDay = runsOnBusinessDay;
	}
}
