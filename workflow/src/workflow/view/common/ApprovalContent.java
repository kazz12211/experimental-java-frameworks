package workflow.view.common;

import workflow.controller.AbstractActivityContent;
import workflow.controller.ActivityWizard;
import workflow.model.ApprovalAction;
import ariba.ui.aribaweb.core.AWResponseGenerating;

public class ApprovalContent extends AbstractActivityContent {

	
	private void checkApproved() {
		if(this.isInActivityWizard()) {
			ActivityWizard wizard = (ActivityWizard) this.getWizard();
			ApprovalAction action = (ApprovalAction) this.getActivity();
			if(action.getApproved() == null || action.getApproved().booleanValue() == false) {
				wizard.setActionState("submitActivity", false);
				wizard.setActionState("rejectActivity", true);
			} else {
				wizard.setActionState("submitActivity", true);
				wizard.setActionState("rejectActivity", false);
			}
		}
	}
	
	public AWResponseGenerating approvedChanged() {
		this.checkApproved();
		return null;
	}

}
