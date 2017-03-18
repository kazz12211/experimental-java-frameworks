package workflow.controller.helper;

import java.util.Date;
import java.util.List;

import core.util.ListUtils;
import workflow.WorkflowError;
import workflow.WorkflowException;
import workflow.controller.Trace;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.WorkflowDef;
import workflow.controller.rule.flow.ActivityRef;
import workflow.controller.rule.flow.RuleDef;
import workflow.model.Error;
import workflow.model.Folder;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.Transition;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public class WorkflowHandler extends WorkflowManagerHelper {

	public WorkflowHandler(WorkflowManager manager) {
		super(manager);
	}
	
	public List<Request> submitted(Workflow workflow, User submitter) throws Exception {		
		WorkflowDef workflowDef = manager.rules.workflowDefForModel(workflow);
		if (workflowDef == null) {
			String errorStr = "WorkflowHandler: No workflow definition for '"
					+ workflow.getName() + "'.";
			Log.customer.warn(errorStr);
			Trace.writeLog(errorStr);
			String format = WorkflowError.getDescription(WorkflowError.NO_WORKFLOW_DEF);
			String message = Fmt.S(format, workflow.getName());
			Trace.writeLog("WorkflowHandler: throwing " + message);
			this.throwWorkflowException(WorkflowError.NO_WORKFLOW_DEF, Fmt.S(format, workflow.getName()), null);
		}

		RuleDef rule = manager.rules.ruleDefForModel(workflow);
		if (rule == null) {
			String errorStr = "WorkflowHandler: No rule definition for '"
					+ workflow.getName() + "'.";
			Log.customer.warn(errorStr);
			Trace.writeLog(errorStr);
			//String format = WorkflowError.getDescription(WorkflowError.NO_RULE_DEF);
			//this.throwWorkflowException(WorkflowError.NO_RULE_DEF, Fmt.S(format, workflow.getName()), null);
		}

		List<Request> requests = ListUtils.list();
		if(rule != null) {
			Log.customer.debug("WorkflowHandler: workflow (" + workflow.getClassName() + ") submitted. Finding first activity refererences");
			Trace.writeLog("WorkflowHandler: workflow (" + workflow.getClassName() + ") submitted. Finding first activity refererences");
			List<ActivityRef> activities = rule.getFirstActivityRefs(workflow);
			for (ActivityRef act : activities) {
				Log.customer.debug("WorkflowHandler: submitting request for activity (" + act + ")");
				Trace.writeLog("WorkflowHandler: submitting request for activity (" + act + ")");
				List<Request> rs = manager.submitAndSave(workflow, act.getId());
				if (ListUtils.nullOrEmpty(rs) == false) {
					for(Request request : rs) {
						requests.add(request);
					}
				}
			}
		}
		return requests;

	}
		
	public Workflow withdraw(Workflow workflow) {
		workflow.willWithdraw();
		workflow.setSubmittedDate(null);
		workflow.setStatusCode(Status.SAVED);
		this.clearTransitions(workflow);
		this.clearRequests(workflow);
		return workflow;
	}
	
	private void clearTransitions(Workflow workflow) {
		List<Transition> transitions = ListUtils.list();
		transitions.addAll(workflow.getTransitions());
		for(Transition transition : transitions) {
			workflow.removeTransition(transition);
		}
	}
	
	private void clearRequests(Workflow workflow) {
		List<Request> requests = ListUtils.list();
		requests.addAll(workflow.getRequests());
		for(Request request : requests) {
			request.removeFromActor();
			workflow.removeRequest(request);
		}
	}
	
	public Workflow remove(User owner, Workflow workflow) {
		workflow.setDeleted(new Boolean(true));
		workflow.setDeletedDate(new Date());
		return workflow;
	}

	public Workflow unremove(User user, Workflow workflow) {
		workflow.setDeleted(new Boolean(false));
		workflow.setDeletedDate(null);
		return workflow;
	}

	public Workflow complete(Workflow workflow) throws WorkflowException {
		try {
			workflow.setStatusCode(Status.COMPLETED);
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.COMPLETE_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.COMPLETE_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
		return workflow;
	}
	
	public Workflow stopWithError(Workflow workflow, Error error) {
		workflow.setStatusCode(Status.ERROR);
		this.recordError(workflow, error);
		manager.notifier.error(workflow, error);
		return workflow;
	}
	
	public Workflow reject(Workflow workflow) throws WorkflowException {
		try {
			workflow.setStatusCode(Status.REJECTED);
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.REJECT_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.REJECT_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
		return workflow;
	}

	public Workflow save(Workflow workflow) throws WorkflowException {
		try {
			if(workflow.getRequester() == workflow.getCreator())
				workflow.setRequester(null);
			workflow.setStatusCode(Status.SAVED);
			workflow.setHidden(new Boolean(false));
			if(workflow.getCreatedDate() == null)
				workflow.setCreatedDate(new Date());
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SAVE_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.SAVE_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
		return workflow;
	}
	
	public Workflow submit(Workflow workflow) throws WorkflowException {
		try {
			if(workflow.getRequester() == workflow.getCreator())
				workflow.setRequester(null);
			workflow.setStatusCode(Status.SUBMITTED);
			workflow.setHidden(new Boolean(false));
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SUBMIT_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.SUBMIT_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
		return workflow;
	}
	
	public Error createError(Workflow workflow, Exception e) {
		Error error = null;
		if(e instanceof WorkflowException) {
			WorkflowException we = (WorkflowException)e;
			error = ObjectContext.get().create(Error.class);
			error.setCode(we.getErrorCode());
			error.setDescription(we.getErrorDescription());
			error.setException(we.getException());
		} else {
			error = ObjectContext.get().create(Error.class);
			error.setCode(WorkflowError.UNCAUGHT_EXCEPTION);
			error.setDescription(WorkflowError.getDescription(WorkflowError.UNCAUGHT_EXCEPTION));
			error.setException(e.getMessage() != null ? e.getMessage() : e.toString());
		}
		return error;
	}
	
	public Error recordError(Workflow workflow, Error error) {
		Log.customer.error("WorkflowHandler: recording error " + error.getDescription() + "(" + error.getCode() + ")");
		try {
			workflow.addError(error);
			ObjectContext.get().save();
		} catch (Exception e) {
			Log.customer.error("WorkflowHandler: recording error failed.", e);
		}
		return error;
	}
	
	public void reallyRemove(Workflow workflow) {
		User creator = workflow.getCreator();
		Folder folder = workflow.getFolder();
		if(creator != null)
			creator.removeWorkflow(workflow);
		if(folder != null)
			folder.removeWorkflow(workflow);
			
		OrphanManipulation.deleteWorkflow(workflow);
	}

	public void expire(Workflow workflow) throws WorkflowException {
		try {
			workflow.setStatusCode(Status.EXPIRED);
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.EXPIRE_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.EXPIRE_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
	}

	public boolean isEditable(Workflow workflow) {
		if(workflow.getStatus().getCode().equals(Status.SAVED))
			return true;
		if(workflow.getStatus().getClass().equals(Status.SUBMITTED)) {
			for(Request request: workflow.getRequests()) {
				if(!request.getStatus().getCode().equals(Status.REQUESTED))
					return false;
			}
			return true;
		}
		return false;
	}

}
