package workflow.controller;

import workflow.model.Activity;
import workflow.model.Request;
import workflow.model.Workflow;

public interface WorkflowManagerCallback {

	public void onCreate(Workflow workflow);
	public void onSave(Workflow workflow);
	public void onSubmit(Workflow workflow);
	public void onComplete(Workflow workflow);
	public void onReject(Workflow workflow);
	public void onExpire(Workflow workflow);
	public void onWithdraw(Workflow workflow);
	public void onDelete(Workflow workflow);
	public void onUndelete(Workflow workflow);
	public void onSubmit(Request request);
	public void onRequest(Request request);
	public void onReject(Request request);
	public void onExpire(Request request);
	public void onCreate(Activity activity);
	public void onChange(Request request);
	public void onError(Workflow workflow);
	
}
