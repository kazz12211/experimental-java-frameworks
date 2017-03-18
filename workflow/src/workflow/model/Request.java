package workflow.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import core.BaseObject;
import core.util.DateUtils;
import workflow.model.listener.RequestListener;
import ariba.util.core.ListUtil;
import ariba.util.core.StringUtil;

@Entity
@EntityListeners(RequestListener.class)
public class Request extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	Date createdDate;
	Date modifiedDate;
	Date submittedDate;
	Date requestedDate;
	@ManyToOne
	Actor requestTo;
	@OneToOne
	Activity action;
	String name;
	@ManyToOne
	Status status;
	@ManyToOne
	Workflow workflow;
	String activityClassName;
	String activityRefId;
	Date expirationDate;
	Date expiredDate;

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
	public Date getRequestedDate() {
		return requestedDate;
	}
	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
	}
	public Actor getRequestTo() {
		return requestTo;
	}
	public void setRequestTo(Actor requestTo) {
		this.requestTo = requestTo;
	}
	public Activity getAction() {
		return action;
	}
	public void setAction(Activity action) {
		this.action = action;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	public Workflow getWorkflow() {
		return workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	public String getActivityClassName() {
		return activityClassName;
	}
	public void setActivityClassName(String activityClassName) {
		this.activityClassName = activityClassName;
	}
	public String getActivityRefId() {
		return activityRefId;
	}
	public void setActivityRefId(String activityRefId) {
		this.activityRefId = activityRefId;
		
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public Date getExpiredDate() {
		return expiredDate;
	}
	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}
	
	public Long getId() {
		return id;
	}
	
	
	public Long elapsedTime() {
		if(this.status == null)
			return null;
		String statusCode = status.getCode();
		if(statusCode.equals(Status.SUBMITTED) || statusCode.equals(Status.PENDING)) {
			return new Long(new Date().getTime() - requestedDate.getTime());
		}
		return null;
	}
	
	public Long handledTime() {
		if(this.status == null)
			return null;
		String statusCode = status.getCode();
		if((statusCode.equals(Status.SUBMITTED)  || statusCode.equals(Status.REJECTED)) && submittedDate != null) {
			return new Long(submittedDate.getTime() - requestedDate.getTime());
		}
		return null;
	}
	
	public void setStatusCode(String statusCode) {
		this.status = Status.get(statusCode);
		if(statusCode.equals(Status.SUBMITTED))
			this.setSubmittedDate(new Date());
		else if(statusCode.equals(Status.REQUESTED))
			this.setRequestedDate(new Date());
		else if(statusCode.equals(Status.EXPIRED))
			this.setExpiredDate(new Date());
		else if(statusCode.equals(Status.REJECTED))
			this.setSubmittedDate(new Date());
	}

	public List<User> actingUsers() {
		return this.actingUsers(this.getRequestTo());
	}
	
	public List<User> actingUsers(Actor actor) {
		List<User> users = ListUtil.list();
		
		if(actor == null)
			return users;
		
		if(actor instanceof Role) {
			for(User user : ((Role)actor).getUsers()) {
				users.add(user);
			}
		} else if(actor instanceof User) {
			users.add((User) actor);
		}
		return users;
	}
	
	public Request removeFromActor(Actor actor) {
		List<User> users = this.actingUsers(actor);
		for(User user : users) {
			user.removeRequest(this);
		}
		this.setRequestTo(null);
		return this;
	}
	
	public Request removeFromActor() {
		return this.removeFromActor(requestTo);
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
	public Integer requestedYear() {
		if(requestedDate == null)
			return null;
		int comps[] = DateUtils.dateComponents(requestedDate);
		return new Integer(comps[0]);
	}
	
	public Integer requestedMonth() {
		if(requestedDate == null)
			return null;
		int comps[] = DateUtils.dateComponents(requestedDate);
		return new Integer(comps[1]);
	}
	
	public boolean handled() {
		return this.getAction() != null;
	}
	
	public boolean isViewable() {
		return (this.getStatus() != null
				&& this.getWorkflow() != null
				&& this.getWorkflow().isArchived() == false
				&& this.getWorkflow().isDeleted() == false
				&& this.getWorkflow().isHidden() == false);
	}
	
	public boolean equalsTo(Request request) {
		if(this.getWorkflow() == null || request.getWorkflow() == null)
			return false;
		Workflow w1 = this.getWorkflow();
		Workflow w2 = request.getWorkflow();
		if(w1.getId().compareTo(w2.getId()) != 0)
			return false;
		if(this.getActivityClassName() == null || request.getActivityClassName() == null)
			return false;
		String c1 = this.getActivityClassName();
		String c2 = request.getActivityClassName();
		String i1 = this.getActivityRefId();
		String i2 = request.getActivityRefId();
		return (c1.equals(c2) && i1.equals(i2));
	}
	
	public boolean isMissingActivityClass() {
		return (StringUtil.nullOrEmptyOrBlankString(activityClassName));
	}
	
	public boolean isMissingActor() {
		return (this.requestTo == null);
	}
	
	public boolean isNotSentTo(User user) {
		return (user.getRequests().contains(this) == false);
	}
	
	public List<User> expectedActingUsers() {
		Actor actor = this.getRequestTo();
		List<User> users = ListUtil.list();
		if(actor instanceof Role) {
			users.addAll(((Role) actor).getUsers());
		} else if(actor instanceof User) {
			users.add((User)actor);
		}
		return users;
	}
}
