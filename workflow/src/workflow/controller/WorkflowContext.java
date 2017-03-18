package workflow.controller;

import workflow.model.Workflow;


public class WorkflowContext extends AbstractContext {

	private Workflow workflow;
		
	public void setModel(Object workflow) {
		if(workflow instanceof Workflow)
			this.workflow = (Workflow)workflow;
	}
	public Object getModel() {
		return workflow;
	}

	public Workflow getWorkflow() {
		return (Workflow) getModel();
	}
	
}
