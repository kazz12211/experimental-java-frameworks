package workflow.view.management;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import core.util.ListUtils;
import workflow.app.DataTableComponent;
import workflow.aribaweb.component.ConfirmationPanel;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class WorkflowTrash extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Workflow> workflows;
	public WorkflowDef selectedModel;
	public Date createdFrom;
	public Date createdTo;
	public String titleQueryStr;
	private WorkflowManager workflowManager;
	
	
	@Override
	public void init() {
		super.init();
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
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
		if(titleQueryStr != null)
			predicates.add(new Predicate.KeyValue("title", "*"+titleQueryStr+"*"));
		if(from != null)
			predicates.add(new Predicate.KeyValue("createdDate", from, Predicate.Operator.Gte));
		if(to != null)
			predicates.add(new Predicate.KeyValue("createdDate", to, Predicate.Operator.Lte));
		predicates.add(new Predicate.KeyValue("deleted", new Boolean(true)));
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
	
	
	public boolean unableToUnremove() {
		List<Workflow> selection = displayGroup.selectedObjects();
		if(selection == null || selection.isEmpty())
			return true;
		for(Workflow w : selection) {
			if(!w.isDeleted())
				return true;
		}
		return false;
	}
	
	public AWResponseGenerating unremoveSelection() {
		String message = AWLocal.localizedJavaString(1, "Do you really want to get the workflow out of trash?", WorkflowTrash.class, requestContext());
		return ConfirmationPanel.run(this, message, "doUnremoveSelection", null);
	}

	public void doUnremoveSelection() {
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			User user = w.getCreator();
			this.workflowManager.unremove(user, w);
		}
		ObjectContext.get().save();
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			this.workflowManager.undeleted(w);
		}
		this.search();
	}
	
	public AWResponseGenerating reallyRemoveSelection() {
		String message = AWLocal.localizedJavaString(2, "Do you really want to delete the workflow?", WorkflowTrash.class, requestContext());
		return ConfirmationPanel.run(this, message, "doReallyRemoveSelection", null);
	}
	public void doReallyRemoveSelection() {
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			this.workflowManager.reallyRemove(w);
		}
		ObjectContext.get().save();
		this.search();
	}
	
	@Override
	public boolean isStateless() { return false; }

}
