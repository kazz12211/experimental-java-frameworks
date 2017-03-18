package core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {

	public static int[] dateComponents(Date date) {
		int comps[] = new int[7];
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		comps[0] = cal.get(Calendar.YEAR);
		comps[1] = cal.get(Calendar.MONTH) + 1;
		comps[2] = cal.get(Calendar.DATE);
		comps[3] = cal.get(Calendar.HOUR_OF_DAY);
		comps[4] = cal.get(Calendar.MINUTE);
		comps[5] = cal.get(Calendar.SECOND);
		comps[6] = cal.get(Calendar.MILLISECOND);
		
		return comps;
	}
	
	public static int[] dateComponents() {
		return dateComponents(new Date());
	}
	
	public static Date dateWithComponents(int comps[]) {
		return dateWithComponents(comps[0], comps[1], comps[2], comps[3], comps[4], comps[5], comps[6]);
	}
	
	public static Date dateWithComponents(int year, int month, int day, int hour, int minute, int second, int millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millis);
		return cal.getTime();
	}

	public static Date dateWithComponents(int year, int month, int day) {
		return dateWithComponents(year, month, day, 0, 0, 0, 0);
	}
	
	public static Integer age(Date birthday) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(birthday);
		int y = cal.get(java.util.Calendar.YEAR);
		int m = cal.get(java.util.Calendar.MONTH) + 1;
		int d = cal.get(java.util.Calendar.DATE);
		if(y == 1900 && m == 1 && d == 1)
			return null;
		
	    cal.setTime(new Date());
	    int today_y = cal.get(java.util.Calendar.YEAR);
	    int today_m = cal.get(java.util.Calendar.MONTH)+1;
	    int today_d = cal.get(java.util.Calendar.DATE);
	 
	    int age = (today_y - y - ((m > today_m || (m == today_m && d > today_d)) ? 1:0));
	    return new Integer(age);
	}
	
	public static Date birthday(Integer age) {
		int comps[] = dateComponents();
		comps[0] -= (age.intValue() + 1);
		comps[3] = 0;
		comps[4] = 0;
		comps[5] = 0;
		comps[6] = 0;
		return dateWithComponents(comps);
	}
	
	public static Date dateByAddingDays(Date date, int days) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	public static Date startTimeOfTheDay(Date date) {
		int comps[] = dateComponents(date);
		comps[3] = 0;
		comps[4] = 0;
		comps[5] = 0;
		comps[6] = 0;
		return dateWithComponents(comps);
	}
	
	public static Date endTimeOfTheDay(Date date) {
		int comps[] = dateComponents(date);
		comps[3] = 23;
		comps[4] = 59;
		comps[5] = 59;
		comps[6] = 999;
		return dateWithComponents(comps);
	}
	
    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }
    


}
