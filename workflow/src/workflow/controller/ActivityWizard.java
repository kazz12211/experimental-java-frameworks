package workflow.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import workflow.WorkflowException;
import workflow.model.Activity;
import workflow.model.Error;
import workflow.model.Counts;
import workflow.model.Request;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.wizard.core.WizardActionTarget;
import ariba.util.log.Log;

public abstract class ActivityWizard extends AbstractWizard {
	
	public static final String SAVE_ACTION = "saveActivity";
	public static final String SUBMIT_ACTION = "submitActivity";
	public static final String REJECT_ACTION = "rejectActivity";
	public static final String RETURN_ACTION = "returnActivity";
	
	public ActivityWizard(
			String awzName,
			User actor,
			String mode,
			Request request,
			AWRequestContext requestContext,
			AWResourceManager resourceManager) {
		super(awzName, mode, new ActivityContext(), requestContext, resourceManager);
		
		Activity activity;
		try {
			if(ActivityWizard.MODE_NEW.equals(mode)) {
				activity = workflowManager.createActivityForRequest(request, actor);
			} else {
				activity = request.getAction();
			}
			getActivityContext().setModel(activity);
			getActivityContext().setRequest(request);
			getActivityContext().setActor(actor);
			this.initialize(requestContext);
			this.initializeUI(requestContext);
		} catch (WorkflowException e) {
			Log.customer.error("ActivityWizard: could not instantiate the activity.", e);
		}
	}
	
	public ActivityContext getActivityContext() {
		return (ActivityContext) super.getContext();
	}
		
	public Request getRequest() {
		return (Request) getActivityContext().get("model");
	}

	
	@Override
	protected void initUIForNew(AWRequestContext requestContext) {
		this.setActionState(SAVE_ACTION, false);
		this.setActionState(SUBMIT_ACTION, true);
		this.setActionState(WorkflowWizard.SAVE_ACTION, false);
		this.setActionState(WorkflowWizard.SUBMIT_ACTION, false);
	}

	@Override
	protected void initUIForEdit(AWRequestContext requestContext) {
		this.setActionState(SAVE_ACTION, true);
		this.setActionState(SUBMIT_ACTION, false);
		this.setActionState(WorkflowWizard.SAVE_ACTION, false);
		this.setActionState(WorkflowWizard.SUBMIT_ACTION, false);
	}

	@Override
	protected void initUIForInspect(AWRequestContext requestContext) {
		this.setActionState(SAVE_ACTION, false);
		this.setActionState(RETURN_ACTION, false);
		this.setActionState(SUBMIT_ACTION, false);
		this.setActionState(WorkflowWizard.SAVE_ACTION, false);
		this.setActionState(WorkflowWizard.SUBMIT_ACTION, false);
	}

	@Override
	protected void initForNew(AWRequestContext requestContext) {		
	}

	@Override
	protected void initForEdit(AWRequestContext requestContext) {		
	}

	@Override
	protected void initForInspect(AWRequestContext requestContext) {		
	}

	@Override
	protected void prepareForSubmit() throws WorkflowException {
		Activity activity = getActivityContext().getActivity();
		activity.setActor(getActivityContext().getActor());
	}
	
	protected abstract void prepareForReject() throws WorkflowException;
	protected abstract void prepareForReturn() throws WorkflowException;
	
	
	protected void prepareForSave() throws WorkflowException {
		Activity activity = getActivityContext().getActivity();
		activity.setActor(getActivityContext().getActor());
	}

	protected Request submitRequest() throws WorkflowException {
		Request request = this.getActivityContext().getRequest();
		Activity activity = getActivityContext().getActivity();
		return workflowManager.submitAndSave(request, activity);
	}
	
	@Override
	public WizardActionTarget cancelAction() {
		if(this.isNewMode()) {
			try {
				Activity activity = getActivityContext().getActivity();
				
				Log.customer.debug("ActivityWizard: cancelling new activity (" + activity.getClass().getName() + ")");
	
				User actor = activity.getActor();
				actor.removeActivity(activity);
				ObjectContext.get().remove(activity);
			} catch (EntityNotFoundException ene) {
				Log.customer.error("***** FIX ME *****", ene);
			}
		}
		return super.cancelAction();
	}
	
