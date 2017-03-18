package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import workflow.model.Status;
import workflow.model.Workflow;

public class WorkflowListener {

	@PrePersist
	public void onPrePersist(Object model) {
		Workflow workflow = (Workflow) model;
		
		if(workflow.getClassName() == null) {
			workflow.setClassName(workflow.getClass().getName());
		}
		if(workflow.getStatus() == null) {
			Status status = Status.get(Status.SAVED);
			workflow.setStatus(status);
		}
		
		if(workflow.getCreatedDate() == null)
			workflow.setCreatedDate(new Date());
		
		if(workflow.getStatus().getCode().equals(Status.SUBMITTED) && workflow.getSubmittedDate() == null) {
			workflow.setSubmittedDate(new Date());
		} else if(workflow.getStatus().getCode().equals(Status.EXPIRED) && workflow.getExpiredDate() == null) {
			workflow.setExpiredDate(new Date());
		} else if(workflow.getStatus().getCode().equals(Status.REJECTED) && workflow.getRejectedDate() == null) {
			workflow.setRejectedDate(new Date());
		}
		workflow.setModifiedDate(new Date());
	}
	
	@PreUpdate
	public void onPreUpdate(Object model) {
		Workflow workflow = (Workflow) model;
		workflow.setModifiedDate(new Date());
	}


}
