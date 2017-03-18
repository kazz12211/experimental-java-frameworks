package workflow.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import core.util.DateUtils;
import core.util.MapUtils;
import ariba.ui.meta.persistence.ObjectContext;

@Entity
public class CompanyHoliday {
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	Date date;
	String description;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = DateUtils.startTimeOfTheDay(date);
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getId() {
		return id;
	}

	public static CompanyHoliday get(Date date) {
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put("date", DateUtils.startTimeOfTheDay(date));
		return ObjectContext.get().findOne(CompanyHoliday.class, fieldValues);
	}
	
	public static boolean isHoliday(Date date) {
		return get(date) != null;
	}
}
