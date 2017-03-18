package workflow.controller;

import workflow.WorkflowException;
import workflow.model.ApprovalAction;
import workflow.model.Request;
import workflow.model.User;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.wizard.core.WizardActionTarget;
import ariba.util.fieldvalue.FieldValue;

public abstract class ApprovalWizard extends ActivityWizard {

	public ApprovalWizard(String awzName, User actor, String mode,
			Request model, AWRequestContext requestContext,
			AWResourceManager resourceManager) {
		super(awzName, actor, mode, model, requestContext, resourceManager);
	}
	
	@Override
	protected void prepareForReject() throws WorkflowException {
		ApprovalAction action = (ApprovalAction) this.getActivityContext().getActivity();
		action.setApproved(new Boolean(false));
	}

	@Override
	protected void prepareForReturn() throws WorkflowException {
	}

	@Override
	public WizardActionTarget submitAction() {
		ApprovalAction action = (ApprovalAction) this.getActivityContext().getActivity();
		if(action.getApproved() == null || action.getApproved().booleanValue() == false) {
			return super.rejectAction();
		}
		return super.submitAction();
	}

	protected void checkApproved() {
		ApprovalAction action = (ApprovalAction) FieldValue.getFieldValue(getContext(), "activity");
		if(action.getApproved() == null || action.getApproved().booleanValue() == false) {
			this.setActionState("submitActivity", false);
			this.setActionState("rejectActivity", true);
		} else {
			this.setActionState("submitActivity", true);
			this.setActionState("rejectActivity", false);
		}
	}
	
	@Override
	protected void initUIForNew(AWRequestContext requestContext) {
		super.initUIForNew(requestContext);
		this.checkApproved();
	}

	@Override
	protected void initUIForEdit(AWRequestContext requestContext) {
		super.initUIForEdit(requestContext);
		this.setActionState("rejectActivity", false);
		this.checkApproved();
	}

	@Override
	protected void initUIForInspect(AWRequestContext requestContext) {
		super.initUIForInspect(requestContext);
		this.setActionState("rejectActivity", false);
	}

}
