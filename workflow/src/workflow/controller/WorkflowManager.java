package workflow.controller;

import java.util.List;
import java.util.Map;

import core.BaseObject;
import core.util.ListUtils;
import workflow.WorkflowException;
import workflow.app.WorkflowApplication;
import workflow.controller.helper.ModelFactory;
import workflow.controller.helper.Notifier;
import workflow.controller.helper.RequestHandler;
import workflow.controller.helper.Rules;
import workflow.controller.helper.TriggerManager;
import workflow.controller.helper.WorkflowHandler;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.WorkflowRule;
import workflow.integration.IntegrationBus;
import workflow.integration.bus.InboundListener;
import workflow.integration.channel.Channel;
import workflow.integration.message.BusMessage;
import workflow.integration.message.MessageType;
import workflow.model.Activity;
import workflow.model.Actor;
import workflow.model.Error;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.util.log.Log;

public class WorkflowManager {

	public static final String WorkflowStateChangedNotification = "WorkflowStateChangedNotification";
	public static final String RequestStateChangedNotification = "RequestStateChangedNotification";
	
	public ModelFactory factory;
	public WorkflowHandler workflowHandler;
	public RequestHandler requestHandler;
	public Rules rules;
	private TriggerManager triggers;
	public Notifier notifier;
	public BusListener busListener;
	public BusPusher busPusher;
	
	private List<WorkflowManagerCallback> callbacks = ListUtils.list();

	public WorkflowManager() {
		factory = new ModelFactory(this);
		notifier = new Notifier(this);
		workflowHandler = new WorkflowHandler(this);
		requestHandler = new RequestHandler(this);
		rules = new Rules(this);
		triggers = new TriggerManager(this);
		this.addCallback(triggers);
		this.addCallback(notifier);
		busListener = new BusListener(this);
		WorkflowApplication app = (WorkflowApplication) WorkflowApplication.sharedInstance();
		app.getIntegrationBus().addBusListener(busListener);
		busPusher = new BusPusher();
		this.addCallback(busPusher);
	}
	
	// CALLBACK SUPPORT
	public void addCallback(WorkflowManagerCallback callback) {
		this.callbacks.add(callback);
	}
	
