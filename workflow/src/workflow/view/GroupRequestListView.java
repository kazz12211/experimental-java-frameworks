package workflow.view;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.app.DataTableComponent;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import workflow.view.ActivityModelChooser.WorkflowAndActivityDef;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class GroupRequestListView extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Request> requests;
	public List<Status> statuss;
	public Status statusFilter = null;
	public boolean includeSelf = false;
	public List<WorkflowAndActivityDef> models;
	public WorkflowAndActivityDef selectedModel = null;
	public Date fromDate;
	public Date toDate;
	public WorkflowManager workflowManager;
	
	@Override
	public void init() {
		super.init();
		statuss = ListUtils.list();
		statuss.add(Status.get(Status.REQUESTED));
		statuss.add(Status.get(Status.SUBMITTED));
		statuss.add(Status.get(Status.REJECTED));
		statuss.add(Status.get(Status.EXPIRED));
		statusFilter = statuss.get(0);
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		toDate = new Date();
		fromDate = DateUtils.dateByAddingDays(toDate, -30);
		requests = ListUtils.list();
	}

	@Override
	protected void awake() {
		super.awake();
		this.filterObjects();
	}
	
	private List<User> users() {
		List<User> users = ListUtils.list();
		User me = (User) session().getFieldValue("user");
		if(this.includeSelf) {
			users.add(me);
		}
		List<User> subordinates = me.subordinates();
		users.addAll(subordinates);
		return users;
	}
	
	private void clear() {
		if(this.requests != null)
			this.requests.clear();
	}

	public AWResponseGenerating search() {
		this.filterObjects();
		return null;
	}
	
	private void filterObjects() {
		
		this.clear();
		
		if(fromDate == null || toDate == null || selectedModel == null || statusFilter == null)
			return;
		
		Date from = DateUtils.startTimeOfTheDay(fromDate);
		Date to = DateUtils.endTimeOfTheDay(toDate);
		
		List<User> users = this.users();
		for(User user : users) {
			List<Request> list1 = user.requestsOfStatus(statusFilter.getCode());
			for(Request r : list1) {
				if(!(selectedModel.workflowDef.getModelName().equals(r.getWorkflow().getClassName()) &&
						selectedModel.activityDef.getModelName().equals(r.getActivityClassName())))
					continue;
				if(r.getRequestedDate().compareTo(from) < 0 || r.getRequestedDate().compareTo(to) > 0)
					continue;
				requests.add(r);
			}
		}		
	}

	public AWResponseGenerating inspectWorkflow() {
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				((Request) displayGroup.currentItem()).getWorkflow(), 
				this, 
				workflowManager);
	}

	@Override
	public boolean isStateless() { return false; }

}
