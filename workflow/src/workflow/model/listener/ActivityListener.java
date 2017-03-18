package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import workflow.model.Activity;


public class ActivityListener {

	@PrePersist
	public void onPrePersist(Object model) {
		Activity activity = (Activity) model;
		activity.setCreatedDate(new Date());
		activity.setModifiedDate(new Date());
	}
	
	@PreUpdate
	public void onPreUpdate(Object model) {
		Activity activity = (Activity) model;
		activity.setModifiedDate(new Date());
	}

}