	private void _onCreate(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onCreate(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onCreate(workflow);
		}
	}
	private void _onCreate(Activity activity) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onCreate(" + activity.getClass().getName() + ":" + activity.getId() + ")");
			callback.onCreate(activity);
		}
	}
	private void _onRequest(Request request) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onRequest(Request:" + request.getId() + ")");
			callback.onRequest(request);
		}
	}
	private void _onComplete(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onComplete(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onComplete(workflow);
		}
	}
	private void _onReject(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onReject(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onReject(workflow);
		}
	}
	private void _onSave(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onSave(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onSave(workflow);
		}
	}
	private void _onSubmit(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onSubmit(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onSubmit(workflow);
		}
	}
	
	private void _onWithdraw(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onWithdraw(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onWithdraw(workflow);
		}
	}
	
	private void _onDelete(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			callback.onDelete(workflow);
		}
	}
	
	private void _onUndelete(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onUndelete(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onUndelete(workflow);
		}
	}
	
	private void _onSubmit(Request request) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onSubmit(Request:" + request.getId() + ")");
			callback.onSubmit(request);
		}
	}
	private void _onReject(Request request) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onReject(Request:" + request.getId() + ")");
			callback.onReject(request);
		}
	}
	
	private void _onExpire(Request request) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onExpire(Request:" + request.getId() + ")");
			callback.onExpire(request);
		}
	}
	
	private void _onExpire(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onExpire(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onExpire(workflow);
		}
	}
	
	private void _onChange(Request request) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onChange(Request:" + request.getId() + ")");
			callback.onChange(request);
		}
	}
	
	private void _onError(Workflow workflow) {
		for(WorkflowManagerCallback callback : callbacks) {
			Trace.writeLog(callback.getClass().getName() + ": onError(" + workflow.getClassName() + ":" + workflow.getId() + ")");
			callback.onError(workflow);
		}
	}
	
	// MODEL INSTANTIATION
	public Workflow createWorkflow(Class<? extends Workflow> modelClass,
			User creator, User requester, String name, String title) throws WorkflowException {
		Workflow workflow = factory.createWorkflow(modelClass, creator, requester, name, title);
		Trace.writeLog("WorkflowManager: " + modelClass.getName() + " created");
		_onCreate(workflow);
		return workflow;
	}
	public List<Request> createRequests(Workflow workflow, ActivityDef activityDef) throws WorkflowException {
		List<Request> requests = factory.createRequests(workflow, activityDef);
		Trace.writeLog("WorkflowManager: " + "requests of " + activityDef.getModelName() + " created for workflow " + workflow.getClass().getName() + "(" + workflow.getId() + ")");
		return requests;
	}
	public Activity createActivityForRequest(Request request, User actor) throws WorkflowException {
		Activity activity = factory.createActivityForRequest(request, actor);
		Trace.writeLog("WorkflowManager: " + activity.getClass().getName() + "(" + actor.getUniqueName() + ") created for workflow " + request.getWorkflow().getName() + "(" + request.getWorkflow().getId() + ")");
		_onCreate(activity);
		return activity;
	}
	
	public Workflow copyWorkflow(Workflow workflow, User creator) throws WorkflowException {
		Workflow wf = factory.copyWorkflow(workflow, creator);
		Trace.writeLog("WorkflowManager: Copy of " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " created");
		_onCreate(wf);
		return wf;
	}

	// WORKFLOW HANDLING
	// saves
	public List<Request> submittedAndSave(Workflow workflow, User submitter) throws Exception {
		List<Request> requests = workflowHandler.submitted(workflow, submitter);
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + " submitted by " + submitter.getUniqueName());
		for(Request request : requests) {
			Actor requestTo = request.getRequestTo();
			if(requestTo != null)
				Trace.writeLog("WorkflowManager: " + "request of " + request.getActivityClassName() + " for actor " + request.getRequestTo().getUniqueName() + " sent");
			else
				Trace.writeLog("WorkflowManager: " + "request of " + request.getActivityClassName() + " sent but no actor assigned");
			_onRequest(request);
		}
		return requests;
	}
	public Workflow withdraw(Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + "withdrawing " + workflow.getClass().getName() + "(" + workflow.getId() + ")");
		return workflowHandler.withdraw(workflow);
	}
	public Workflow remove(User owner, Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + "removing " + workflow.getClass().getName() + "(" + workflow.getId() + ")");
		return workflowHandler.remove(owner, workflow);
	}
	public Workflow unremove(User user, Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + "unremoving " + workflow.getClass().getName() + "(" + workflow.getId() + ")");
		return workflowHandler.unremove(user, workflow);
	}
	//saves
	public Workflow completeAndSave(Workflow workflow) throws WorkflowException {
		Workflow w = workflowHandler.complete(workflow);
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " completed");
		_onComplete(w);
		return w;
	}
	// saves
	public Workflow stopWithErrorAndSave(Workflow workflow, Error error) {
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " stopped with " + error);
		Workflow w = workflowHandler.stopWithError(workflow, error);
		_onError(w);
		return w;
	}
	// saves
	public Workflow rejectAndSave(Workflow workflow) throws WorkflowException {
		Workflow w = workflowHandler.reject(workflow);
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " rejected");
		_onReject(w);
		return w;
	}
	// saves
	public Workflow saveAndSave(Workflow workflow) throws WorkflowException {
		Workflow w = workflowHandler.save(workflow);
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " saved");
		_onSave(w);
		return w;
	}
	// saves
	public Workflow submitAndSave(Workflow workflow) throws WorkflowException {
		Workflow w = workflowHandler.submit(workflow);
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " submitted");
		_onSubmit(w);
		return w;
	}
	
	public Error createError(Workflow workflow, Exception e) {
		return workflowHandler.createError(workflow, e);
	}
	public void recordError(Workflow workflow, Error error) {
		workflowHandler.recordError(workflow, error);
	}
	
	
	public void reallyRemove(Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + "really removing " + workflow.getClass().getName() + "(" + workflow.getId() + ")");
		workflowHandler.reallyRemove(workflow);
	}
	public boolean isEditable(Workflow workflow) {
		return workflowHandler.isEditable(workflow);
	}

	public Map<String, Object> userInfo(Workflow workflow) {
		return rules.userInfoForModel(workflow);
	}
	
	
	// REQUEST HANDLING
	// saves
	public List<Request> submitAndSave(Workflow workflow, ActivityDef activityDef) throws WorkflowException {
		List<Request> rs = requestHandler.requests(workflow, activityDef);
		Trace.writeLog("WorkflowManager: " + "requests for " + workflow.getClass().getName() + "(" + workflow.getId() + ") created");
		return rs;
	}
	// saves
	public List<Request> submitAndSave(Workflow workflow, String activityId) throws WorkflowException {
		List<Request> rs = requestHandler.requests(workflow, activityId);
		Trace.writeLog("WorkflowManager: " + "requests for " + workflow.getClass().getName() + "(" + workflow.getId() + ") created");
		return rs;
	}
	// saves
	public List<Request> submitAndSave(Workflow workflow, Request source, ActivityDef activityDef) throws WorkflowException {
		List<Request> rs = requestHandler.requests(workflow, source, activityDef);
		Trace.writeLog("WorkflowManager: " + "subsequence requests for " + workflow.getClass().getName() + "(" + workflow.getId() + ") created");
		return rs;
	}
	// saves
	public Request removeAndSave(Request request) {
		Trace.writeLog("WorkflowManager: " + "removing request for " + request.getWorkflow().getClass().getName() + "(" + request.getWorkflow().getId() + ")");
		return requestHandler.remove(request);
	}
	public boolean isEditable(Request request) {
		return requestHandler.isEditable(request);
	}
	// saves
	public List<Request> submittedAndSave(Request request) throws WorkflowException {
		List<Request> reqs = requestHandler.submitted(request);
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") submitted");
		for(Request r : reqs) {
			if(r.getStatusCode().equals(Status.REQUESTED)) {
				Trace.writeLog("WorkflowManager: " + "request (" + r.getActivityClassName() + ") sent");
				_onRequest(r);
			}
		}
		/*if(request.getWorkflow().getStatusCode().equals(Status.COMPLETED)) {
			_onComplete(request.getWorkflow());
		} else */if(request.getWorkflow().getStatusCode().equals(Status.REJECTED)) {
			Trace.writeLog("WorkflowManager: " + request.getWorkflow().getClass().getName() + "(" + request.getWorkflow().getId() + ") rejected");
			_onReject(request.getWorkflow());
		}
		return reqs;
	}
	public Request resend(Request request) throws WorkflowException {
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") re-sent");
		_onRequest(request);
		return request;
	}
	
	// saves
	public Request submitAndSave(Request request) throws WorkflowException {
		Request r = requestHandler.submit(request);
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") submitted");
		_onSubmit(request);
		return r;
	}
	
	public Request submitAndSave(Request request, Activity activity) throws WorkflowException {
		requestHandler.attachActivity(request, activity);
		Trace.writeLog("WorkflowManager: " + activity.getClass().getName() + "attached to request (" + request.getActivityClassName() + ") submitted");
		return this.submitAndSave(request);
	}
	// saves
	public Request saveAndSave(Request request) throws WorkflowException {
		Request r = requestHandler.save(request);
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") saved");
		_onChange(request);
		return r;
	}


	// saves
	public Request rejectAndSave(Request request) throws WorkflowException {
		Request r = requestHandler.reject(request);
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") rejected");
		_onReject(r);
		return r;
	}
	public Request rejectAndSave(Request request, Activity activity) throws WorkflowException {
		requestHandler.attachActivity(request, activity);
		Trace.writeLog("WorkflowManager: " + activity.getClass().getName() + "attached to request (" + request.getActivityClassName() + ") submitted");
		return this.rejectAndSave(request);
	}
	
	public void expireAndSave(Request request) throws WorkflowException {
		requestHandler.expire(request);
		Trace.writeLog("WorkflowManager: " + "request (" + request.getActivityClassName() + ") expired");
		_onExpire(request);
		workflowHandler.expire(request.getWorkflow());
		Trace.writeLog("WorkflowManager: " + request.getWorkflow().getClass().getName() + "(" + request.getWorkflow().getId() + ")" + " expired");
		_onExpire(request.getWorkflow());
	}
	
	public Map<String, Object> userInfo(Request request) {
		return rules.userInfoForModel(request.getWorkflow(), request.getActivityRefId());
	}

	// MVC
	public String controllerNameForModel(Object model) {
		return MVC.controllerNameForModel(rules.getWorkflowRule(), model);
	}


	
	static class MVC {
		
		public static String controllerNameForModel(WorkflowRule workflowRule, Object model) {
			if (model instanceof Workflow) {
				return workflowRule.getControllerNameForWorkflow(model.getClass()
						.getName());
			} else if (model instanceof Request) {
				String modelName = ((Request) model).getActivityClassName();
				Workflow workflow = ((Request) model).getWorkflow();
				return workflowRule.getControllerNameForActivity(workflow,
						modelName);
			}
			return null;
		}
	}



	public void reloadRule() {
		rules.reload();
	}

	public void withdrawn(Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " withdrawn");
		this._onWithdraw(workflow);
	}
	
	public void deleted(Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " deleted");
		this._onDelete(workflow);
	}
	
	public void undeleted(Workflow workflow) {
		Trace.writeLog("WorkflowManager: " + workflow.getClass().getName() + "(" + workflow.getId() + ")" + " undeleted");
		this._onUndelete(workflow);
	}

	public class BusListener extends InboundListener {

		WorkflowManager mgr;
		
		public BusListener(WorkflowManager mgr) {
			this.mgr = mgr;
		}
		
		@Override
		public void onMessage(IntegrationBus bus, Channel channel,
				BusMessage message) {
			this.mgr.receiveMessage(message);
		}

		@Override
		public void failed(IntegrationBus bus, Channel channel,
				BusMessage message, Throwable error) {
			this.mgr.receiveMessageFailed(message, error);
		}
		
	}



	public void receiveMessage(BusMessage message) {
		Log.customer.info("Message received via integration bus. The message : " + message);
	}

	public void receiveMessageFailed(BusMessage message, Throwable error) {
		Log.customer.error("Failed to receive message via integration bus. The message : " + message, error);
	}
	
	public class WorkflowBusMessage extends BusMessage {

		private static final long serialVersionUID = 1L;
		private BaseObject model;
		
		public WorkflowBusMessage(BaseObject model) {
			this.model = model;
		}
		
		public BaseObject getModel() {
			return model;
		}
		
		@Override
		public boolean isOutgoing() { return true; }
		
	}
	
	public class BusPusher implements WorkflowManagerCallback {

		private BusMessage createMessage(BaseObject model, String type) {
			WorkflowBusMessage message = new WorkflowBusMessage(model);
			MessageType mt = new MessageType(model.getClass().getName(), type);
			message.setType(mt);
			return message;
		}
		
		private void pushMessage(BaseObject model, String type) {
			BusMessage message = this.createMessage(model, type);
			WorkflowApplication app = (WorkflowApplication) WorkflowApplication.sharedInstance();
			try {
				app.getIntegrationBus().post(message);
			} catch (Exception e) {
				Log.customer.error("Could not push message " + message, e);
			}
		}
		
		@Override
		public void onCreate(Workflow workflow) {
			this.pushMessage(workflow, "create");
		}

		@Override
		public void onSave(Workflow workflow) {
			this.pushMessage(workflow, "save");
		}

		@Override
		public void onSubmit(Workflow workflow) {
			this.pushMessage(workflow, "submit");
		}

		@Override
		public void onComplete(Workflow workflow) {
			this.pushMessage(workflow, "complete");
		}

		@Override
		public void onReject(Workflow workflow) {
			this.pushMessage(workflow, "reject");
		}

		@Override
		public void onExpire(Workflow workflow) {
			this.pushMessage(workflow, "expire");
		}

		@Override
		public void onWithdraw(Workflow workflow) {
			this.pushMessage(workflow, "withdraw");
		}

		@Override
		public void onDelete(Workflow workflow) {
			this.pushMessage(workflow, "delete");
		}

		@Override
		public void onUndelete(Workflow workflow) {
			this.pushMessage(workflow, "undelete");
		}

		@Override
		public void onSubmit(Request request) {
			this.pushMessage(request.getAction(), "submit");
		}

		@Override
		public void onRequest(Request request) {
			this.pushMessage(request, "request");
		}

		@Override
		public void onReject(Request request) {
			this.pushMessage(request, "reject");
		}

		@Override
		public void onExpire(Request request) {
			this.pushMessage(request, "expire");
		}

		@Override
		public void onCreate(Activity activity) {
		
		}

		@Override
		public void onChange(Request request) {
			this.pushMessage(request, "change");
		}

		@Override
		public void onError(Workflow workflow) {
			this.pushMessage(workflow, "error");
		}
		
	}
}
