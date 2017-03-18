package workflow.controller.rule;

import java.util.Date;

import workflow.model.BusinessCalendar;

public class ExpirationDef {

	public static final String EXPIRATION_HOURS			= "hours";
	public static final String EXPIRATION_MINUTES		= "minutes";
	public static final String EXPIRATION_DAYS			= "days";
	public static final String EXPIRATION_BUSINESS_DAYS = "businessDays";
	
	String type;
	String value;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getTypeAndValue() {
		return type + ":" + value;
	}
	
	public Date expirationDateFor(Date base) {
		if(base == null)
			return null;
		long duration = Long.parseLong(value);
		if(EXPIRATION_DAYS.equals(type)) {
			duration = duration * 1000 * 60 * 60 * 24;
		} else if(EXPIRATION_HOURS.equals(type)) {
			duration = duration * 1000 * 60 * 60;
		} else if(EXPIRATION_MINUTES.equals(type)) {
			duration = duration * 1000 * 60;
		} else if(EXPIRATION_BUSINESS_DAYS.equals(type)) {
			int days = (int)duration;
			BusinessCalendar cal = new BusinessCalendar();
			return cal.getBusinessDay(base, days);
		} else {
			return null;
		}
		
		Long time = base.getTime() + duration;
		return new Date(time);
	}

}
