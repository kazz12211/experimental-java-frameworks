package workflow.aribaweb.component;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;

public class ConfirmationPanel extends AWComponent {
	ConfirmationCallback callback;
	String message;
	
	public static AWResponseGenerating run(AWComponent caller, String message, String okAction, String cancelAction) {
		ConfirmationPanel panel = (ConfirmationPanel) caller.pageWithName("ConfirmationPanel");
		panel.message = message;
		panel.callback = new ConfirmationCallback(caller, okAction, cancelAction);
		return panel;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public boolean isStateless() { return false; }
	@Override
	public boolean isClientPanel() { return true; }
	
	public AWResponseGenerating ok() {
		return callback.okAction(this);
	}
	public AWResponseGenerating cancel() {
		return callback.cancelAction(this);
	}

}
