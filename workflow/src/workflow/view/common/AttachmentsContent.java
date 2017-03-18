package workflow.view.common;

import workflow.controller.WizardContentView;
import workflow.model.Attachment;
import ariba.ui.aribaweb.core.AWErrorInfo;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponse;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.util.AWContentType;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;

public abstract class AttachmentsContent extends WizardContentView {

	private static final long MAX_FILE_SIZE		= 2 * 1024 * 1024;	// 2 megs
	
	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public Attachment currentAttachment;

	public abstract void add();
	public abstract void remove();
	protected abstract Class<? extends Attachment> attachmentClass();
	
	protected Attachment createAttachment() {
		return ObjectContext.get().create(this.attachmentClass());
	}

	public void update() {
		currentAttachment.setFileName(null);
	}
	
	public void upload() {
		errorManager().checkErrorsAndEnableDisplay();
	}
	
	public long maxFileSize() {
		return MAX_FILE_SIZE;
	}
	
	public String maxFileSizeString() {
		String maxFileSize = Long.toString(MAX_FILE_SIZE);
		return maxFileSize;
	}
	
	public void setFileSizeExceeded(boolean isError) {
		if(isError) {
			AWErrorInfo error = new AWErrorInfo(
					currentAttachment,
					"fileName",
					null,
					AWLocal.localizedJavaString(1, "Maximum file size exceeded", AttachmentsContent.class, requestContext()),
					maxFileSizeString(),
					false);
			recordValidationError(error);
			currentAttachment.setContentType(null);
			currentAttachment.setData(null);
			currentAttachment.setFileName(null);
			currentAttachment.setDate(null);
		}
	}
	
	public AWResponseGenerating doDownload() {
		AWResponse fileResponse = application().createResponse();
		fileResponse.setContentType(AWContentType.contentTypeNamed(currentAttachment.getContentType()));
		fileResponse.setHeaderForKey("attachment; filename=\"" + currentAttachment.getFileName() + "\"", "Content-Disposition");
		fileResponse.setContent(currentAttachment.getData());
		return fileResponse;
	}
	

}
