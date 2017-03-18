package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;

import workflow.model.Attachment;

public class AttachmentListener {
	
	@PrePersist
	public void onPrePersist(Object object) {
		Attachment attachment = (Attachment) object;
		if(attachment.getDate() == null)
			attachment.setDate(new Date());
	}

}
