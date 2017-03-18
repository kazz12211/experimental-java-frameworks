package workflow.view.management;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.app.DataTableComponent;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.model.Activity;
import workflow.model.Request;
import workflow.model.Status;
import workflow.view.ActivityModelChooser.WorkflowAndActivityDef;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class ActivitySearch extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Activity> activities;
	public WorkflowAndActivityDef selectedModel;
	public Date requestedFrom;
	public Date requestedTo;
	public String titleQueryStr;
	public List<Status> statuss;
	public Status selectedStatus;
	public Boolean includeArchives;
	private WorkflowManager workflowManager;
	
	
	@Override
	public void init() {
		super.init();
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		statuss = ListUtils.list();
		statuss.add(Status.get(Status.SUBMITTED));
		statuss.add(Status.get(Status.REJECTED));
		statuss.add(Status.get(Status.EXPIRED));
		selectedStatus = statuss.get(0);
	}

	public AWResponseGenerating search() {
		Date from = null;
		Date to = null;
		if(requestedFrom != null) {
			from = DateUtils.startTimeOfTheDay(requestedFrom);
		}
		if(requestedTo != null) {
			to = DateUtils.endTimeOfTheDay(requestedTo);
		}
		
		List<Predicate> predicates = ListUtils.list();
		
		if(selectedModel != null) {
			predicates.add(new Predicate.KeyValue("request.activityClassName", selectedModel.activityDef.getModelName()));
			predicates.add(new Predicate.KeyValue("request.workflow.className", selectedModel.workflowDef.getModelName()));
		}
		if(selectedStatus != null)
			predicates.add(new Predicate.KeyValue("request.status", selectedStatus));
		if(titleQueryStr != null)
			predicates.add(new Predicate.KeyValue("request.workflow.title", "*"+titleQueryStr+"*"));
		if(from != null)
			predicates.add(new Predicate.KeyValue("request.requestedDate", from, Predicate.Operator.Gte));
		if(to != null)
			predicates.add(new Predicate.KeyValue("request.requestedDate", to, Predicate.Operator.Lte));
		if(includeArchives == null || includeArchives.booleanValue() == false)
			predicates.add(new Predicate.KeyValue("request.workflow.archived", new Boolean(false)));

		predicates.add(new Predicate.KeyValue("request.workflow.hidden", new Boolean(false)));
		QuerySpecification spec = new QuerySpecification(Activity.class.getName(), new Predicate.And(predicates));
		activities = ObjectContext.get().executeQuery(spec);
		return null;
	}

	public AWResponseGenerating inspect() {
		Activity activity = (Activity) displayGroup.currentItem();
		Request request = activity.getRequest();
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				request.getWorkflow(), 
				this, 
				workflowManager);
	}
	@Override
	public boolean isStateless() { return false; }

}
