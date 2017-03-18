package workflow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import core.BaseObject;
import workflow.model.listener.AttachmentListener;
import ariba.ui.meta.persistence.ObjectContext;

@Entity
@EntityListeners(AttachmentListener.class)
public class Attachment extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	
	Date date;
	byte[] data;
	String fileName;
	String contentType;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Long getId() {
		return id;
	}

	public Attachment copy() {
		Attachment copy = ObjectContext.get().create(this.getClass());
		copy.setContentType(this.getContentType());
		copy.setData(this.getData().clone());
		copy.setDate(this.getDate());
		copy.setFileName(this.getFileName());
		return copy;
	}
	
}
