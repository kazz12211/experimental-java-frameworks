package workflow.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import core.BaseObject;
import core.util.ClassUtils;
import core.util.DateUtils;
import core.util.ListUtils;
import workflow.model.listener.WorkflowListener;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.meta.annotations.Trait.RichText;
import ariba.ui.meta.persistence.ObjectContext;

@Entity
@EntityListeners(WorkflowListener.class)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Workflow extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;

	String title;
	String name;
	
	@ManyToOne
	User creator;
	
	@ManyToOne
	User requester;
	
	Date createdDate;
	Date modifiedDate;
	Date submittedDate;
	Date rejectedDate;
	Date completedDate;
	Date firedDate;
	Date expiredDate;
	
	@ManyToOne
	Status status;
	
	@OneToMany
	List<Request> requests;
	
	Boolean hidden;
	
	@RichText
	@Column(name="comment", length=1024)
	String comment;
	@OneToMany
	List<WorkflowAttachment> attachments;
	@OneToMany
	List<Error> errors;
	
	@OneToMany
	List<Transition> transitions;
	
	Boolean archived;
	String className;
	Boolean deleted;
	Date deletedDate;
	
	@ManyToOne
	Folder folder;

	public Long getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public User getRequester() {
		return requester;
	}
	public void setRequester(User requester) {
		this.requester = requester;
	}

	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public Date getSubmittedDate() {
		return submittedDate;
	}
	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}

	public Date getRejectedDate() {
		return rejectedDate;
	}
	public void setRejectedDate(Date rejectedDate) {
		this.rejectedDate = rejectedDate;
	}

	public Date getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	
	public Date getFiredDate() {
		return firedDate;
	}
	public void setFiredDate(Date firedDate) {
		this.firedDate = firedDate;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}
	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

	public String getStatusCode() {
		Status s = this.getStatus();
		if(s != null)
			return s.getCode();
		return "";
	}
	
	public List<Request> getRequests() {
		if(requests == null) 
			requests = ListUtils.list();
		return requests;
	}
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}	
	public void setStatusCode(String statusCode) {
		this.status = Status.get(statusCode);
		if(statusCode.equals(Status.COMPLETED)) {
			this.setCompletedDate(new Date());
		} else if(statusCode.equals(Status.EXPIRED)) {
			this.setExpiredDate(new Date());
		} else if(statusCode.equals(Status.REJECTED)) {
			this.setRejectedDate(new Date());
		} else if(statusCode.equals(Status.SUBMITTED)) {
			this.setSubmittedDate(new Date());
		}
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	
	public List<WorkflowAttachment> getAttachments() {
		if(attachments == null)
			attachments = ListUtils.list();
		return attachments;
	}

	public void setAttachments(List<WorkflowAttachment> attachments) {
		this.attachments = attachments;
	}
	public void addAttachment(WorkflowAttachment attachment) {
		this.getAttachments().add(attachment);
	}
	public void removeAttachment(WorkflowAttachment attachment) {
		this.getAttachments().remove(attachment);
	}
	
	public List<Error> getErrors() {
		if(errors == null)
			errors = ListUtils.list();
		return errors;
	}
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	public Error addError(int errorCode, String errorDesc, String exception, ObjectContext objectContext) {
		Error error = objectContext.create(Error.class);
		error.setCode(errorCode);
		error.setDescription(errorDesc);
		error.setException(exception);
		this.getErrors().add(error);
		return error;
	}
	
	public void addError(Error error) {
		this.getErrors().add(error);
	}
	public void removeError(Error error) {
		this.getErrors().remove(error);
	}
	
	public List<Transition> getTransitions() {
		if(transitions == null)
			transitions = ListUtils.list();
		return transitions;
	}
	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}
	
	
	public Boolean getArchived() {
		return archived;
	}
	public void setArchived(Boolean archived) {
		this.archived = archived;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public Date getDeletedDate() {
		return deletedDate;
	}
	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}
	
	public Folder getFolder() {
		return folder;
	}
	public void setFolder(Folder folder) {
		this.folder = folder;
	}
	
	public void addRequest(Request request) {
		if(this.getRequests().contains(request) == false) {
			this.getRequests().add(request);
			request.setWorkflow(this);
		}
	}
	public void removeRequest(Request request) {
		if(this.getRequests().contains(request)) {
			this.getRequests().remove(request);
			request.setWorkflow(null);
		}
	}
	
	public Activity getActivityOfType(String activityClass) {
		for(Request r : this.getRequests()) {
			Activity a = r.getAction();
			if(ClassUtils.instanceOf(a, activityClass))
				return a;
		}
		return null;
	}
	
	public Long elapsedTime() {
		if(this.status == null)
			return null;
		String statusCode = status.getCode();
		if(statusCode.equals(Status.SUBMITTED) || statusCode.equals(Status.PENDING)) {
			return new Long(new Date().getTime() - submittedDate.getTime());
		}
		return null;
	}
	
	public Long handledTime() {
		if(this.status == null)
			return null;
		String statusCode = status.getCode();
		if(statusCode.equals(Status.COMPLETED) && completedDate != null) {
			return new Long(completedDate.getTime() - submittedDate.getTime());
		}
		if(statusCode.equals(Status.REJECTED) && rejectedDate != null) {
			return new Long(rejectedDate.getTime() - submittedDate.getTime());
		}
		return null;
	}
	
	public List<Request> getSortedSubmittedRequests() {
		List<Request> reqs = ListUtils.list();
		for(Request r : this.getRequests()) {
			if(r.getSubmittedDate() != null)
				reqs.add(r);
		}
		Collections.sort(reqs, new Comparator<Request>() {

			@Override
			public int compare(Request r1, Request r2) {
				return r1.getSubmittedDate().compareTo(r2.getSubmittedDate());
			}});
		return reqs;
	}

	public void addTransition(Transition transition) {
		this.getTransitions().add(transition);
		transition.setWorkflow(this);
	}
	public void removeTransition(Transition transition) {
		this.getTransitions().remove(transition);
		transition.setWorkflow(null);
	}
	
	public boolean isArchived() {
		return (archived != null && archived.booleanValue() == true);
	}
	public boolean isHidden() {
		return (hidden != null && hidden.booleanValue() == true);
	}
	public boolean isDeleted() {
		return (deleted != null && deleted.booleanValue() == true);
	}

	public List<Activity> getActivities() {
		List<Activity> activities = ListUtils.list();
		for(Request request : this.getRequests()) {
			if(request.getAction() != null)
				activities.add(request.getAction());
		}
		return activities;
	}
	
	public Integer submittedYear() {
		if(submittedDate == null)
			return null;
		int comps[] = DateUtils.dateComponents(submittedDate);
		return new Integer(comps[0]);
	}
	public Integer submittedMonth() {
		if(submittedDate == null)
			return null;
		int comps[] = DateUtils.dateComponents(submittedDate);
		return new Integer(comps[1]);
	}
	
	public boolean enableToDelete() {
		String statusCode = (String) this.getValueForKeyPath("status.code");
		return (Status.COMPLETED.equals(statusCode) || Status.ERROR.equals(statusCode) || Status.EXPIRED.equals(statusCode) || Status.REJECTED.equals(statusCode) || Status.SAVED.equals(statusCode));
	}
	
	public List<Request> handledRequests() {
		List<Request> handledRequests = ListUtils.list();
		for(Request request : this.getRequests()) {
			if(request.getAction() != null)
				handledRequests.add(request);
		}
		return handledRequests;
	}
	
	public int numberOfHandledRequests() {
		List<Request> handledRequests = this.handledRequests();
		return handledRequests.size();
	}
	
	public boolean enableToWithdraw() {
		int numRequests = this.numberOfHandledRequests();
		String statusCode = (String) this.getValueForKeyPath("status.code");
		return (Status.SUBMITTED.equals(statusCode) && numRequests == 0);
	}
	
	public boolean enableToEdit() {
		String statusCode = (String) this.getValueForKeyPath("status.code");
		return (enableToWithdraw() || Status.SAVED.equals(statusCode));
	}

	public boolean isViewable() {
		return (isArchived() == false && isHidden() == false && isDeleted() == false);
	}

	public boolean isViewableIn(Folder f) {
		return (isArchived() == false && isHidden() == false && isDeleted() == false && getFolder() == f);
	}

	public boolean done() {
		return (Status.COMPLETED.equals(this.getStatusCode())
				|| Status.EXPIRED.equals(this.getStatusCode())
				|| Status.REJECTED.equals(this.getStatusCode()));
	}

	private static final String[] _fields = {
		"className", "comment", "creator", "name", "requester", "title"
	};
	
	public List<String> fields() {
		return Arrays.asList(_fields);
	}
	
	public void copyFields(Workflow copy) {
		copy.setArchived(new Boolean(false));
		copy.setDeleted(new Boolean(false));
		if(attachments != null) {
			for(WorkflowAttachment attachment : attachments) {
				this.addAttachment((WorkflowAttachment) attachment.copy());
			}
		}
		for(String key : fields()) {
			copy.setValueForKey(this.getValueForKey(key), key);
		}
	}
	
	
	public void willWithdraw() {
		
	}

	public void willWithdraw(AWRequestContext requestContext) {

	}

}
