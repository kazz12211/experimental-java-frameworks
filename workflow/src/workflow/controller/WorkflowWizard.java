package workflow.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import workflow.WorkflowException;
import workflow.model.Error;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.widgets.MessageBanner;
import ariba.ui.wizard.core.WizardActionTarget;
import ariba.util.log.Log;

public abstract class WorkflowWizard extends AbstractWizard {

	public static final String SAVE_ACTION = "saveWorkflow";
	public static final String SUBMIT_ACTION = "submitWorkflow";
	
	public WorkflowWizard(String awzName, User creator, User requester, String mode, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super(awzName, mode, new WorkflowContext(), requestContext, resourceManager);
		try {
			Workflow workflow = workflowManager.createWorkflow(this.workflowClass(), creator, requester, this.getWorkflowName(), null);
			this.getWorkflowContext().setModel(workflow);
			this.initialize(requestContext);
			this.initializeUI(requestContext);
		} catch (WorkflowException e) {
			Log.customer.error("WorkflowWizard: could not instantiate the workflow object", e);
		}
	}
	
	public WorkflowWizard(String awzName, String mode, Workflow model, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super(awzName, mode, model, new WorkflowContext(), requestContext, resourceManager);
	}
	
	public WorkflowWizard(String awzName, User creator, User requester, Workflow workflow, String title, String mode, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super(awzName, mode, new WorkflowContext(), requestContext, resourceManager);
		try {
			Workflow w = workflowManager.factory.copyWorkflow(workflow, creator);
			w.setTitle(title);
			this.getWorkflowContext().setModel(w);
			this.initialize(requestContext);
			this.initializeUI(requestContext);
		} catch (WorkflowException e) {
			Log.customer.error("WorkflowWizard: could not clone the workflow object", e);
		}
		
	}

	public WorkflowContext getWorkflowContext() {
		return (WorkflowContext)super.getContext();
	}
	
	
	protected abstract String getWorkflowName();
	
	public Workflow getWorkflow() {
		return (Workflow) getWorkflowContext().getWorkflow();
	}
	
	protected abstract void prepareForSave() throws WorkflowException;

	protected abstract Class<? extends Workflow> workflowClass();
	
	@Override
	public WizardActionTarget cancelAction() {
		if(this.isNewMode()) {
			try {
				Log.customer.debug("WorkflowWizard: cancelling new workflow (" + this.workflowClass().getName() + ")");
				Workflow workflow = this.getWorkflow();
				User creator = workflow.getCreator();
				creator.removeWorkflow(workflow);
				ObjectContext.get().remove(workflow);
			} catch (EntityNotFoundException ene) {
				Log.customer.error("***** FIX ME *****", ene);
			}
		}
		return super.cancelAction();
	}

	public WizardActionTarget saveAction() {
		try {
			prepareForSave();
			workflowManager.saveAndSave(this.getWorkflow());
		} catch (WorkflowException e) {
			MessageBanner.setMessage(e.getErrorDescription(), this.getCaller().session());
		}

		return super.closeAction();
	}
	
	public WizardActionTarget submitAction() {
		Error error = null;

		try {
			this.prepareForSubmit();
			workflowManager.submitAndSave(this.getWorkflow());
		} catch (WorkflowException e) {
			error = workflowManager.createError(getWorkflow(), e);
			Log.customer.error("WorkflowWizard: could not submit workflow id " + this.getWorkflow().getId(), e);
		}
		if(error != null && getWorkflow() != null) {
			workflowManager.stopWithErrorAndSave(getWorkflow(), error);
			return super.closeAction();
		}
		
		List<Request> requests = null;
		try {
			requests = workflowManager.submittedAndSave(this.getWorkflow(), this.getCurrentUser());
		} catch (Exception e) {
			error = workflowManager.createError(getWorkflow(), e);
			Log.customer.error("WorkflowWizard: could not send requests of workflow id " + this.getWorkflow().getId(), e);
		}
		
		if(error != null) {
			workflowManager.stopWithErrorAndSave(getWorkflow(), error);
			return super.closeAction();
		}
		

		if(requests == null || requests.isEmpty()) {
			try {
				workflowManager.completeAndSave(this.getWorkflow());
			} catch (WorkflowException e) {
				error = workflowManager.createError(getWorkflow(), e);
				Log.customer.error("WorkflowWizard: could not complete workflow id " + this.getWorkflow().getId(), e);
			}
			if(error != null) {
				workflowManager.stopWithErrorAndSave(getWorkflow(), error);
			}
		}
		return super.closeAction();
	}
	
	
	@Override
	protected void initUIForNew(AWRequestContext requestContext) {
		this.setActionState(SAVE_ACTION, true);
		this.setActionState(SUBMIT_ACTION, true);
		this.setActionState(ActivityWizard.SAVE_ACTION, false);
		this.setActionState(ActivityWizard.SUBMIT_ACTION, false);
		this.setActionState(ActivityWizard.REJECT_ACTION, false);
		this.setActionState(ActivityWizard.RETURN_ACTION, false);

	}
			
	@Override 
	protected void initUIForEdit(AWRequestContext requestContext) {
		Workflow workflow = this.getWorkflowContext().getWorkflow();
		Status status = null;
		if(workflow != null) {
			status = workflow.getStatus();
		}
		if(status != null) {
			if(status.getCode().equals(Status.SAVED)) {
				this.setActionState(SAVE_ACTION, true);
				this.setActionState(SUBMIT_ACTION, true);
			} else if(status.getCode().equals(Status.SUBMITTED)) {
				this.setActionState(SAVE_ACTION, false);
				this.setActionState(SUBMIT_ACTION, false);
			} else {
				this.setActionState(SAVE_ACTION, false);
				this.setActionState(SUBMIT_ACTION, false);
			}
		}
		this.setActionState(ActivityWizard.SAVE_ACTION, false);
		this.setActionState(ActivityWizard.SUBMIT_ACTION, false);
		this.setActionState(ActivityWizard.REJECT_ACTION, false);
		this.setActionState(ActivityWizard.RETURN_ACTION, false);
	}
	
	@Override
	protected void initUIForInspect(AWRequestContext requestContext) {
		this.setActionState(SAVE_ACTION, false);
		this.setActionState(SUBMIT_ACTION, false);
		this.setActionState(ActivityWizard.SAVE_ACTION, false);
		this.setActionState(ActivityWizard.SUBMIT_ACTION, false);
		this.setActionState(ActivityWizard.REJECT_ACTION, false);
		this.setActionState(ActivityWizard.RETURN_ACTION, false);
	}
}
