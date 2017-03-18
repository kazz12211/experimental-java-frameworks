package workflow.controller.helper;

import java.util.Date;
import java.util.List;

import core.util.ListUtils;
import workflow.WorkflowError;
import workflow.WorkflowException;
import workflow.controller.Trace;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.ActorRoleDef;
import workflow.model.Activity;
import workflow.model.Actor;
import workflow.model.Request;
import workflow.model.Role;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public class ModelFactory extends WorkflowManagerHelper {

	public ModelFactory(WorkflowManager manager) {
		super(manager);
	}
	public Workflow createWorkflow(Class<? extends Workflow> modelClass,
			User creator, User requester, String name, String title) throws WorkflowException {
		Log.customer.debug("ModelFactory: Instantiating workflow (" + modelClass.getName() + ")");
		
		try {
			Workflow workflow = ObjectContext.get().create(modelClass);
			workflow.setCreatedDate(new Date());
			workflow.setCreator(creator);
			workflow.setName(name);
			workflow.setTitle(title);
			workflow.setStatusCode(Status.SAVED);
			workflow.setArchived(new Boolean(false));
			workflow.setDeleted(new Boolean(false));
			workflow.setHidden(new Boolean(true));
			if (creator != requester) {
				workflow.setRequester(requester);
			}
			creator.getWorkflows().add(workflow);
			return workflow;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.INSTANTIATE_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.INSTANTIATE_WORKFLOW_FAILED, 
					Fmt.S(format, name), 
					e);
		}
		return null;
	}
	
	public List<Request> createRequests(Workflow workflow, ActivityDef activityDef) throws WorkflowException {
		List<Actor> actors = ListUtils.list();
		for(ActorRoleDef actorRole : activityDef.getActorRoles()) {
			Actor actor = null;
			try {
				actor = actorRole.getFetcher().getActor(workflow);
			} catch (Exception e) {
				String format = WorkflowError.getDescription(WorkflowError.INVALID_ROLE_ASSIGNED);
				throw new WorkflowException(
						WorkflowError.INVALID_ROLE_ASSIGNED, 
						Fmt.S(format, activityDef.getName()), 
						e);
			}
			if(actor == null) {
				Trace.writeLog("ModelFactory: No actor for request for activity (" + activityDef.getModelName() + "). The request has not been created.");
				Log.customer.debug("ModelFactory: No actor for request for activity (" + activityDef.getModelName() + "). The request has not been created.");
			} else {
				actors.add(actor);
			}
		}
		
		List<Request> requests = ListUtils.list();
		if(ListUtils.nullOrEmpty(actors) == false) {
			for(Actor actor : actors) {
				Request request = this.createRequest(workflow, activityDef, actor);
				if(request != null) {
					Trace.writeLog("ModalFactory: adding actor(" + actor.getUniqueName() + ") to request");
					requests.add(request);
				}
			}
		}
		
		return requests;
	}

	public Request createRequest(Workflow workflow, ActivityDef activityDef, Actor actor) throws WorkflowException {
		Log.customer.debug("ModelFactory: Instantiating request for activity (" + activityDef.getModelName() + ")");
		try {
			Request request = ObjectContext.get().create(Request.class);
			//ObjectContext.get().recordForInsert(request);
			request.setRequestTo(actor);
			Trace.writeLog("ModelFactory: creating request to  (" + actor.getUniqueName() + ")");
			Log.customer.debug("ModelFactory: request to  (" + actor.getUniqueName() + ")");
			request.setActivityClassName(activityDef.getModelName());
			request.setActivityRefId(activityDef.getId());
			request.setName(activityDef.getName());
			request.setCreatedDate(new Date());
			request.setStatusCode(Status.REQUESTED);
			if(activityDef.getExpiration() != null)
				request.setExpirationDate(activityDef.getExpiration().expirationDateFor(new Date()));
			workflow.addRequest(request);
			addRequestToActor(request, actor);
			return request;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.INSTANTIATE_REQUEST_FAILED);
			this.throwWorkflowException(
					WorkflowError.INSTANTIATE_REQUEST_FAILED, 
					Fmt.S(format, activityDef.getName()), 
					e);
		}
		return null;
	}

	private void addRequestToActor(Request request, Actor actor) {
		if (actor instanceof Role) {
			List<User> users = ((Role) actor).getUsers();
			if(ListUtils.nullOrEmpty(users)) {
				users = Role.administrativeUsers();
			}
			for (User user : users) {
				user.addRequest(request);
			}
		} else if (actor instanceof User) {
			((User) actor).addRequest(request);
		}
	}


	public Activity createActivityForRequest(Request request, User actor) throws WorkflowException {
		Log.customer.debug("ModelFactory: Instantiating activity (" + request.getActivityClassName() + ")");
		try {
			String activityClass = request.getActivityClassName();
			Activity activity = ObjectContext.get().create(activityClass);
			activity.setCreatedDate(new Date());
			activity.setActor(actor);
			activity.setName(request.getName());
			/*
			activity.setRequest(request);
			request.setAction(activity);
			actor.getActivities().add(activity);
			*/
			Trace.writeLog("ModelFactory: created activity " + activityClass + " for request(" + request.getId() + ")");
			return activity;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.INSTANTIATE_ACTIVITY_FAILED);
			this.throwWorkflowException(
					WorkflowError.INSTANTIATE_ACTIVITY_FAILED, 
					Fmt.S(format, request.getName()), 
					e);
		}
		return null;
	}
	public Workflow copyWorkflow(Workflow workflow, User creator) throws WorkflowException {
		Log.customer.debug("ModelFactory: Copying workflow (" + workflow.getClass().getName() + ")");
		Workflow copy = null;
		try {
			copy = ObjectContext.get().create(workflow.getClass());
			workflow.copyFields(copy);
			copy.setHidden(new Boolean(true));
			copy.setCreator(creator);
			if(copy.getCreator() == copy.getRequester())
				copy.setRequester(null);
			creator.getWorkflows().add(copy);
			return copy;
		} catch (Exception e) {
			String format = WorkflowError.getDescription(WorkflowError.INSTANTIATE_WORKFLOW_FAILED);
			this.throwWorkflowException(
					WorkflowError.INSTANTIATE_WORKFLOW_FAILED, 
					Fmt.S(format, workflow.getName()), 
					e);
		}
		return null;
	}

}
