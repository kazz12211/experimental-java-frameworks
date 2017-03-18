package workflow.notif;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import core.util.XMLConfig;
import ariba.ui.widgets.XMLUtil;
import ariba.util.core.Fmt;

public class MailTemplate extends XMLConfig {

	String subject;
	String body;
	
	public MailTemplate() {
	}
	
	public void init(InputStream inputStream) throws Exception {
		InputSource source = new InputSource(inputStream);
		Element docElem = XMLUtil.document(source, false, false, null).getDocumentElement();
		if(docElem.getNodeName().equals("mail-template")) {
			Element subjectElem = this.elementNamed(docElem, "subject");
			subject = XMLUtil.getText(subjectElem, null);
			Element bodyElem = this.elementNamed(docElem, "body");
			body = XMLUtil.getText(bodyElem, null);
		}
	}
	
	public void init(String filename) throws MalformedURLException {
		String path = super.getResourcePath(filename);
		URL url = new URL(path);
		Element docElem = XMLUtil.document(url, false, false, null).getDocumentElement();
		if(docElem.getNodeName().equals("mail-template")) {
			Element subjectElem = this.elementNamed(docElem, "subject");
			subject = XMLUtil.getText(subjectElem, null);
			Element bodyElem = this.elementNamed(docElem, "body");
			body = XMLUtil.getText(bodyElem, null);
		}
	}
	
	public String subject() {
		return subject;
	}
	
	public String merge(String[] args) {
		return Fmt.S(body, args);
	}
}
