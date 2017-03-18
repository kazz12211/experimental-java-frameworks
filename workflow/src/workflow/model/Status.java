package workflow.model;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import core.BaseObject;
import ariba.ui.meta.persistence.ObjectContext;

@Entity
public class Status extends BaseObject {

	public final static String SAVED = "saved";
	public final static String SUBMITTED = "submitted";
	public final static String REJECTED = "rejected";
	public final static String COMPLETED = "completed";
	public final static String PENDING = "pending";
	public final static String EXPIRED = "expired";
	public final static String REQUESTED = "requested";
	public static final String ERROR = "error";

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	String code;
	String label;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public Long getId() {
		return id;
	}
	
	public static Status get(String code) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("code", code);
		return ObjectContext.get().findOne(Status.class, fieldValues);
	}
	
	public static Status createAndInsert(String code, String label) {
		ObjectContext oc = ObjectContext.get();
		Status status = oc.create(Status.class);
		status.setCode(code);
		status.setLabel(label);
		oc.save();
		return status;
	}

}
