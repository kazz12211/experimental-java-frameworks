package workflow.controller.helper;

import java.util.List;

import workflow.model.Activity;
import workflow.model.ActivityAttachment;
import workflow.model.Request;
import workflow.model.Transition;
import workflow.model.User;
import workflow.model.Workflow;
import workflow.model.WorkflowAttachment;
import ariba.ui.meta.persistence.ObjectContext;

public class OrphanManipulation {

	public static void deleteWorkflow(Workflow workflow) {
		List<WorkflowAttachment> attachments = workflow.getAttachments();
		for(WorkflowAttachment attachment : attachments) {
			_delete(attachment);
		}
		
		List<Transition> transitions = workflow.getTransitions();
		for(Transition transition : transitions) {
			_delete(transition);
		}
		
		List<Request> requests = workflow.getRequests();
		for(Request request : requests) {
			request.removeFromActor();
			deleteRequest(request);
		}
		_delete(workflow);
	}
	
	public static void deleteRequest(Request request) {
		Activity action = request.getAction();
		deleteActivity(action);
		_delete(request);
	}
	
	public static void deleteActivity(Activity activity) {
		if(activity != null) {
			List<ActivityAttachment> attachments = activity.getAttachments();
			for(ActivityAttachment attachment : attachments)
				_delete(attachment);
			User user = activity.getActor();
			if(user != null) {
				user.removeActivity(activity);
			}
			_delete(activity);
		}
	}
	
	private static void _delete(Object object) {
		ObjectContext.get().remove(object);
	}
}
