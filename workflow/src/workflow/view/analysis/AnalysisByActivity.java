package workflow.view.analysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.model.Activity;
import workflow.model.Status;
import workflow.view.ActivityModelChooser.WorkflowAndActivityDef;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class AnalysisByActivity extends AWComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Activity> activities;
	public WorkflowAndActivityDef selectedModel;
	public Date startDate;
	public Date endDate;
	public Boolean includeArchives;
	public Boolean submittedOnly;
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
				AWLocal.localizedJavaString(1, "By Activity", AnalysisByActivity.class, requestContext()),
				new String[] {"name", "request.workflow.name", "actor.name"},
				new String[] {},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(2, "By Actor", AnalysisByActivity.class, requestContext()),
				new String[] {"actor.name", "name", "request.workflow.name"},
				new String[] {},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(3, "By Workflow-Activity", AnalysisByActivity.class, requestContext()),
				new String[] {"actor.name"},
				new String[] {"request.workflow.name", "name"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(4, "By Workflow-Actor", AnalysisByActivity.class, requestContext()),
				new String[] {"name"},
				new String[] {"request.workflow.name", "actor.name"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(5, "By Activity (Year/Month request base)", AnalysisByActivity.class, requestContext()),
				new String[] {"name", "request.workflow.name", "actor.name"},
				new String[] {"request.requestedYear", "request.requestedMonth"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(6, "By Actor (Year/Month request base)", AnalysisByActivity.class, requestContext()),
				new String[] {"actor.name", "name", "request.workflow.name"},
				new String[] {"request.requestedYear", "request.requestedMonth"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(7, "By Activity (Year/Month submit base)", AnalysisByActivity.class, requestContext()),
				new String[] {"name", "request.workflow.name", "actor.name"},
				new String[] {"request.submittedYear", "request.submittedMonth"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(8, "By Actor (Year/Month submit base)", AnalysisByActivity.class, requestContext()),
				new String[] {"actor.name", "name", "request.workflow.name"},
				new String[] {"request.submittedYear", "request.submittedMonth"},
				new String[] {"request.requestedDate", "request.submittedDate", "request.handledTime"}
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
		if(selectedModel != null) {
			predicates.add(new Predicate.KeyValue("request.activityClassName", selectedModel.activityDef.getModelName()));
			predicates.add(new Predicate.KeyValue("request.workflow.className", selectedModel.workflowDef.getModelName()));
		}
		predicates.add(new Predicate.KeyValue("request.requestedDate", from,
				Predicate.Operator.Gte));
		predicates.add(new Predicate.KeyValue("request.requestedDate", to,
				Predicate.Operator.Lte));
		if (!includeArchives.booleanValue())
			predicates.add(new Predicate.KeyValue("request.workflow.archived",
					new Boolean(false)));
		if (submittedOnly.booleanValue()) {
			Predicate submitPred = new Predicate.Or(
					new Predicate.KeyValue("request.status", Status.get(Status.SUBMITTED)),
					new Predicate.KeyValue("request.status", Status.get(Status.REJECTED)));
			predicates.add(submitPred);
		}
		predicates.add(new Predicate.KeyValue("request.workflow.deleted", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("request.workflow.hidden", new Boolean(false)));
		QuerySpecification spec = new QuerySpecification(
				Activity.class.getName(), new Predicate.And(predicates));
		activities = ObjectContext.get().executeQuery(spec);
		return null;
	}
	
	public AWResponseGenerating inspect() {
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				((Activity) displayGroup.currentItem()).getRequest().getWorkflow(), 
				this, 
				manager);
	}

	@Override
	public boolean isStateless() { return false; }

}
