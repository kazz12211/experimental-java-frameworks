package workflow.model;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import workflow.util.MailMonitor;
import core.BaseObject;
import ariba.ui.aribaweb.util.AWMimeParsedMessage;

@Entity
public class Mail extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long id;
	String sender;
	String recepients;
	String subject;
	Date date;
	@Lob
	byte[] mimeData;
	String indexText;
	@Column(name="textContent", length=30000)
	String textContent;
	
	@Transient
	private AWMimeParsedMessage _parsedMessage = null;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public byte[] getMimeData() {
		return mimeData;
	}

	public void setMimeData(byte[] mimeData) {
		this.mimeData = mimeData;
	}

	public String getIndexText() {
		return indexText;
	}

	public void setIndexText(String indexText) {
		this.indexText = indexText;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public String getRecepients() {
		return recepients;
	}
	public void setRecepients(String recepients) {
		this.recepients = recepients;
	}
	
	public AWMimeParsedMessage getParsedMessage() {
		if(_parsedMessage == null) {
			_parsedMessage = new AWMimeParsedMessage(MailMonitor.mimeForData(mimeData));
		}
		return _parsedMessage;
	}

	public Long getId() {
		return id;
	}

	public void init(MimeMessage message) throws MessagingException {
		mimeData = MailMonitor.bytesForMime(message);
		subject = message.getSubject();
		date = new Date(message.getSentDate().getTime());
		AWMimeParsedMessage parsedMessage = getParsedMessage();
		indexText = parsedMessage.getPlainText();
		User user = MailMonitor.findOrCreateSender(parsedMessage);
		sender = user.getEmail();
	}
	
}

