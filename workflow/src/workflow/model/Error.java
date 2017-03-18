package workflow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import workflow.model.listener.ErrorListener;
import core.BaseObject;

@Entity
@EntityListeners(ErrorListener.class)
public class Error extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	int code;
	String description;
	String exception;
	Date timestamp;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Long getId() {
		return id;
	}
	
	public String toString() {
		return "Error(" + description + ", code=" + code + ")";
	}
	
}
