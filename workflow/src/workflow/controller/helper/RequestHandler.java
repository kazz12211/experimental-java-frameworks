package workflow.controller.helper;

import java.util.List;

import core.util.ListUtils;
import workflow.WorkflowError;
import workflow.WorkflowException;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.WorkflowDef;
import workflow.controller.rule.flow.PathDef;
import workflow.controller.rule.flow.RuleDef;
import workflow.model.Activity;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.Transition;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public class RequestHandler extends WorkflowManagerHelper {

	public RequestHandler(WorkflowManager manager) {
		super(manager);
	}

	public List<Request> requests(Workflow workflow, String activityId) throws WorkflowException {
		ActivityDef activityDef = manager.rules.lookupActivityDef(workflow, activityId);
		return requests(workflow, activityDef);
	}

	public List<Request> requests(Workflow workflow, ActivityDef activityDef) throws WorkflowException {
		try {
			List<Request> requests = manager.createRequests(workflow, activityDef);
			for(Request request : requests) {
				this.addTransition(workflow, null, request);
			}
			ObjectContext.get().save();
			return requests;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SUBMIT_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.SUBMIT_REQUEST_FAILED, 
					Fmt.S(format, activityDef.getName()), 
					e);
		}
		return null;
	}
	
	public List<Request> requests(Workflow workflow, Request source, ActivityDef activityDef) throws WorkflowException {
		try {
			List<Request> requests = manager.createRequests(workflow, activityDef);
			if(ListUtils.nullOrEmpty(requests) == false) {
				for(Request request : requests) {
					this.addTransition(workflow, source, request);
				}
				ObjectContext.get().save();
			}
			return requests;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SUBMIT_SUBSEQUENT_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.SUBMIT_SUBSEQUENT_REQUEST_FAILED, 
					Fmt.S(format, source.getName(), activityDef.getName()), 
					e);
		}
		return null;
	}
	
	private void addTransition(Workflow workflow, Request source, Request destination) {
		if(source == null && destination == null)
			return;
		Transition transition = ObjectContext.get().create(Transition.class);
		transition.setSource(source);
		transition.setDestination(destination);
		workflow.addTransition(transition);
	}
	
	public Request remove(Request request) {
		request.removeFromActor();
		OrphanManipulation.deleteRequest(request);
		return request;
	}
	
	public Request reject(Request request) throws WorkflowException {
		try {
			request.setStatusCode(Status.REJECTED);
			ObjectContext.get().save();
		return request;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.REJECT_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.REJECT_REQUEST_FAILED, 
					Fmt.S(format, request.getName()), 
					e);
		}
		return null;
	}
	
	public Request findSource(Request request) {
		List<Request> requests = this.findSources(request);
		if(requests.isEmpty())
			return null;
		else
			return requests.get(0);
	}
	
	public List<Request> findSources(Request request) {
		List<Request> requests = ListUtils.list();
		for (Transition transition : request.getWorkflow().getTransitions()) {
			if (transition.getDestination() == request)
				requests.add(transition.getSource());
		}
		return requests;
	}
	
	public List<Request> findSiblings(Request request) {
		Request source = this.findSource(request);
		List<Request> requests = ListUtils.list();
		for (Transition transition : request.getWorkflow().getTransitions()) {
			if (transition.getSource() == source
					&& transition.getDestination() != request 
					&& transition.getDestination() != null)
				requests.add(transition.getDestination());
		}
		return requests;
	}
	
	public boolean isEditable(Request request) {
		Object pk = ObjectContext.get().getPrimaryKey(request);
		ObjectContext.get().find(Request.class, pk);
		if (!request.getWorkflow().getStatus().getCode()
				.equals(Status.SUBMITTED))
			return false;
		List<Request> nexts = this.getNextRequests(request);
		for (Request r : nexts) {
			if (r.getSubmittedDate() != null)
				return false;
		}
		return true;
	}
	
	public List<Request> getNextRequests(Request request) {
		List<Transition> transitions = request.getWorkflow().getTransitions();
		List<Request> requests = ListUtils.list();
		for (Transition transition : transitions) {
			if (request == transition.getSource() && transition.getDestination() != null)
				requests.add(transition.getDestination());
		}
		return requests;
	}

	public List<Request> submitted(Request request) throws WorkflowException {
		WorkflowDef workflowDef = manager.rules.workflowDefForModel(request.getWorkflow());
		if (workflowDef == null) {
			String errorStr = "RequestHandler: No workflow definition for '"
					+ request.getWorkflow().getName() + "'.";
			Log.customer.warn(errorStr);
			String format = WorkflowError
					.getDescription(WorkflowError.NO_WORKFLOW_DEF);
			this.throwWorkflowException(WorkflowError.NO_WORKFLOW_DEF, Fmt.S(
					format, request.getWorkflow().getName()), null);
		}
		RuleDef ruleDef = manager.rules.ruleDefForModel(request.getWorkflow());
		if (ruleDef == null) {
			String errorStr = "RequestHandler: No rule definition for '"
					+ request.getWorkflow().getName() + "'.";
			Log.customer.warn(errorStr);
			String format = WorkflowError
					.getDescription(WorkflowError.NO_RULE_DEF);
			Fmt.S(format, request.getWorkflow().getName());
			this.throwWorkflowException(WorkflowError.NO_RULE_DEF, Fmt.S(
					format, request.getWorkflow().getName()), null);
		}
		
		List<Request> requests = ListUtils.list();

		List<Request> parallels = this.findSiblings(request);
		boolean parallelDone = true;
		for (Request req : parallels) {
			if (req.getStatus() != null && Status.REQUESTED.equals(req.getStatus().getCode())) {
				parallelDone = false;
				break;
			}
		}

		if (parallelDone) {
			List<PathDef> paths = ruleDef.findPaths(request);
			if (paths.size() > 0) {
				Log.customer.debug("RequestHandler: path found for request (" + request.getActivityClassName() + ")");
				PathDef exitPath = manager.rules.exitPath(paths);
				if (exitPath != null) {
					Log.customer.debug("RequestHandler: exitPath exists. ExitStatus = "+ exitPath.getExitStatus());
					request.getWorkflow().setStatusCode(
							exitPath.getExitStatus());
					ObjectContext.get().save();
				} else {
					for (PathDef path : paths) {
						String destId = path.getDestinationId();
						if (destId != null) {
							Log.customer.debug("RequestHandler: destination found. destId = "+ destId);
							ActivityDef activityDef = manager.rules.lookupActivityDef(request.getWorkflow(), destId);
							List<Request> rs = this.requests(request.getWorkflow(), request, activityDef);
							if (ListUtils.nullOrEmpty(rs) == false) {
								for(Request r : rs) {
									requests.add(r);
								}
							}
						}
					}
				}
			}
		}
		return requests;
	}

	public Request submit(Request request) throws WorkflowException {
		try {
			request.setStatusCode(Status.SUBMITTED);
			ObjectContext.get().save();
			return request;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SUBMIT_A_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.SUBMIT_A_REQUEST_FAILED, 
					Fmt.S(format, request.getName()), 
					e);
		}
		return request;
	}

	public void expire(Request request) throws WorkflowException {
		try {
			request.setStatusCode(Status.EXPIRED);
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.EXPIRE_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.EXPIRE_REQUEST_FAILED, 
					Fmt.S(format, request.getName()), 
					e);
		}
		
	}

	public void attachActivity(Request request, Activity activity) {
		User actor = activity.getActor();
		activity.setRequest(request);
		request.setAction(activity);
		actor.getActivities().add(activity);
	}

	public Request save(Request request) throws WorkflowException {
		try {
			ObjectContext.get().save();
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.SAVE_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.SAVE_REQUEST_FAILED, 
					Fmt.S(format, request.getName()), 
					e);
		}
		return request;
	}
	
}
