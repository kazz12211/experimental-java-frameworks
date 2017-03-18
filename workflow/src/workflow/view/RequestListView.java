package workflow.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.ListUtils;
import core.util.ListFilter;
import workflow.app.DataTableComponent;
import workflow.controller.ActivityWizard;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class RequestListView extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<StatusData> statuss;
	public StatusData statusFilter = null;
	public WorkflowManager workflowManager;
	public List<WorkflowDef> workflowModels;
	public WorkflowDef modelFilter;

	public class StatusData {
		public String code;
		public String label;
		public StatusData(String code, String label) {
			this.code = code;
			this.label = label;
		}
		public StatusData(Status status) {
			this.code = status.getCode();
			this.label = status.getLabel();
		}
	}
	
	@Override
	public void init() {
		super.init();
		statuss = ListUtils.list();
		statuss.add(new StatusData(Status.get(Status.REQUESTED)));
		statuss.add(new StatusData(Status.get(Status.SUBMITTED)));
		statuss.add(new StatusData(Status.get(Status.REJECTED)));
		statuss.add(new StatusData(Status.get(Status.EXPIRED)));
		statusFilter = statuss.get(0);
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
	}

	@Override
	protected void awake() {
		super.awake();
		workflowModels = workflowManager.rules.allWorkflowModels();
		Collections.sort(workflowModels, new Comparator<WorkflowDef>() {
			@Override
			public int compare(WorkflowDef arg0, WorkflowDef arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}});
	}
	
	public void setStatusFilter(StatusData status) {
		this.statusFilter = status;
	}

	public List<Request> filteredObjects() {
		User user = (User) session().getFieldValue("user");
		List<Request> requests;
		if(statusFilter == null)
			requests = user.viewableRequests();
		else 
			requests = user.requestsOfStatus(statusFilter.code);
		
		if(modelFilter != null) {
			requests = (List<Request>) ListUtils.filteredList(requests, new ListFilter<Request>() {
				@Override
				public boolean filter(Request object) {
					return object.getWorkflow().getClassName().equals(modelFilter.getModelName());
				}});
		}
		return requests;
	}
		
	private boolean enableToEdit(Request request) {
		return workflowManager.isEditable(request);
	}
	
	public boolean enableToEditRequest() {
		return this.enableToEdit((Request) displayGroup.currentItem());
	}
	
	public AWResponseGenerating inspect() {
		Request request = (Request) displayGroup.currentItem();
		User user = (User)session().getFieldValue("user");
		return WizardLauncher.startActivityWizard(
				user, 
				ActivityWizard.MODE_INSPECT, 
				request, 
				this, 
				workflowManager);
	}
	public AWResponseGenerating action() {
		Request request = (Request) displayGroup.currentItem();
		User user = (User)session().getFieldValue("user");
		if(workflowManager.isEditable(request)) {
			if(request.getAction() == null)
				return WizardLauncher.startActivityWizard(
						user, 
						ActivityWizard.MODE_NEW, 
						request, 
						this, 
						workflowManager);
			else
				return WizardLauncher.startActivityWizard(
						user, 
						ActivityWizard.MODE_EDIT, 
						request, 
						this, 
						workflowManager);
		} else {
			return WizardLauncher.startActivityWizard(
					user, 
					ActivityWizard.MODE_INSPECT, 
					request, 
					this, 
					workflowManager);
		}
	}
	
	
	@Override
	public boolean isStateless() { return false; }
}
