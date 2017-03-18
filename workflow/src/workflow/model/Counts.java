package workflow.model;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.MapUtil;

@Entity
public class Counts {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	Long userId;
	long savedWorkflow;
	long submittedWorkflow;
	long completedWorkflow;
	long rejectedWorkflow;
	long pendingWorkflow;
	long expiredWorkflow;
	long errorWorkflow;
	long requestedRequest;
	long submittedRequest;
	long rejectedRequest;
	long expiredRequest;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public long getSavedWorkflow() {
		return savedWorkflow;
	}
	public void setSavedWorkflow(long savedWorkflow) {
		this.savedWorkflow = savedWorkflow;
	}
	public long getSubmittedWorkflow() {
		return submittedWorkflow;
	}
	public void setSubmittedWorkflow(long submittedWorkflow) {
		this.submittedWorkflow = submittedWorkflow;
	}
	public long getCompletedWorkflow() {
		return completedWorkflow;
	}
	public void setCompletedWorkflow(long completedWorkflow) {
		this.completedWorkflow = completedWorkflow;
	}
	public long getRejectedWorkflow() {
		return rejectedWorkflow;
	}
	public void setRejectedWorkflow(long rejectedWorkflow) {
		this.rejectedWorkflow = rejectedWorkflow;
	}
	public long getPendingWorkflow() {
		return pendingWorkflow;
	}
	public void setPendingWorkflow(long pendingWorkflow) {
		this.pendingWorkflow = pendingWorkflow;
	}
	public long getExpiredWorkflow() {
		return expiredWorkflow;
	}
	public void setExpiredWorkflow(long expiredWorkflow) {
		this.expiredWorkflow = expiredWorkflow;
	}
	public long getErrorWorkflow() {
		return errorWorkflow;
	}
	public void setErrorWorkflow(long errorWorkflow) {
		this.errorWorkflow = errorWorkflow;
	}
	public long getRequestedRequest() {
		return requestedRequest;
	}
	public void setRequestedRequest(long requestedRequest) {
		this.requestedRequest = requestedRequest;
	}
	public long getSubmittedRequest() {
		return submittedRequest;
	}
	public void setSubmittedRequest(long submittedRequest) {
		this.submittedRequest = submittedRequest;
	}
	public long getRejectedRequest() {
		return rejectedRequest;
	}
	public void setRejectedRequest(long rejectedRequest) {
		this.rejectedRequest = rejectedRequest;
	}
	public long getExpiredRequest() {
		return expiredRequest;
	}
	public void setExpiredRequest(long expiredRequest) {
		this.expiredRequest = expiredRequest;
	}
	public Long getId() {
		return id;
	}
	
	public static Counts find(User user, ObjectContext oc) {
		return find(user.getId(), oc);
	}
	
	public static void createOrUpdate(User user, ObjectContext oc) {
		Counts counts = find(user, oc);
		if(counts == null) {
			counts = oc.create(Counts.class);
			counts.setUserId(user.getId());
		}
		counts.setCompletedWorkflow(user.completedWorkflowCount());
		counts.setErrorWorkflow(user.errorWorkflowCount());
		counts.setExpiredWorkflow(user.expiredWorkflowCount());
		counts.setPendingWorkflow(user.pendingWorkflowCount());
		counts.setRejectedWorkflow(user.rejectedWorkflowCount());
		counts.setSavedWorkflow(user.savedWorkflowCount());
		counts.setSubmittedWorkflow(user.submittedWorkflowCount());
		counts.setExpiredRequest(user.expiredRequestCount());
		counts.setRejectedRequest(user.rejectedRequestCount());
		counts.setRequestedRequest(user.requestedRequestCount());
		counts.setSubmittedRequest(user.submittedRequestCount());
		oc.save();
	}

	public static void remove(User user, ObjectContext oc) {
		Counts counts = find(user, oc);
		if(counts != null) {
			oc.remove(counts);
			oc.save();
		}
	}
	
	public static Counts find(Long userId, ObjectContext oc) {
		Map<String, Object> fieldValues = MapUtil.map();
		fieldValues.put("userId", userId);
		return oc.findOne(Counts.class, fieldValues);
	}
	
