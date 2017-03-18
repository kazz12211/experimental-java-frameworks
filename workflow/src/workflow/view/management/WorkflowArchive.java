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

public class WorkflowArchive extends DataTableComponent {

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
		predicates.add(new Predicate.KeyValue("archived", new Boolean(true)));
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
	
	
	public boolean unableToRemove() {
		List<Workflow> selection = displayGroup.selectedObjects();
		if(selection == null || selection.isEmpty())
			return true;
		for(Workflow w : selection) {
			if(w.isDeleted())
				return true;
		}
		return false;
	}

	public boolean unableToUnarchive() {
		List<Workflow> selection = displayGroup.selectedObjects();
		if(selection == null || selection.isEmpty())
			return true;
		for(Workflow w : selection) {
			if(!w.isArchived())
				return true;
		}
		return false;
	}
	
	public AWResponseGenerating removeSelection() {
		String message = AWLocal.localizedJavaString(1, "Do you really want to move the workflow to trash?", WorkflowArchive.class, requestContext());
		return ConfirmationPanel.run(this, message, "doRemoveSelection", null);
	}
	
	public void doRemoveSelection() {
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			User user = w.getCreator();
			this.workflowManager.remove(user, w);
		}
		ObjectContext.get().save();
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			this.workflowManager.deleted(w);
		}
		this.search();
	}
		
	public AWResponseGenerating unarchiveSelection() {
		String message = AWLocal.localizedJavaString(2, "Do you really want to unarchive the workflow?", WorkflowArchive.class, requestContext());
		return ConfirmationPanel.run(this, message, "doUnarchiveSelection", null);
	}
	
	public void doUnarchiveSelection() {
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow)o;
			w.setArchived(new Boolean(false));
		}
		ObjectContext.get().save();
		this.search();
	}
	
	
	@Override
	public boolean isStateless() { return false; }

}
