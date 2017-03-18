package workflow.aribaweb.component;

import ariba.ui.aribaweb.core.AWActionCallback;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.widgets.ModalPageWrapper;

public class ModalPage extends AWComponent {

	protected Callback _callback;
	
	public AWResponseGenerating close() {
		ModalPageWrapper.prepareToExit(this);
		_callback.prepare(requestContext());
		return _callback.doneAction(this);
	}
	public void setCaller(AWComponent caller) {
		this._callback = new Callback(caller);
	}
	@Override
	public boolean isStateless() { return false; }
	@Override
	public boolean isClientPanel() { return true; }
	
	public class Callback extends AWActionCallback {
		
		AWComponent caller;
		public Callback(AWComponent caller) {
			super(caller);
			this.caller = caller;
		}
		@Override
		public AWResponseGenerating doneAction(AWComponent sender) {
			return caller.pageComponent();
		}
		
		public AWComponent getCaller() {
			return caller;
		}
	}
}
