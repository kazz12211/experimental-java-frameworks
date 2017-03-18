package workflow.controller;

import workflow.WorkflowException;
import workflow.model.User;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.wizard.component.ComponentActionTarget;
import ariba.ui.wizard.core.Wizard;
import ariba.ui.wizard.core.WizardAction;
import ariba.ui.wizard.core.WizardActionTarget;
import ariba.ui.wizard.core.WizardStep;
import ariba.util.fieldvalue.FieldValue;

public abstract class AbstractWizard extends Wizard {

	public static final String MODE_EDIT = "edit";
	public static final String MODE_INSPECT = "inspect";
	public static final String MODE_NEW = "new";

	private AWComponent caller;
	private boolean fromModule = false;
	protected String mode;
	protected User currentUser;
	protected WorkflowManager workflowManager;
	
	public AbstractWizard(String awzName, String mode, Context context, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super(awzName, context, resourceManager);
		this.mode = mode;
		this.currentUser = (User) requestContext.session().getFieldValue("user");
		this.workflowManager = (WorkflowManager) FieldValue.getFieldValue(requestContext.session(), "workflowManager");
	}
	
	public AbstractWizard(String awzName, String mode, Object model, Context context, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super(awzName, context, resourceManager);
		this.mode = mode;
		this.currentUser = (User) requestContext.session().getFieldValue("user");
		this.workflowManager = (WorkflowManager) FieldValue.getFieldValue(requestContext.session(), "workflowManager");
		context.setModel(model);
		this.initialize(requestContext);
		this.initializeUI(requestContext);
	}

	public AWComponent getCaller() {
		return caller;
	}
	public void setCaller(AWComponent caller) {
		this.caller = caller;
	}
	public void setFromModule(boolean flag) {
		this.fromModule = flag;
	}
	
	public boolean isFromModule() {
		return fromModule;
	}

	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
	public boolean isEditMode() {
		return AbstractWizard.MODE_EDIT.equals(mode);
	}
	public boolean isInspectMode() {
		return AbstractWizard.MODE_INSPECT.equals(mode);
	}
	public boolean isNewMode() {
		return AbstractWizard.MODE_NEW.equals(mode);
	}

	protected abstract void localize(AWRequestContext requestContext);
	
	protected void initializeUI(AWRequestContext requestContext) {
		this.localize(requestContext);
		
		if(this.isEditMode()) {
			initUIForEdit(requestContext);
		} else if(this.isInspectMode()) {
			initUIForInspect(requestContext);
		} else if(this.isNewMode()) {
			initUIForNew(requestContext);
		}
	}

	protected void initialize(AWRequestContext requestContext) {
		if(this.isEditMode()) {
			initForEdit(requestContext);
		} else if(this.isInspectMode()) {
			initForInspect(requestContext);
		} else if(this.isNewMode()) {
			initForNew(requestContext);
		}
	}
	
	protected abstract void initUIForNew(AWRequestContext requestContext);
	protected abstract void initUIForEdit(AWRequestContext requestContext);
	protected abstract void initUIForInspect(AWRequestContext requestContext);
	
	protected abstract void initForNew(AWRequestContext requestContext);
	protected abstract void initForEdit(AWRequestContext requestContext);
	protected abstract void initForInspect(AWRequestContext requestContext);

	protected abstract void prepareForSubmit() throws WorkflowException;
	
	public WizardActionTarget closeAction() {
		ComponentActionTarget target = new ComponentActionTarget(this, this.getCaller(), true);
		return target;
	}

	public WizardActionTarget cancelAction() {
		return closeAction();
	}
	
	public abstract WizardActionTarget submitAction();
		
	public User getCurrentUser() {
		return currentUser;
	}
	
	public WizardAction setActionState(String actionName, boolean flag) {
		WizardAction action = this.getActionWithName(actionName);
		if(action != null) {
			action.setEnabled(flag);
			return action;
		}
		return null;
	}
	
	public WizardStep removeStep(String stepName) {
		WizardStep step = this.getStepWithName(stepName);
		if(step != null) {
			this.removeStep(step);
			return step;
		}
		return null;
	}

	public WizardStep hideStep(String stepName) {
		WizardStep step = this.getStepWithName(stepName);
		if(step != null) {
			step.setVisible(false);
			return step;
		}
		return null;
	}
	public WizardStep showStep(String stepName) {
		WizardStep step = this.getStepWithName(stepName);
		if(step != null) {
			step.setVisible(true);
			return step;
		}
		return null;
	}
}
