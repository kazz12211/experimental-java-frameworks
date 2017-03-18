package workflow.view;

import java.util.List;

import core.util.ListUtils;
import workflow.controller.Context;
import workflow.model.Activity;
import workflow.model.Request;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.wizard.component.WizardFrameContent;
import ariba.ui.wizard.core.Wizard;

public class ActivityTabs extends AWComponent {
	
	public List<Activity> activities;
	public Activity activity;
	
	protected void awake() {
		activities = ListUtils.list();
		for(Request request : getWorkflow().getSortedSubmittedRequests()) {
			if(request.getAction() != null)
				activities.add(request.getAction());
		}
		if(activities.size() > 0 && activity == null)
			activity = activities.get(0);
		super.awake();
	}
	
	public Workflow getWorkflow() {
		return (Workflow) valueForBinding("workflow");
	}
	public String[] models() {
		return (String[]) valueForBinding("models");
	}
	public String[] labels() {
		return (String[]) valueForBinding("labels");
	}
	public String[] components() {
		return (String[]) valueForBinding("components");
	}
	
	private int indexOfActivity() {
		String[] models = this.models();
		for(int i = 0; i < models.length; i++) {
			String model = models[i];
			if(model.equals(activity.getClass().getName()))
				return i;
		}
		return -1;
	}
	
	public String getLabel() {
		String[] labels = this.labels();
		int index = this.indexOfActivity();
		if(index >= 0)
			return labels[index];
		return "";
	}
	
	public String getComponentName() {
		String[] components = this.components();
		int index = this.indexOfActivity();
		if(index >= 0)
			return components[index];
		return "";
	}
		
	public WizardFrameContent getFrameContent() {
		AWComponent parent = null;
		for(parent = this.parent(); parent != null ; parent = parent.parent()) {
			if(parent instanceof WizardFrameContent) {
				return (WizardFrameContent) parent;
			}
		}
		return null;
	}
	
	
	public Context getContext() {
		WizardFrameContent content = this.getFrameContent();
		if(content != null && content.getContext() instanceof Context) {
			return (Context) content.getContext();
		}
		return null;
	}
	
	public Wizard getWizard() {
		WizardFrameContent content = this.getFrameContent();
		if(content != null) {
			return (Wizard) content.getFrame().getWizard();
		}
		return null;
	}

	public boolean isInFrameContent() {
		return (this.getFrameContent() != null);
	}
	
	public AWResponseGenerating chooseActivity() {
		if(activities.size() > 0 && activity == null) {
			activity = activities.get(0);
		}
		return null;
	}
	
	@Override
	public boolean isStateless() { return false; }
}
