package workflow.view.common;

import workflow.model.Attachment;
import workflow.model.Workflow;
import workflow.model.WorkflowAttachment;

public class WorkflowAttachmentsContent extends AttachmentsContent {

	@Override
	public void add() {
		WorkflowAttachment attachment = (WorkflowAttachment) this.createAttachment();
		Workflow workflow = this.getWorkflow();
		if(workflow != null) {
			workflow.addAttachment(attachment);
		}
	}

	@Override
	public void remove() {
		Workflow workflow = this.getWorkflow();
		if(workflow != null) {
			workflow.removeAttachment((WorkflowAttachment) currentAttachment);
		}

	}

	@Override
	protected Class<? extends Attachment> attachmentClass() {
		return WorkflowAttachment.class;
	}

	
}
