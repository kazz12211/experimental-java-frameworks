package workflow.view.analysis;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class AnalysisByWorkflow extends AWComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Workflow> workflows;
	public WorkflowDef selectedModel;
	public Date startDate;
	public Date endDate;
	public Boolean includeArchives;
	private WorkflowManager manager;
	public List<PivotLayout> layouts;
	public PivotLayout selectedLayout;
	public PivotLayout iter;
	public boolean layoutChangeLatch;
	
	@Override
	public void init() {
		super.init();
		manager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		selectedModel = null;
		endDate = new Date();
		startDate = DateUtils.dateByAddingDays(endDate, -30);

		layouts = ListUtils.list();
		PivotLayout layout;
		layout = new PivotLayout(
				AWLocal.localizedJavaString(1, "By Workflow", AnalysisByWorkflow.class, requestContext()),
				new String[] {"name", "creator.name"},
				new String[] {"status.label"},
				new String[] {"title", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(2, "By Creator", AnalysisByWorkflow.class, requestContext()),
				new String[] {"creator.name", "name"},
				new String[] {"status.label"},
				new String[] {"title", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(3, "By Status", AnalysisByWorkflow.class, requestContext()),
				new String[] {"status.label", "name"},
				new String[] {},
				new String[] {"title", "creator.name", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(4, "By Workflow (Year/Month)", AnalysisByWorkflow.class, requestContext()),
				new String[] {"name", "creator.name", "status.label"},
				new String[] {"submittedYear", "submittedMonth"},
				new String[] {"title", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(5, "By Creator (Year/Month)", AnalysisByWorkflow.class, requestContext()),
				new String[] {"creator.name", "name", "status.label"},
				new String[] {"submittedYear", "submittedMonth"},
				new String[] {"title", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(6, "By Status (Year/Month)", AnalysisByWorkflow.class, requestContext()),
				new String[] {"status.label", "name"},
				new String[] {"submittedYear", "submittedMonth"},
				new String[] {"title", "creator.name", "requester.name", "submittedDate", "rejectedDate", "completedDate", "elapsedTime", "handledTime"}
				);
		layouts.add(layout);
		selectedLayout = layouts.get(0);
	}
	
	public AWResponseGenerating layoutChanged() {
		layoutChangeLatch = true;
		return null;
	}

	public AWResponseGenerating search() {
		Date from = DateUtils.startTimeOfTheDay(startDate);
		Date to = DateUtils.endTimeOfTheDay(endDate);
		List<Predicate> predicates = ListUtils.list();
		if(selectedModel != null)
			predicates.add(new Predicate.KeyValue("className", selectedModel
				.getModelName()));
		predicates.add(new Predicate.KeyValue("submittedDate", from,
				Predicate.Operator.Gte));
		predicates.add(new Predicate.KeyValue("submittedDate", to,
				Predicate.Operator.Lte));
		if (!includeArchives.booleanValue())
			predicates.add(new Predicate.KeyValue("archived",
					new Boolean(false)));
		predicates.add(new Predicate.KeyValue("deleted", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("hidden", new Boolean(false)));
		QuerySpecification spec = new QuerySpecification(
				Workflow.class.getName(), new Predicate.And(predicates));
		workflows = ObjectContext.get().executeQuery(spec);
		return null;
	}
	
	public AWResponseGenerating inspect() {
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				(Workflow) displayGroup.currentItem(), 
				this, 
				manager);
	}

	@Override
	public boolean isStateless() { return false; }

}
