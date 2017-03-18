package workflow.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import core.BaseObject;
import core.util.NumberUtils;

@Entity
public class UserPreference extends BaseObject {

	public static final String KEY_NOTIF_REQUEST = "notif.request";		// send to requestTo of Request
	public static final String KEY_NOTIF_ACTION = "notif.action";		// send to creator and requester of Workflow
	public static final String KEY_NOTIF_COMPLETE = "notif.complete";	// send to creator and requester of Workflow
	public static final String KEY_NOTIF_ERROR = "notif.error";			// send to creator and requester of Workflow
	public static final String KEY_NOTIF_REJECT = "notif.reject";		// send to creator and requester of Workflow
	public static final String KEY_NOTIF_EXPIRE = "notif.expire";		// send to creator and requester of Workflow
	public static final String KEY_SHOW_COUNTS = "display.showCounts";	// show/hide workflow & request counts

	static private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	String key;
	String value;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getId() {
		return id;
	}

	// convenient methods
	
	public Boolean getBooleanValue() {
		if("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "0".equals(value))
			return new Boolean(false);
		else if("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value))
			return new Boolean(true);
		else
			return null;
	}
	public void setBooleanValue(Boolean b) {
		if(b == null)
			this.value = null;
		else
			this.value = b.booleanValue() == true ? "true" : "false";
	}
	
	public Integer getIntegerValue() {
		if(value != null)
			return NumberUtils.toInteger(value);
		return null;
	}
	public void setIntegerValue(Integer i) {
		if(i == null)
			this.value = null;
		else
			this.value = Integer.toString(i);
	}
	
	public Long getLongValue() {
		if(value != null)
			return NumberUtils.toLong(value);
		return null;
	}
	public void setLongValue(Long l) {
		if(l == null)
			this.value = null;
		else
			this.value = Long.toString(l);
	}
	
	public Double getDoubleValue() {
		if(value != null)
			return NumberUtils.toDouble(value);
		return null;
	}
	public void setDoubleValue(Double d) {
		if(d == null)
			this.value = null;
		else
			this.value = Double.toString(d);
	}
	
	public String getStringValue() {
		return value;
	}
	public void setStringValue(String s) {
		this.value = s;
	}
	
	public Date getDateValue() throws Exception {
		if(value == null)
			return null;
		return DATE_FORMAT.parse(value);
	}
	public void setDateValue(Date d) {
		if(d == null)
			this.value = null;
		else
			this.value = DATE_FORMAT.format(d);
	}
}
