package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import workflow.model.Request;

public class RequestListener {

	@PrePersist
	public void onPrePersist(Object model) {
		Request request = (Request) model;
		request.setCreatedDate(new Date());
	}
	
	@PreUpdate
	public void onPreUpdate(Object model) {
		Request request = (Request) model;
		request.setModifiedDate(new Date());
	}

}
