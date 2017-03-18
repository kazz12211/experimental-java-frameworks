package workflow.controller.helper;

import java.util.List;

import core.util.ClassUtils;
import workflow.WorkflowError;
import workflow.WorkflowException;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowManagerCallback;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.WorkflowDef;
import workflow.controller.rule.trigger.TriggerDef;
import workflow.controller.trigger.Trigger;
import workflow.model.Activity;
import workflow.model.Error;
import workflow.model.Request;
import workflow.model.Workflow;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public class TriggerManager extends WorkflowManagerHelper implements WorkflowManagerCallback {

	public TriggerManager(WorkflowManager manager) {
		super(manager);
	}
	
	public void fire(Request request, String stage) throws WorkflowException {
		ActivityDef activityDef = manager.rules.lookupActivityDef(request.getWorkflow(),
				request.getActivityRefId());
		if (activityDef == null)
			return;
		List<TriggerDef> triggers = activityDef.getTriggersForStage(stage);
		if (triggers == null)
			return;
		for (TriggerDef trigger : triggers) {
			try {
				this.fireTrigger(trigger.getTriggerClass(), request);
			} catch (WorkflowException e) {
				String format = WorkflowError.getDescription(WorkflowError.REQUEST_TRIGGER_ERROR);
				this.throwWorkflowException(
						WorkflowError.REQUEST_TRIGGER_ERROR, 
						Fmt.S(format, request.getName(), stage), 
						e);
			}
		}
	}

	private void fire(Activity activity, String stage) throws WorkflowException {
		Request r = activity.getRequest();
		if(r == null)
			return;
		Workflow w = r.getWorkflow();
		if(w == null)
			return;
		ActivityDef activityDef = manager.rules.lookupActivityDef(w, r.getActivityRefId());
		if (activityDef == null)
			return;
		List<TriggerDef> triggers = activityDef.getTriggersForStage(stage);
		if (triggers == null)
			return;
		for (TriggerDef trigger : triggers) {
			try {
				this.fireTrigger(trigger.getTriggerClass(), activity);
			} catch (WorkflowException e) {
				String format = WorkflowError.getDescription(WorkflowError.ACTIVITY_TRIGGER_ERROR);
				this.throwWorkflowException(
						WorkflowError.ACTIVITY_TRIGGER_ERROR, 
						Fmt.S(format, activityDef.getName(), stage), 
						e);
			}
		}
	}

	public void fire(List<Request> requests, String stage) throws WorkflowException {
		for (Request request : requests) {
			this.fire(request, stage);
		}
	}
	
	public void fire(Workflow workflow, String stage) throws WorkflowException {
		WorkflowDef workflowDef = manager.rules.workflowDefForModel(workflow);
		if (workflowDef == null)
			return;
		List<TriggerDef> triggers = workflowDef.getTriggersForStage(stage);
		if (triggers == null)
			return;
		for (TriggerDef trigger : triggers) {
			try {
				this.fireTrigger(trigger.getTriggerClass(), workflow);
			} catch (WorkflowException e) {
				String format = WorkflowError.getDescription(WorkflowError.WORKFLOW_TRIGGER_ERROR);
				this.throwWorkflowException(
						WorkflowError.WORKFLOW_TRIGGER_ERROR, 
						Fmt.S(format, workflow.getName(), stage), 
						e);
			}
		}
	}
	
	protected void fireTrigger(String triggerClassName, Object model) throws WorkflowException {
		Class<?> triggerClass = null;
		try {
			triggerClass = ClassUtils.classForName(triggerClassName, Trigger.class);
		} catch (Exception e) {
			Log.customer.error("TriggerManager: could not fire trigger '"
					+ triggerClassName
					+ "'. It might not implements Trigger interface.");
			String format = WorkflowError.getDescription(WorkflowError.INSTANTIATE_TRIGGER_ERROR);
			this.throwWorkflowException(
					WorkflowError.INSTANTIATE_TRIGGER_ERROR, 
					Fmt.S(format, model.getClass().getName(), triggerClassName), 
					e);
		}
		if (triggerClass != null) {
			try {
				Trigger t = (Trigger) triggerClass.newInstance();
				t.fire(model);
			} catch (Exception e) {
				Log.customer.error("TriggerManager: could not fire trigger '"
						+ triggerClassName
						+ "'. Instantiation failed.", e);
				String format = WorkflowError.getDescription(WorkflowError.FIRE_TRIGGER_ERROR);
				this.throwWorkflowException(
						WorkflowError.FIRE_TRIGGER_ERROR, 
						Fmt.S(format, model.getClass().getName(), triggerClassName), 
						e);
			}
		}
	}
	
	@Override
	public void onCreate(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_CREATE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onSave(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_SAVE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onSubmit(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_SUBMIT);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onComplete(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_COMPLETE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onReject(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_REJECT);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onExpire(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_EXPIRE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onSubmit(Request request) {
		Error error = null;
		try {
			this.fire(request, Trigger.STAGE_SUBMIT);
		} catch (WorkflowException e) {
			error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}

	@Override
	public void onRequest(Request request) {
		Error error = null;
		try {
			this.fire(request, Trigger.STAGE_REQUEST);
		} catch (WorkflowException e) {
			error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}

	@Override
	public void onReject(Request request) {
		Error error = null;
		try {
			this.fire(request, Trigger.STAGE_REJECT);
		} catch (WorkflowException e) {
			error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}

	@Override
	public void onExpire(Request request) {
		Error error = null;
		try {
			this.fire(request, Trigger.STAGE_EXPIRE);
		} catch (WorkflowException e) {
			error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}
	
	@Override
	public void onCreate(Activity activity) {
		Error error = null;
		Request request = activity.getRequest();
		try {
			this.fire(activity, Trigger.STAGE_CREATE);
		} catch (WorkflowException e) {
			if(request != null)
				error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}

	@Override
	public void onWithdraw(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_WITHDRAW);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onDelete(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_DELETE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onUndelete(Workflow workflow) {
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_UNDELETE);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}

	@Override
	public void onChange(Request request) {
		if(request == null)
			return;
		
		Error error = null;
		try {
			this.fire(request, Trigger.STAGE_CHANGE);
		} catch (WorkflowException e) {
			error = this.manager.createError(request.getWorkflow(), e);
		}
		if(error != null) {
			this.manager.recordError(request.getWorkflow(), error);
		}
	}
	
	@Override
	public void onError(Workflow workflow) {
		if(workflow == null)
			return;
		Error error = null;
		try {
			this.fire(workflow, Trigger.STAGE_ERROR);
		} catch (WorkflowException e) {
			error = this.manager.createError(workflow, e);
		}
		if(error != null) {
			this.manager.recordError(workflow, error);
		}
	}
}
