package workflow.controller;

import ariba.ui.wizard.component.WizardFrameContent;
import ariba.ui.wizard.core.Wizard;
import ariba.ui.wizard.core.WizardAction;
import ariba.ui.wizard.core.WizardActionTarget;
import ariba.util.fieldvalue.FieldValue;

public abstract class AbstractFrameContent extends WizardFrameContent {

	UIValidator validator;
	
	@Override
	public void init() {
		super.init();
		this.reviewFrameState();
	}
	
	public Wizard getWizard() {
		return this.getFrame().getWizard();
	}
	
	public Context getContext() {
		Wizard wiz = this.getWizard();
		Object context = wiz.getContext();
		if(context instanceof Context)
			return (Context) context;
		return null;
	}
	
	protected boolean needsValidation() {
		Wizard wizard = this.getWizard();
		if(wizard instanceof AbstractWizard) {
			return !((AbstractWizard) wizard).isInspectMode();
		}
		return true;
	}
	
	public boolean isInWorkflowWizard() {
		Wizard wizard = this.getWizard();
		return (wizard instanceof WorkflowWizard);
	}
	public boolean isInActivityWizard() {
		Wizard wizard = this.getWizard();
		return (wizard instanceof ActivityWizard);
	}
	
	// SEE WizardFrameDelegate class
	@Override
	public WizardActionTarget actionClicked(WizardAction action) {
		WizardActionTarget target = this._actionClicked(action);
		this.reviewFrameState();
		return target;
	}
	
	protected WizardActionTarget _actionClicked(WizardAction action) {
		Wizard wizard = this.getWizard();
		if(isInActivityWizard()) {
			if(action.getName().equals(ActivityWizard.SUBMIT_ACTION) && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayForSubmit(action))
					return wizard.getCurrentActionTarget();
				return ((ActivityWizard)wizard).submitAction();
			} else if(action.getName().equals(ActivityWizard.SAVE_ACTION) && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayForSave(action))
					return wizard.getCurrentActionTarget();
				return ((ActivityWizard)wizard).saveAction();
			} else if(action.getName().equals("cancel") || wizard.cancel == action) {
				return ((ActivityWizard)wizard).cancelAction();
			} else if(wizard.next == action && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayToNext(action))
					return wizard.getCurrentActionTarget();
			}
			return super.actionClicked(action);
		} else if(isInWorkflowWizard()) {
			if(action.getName().equals(WorkflowWizard.SAVE_ACTION) && this instanceof WorkflowFrameContent && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayForSave(action))
					return wizard.getCurrentActionTarget();
				return ((WorkflowWizard)wizard).saveAction();
			} else if(action.getName().equals(WorkflowWizard.SUBMIT_ACTION) && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayForSubmit(action))
					return wizard.getCurrentActionTarget();
				return ((WorkflowWizard)wizard).submitAction();
			} else if(action.getName().equals("cancel") || wizard.cancel == action) {
				return ((WorkflowWizard)wizard).cancelAction();
			} else if(wizard.next == action && this.needsValidation()) {
				if(this.checkErrorsAndEnableDisplayToNext(action))
					return wizard.getCurrentActionTarget();
			}
			return super.actionClicked(action);
		}
		return super.actionClicked(action);
	}

	protected boolean checkErrorsAndEnableDisplayForSave(WizardAction action) {
		if(validator != null) {
			validator.validateInContent(this, requestContext());
			return errorManager().checkErrorsAndEnableDisplay();
		}
		return false;
	}
	
	protected boolean checkErrorsAndEnableDisplayForSubmit(WizardAction action) {
		if(validator != null) {
			validator.validateInContent(this, requestContext());
			return errorManager().checkErrorsAndEnableDisplay();
		}
		return false;
	}
	protected boolean checkErrorsAndEnableDisplayToNext(WizardAction action) {
		if(validator != null) {
			validator.validateInContent(this, requestContext());
			return errorManager().checkErrorsAndEnableDisplay();
		}
		return false;
	}

	public Object getValidationTargetValue(String key) {
		Object target = this.getValidationTarget();
		return FieldValue.getFieldValue(target, key);	
	}
	
	public abstract Object getValidationTarget();

	protected abstract void reviewFrameState();
	
	@Override
	public boolean isStateless() { return false; }
	
	protected void setValidator(UIValidator validator) {
		this.validator = validator;
	}
	
	public abstract boolean editable();
}
