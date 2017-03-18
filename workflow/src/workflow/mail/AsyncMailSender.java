package workflow.mail;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.artesware.mail.MailAttachment;
import com.artesware.mail.MailDataAttachment;
import com.artesware.mail.MailDelivery;
import com.artesware.mail.MailDeliveryHTML;
import com.artesware.mail.MailDeliveryPlainText;
import com.artesware.mail.MailFileAttachment;
import com.artesware.mail.MailSender;

public class AsyncMailSender {

	private static AsyncMailSender _sharedInstance = null;
	private ErrorHandler errorHandler;
	
	public static int currentQueueSize() {
		return MailSender.sharedInstance().status().currentQueueSize();
	}
	public static int errorCount() {
		return MailSender.sharedInstance().status().errorCount();
	}
	public static int mailCount() {
		return MailSender.sharedInstance().status().mailCount();
	}
	public static boolean isRunning() {
		return MailSender.sharedInstance().isRunning();
	}
	
	public static AsyncMailSender getInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new AsyncMailSender();
			_sharedInstance.errorHandler = new AsyncMailErrorHandler();
			MailDelivery.setCallBackObjectWithMethod(_sharedInstance.errorHandler, "handleError");
		}
		return _sharedInstance;
	}
	
	public AsyncMailErrorHandler getErrorHandler() {
		return (AsyncMailErrorHandler) errorHandler;
	}
	
	public void sendMessage(
			Object source,
			String to, 
			String subject, 
			String message) throws Exception {
		sendMessage(source, to, null, subject, message);
	}
	public void sendMessage(
			Object source,
			String to, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		sendMessage(source, to, null, ccs, bccs, subject, message);
	}
	
	public void sendMessage(
			Object source,
			String to, 
			String from, 
			String subject, 
			String message) throws Exception {
		sendMessage(source, to, from, null, subject, message);
	}
	public void sendMessage(
			Object source,
			String to, 
			String from, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		sendMessage(source, to, from, null, ccs, bccs, subject, message);
	}	
	public void sendMessage(
			Object source,
			String to, 
			String from, 
			String replyTo, 
			String subject, 
			String message) throws Exception {
		String addresses[] = new String[]{to};
		sendMessage(source, addresses, from, replyTo, subject, message);
	}
	public void sendMessage(
			Object source,
			String to, 
			String from, 
			String replyTo, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		String addresses[] = new String[]{to};
		sendMessage(source, addresses, from, replyTo, ccs, bccs, subject, message);
	}
	
	public void sendMessage(
			Object source,
			String[] recepients, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		sendMessage(source, recepients, null, ccs, bccs, subject, message);
	}

	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String subject, 
			String message) throws Exception {
		sendMessage(source, recepients, from, null, subject, message);
	}
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		sendMessage(source, recepients, from, null, ccs, bccs, subject, message);
	}

	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String subject, 
			String message) throws Exception {
		sendMessage(source, recepients, from, replyTo, subject, message, null, null);
	}
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message) throws Exception {
		sendMessage(source, recepients, from, replyTo, ccs, bccs, subject, message, null, null);
	}
	
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String subject, 
			String message,
			String path,
			String attachmentFilename) throws Exception {
		
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryPlainText delivery = new MailDeliveryPlainText();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		delivery.setSubject(subject);
		delivery.setTextContent(message);
		if(path != null && attachmentFilename != null) {
			File file = new File(path);
			if(file.exists()) {
				MailFileAttachment attachment = new MailFileAttachment(attachmentFilename, null, file);
				delivery.addAttachment(attachment);
			}
		}
		
		delivery.setAnObject(source);
		delivery.sendMail();
	}
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message,
			String path,
			String attachmentFilename) throws Exception {
		
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryPlainText delivery = new MailDeliveryPlainText();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		if(ccs != null)
			delivery.setCCAddresses(Arrays.asList(ccs));
		if(bccs != null)
			delivery.setBCCAddresses(Arrays.asList(bccs));
		delivery.setSubject(subject);
		delivery.setTextContent(message);
		if(path != null && attachmentFilename != null) {
			File file = new File(path);
			if(file.exists()) {
				MailFileAttachment attachment = new MailFileAttachment(attachmentFilename, null, file);
				delivery.addAttachment(attachment);
			}
		}
		
		delivery.setAnObject(source);
		delivery.sendMail();
	}
	
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo,
			String subject, 
			String message,
			String path,
			byte[] attachmentData,
			String attachmentFilename) throws Exception {
		
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryPlainText delivery = new MailDeliveryPlainText();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		delivery.setSubject(subject);
		delivery.setTextContent(message);
		if(attachmentData != null) {
			MailAttachment attachment = new MailDataAttachment(attachmentFilename, MailAttachment.generateContentId(), attachmentData);
			delivery.addAttachment(attachment);
		}
		
		delivery.setAnObject(source);
		delivery.sendMail();
	}
	
	public void sendMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String[] ccs,
			String[] bccs,
			String subject, 
			String message,
			String path,
			byte[] attachmentData,
			String attachmentFilename) throws Exception {
		
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryPlainText delivery = new MailDeliveryPlainText();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		delivery.setSubject(subject);
		if(ccs != null) {
			delivery.setCCAddresses(Arrays.asList(ccs));
		}
		if(bccs != null) {
			delivery.setBCCAddresses(Arrays.asList(bccs));
		}
		delivery.setTextContent(message);
		if(attachmentData != null) {
			MailAttachment attachment = new MailDataAttachment(attachmentFilename, MailAttachment.generateContentId(), attachmentData);
			delivery.addAttachment(attachment);
		}
		
		delivery.setAnObject(source);
		delivery.sendMail();
	}

	public void sendHTMLMessage(
			Object source,
			String to, 
			String subject, 
			String htmlMessage) throws Exception {
		String addresses[] = new String[]{to};
		sendHTMLMessage(source, addresses, null, subject, htmlMessage);
	}
	
	public void sendHTMLMessage(
			Object source,
			String to, 
			String from, 
			String subject, 
			String htmlMessage) throws Exception {
		String addresses[] = new String[]{to};
		sendHTMLMessage(source, addresses, from, null, subject, htmlMessage);
	}
	
	public void sendHTMLMessage(
			Object source,
			String to, 
			String from, 
			String replyTo, 
			String subject, 
			String htmlMessage) throws Exception {
		String addresses[] = new String[]{to};
		sendHTMLMessage(source, addresses, from, replyTo, subject, htmlMessage);
	}
	
	public void sendHTMLMessage(
			Object source,
			String[] recepients, 
			String subject, 
			String htmlMessage) throws Exception {
		sendHTMLMessage(source, recepients, null, subject, htmlMessage);
	}
	public void sendHTMLMessage(
			Object source,
			String[] recepients, 
			String from, 
			String subject, 
			String htmlMessage) throws Exception {
		sendHTMLMessage(source, recepients, from, null, subject, htmlMessage);
	}
	public void sendHTMLMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String subject, 
			String htmlMessage) throws Exception {
		sendHTMLMessage(source, recepients, from, replyTo, subject, htmlMessage, null, null);
	}

	public void sendHTMLMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String subject, 
			String htmlMessage,
			String path,
			String attachmentFilename) throws Exception {
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryHTML delivery = new MailDeliveryHTML();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		delivery.setSubject(subject);
		delivery.setHtmlText(htmlMessage);
		if(path != null && attachmentFilename != null) {
			File file = new File(path);
			if(file.exists()) {
				MailFileAttachment attachment = new MailFileAttachment(attachmentFilename, null, file);
				delivery.addAttachment(attachment);
			}
		}
		delivery.setAnObject(source);
		delivery.sendMail();
	}
	
	public void sendHTMLMessage(
			Object source,
			String[] recepients, 
			String from, 
			String replyTo, 
			String subject, 
			String htmlMessage,
			String path,
			byte[] attachmentData,
			String attachmentFilename) throws Exception {
		List<String> addresses = Arrays.asList(recepients);
		MailDeliveryHTML delivery = new MailDeliveryHTML();
		delivery.createNewMail();
		if(from != null)
			delivery.setFromAddress(from);
		if(replyTo != null)
			delivery.setReplyAddress(replyTo);
		delivery.setToAddresses(addresses);
		delivery.setSubject(subject);
		delivery.setHtmlText(htmlMessage);
		if(attachmentData != null) {
			MailAttachment attachment = new MailDataAttachment(attachmentFilename, MailAttachment.generateContentId(), attachmentData);
			delivery.addAttachment(attachment);
		}

		delivery.setAnObject(source);
		delivery.sendMail();
	}
}
