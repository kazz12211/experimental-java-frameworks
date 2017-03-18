package workflow.model;

import java.util.Date;

import core.util.DateUtils;

public class BusinessCalendar {

	public Date getBusinessDay(Date base, int days) {
		Date day = DateUtils.dateByAddingDays(base, days);
		while(CompanyHoliday.isHoliday(day)) {
			day = DateUtils.dateByAddingDays(day, 1);
		}
		return day;
	}

}
