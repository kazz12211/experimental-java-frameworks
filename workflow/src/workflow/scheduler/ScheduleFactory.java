package workflow.scheduler;


import java.util.Calendar;
import java.util.Date;

public class ScheduleFactory {
	private static ScheduleFactory _sharedInstance = null;

	public static ScheduleFactory getInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new ScheduleFactory();
		}
		return _sharedInstance;
	}
	
	public Schedule createScheduleWithDescription(ScheduleDescription desc) {
		Date fireTime = this.createFireTimeFromDescription(desc);
		if(fireTime == null)
			return null;
		
		Schedule schedule = new Schedule();
		schedule.setId(desc.getId());
		schedule.setName(desc.getName());
		schedule.setFireTime(fireTime);
		schedule.setStatus(Schedule.INITED);
		schedule.setTaskClass(desc.getTaskClassname());

		String robd = desc.getRunsOnBusinessDay();
		if(robd == null || "false".equalsIgnoreCase(robd))
			schedule.setRunsOnBusinessDay(false);
		else
			schedule.setRunsOnBusinessDay(true);

		return schedule;
	}
	
	private Date createFireTimeFromDescription(ScheduleDescription desc) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		
		int year;
		
		if("*".equals(desc.getYear()) || desc.getYear() == null) {
			year = cal.get(Calendar.YEAR);
		} else {
			year = Integer.parseInt(desc.getYear());
		}
		
		int month;
		
		if("*".equals(desc.getMonth()) || desc.getMonth() == null) {
			month = cal.get(Calendar.MONTH) + 1;
		} else {
			month = Integer.parseInt(desc.getMonth());
		}
		
		int day;
		
		if("*".equals(desc.getDay()) || desc.getDay() == null) {
			day = cal.get(Calendar.DATE);
		} else {
			day = Integer.parseInt(desc.getDay());
		}
		
		int hour;
		
		if("*".equals(desc.getHour()) || desc.getHour() == null) {
			hour = cal.get(Calendar.HOUR_OF_DAY);
		} else {
			hour = Integer.parseInt(desc.getHour());
		}
		
		int minute;
		
		if("*".equals(desc.getMinute()) || desc.getMinute() == null) {
			minute = cal.get(Calendar.MINUTE);
		} else {
			minute = Integer.parseInt(desc.getMinute());
		}
		
		int second;
		
		if("*".equals(desc.getSecond()) || desc.getSecond() == null) {
			second = cal.get(Calendar.SECOND);
		} else {
			second = Integer.parseInt(desc.getSecond());
		}
		
		int dayOfWeek;
		if("*".equals(desc.getDayOfWeek()) || desc.getDayOfWeek() == null) {
			dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		} else {
			dayOfWeek = Integer.parseInt(desc.getDayOfWeek());
		}
		
		if(dayOfWeek != cal.get(Calendar.DAY_OF_WEEK))
			return null;
		
		Calendar newCal = Calendar.getInstance();
		newCal.set(Calendar.YEAR, year);
		newCal.set(Calendar.MONTH, month-1);
		newCal.set(Calendar.DATE, day);
		newCal.set(Calendar.HOUR_OF_DAY, hour);
		newCal.set(Calendar.MINUTE, minute);
		newCal.set(Calendar.SECOND, second);
		newCal.set(Calendar.MILLISECOND, 0);
		
		Date fireTime = newCal.getTime();
				
		return fireTime;
	}
	
}
