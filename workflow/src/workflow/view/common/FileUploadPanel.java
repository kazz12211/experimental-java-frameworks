package workflow.view.common;

import ariba.ui.aribaweb.core.AWActionCallback;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.widgets.ModalPageWrapper;

public class FileUploadPanel extends AWComponent {

	AWActionCallback callback;
	public long maxSize;
	public String fileName;
	public String mimeType;
	public byte[] bytes;
	public boolean fileSizeExceeded;
	
	public void setup(AWActionCallback callback, long uploadMaxSize) {
		this.callback = callback;
		this.maxSize = uploadMaxSize;
	}
	
	public boolean isClientPanel() { return true; }
	
	public AWResponseGenerating done() {
		if(fileSizeExceeded) {
			this.recordValidationError(
					"file", 
					AWLocal.localizedJavaString(1, "Your upload exceeds the maximum allowable size", FileUploadPanel.class, requestContext()), 
					null);
			errorManager().checkErrorsAndEnableDisplay();
			return null;
		}
		
		ModalPageWrapper.prepareToExit(this);
		callback.prepare(requestContext());
		return callback.doneAction(this);
	}
	
}
