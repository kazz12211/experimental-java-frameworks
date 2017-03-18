package workflow.view;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.app.DataTableComponent;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class GroupWorkflowListView extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Workflow> workflows;
	public List<Status> statuss;
	public Status statusFilter = null;
	public WorkflowDef selectedModel = null;
	public Date fromDate;
	public Date toDate;
	public boolean includeSelf = false;
	private WorkflowManager workflowManager;

	@Override
	public void init() {
		super.init();
		statuss = ListUtils.list();
		statuss.add(Status.get(Status.SAVED));
		statuss.add(Status.get(Status.SUBMITTED));
		statuss.add(Status.get(Status.COMPLETED));
		statuss.add(Status.get(Status.REJECTED));
		statuss.add(Status.get(Status.PENDING));
		statuss.add(Status.get(Status.EXPIRED));
		statuss.add(Status.get(Status.ERROR));
		statusFilter = statuss.get(1);
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		toDate = new Date();
		fromDate = DateUtils.dateByAddingDays(toDate, -30);
	}
	
	@Override
	protected void awake() {
		super.awake();
		this.filterObjects();
	}
	
	public AWResponseGenerating search() {
		this.filterObjects();
		return null;
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
		if(workflows != null)
			workflows.clear();
		else
			workflows = ListUtils.list();
	}
	
	private void filterObjects() {
		this.clear();
		
		if(fromDate == null || toDate == null || statusFilter == null || selectedModel == null) {
			return;
		}
		
		List<Predicate> predicates = ListUtils.list();
		predicates.add(new Predicate.KeyValue("className", selectedModel.getModelName()));
		predicates.add(new Predicate.KeyValue("status.code", statusFilter.getCode()));
		Date from = DateUtils.startTimeOfTheDay(fromDate);
		Date to = DateUtils.endTimeOfTheDay(toDate);
		predicates.add(new Predicate.KeyValue("createdDate", from, Predicate.Operator.Gte));
		predicates.add(new Predicate.KeyValue("createdDate", to, Predicate.Operator.Lte));
		predicates.add(new Predicate.KeyValue("deleted", new Boolean(false)));
		
		List<User> users = this.users();
		for(User user : users) {
			List<Predicate> userPredicates = ListUtils.list();
			userPredicates.add(new Predicate.KeyValue("creator", user));
			userPredicates.addAll(predicates);
			QuerySpecification spec = new QuerySpecification(Workflow.class.getName(), new Predicate.And(userPredicates));
			List<Workflow> result = ObjectContext.get().executeQuery(spec);
			workflows.addAll(result);
		}
	}
	
	public AWResponseGenerating inspect() {
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				(Workflow) displayGroup.currentItem(), 
				this, 
				workflowManager);
	}

	@Override
	public boolean isStateless() { return false; }

}