	public static void createOrUpdate(Long userId, ObjectContext oc) {
		User user = oc.find(User.class, userId);
		Counts counts = find(userId, oc);
		if(counts == null) {
			counts = oc.create(Counts.class);
			counts.setUserId(userId);
		}
		counts.setCompletedWorkflow(user.completedWorkflowCount());
		counts.setErrorWorkflow(user.errorWorkflowCount());
		counts.setExpiredWorkflow(user.expiredWorkflowCount());
		counts.setPendingWorkflow(user.pendingWorkflowCount());
		counts.setRejectedWorkflow(user.rejectedWorkflowCount());
		counts.setSavedWorkflow(user.savedWorkflowCount());
		counts.setSubmittedWorkflow(user.submittedWorkflowCount());
		counts.setExpiredRequest(user.expiredRequestCount());
		counts.setRejectedRequest(user.rejectedRequestCount());
		counts.setRequestedRequest(user.requestedRequestCount());
		counts.setSubmittedRequest(user.submittedRequestCount());
		oc.save();
	}

	public static void remove(Long userId, ObjectContext oc) {
		User user = oc.find(User.class, userId);
		oc.remove(user);
		oc.save();
	}
	
	public static void updateWorkflowCount(Long userId, String statusFrom, String statusTo, ObjectContext oc) {
		User user = oc.find(User.class, userId);
		Counts counts = find(userId, oc);
		if(counts == null) {
			counts = oc.create(Counts.class);
			counts.setUserId(userId);
		}
		if(statusFrom != null)
			updateWorkflowCount(counts, user, statusFrom, oc);
		if(statusTo != null)
			updateWorkflowCount(counts, user, statusTo, oc);
	}
	
	public static void updateWorkflowCount(Long userId, String[] statusFroms,
			String statusTo, ObjectContext oc) {
		User user = oc.find(User.class, userId);
		Counts counts = find(userId, oc);
		if(counts == null) {
			counts = oc.create(Counts.class);
			counts.setUserId(userId);
		}
		if(statusFroms != null)
			for(String from : statusFroms)
				updateWorkflowCount(counts, user, from, oc);
		if(statusTo != null)
			updateWorkflowCount(counts, user, statusTo, oc);
	}

	public static void updateRequestCount(Long userId, String statusFrom, String statusTo, ObjectContext oc) {
		User user = oc.find(User.class, userId);
		Counts counts = find(userId, oc);
		if(counts == null) {
			counts = oc.create(Counts.class);
			counts.setUserId(userId);
		}
		if(statusFrom != null)
			updateRequestCount(counts, user, statusFrom, oc);
		if(statusTo != null)
			updateRequestCount(counts, user, statusTo, oc);
	}
	
	public static void updateWorkflowCount(Counts counts, User user, String statusCode,
			ObjectContext oc) {
		if(statusCode.equals(Status.COMPLETED))
			counts.setCompletedWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.ERROR))
			counts.setErrorWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.EXPIRED))
			counts.setExpiredWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.PENDING))
			counts.setPendingWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.REJECTED))
			counts.setRejectedWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.SAVED))
			counts.setSavedWorkflow(user.workflowCountOfStatus(statusCode));
		else if(statusCode.equals(Status.SUBMITTED))
			counts.setSubmittedWorkflow(user.workflowCountOfStatus(statusCode));
		
		oc.save();
	}
	
	public static void updateRequestCount(Counts counts, User user, String statusCode, ObjectContext oc) {
		if(statusCode.equals(Status.SUBMITTED))
			counts.setSubmittedRequest(user.requestsOfStatus(statusCode).size());
		else if(statusCode.equals(Status.EXPIRED))
			counts.setExpiredRequest(user.requestsOfStatus(statusCode).size());
		else if(statusCode.equals(Status.REQUESTED))
			counts.setRequestedRequest(user.requestsOfStatus(statusCode).size());
		else if(statusCode.equals(Status.REJECTED))
			counts.setRejectedRequest(user.requestsOfStatus(statusCode).size());
		
		oc.save();
	}
}
