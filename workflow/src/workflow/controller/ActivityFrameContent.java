package workflow.controller;

import ariba.ui.wizard.core.Wizard;
import ariba.ui.wizard.core.WizardAction;
import ariba.ui.wizard.core.WizardActionTarget;


public abstract class ActivityFrameContent extends AbstractFrameContent {

	public ActivityWizard getActivityWizard() {
		return (ActivityWizard) this.getWizard();
	}
	
	
	@Override
	protected void reviewFrameState() {		
	}


	protected boolean checkErrorsAndEnableDisplayForReject(WizardAction action) {
		if(validator != null) {
			validator.validateInContent(this, requestContext());
			return errorManager().checkErrorsAndEnableDisplay();
		}
		return false;
	}
	
	@Override
	public WizardActionTarget actionClicked(WizardAction action) {
		Wizard wizard = this.getWizard();
		if(wizard instanceof ActivityWizard && action.getName().equals(ActivityWizard.REJECT_ACTION)) {
			if(this.checkErrorsAndEnableDisplayForReject(action) && this.needsValidation())
				return wizard.getCurrentActionTarget();
			return ((ActivityWizard)wizard).rejectAction();
		} else if(wizard instanceof ActivityWizard && action.getName().equals(ActivityWizard.RETURN_ACTION)) {
			((ActivityWizard) wizard).willReturn(this.requestContext());
			return ((ActivityWizard)wizard).returnAction();
		}
		return super.actionClicked(action);
	}
	
	@Override
	public Object getValidationTarget() {
		return getActivityWizard().getActivityContext().getActivity();
	}

	@Override
	public boolean editable() {
		if(this.isInWorkflowWizard())
			return false;
		if(this.getActivityWizard().isInspectMode())
			return false;
		return true;
	}
}
