package workflow.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.ListUtils;
import core.util.ListFilter;
import workflow.app.DataTableComponent;
import workflow.aribaweb.component.ConfirmationPanel;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Counts;
import workflow.model.Status;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.core.Fmt;
import ariba.util.fieldvalue.FieldValue;

public class WorkflowListView extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<StatusData> statuss;
	public StatusData statusFilter = null;
	public WorkflowManager workflowManager;
	public WorkflowDef modelDefForCreation;
	public List<WorkflowDef> availableModels;
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
		statuss.add(new StatusData(Status.get(Status.SAVED)));
		statuss.add(new StatusData(Status.get(Status.SUBMITTED)));
		statuss.add(new StatusData(Status.get(Status.COMPLETED)));
		statuss.add(new StatusData(Status.get(Status.REJECTED)));
		statuss.add(new StatusData(Status.get(Status.PENDING)));
		statuss.add(new StatusData(Status.get(Status.EXPIRED)));
		statuss.add(new StatusData(Status.get(Status.ERROR)));
		statusFilter = statuss.get(1);
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
	}
		
	@Override
	protected void awake() {
		super.awake();
		User user = (User) FieldValue.getFieldValue(session(), "user");
		availableModels = workflowManager.rules.availableWorkflowModels(user);
		Collections.sort(availableModels, new Comparator<WorkflowDef>() {
			@Override
			public int compare(WorkflowDef arg0, WorkflowDef arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}});
	}
	
	public void setStatusFilter(StatusData status) {
		this.statusFilter = status;
	}

	public List<Workflow> filteredObjects() {
		User user = (User) session().getFieldValue("user");
		List<Workflow> filteredObjects;
		if(statusFilter == null)
			filteredObjects = user.viewableWorkflows();
		else
			filteredObjects = user.workflowsOfStatus(statusFilter.code);
		
		if(modelFilter != null) {
			filteredObjects = (List<Workflow>) ListUtils.filteredList(filteredObjects, new ListFilter<Workflow>() {
				@Override
				public boolean filter(Workflow object) {
					return modelFilter.getModelName().equals(object.getClassName());
				}});
		}
		return filteredObjects;
	}
	
	public boolean unableToDeleteWorkflows() {
		User user = (User) FieldValue.getFieldValue(session(), "user");
		if(user == null)
			return true;
		if(displayGroup.selectedObjects().size() == 0)
			return true;
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow) o;
			if(w.enableToDelete() == false)
				return true;
		}
		
		return false;
	}
	
	public boolean unableToWithdrawWorkflows() {
		User user = (User) FieldValue.getFieldValue(session(), "user");
		if(user == null)
			return true;
		if(displayGroup.selectedObjects().size() == 0)
			return true;
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow) o;
			if(w.enableToWithdraw() == false)
				return true;
		}
		
		return false;
	}
	
	public AWResponseGenerating removeSelection() {
		String format = AWLocal.localizedJavaString(1, "Do you really want to remove these %s workflows?", WorkflowListView.class, requestContext());
		String message = Fmt.S(format, displayGroup.selectedObjects().size());
		return ConfirmationPanel.run(this, message, "doRemoveSelection", null);
	}
	
	public void doRemoveSelection() {
		User user = (User) FieldValue.getFieldValue(session(), "user");
		for(Object o : displayGroup.selectedObjects()) {
			this.workflowManager.remove(user, (Workflow) o);
		}
		ObjectContext.get().save();
		for(Object o : displayGroup.selectedObjects()) {
			this.workflowManager.deleted((Workflow) o);
		}
	}
	
	public AWResponseGenerating withdrawSelection() {
		String format = AWLocal.localizedJavaString(2, "Do you really want to withdraw these %s workflows?", WorkflowListView.class, requestContext());
		String message = Fmt.S(format, displayGroup.selectedObjects().size());
		return ConfirmationPanel.run(this, message, "doWithdrawSelection", null);
	}
	
	public void doWithdrawSelection() {
		List<User> creators = ListUtils.list();
		for(Object o : displayGroup.selectedObjects()) {
			Workflow w = (Workflow) o;
			w.willWithdraw(this.requestContext());
			this.workflowManager.withdraw(w);
		}
		ObjectContext.get().save();
		for(Object o : displayGroup.selectedObjects()) {
			this.workflowManager.withdrawn((Workflow) o);
			if(((Workflow) o).getCreator() != null)
				creators.add(((Workflow) o).getCreator());
		}
		for(User user : creators) {
			Counts.createOrUpdate(user, ObjectContext.get());
		}
	}
	
	
	public AWResponseGenerating inspect() {
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				(Workflow) displayGroup.currentItem(), 
				this, 
				workflowManager);
	}

	public AWResponseGenerating edit() {
		if(workflowManager.isEditable((Workflow) displayGroup.currentItem())) {
			return WizardLauncher.startWorkflowWizard(
					WorkflowWizard.MODE_EDIT, 
					(Workflow) displayGroup.currentItem(), 
					this, 
					workflowManager);
		} else {
			return this.inspect();
		}
	}
	
	public AWResponseGenerating makeCopy() {
		Workflow workflow = (Workflow) displayGroup.currentItem();
		String fmt = AWLocal.localizedJavaString(1000, "Copy of %s", WorkflowListView.class, requestContext());
		String title = Fmt.S(fmt, workflow.getTitle());
		return WizardLauncher.copyWorkflowWizard(
				(User) session().getFieldValue("user"), 
				workflow, 
				title,
				this, 
				workflowManager);
	}
	
	public AWResponseGenerating createNew() {
		if(this.modelDefForCreation == null)
			return null;
		User user = (User) FieldValue.getFieldValue(session(), "user");
		return WizardLauncher.startWorkflowWizard(user, modelDefForCreation, this, workflowManager);
	}
	
	@Override
	public boolean isStateless() { return false; }
	
	
}
