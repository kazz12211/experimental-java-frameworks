package workflow.controller.helper;

import workflow.WorkflowException;
import workflow.controller.WorkflowManager;
import ariba.util.log.Log;

public abstract class WorkflowManagerHelper {

	protected WorkflowManager manager;
	
	public WorkflowManagerHelper(WorkflowManager manager) {
		this.manager = manager;
	}
	
	protected void throwWorkflowException(int errorCode, String errorStr, Throwable cause) throws WorkflowException {
		WorkflowException e =  new WorkflowException(errorCode, errorStr, cause);
		Log.customer.error(this.getClass().getSimpleName(), e);
		throw e;
	}
}
