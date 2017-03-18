package workflow.notif;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import core.util.ListUtils;
import workflow.app.AppConfigManager;
import workflow.mail.AsyncMailSender;
import workflow.mail.ErrorHandler;
import workflow.model.Request;
import workflow.model.User;
import workflow.model.UserPreference;
import workflow.model.Workflow;
import workflow.model.Error;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public class MailNotification implements ErrorHandler {

	private static final String TEMPLATE_REQUEST = "notif_request.xml";
	private static final String TEMPLATE_COMPLETE = "notif_complete.xml";
	private static final String TEMPLATE_ERROR = "notif_error.xml";
	private static final String TEMPLATE_REJECT = "notif_reject.xml";
	private static final String TEMPLATE_EXPIRE = "notif_expire.xml";
	private static final String TEMPLATE_EXPIRE_REQUEST = "notif_expire_req.xml";
	
	private static MailNotification _sharedInstance = new MailNotification();
	
	protected MailNotification() {
		AsyncMailSender.getInstance().getErrorHandler().addErrorHandler(this);
	}
	
	public static MailNotification getInstance() {
		return _sharedInstance;
	}
	
	@Override
	public void handleError(Object object, List<Address> errorEmailAddresses, MimeMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isHandlerForObject(Object object) {
		return this == object;
	}
	
	private MailTemplate loadTemplate(String templateName) throws Exception {
		MailTemplate template = new MailTemplate();
		template.init(templateName);
		return template;
	}

	private List<User> filteredRecepients(List<User> recepients, String prefKey) {
		List<User> filteredRecepients = ListUtils.list();
		for(User user : recepients) {
			UserPreference pref = user.getUserPreference(prefKey);
			if(pref != null && pref.getBooleanValue() == true && user.getEmail() != null) {
				filteredRecepients.add(user);
			}
		}
		return filteredRecepients;
	}
	
	private void sendMessageWithTemplate(User recepient, String templateName, String[] subjectParams, String[] bodyParams) {
		boolean flag = AppConfigManager.getInstance().getBoolean(AppConfigManager.WORKFLOW_NOTIFICATION, true);
		if(!flag) {
			return;
		}

		MailTemplate template = null;
		try {
			template = this.loadTemplate(templateName);
		} catch (Exception e) {
			Log.customer.error("MailNotification: could not load mail template '" + templateName + "' does not exist. The notification has not been sent.", e);
			return;
		}
		String subject = Fmt.S(template.subject(), subjectParams);
		String message = template.merge(bodyParams);
		try {
			AsyncMailSender.getInstance().sendMessage(this, recepient.getEmail(), subject, message);
		} catch (Exception e) {
			Log.customer.error("MailNotification: could not send message to '" + recepient.getEmail() + "'", e);
		}
		
	}
	public void sendRequestNotification(List<User> recepients, Request request) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_REQUEST);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The request notification has not been sent.");
			return;
		}
		
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {request.getWorkflow().getName()};
			String bodyParams[] = new String[] {recepient.getName(), request.getWorkflow().getName(), request.getWorkflow().getTitle(), request.getName()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_REQUEST, subjectParams, bodyParams);
		}
		
	}

	public void sendCompleteNotification(List<User> recepients,
			Workflow workflow) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_COMPLETE);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The complete notification has not been sent.");
			return;
		}
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {workflow.getName()};
			String bodyParams[] = new String[] {recepient.getName(), workflow.getName(), workflow.getTitle()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_COMPLETE, subjectParams, bodyParams);
		}
		
	}

	public void sendErrorNotification(List<User> recepients,
			Workflow workflow, Error error) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_ERROR);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The error notification has not been sent.");
			return;
		}
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {workflow.getName()};
			String bodyParams[] = new String[] {recepient.getName(), workflow.getName(), workflow.getTitle(), error.getDescription()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_ERROR, subjectParams, bodyParams);
		}
		
	}

	public void sendRejectNotification(List<User> recepients,
			Workflow workflow) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_REJECT);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The reject notification has not been sent.");
			return;
		}
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {workflow.getName()};
			String bodyParams[] = new String[] {recepient.getName(), workflow.getName(), workflow.getTitle()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_REJECT, subjectParams, bodyParams);
		}
	}

	public void sendExpireNotification(List<User> recepients, Request request) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_REJECT);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The expire notification has not been sent.");
			return;
		}
		
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {request.getWorkflow().getName()};
			String bodyParams[] = new String[] {recepient.getName(), request.getWorkflow().getName(), request.getName()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_EXPIRE_REQUEST, subjectParams, bodyParams);
		}
	}

	public void sendExpireNotification(List<User> recepients, Workflow workflow) {
		List<User> filteredRecepients = this.filteredRecepients(recepients, UserPreference.KEY_NOTIF_EXPIRE);
		if(filteredRecepients.isEmpty()) {
			Log.customer.info("MailNotification : recepients are empty. The expire notification has not been sent.");
			return;
		}
		for(User recepient : filteredRecepients) {
			String subjectParams[] = new String[] {workflow.getName()};
			String bodyParams[] = new String[] {recepient.getName(), workflow.getName(), workflow.getTitle()};
			this.sendMessageWithTemplate(recepient, TEMPLATE_EXPIRE, subjectParams, bodyParams);
		}
	}

}
