package workflow.mail;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

public interface ErrorHandler {

	public void handleError(Object object, List<Address> errorEmailAddresses, MimeMessage message);

	public boolean isHandlerForObject(Object object);
}
