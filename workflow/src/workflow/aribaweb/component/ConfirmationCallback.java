package workflow.aribaweb.component;

import java.lang.reflect.Method;

import ariba.ui.aribaweb.core.AWActionCallback;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.util.log.Log;

public class ConfirmationCallback extends AWActionCallback {
	
	AWComponent caller;
	private String okAction;
	private String cancelAction;
	
	public ConfirmationCallback(AWComponent caller, String okAction, String cancelAction) {
		super(caller);
		this.caller = caller;
		this.okAction = okAction;
		this.cancelAction = cancelAction;
	}
	
	public AWResponseGenerating okAction(AWComponent sender) {
		if(okAction != null) {
			Method method = null;
			try {
				method = methodNamed(okAction);
			} catch (Exception e) {
				Log.customer.warn("ConfirmationCallback: could not find callback method '" + okAction + "' on component '" + sender.getClass().getName() + "'");
			}
			if(method != null) {
				try {
					method.invoke(caller, new Object[0]);
				} catch (Exception e) {
					Log.customer.warn("ConfirmationCallback: error while executing callback method '" + okAction + "' on component '" + sender.getClass().getName() + "'", e);
				}
			}
		}
		return doneAction(sender);
	}
	public AWResponseGenerating cancelAction(AWComponent sender) {
		if(cancelAction != null) {
			Method method = null;
			try {
				method = methodNamed(cancelAction);
			} catch (Exception e) {
				Log.customer.warn("ConfirmationCallback: could not find callback method '" + cancelAction + "' on component '" + sender.getClass().getName() + "'");
			}
			if(method != null) {
				try {
					method.invoke(caller, new Object[0]);
				} catch (Exception e) {
					Log.customer.warn("ConfirmationCallback: error while executing callback method '" + cancelAction + "' on component '" + sender.getClass().getName() + "'", e);
				}
			}
		}
		return doneAction(sender);
	}
	
	@Override
	public AWResponseGenerating doneAction(AWComponent sender) {		
		return caller.pageComponent();
	}

	private Method methodNamed(String methodName) throws Exception {
		Method method = caller.getClass().getDeclaredMethod(methodName, new Class[0]);
		return method;
	}
}
