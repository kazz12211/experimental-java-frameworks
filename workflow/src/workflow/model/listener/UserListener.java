package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import workflow.model.User;

public class UserListener {

	@PrePersist
	public void onPrePersist(Object model) {
		User user = (User) model;
		user.setCreatedDate(new Date());
	}
	
	@PreUpdate
	public void onPreUpdate(Object model) {
		User user = (User) model;
		user.setModifiedDate(new Date());
	}
}
