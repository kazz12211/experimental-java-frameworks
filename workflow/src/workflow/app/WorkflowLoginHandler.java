package workflow.app;

import workflow.model.User;
import ariba.ui.aribaweb.core.AWSession;

public interface WorkflowLoginHandler {
	
	public void execute(User user, AWSession session) throws Exception;

}
