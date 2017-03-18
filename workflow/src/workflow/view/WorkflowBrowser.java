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
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class WorkflowBrowser extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Workflow> workflows;
	public WorkflowDef selectedModel;
	public Date createdFrom;
	public Date createdTo;
	public String titleQueryStr;
	public List<Status> statuss;
	public Status selectedStatus;
	private WorkflowManager workflowManager;

	@Override
	public void init() {
		super.init();
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		statuss = ListUtils.list();
		statuss.add(Status.get(Status.SAVED));
		statuss.add(Status.get(Status.SUBMITTED));
		statuss.add(Status.get(Status.COMPLETED));
		statuss.add(Status.get(Status.REJECTED));
		statuss.add(Status.get(Status.PENDING));
		statuss.add(Status.get(Status.EXPIRED));
		statuss.add(Status.get(Status.ERROR));
		selectedStatus = statuss.get(1);
		createdTo = new Date();
		createdFrom = DateUtils.dateByAddingDays(createdTo, -30);
	}

	public AWResponseGenerating search() {
		Date from = null;
		Date to = null;
		if(createdFrom != null) {
			from = DateUtils.startTimeOfTheDay(createdFrom);
		}
		if(createdTo != null) {
			to = DateUtils.endTimeOfTheDay(createdTo);
		}
		
		List<Predicate> predicates = ListUtils.list();
		
		if(selectedModel != null)
			predicates.add(new Predicate.KeyValue("className", selectedModel.getModelName()));
		if(selectedStatus != null)
			predicates.add(new Predicate.KeyValue("status", selectedStatus));
		if(titleQueryStr != null)
			predicates.add(new Predicate.KeyValue("title", "*"+titleQueryStr+"*"));
		if(from != null)
			predicates.add(new Predicate.KeyValue("createdDate", from, Predicate.Operator.Gte));
		if(to != null)
			predicates.add(new Predicate.KeyValue("createdDate", to, Predicate.Operator.Lte));
		predicates.add(new Predicate.KeyValue("archived", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("deleted", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("hidden", new Boolean(false)));
		QuerySpecification spec = new QuerySpecification(Workflow.class.getName(), new Predicate.And(predicates));
		workflows = ObjectContext.get().executeQuery(spec);
		return null;
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
