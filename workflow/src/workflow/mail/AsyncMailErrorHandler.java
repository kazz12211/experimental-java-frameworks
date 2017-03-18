package workflow.mail;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import ariba.util.core.ListUtil;

public class AsyncMailErrorHandler implements ErrorHandler {

	private List<ErrorHandler> delegates;
	
	public AsyncMailErrorHandler() {
		delegates = ListUtil.list();
	}
	
	public void addErrorHandler(ErrorHandler errorHandler) {
		delegates.add(errorHandler);
	}
	
	public void removeErrorHandler(ErrorHandler errorHandler) {
		delegates.remove(errorHandler);
	}
	
	@Override
	public void handleError(Object object, List<Address> errorEmailAddresses, MimeMessage message) {
		for(ErrorHandler handler : delegates) {
			if(handler.isHandlerForObject(object)) {
				handler.handleError(object, errorEmailAddresses, message);
				return;
			}
		}
	}

	@Override
	public boolean isHandlerForObject(Object object) {
		return true;
	}

}
