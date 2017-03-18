package workflow.controller.helper;

import java.util.List;

import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowManagerCallback;
import workflow.model.Activity;
import workflow.model.Error;
import workflow.model.Request;
import workflow.model.Role;
import workflow.model.User;
import workflow.model.Workflow;
import workflow.notif.MailNotification;
import ariba.util.core.ListUtil;

public class Notifier extends WorkflowManagerHelper implements WorkflowManagerCallback{

	public Notifier(WorkflowManager manager) {
		super(manager);
	}

	private List<User> owners(Workflow workflow) {
		List<User> owners = ListUtil.list();
		owners.add(workflow.getCreator());
		if (workflow.getRequester() != null
				&& workflow.getRequester() != workflow.getCreator())
			owners.add(workflow.getRequester());
		return owners;
	}
	
	public void complete(Workflow workflow) {
		List<User> recepients = this.owners(workflow);
		MailNotification.getInstance().sendCompleteNotification(recepients, workflow);
	}
	
	public void error(Workflow workflow, Error error) {
		List<User> recepients = this.owners(workflow);
		MailNotification.getInstance().sendErrorNotification(recepients,
				workflow, error);
	}
	
	public void reject(Workflow workflow) {
		List<User> recepients = this.owners(workflow);
		MailNotification.getInstance().sendRejectNotification(recepients,
				workflow);
	}

	public void request(Request request) {
		List<User> recepients = ListUtil.list();
		if (request.getRequestTo() instanceof Role) {
			recepients.addAll(((Role) request.getRequestTo()).getUsers());
		} else {
			recepients.add((User) request.getRequestTo());
		}
		MailNotification.getInstance().sendRequestNotification(recepients, request);
	}
	
	public void request(List<Request> requests) {
		for(Request request : requests) {
			this.request(request);
		}
	}
	
	public void expire(Request request) {
		List<User> recepients = ListUtil.list();
		if (request.getRequestTo() instanceof Role) {
			recepients.addAll(((Role) request.getRequestTo()).getUsers());
		} else {
			recepients.add((User) request.getRequestTo());
		}
		MailNotification.getInstance().sendExpireNotification(recepients, request);
	}
	
	public void expire(Workflow workflow) {
		List<User> recepients = this.owners(workflow);
		MailNotification.getInstance().sendExpireNotification(recepients,
				workflow);
	}

	@Override
	public void onCreate(Workflow workflow) {
		
	}

	@Override
	public void onSave(Workflow workflow) {
		
	}

	@Override
	public void onSubmit(Workflow workflow) {
		
	}

	@Override
	public void onComplete(Workflow workflow) {
		this.complete(workflow);
	}

	@Override
	public void onReject(Workflow workflow) {
		this.reject(workflow);
	}

	@Override
	public void onExpire(Workflow workflow) {
		this.expire(workflow);
	}

	@Override
	public void onSubmit(Request request) {
		
	}

	@Override
	public void onRequest(Request request) {
		this.request(request);
	}

	@Override
	public void onReject(Request request) {
		
	}

	@Override
	public void onExpire(Request request) {
		this.expire(request);
	}

	@Override
	public void onCreate(Activity activity) {		
	}

	@Override
	public void onWithdraw(Workflow workflow) {
	}

	@Override
	public void onDelete(Workflow workflow) {
	}

	@Override
	public void onUndelete(Workflow workflow) {
	}
	
	@Override
	public void onChange(Request request) {
	}
	
	@Override
	public void onError(Workflow workflow) {
	}
}
