package workflow.view.pref;

import java.util.List;

import workflow.controller.WorkflowManager;
import workflow.model.User;

public class PersonInfoView extends AbstractPreferenceView {

	private List<String> availableModels;
	WorkflowManager workflowManager;
		
	@Override
	protected void awake() {
		super.awake();
		User user = (User) session().getFieldValue("user");
		if(workflowManager == null)
			workflowManager = (WorkflowManager) session().getFieldValue("workflowManager");
		availableModels = workflowManager.rules.availableWorkflowModelNames(user);
	}
	
	public List<String> getAvailableModels() {
		return availableModels;
	}
	
	public String getAvailableModelNames() {
		StringBuffer buffer = new StringBuffer();
		for(String name : availableModels) {
			if(buffer.length() > 0)
				buffer.append(", ");
			buffer.append(name);
		}
		return buffer.toString();
	}
	
}
