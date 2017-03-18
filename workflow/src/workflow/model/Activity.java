package workflow.model;

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
import javax.persistence.OneToOne;

import workflow.model.listener.ActivityListener;
import core.BaseObject;
import ariba.ui.meta.annotations.Trait.RichText;
import ariba.util.core.ListUtil;

@Entity
@EntityListeners(ActivityListener.class)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Activity extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	@ManyToOne
	User actor;
	Date createdDate;
	Date modifiedDate;
	String name;
	@OneToOne
	Request request;
	@RichText
	@Column(name="comment", length=1024)
	String comment;
	@OneToMany
	List<ActivityAttachment> attachments;


	public User getActor() {
		return actor;
	}
	public void setActor(User actor) {
		this.actor = actor;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Request getRequest() {
		return request;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	
	public Long getId() {
		return id;
	}
		
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<ActivityAttachment> getAttachments() {
		if(attachments == null)
			attachments = ListUtil.list();
		return attachments;
	}
	public void setAttachments(List<ActivityAttachment> attachments) {
		this.attachments = attachments;
	}
	
	public void addAttachment(ActivityAttachment attachment) {
		this.getAttachments().add(attachment);
	}
	public void removeAttachment(ActivityAttachment attachment) {
		this.getAttachments().remove(attachment);
	}
}
