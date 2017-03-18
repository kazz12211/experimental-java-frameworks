package workflow.controller;

import workflow.model.Activity;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.wizard.component.WizardFrameContent;
import ariba.ui.wizard.core.Wizard;
import ariba.util.fieldvalue.FieldValue;

public abstract class WizardContentView extends AWComponent {

	public WizardFrameContent getFrameContent() {
		AWComponent parent = null;
		for(parent = this.parent(); parent != null ; parent = parent.parent()) {
			if(parent instanceof WizardFrameContent) {
				return (WizardFrameContent) parent;
			}
		}
		return null;
	}
	
	
	public Context getContext() {
		WizardFrameContent content = this.getFrameContent();
		if(content != null && content.getContext() instanceof Context) {
			return (Context) content.getContext();
		}
		return null;
	}
	
	public Wizard getWizard() {
		WizardFrameContent content = this.getFrameContent();
		if(content != null) {
			return (Wizard) content.getFrame().getWizard();
		}
		return null;
	}
	
	public boolean isInWorkflowWizard() {
		return (this.getWizard() instanceof WorkflowWizard);
	}
	public boolean isInActivityWizard() {
		return (this.getWizard() instanceof ActivityWizard);
	}
	
	public boolean forceDisabled() {
		if(this.hasBinding("forceDisabled"))
			return this.booleanValueForBinding("forceDisabled");
		return false;
	}

	public boolean editable() {
		if(this.forceDisabled())
			return false;
		
		WizardFrameContent frameContent = getFrameContent();
		if(frameContent instanceof AbstractFrameContent)
			return ((AbstractFrameContent) frameContent).editable();
		
		return true;
	}
	
	public Workflow getWorkflow() {
		if(this.isInActivityWizard()) {
			return (Workflow) FieldValue.getFieldValue(this.getContext(), "request.workflow");
		} else if(this.isInWorkflowWizard()) {
			return (Workflow) FieldValue.getFieldValue(this.getContext(), "workflow");
		} else if(this.hasBinding("activity")) {
			Activity activity = (Activity) this.valueForBinding("activity");
			if(activity != null && activity.getRequest() != null)
				return activity.getRequest().getWorkflow();
		} else if(this.hasBinding("workflow")) {
			return (Workflow) valueForBinding("workflow");
		}
		return null;
	}

	@Override
	public boolean isStateless() { return false; }
}
