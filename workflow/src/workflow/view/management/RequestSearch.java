package workflow.view.management;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.app.DataTableComponent;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Request;
import workflow.model.Status;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class RequestSearch extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Request> requests;
	public WorkflowDef selectedModel;
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
		statuss.add(Status.get(Status.REQUESTED));
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
		
		if(selectedModel != null)
			predicates.add(new Predicate.KeyValue("workflow.className", selectedModel.getModelName()));
		if(selectedStatus != null)
			predicates.add(new Predicate.KeyValue("status", selectedStatus));
		if(titleQueryStr != null)
			predicates.add(new Predicate.KeyValue("workflow.title", "*"+titleQueryStr+"*"));
		if(from != null)
			predicates.add(new Predicate.KeyValue("requestedDate", from, Predicate.Operator.Gte));
		if(to != null)
			predicates.add(new Predicate.KeyValue("requestedDate", to, Predicate.Operator.Lte));
		if(includeArchives == null || includeArchives.booleanValue() == false)
			predicates.add(new Predicate.KeyValue("workflow.archived", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("workflow.hidden", new Boolean(false)));

		QuerySpecification spec = new QuerySpecification(Request.class.getName(), new Predicate.And(predicates));
		requests = ObjectContext.get().executeQuery(spec);
		return null;
	}

	public AWResponseGenerating inspect() {
		Request request = (Request) displayGroup.currentItem();
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				request.getWorkflow(), 
				this, 
				workflowManager);
	}

	@Override
	public boolean isStateless() { return false; }

}
