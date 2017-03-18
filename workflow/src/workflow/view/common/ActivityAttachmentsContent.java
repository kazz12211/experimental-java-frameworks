package workflow.view.common;

import workflow.model.Activity;
import workflow.model.ActivityAttachment;
import workflow.model.Attachment;
import ariba.util.fieldvalue.FieldValue;

public class ActivityAttachmentsContent extends AttachmentsContent {

	@Override
	public void add() {
		ActivityAttachment attachment = (ActivityAttachment) this.createAttachment();
		Activity activity = this.getActivity();
		if(activity != null) {
			activity.addAttachment(attachment);
		}
	}

	@Override
	public void remove() {
		Activity activity = this.getActivity();
		if(activity != null) {
			activity.removeAttachment((ActivityAttachment) currentAttachment);
		}
	}

	public Activity getActivity() {
		if(this.hasBinding("activity")) {
			return (Activity) valueForBinding("activity");
		} else {
			return (Activity) FieldValue.getFieldValue(this.getContext(), "activity");
		}
	}

	@Override
	protected Class<? extends Attachment> attachmentClass() {
		return ActivityAttachment.class;
	}

}
