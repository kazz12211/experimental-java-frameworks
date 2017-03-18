package workflow.app;

import workflow.model.User;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocalLoginSessionHandler;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.core.AWSession;
import ariba.ui.meta.layouts.MetaNavTabBar;
import ariba.util.log.Log;

public abstract class WorkflowLoginPage extends AWComponent {
	AWLocalLoginSessionHandler.CompletionCallback _callback;
	public void init(AWLocalLoginSessionHandler.CompletionCallback callback) {
		this._callback = callback;
	}

	@Override
	protected boolean shouldValidateSession() {
		return false;
	}
	
	protected abstract User authUser();
	protected abstract void recordAuthError();
	
	public AWResponseGenerating login() {

		User user = authUser();

		if (user == null) {
			this.recordAuthError();
		} else {
			WorkflowUserBinder.bindUserToSession(user, session());
			this.executeLoginHandlers(user, session());
			MetaNavTabBar.invalidateState(session());
			return _callback.proceed(requestContext());
		}
		errorManager().checkErrorsAndEnableDisplay();
		return null;
	}

	public AWResponseGenerating cancel() {
		return _callback.cancel(requestContext());
	}


	protected abstract WorkflowLoginHandler[] createLoginHandlers();
	
	private void executeLoginHandlers(User user, AWSession session) {
		WorkflowLoginHandler[] handlers = this.createLoginHandlers();
		if(handlers != null) {
			for(WorkflowLoginHandler handler : handlers) {
				try {
					handler.execute(user, session);
				} catch (Exception e) {
					Log.customer.error(e);
				}
			}
		}
	}
}
