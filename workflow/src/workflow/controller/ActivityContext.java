package workflow.controller;

import workflow.model.Activity;
import workflow.model.Request;
import workflow.model.User;
import workflow.model.Workflow;


public class ActivityContext extends AbstractContext {

	private Request request;
	private Activity activity;
	private User actor;
	
	public void setModel(Object model) {
		this.activity = (Activity) model;
	}
	public Object getModel() {
		return activity;
	}
	
	public Request getRequest() {
		return request;
	}
	
	public Workflow getWorkflow() {
		return this.getRequest().getWorkflow();
	}

	public Activity getActivity() {
		return activity;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	public void setActor(User actor) {
		this.actor = actor;
	}
	public User getActor() {
		return actor;
	}
}
