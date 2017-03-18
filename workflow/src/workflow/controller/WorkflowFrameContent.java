package workflow.controller;


public abstract class WorkflowFrameContent extends AbstractFrameContent {
	
	public WorkflowWizard getWorkflowWizard() {
		return (WorkflowWizard) this.getWizard();
	}
	
	@Override
	public Object getValidationTarget() {
		return getWorkflowWizard().getWorkflowContext().getWorkflow();
	}

	@Override
	protected boolean needsValidation() {
		if(this.isInActivityWizard())
			return false;
		return super.needsValidation();
	}

	@Override
	protected void reviewFrameState() {		
	}

	@Override
	public boolean editable() {
		if(this.isInActivityWizard())
			return false;
		if(this.getWorkflowWizard().isInspectMode())
			return false;
		return true;
	}
}
