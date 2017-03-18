package workflow.controller.helper;

import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;
import core.util.NotificationCenter;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowManagerCallback;
import workflow.model.Activity;
import workflow.model.Counts;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.util.AWChangeNotifier;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.log.Log;

public class ChangeWatcher implements WorkflowManagerCallback {

	List<AWChangeNotifier> notifiers = ListUtils.list();
		
	public void registerChangeNotifier(AWChangeNotifier notifier) {
		if(notifiers.contains(notifier) == false)
			notifiers.add(notifier);
	}
	public void unregisterChangeNotifier(AWChangeNotifier notifier) {
		if(notifiers.contains(notifier))
			notifiers.remove(notifier);
	}
	
	private void notifyChange() {
		synchronized(notifiers) {
			for(AWChangeNotifier notifier : notifiers) {
				try {
					notifier.notifyChange();
				} catch (Exception e) {
					Log.customer.warn("ChangeWatch: notify error", e);
				}
			}
		}
	}
	
	@Override
	public void onCreate(Workflow workflow) {
	}

	@Override
	public void onSave(Workflow workflow) {
		this.notifyWorkflowChanged(workflow, (String)null, Status.SAVED);
	}

	@Override
	public void onSubmit(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, Status.SAVED, Status.SUBMITTED);
	}

	@Override
	public void onComplete(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, Status.SUBMITTED, Status.COMPLETED);
	}

	@Override
	public void onReject(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, Status.SUBMITTED, Status.REJECTED);
	}

	@Override
	public void onExpire(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, Status.SUBMITTED, Status.EXPIRED);
	}

	@Override
	public void onSubmit(Request request) {
		this.notifyChange();
		this.notifyRequestChanged(request, Status.REQUESTED, Status.SUBMITTED);
	}

	@Override
	public void onRequest(Request request) {
		this.notifyChange();
		this.notifyRequestChanged(request, null, Status.REQUESTED);
	}

	@Override
	public void onReject(Request request) {
		this.notifyChange();
		this.notifyRequestChanged(request, Status.REQUESTED, Status.REJECTED);
	}

	@Override
	public void onExpire(Request request) {
		this.notifyChange();
		this.notifyRequestChanged(request, Status.REQUESTED, Status.EXPIRED);
	}

	@Override
	public void onCreate(Activity activity) {
	}

	@Override
	public void onWithdraw(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, Status.SUBMITTED, Status.SAVED);
	}

	@Override
	public void onDelete(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, (String)null, null);
	}

	@Override
	public void onUndelete(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, (String)null, null);
	}
	
	@Override
	public void onChange(Request request) {
		this.notifyChange();
		this.notifyRequestChanged(request, null, null);
	}
	
	@Override
	public void onError(Workflow workflow) {
		this.notifyChange();
		this.notifyWorkflowChanged(workflow, new String[] { Status.SAVED, Status.SUBMITTED, Status.PENDING}, Status.ERROR);
	}

	private Map<String, Object> userInfo(Workflow workflow) {
		Long workflowId = workflow.getId();
		User creator = workflow.getCreator();
		String creatorId = null;
		if(creator != null)
			creatorId = creator.getUniqueName();
		Map<String, Object> map = MapUtils.map();
		map.put("workflowId", workflowId);
		if(creatorId != null)
			map.put("userId", creatorId);
		return map;
		
	}
	
	private Map<String, Object> userInfo(Request request) {
		Long requestId = request.getId();
		List<User> actors = request.actingUsers();
		List<String> actorIds = ListUtils.list();
		for(User actor : actors) {
			actorIds.add(actor.getUniqueName());
		}
		Map<String, Object> map = MapUtils.map();
		map.put("requestId", requestId);
		map.put("userIds", actorIds);
		return map;
	}
	
	private void notifyWorkflowChanged(Workflow workflow, String statusFrom, String statusTo) {
		Map<String, Object> userInfo = this.userInfo(workflow);
		if(statusFrom == null && statusTo == null)
			workflow.getCreator().updateCounts();
		else
			Counts.updateWorkflowCount(workflow.getCreator().getId(), statusFrom, statusTo, ObjectContext.get());
		NotificationCenter.defaultCenter().postNotification(WorkflowManager.WorkflowStateChangedNotification, null, userInfo);
	}
	
	private void notifyWorkflowChanged(Workflow workflow, String[] statusFroms, String statusTo) {
		Map<String, Object> userInfo = this.userInfo(workflow);
		if(statusFroms == null && statusTo == null)
			workflow.getCreator().updateCounts();
		else {
			Counts.updateWorkflowCount(workflow.getCreator().getId(), statusFroms, statusTo, ObjectContext.get());
		}
		NotificationCenter.defaultCenter().postNotification(WorkflowManager.WorkflowStateChangedNotification, null, userInfo);
	}

	private void notifyRequestChanged(Request request, String statusFrom, String statusTo) {
		Map<String, Object> userInfo = this.userInfo(request);
		for(String userId : (List<String>)userInfo.get("userIds")) {
			User user = User.userWithUUID(userId);
			if(user != null) {
				Counts.updateRequestCount(user.getId(), statusFrom, statusTo, ObjectContext.get());
			}
		}
		NotificationCenter.defaultCenter().postNotification(WorkflowManager.RequestStateChangedNotification, null, userInfo);
	}
}