	protected Request saveRequest() throws WorkflowException {
		Request request = this.getActivityContext().getRequest();
		return workflowManager.saveAndSave(request);
	}
	
	public WizardActionTarget saveAction() {
		Error error = null;
		Workflow workflow = this.getActivityContext().getWorkflow();
		Request request = null;
		
		try {
			this.prepareForSave();
			request = this.saveRequest();
		} catch (Exception e) {
			error = workflowManager.createError(workflow, e);
			Log.customer.error("ActivityWizard: could not save action", e);
		}
		
		if(error != null && request != null) {
			workflowManager.stopWithErrorAndSave(workflow, error);
			return super.closeAction();
		}
		return super.closeAction();
	}
	
	@Override
	public WizardActionTarget submitAction() {
		Request request = null;
		Error error = null;
		Workflow workflow = this.getActivityContext().getWorkflow();

		request = this.getActivityContext().getRequest();
		if(workflowManager.isEditable(request) && request.getAction() != null) {
			return saveAction();
		}
		
		try {
			this.prepareForSubmit();
			request = this.submitRequest();
		} catch (WorkflowException e) {
			error = workflowManager.createError(workflow, e);
			Log.customer.error("ActivityWizard: could not submit action", e);
		}
		
		if(error != null && request != null) {
			workflowManager.stopWithErrorAndSave(workflow, error);
			return super.closeAction();
		}

		List<Request> requests = null;
		try {
			requests = workflowManager.submittedAndSave(request);
		} catch (Exception e) {
			error = workflowManager.createError(workflow, e);
			Log.customer.error("ActivityWizard: failed to submit requests", e);
		}

		if (error != null) {
			workflowManager.stopWithErrorAndSave(workflow, error);
			return super.closeAction();
		}
		
		if(!request.getWorkflow().done() && requests == null || requests.isEmpty()) {
			try {
				workflowManager.completeAndSave(workflow);
			} catch (WorkflowException e) {
				error = workflowManager.createError(workflow, e);
				Log.customer.error("ActivityWizard: failed to complete workflow id " + workflow.getId(), e);
			}
			if (error != null) {
				workflowManager.stopWithErrorAndSave(workflow, error);
			}
		}
		return super.closeAction();
	}
	
	public WizardActionTarget rejectAction() {
		Request request = this.getActivityContext().getRequest();
		Activity activity = this.getActivityContext().getActivity();
		Workflow workflow = this.getActivityContext().getWorkflow();
		Error error = null;
		try {
			this.prepareForReject();
			workflowManager.rejectAndSave(request, activity);
			workflowManager.rejectAndSave(workflow);
		} catch (WorkflowException e) {
			error = workflowManager.createError(workflow, e);
			Log.customer.error("ActivityWizard: could not reject action of workflow id " + workflow.getId(), e);
		}
		if(error != null) {
			workflowManager.stopWithErrorAndSave(workflow, error);
		}
		return closeAction();
	}
	
	public WizardActionTarget returnAction() {
		Workflow workflow = this.getActivityContext().getWorkflow();
		Error error = null;
		try {
			this.prepareForReturn();
			workflowManager.withdraw(workflow);
			ObjectContext.get().save();
			workflowManager.withdrawn(workflow);
			if(workflow.getCreator() != null)
				Counts.createOrUpdate(workflow.getCreator(), ObjectContext.get());
		} catch (Exception e) {
			error = workflowManager.createError(workflow, e);
			Log.customer.error("ActivityWizard: could not return workflow id " + workflow.getId(), e);
		}
		if(error != null) {
			workflowManager.stopWithErrorAndSave(workflow, error);
		}
		return closeAction();
		
	}

	public void willReturn(AWRequestContext requestContext) {
		Workflow workflow = this.getActivityContext().getWorkflow();
		workflow.willWithdraw(requestContext);
	}
}
