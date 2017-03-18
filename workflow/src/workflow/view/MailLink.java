package workflow.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import workflow.model.User;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.util.core.Fmt;
import ariba.util.i18n.I18NUtil;
import ariba.util.log.Log;

public class MailLink extends AWComponent {

	protected User getUser() {
		return (User) valueForBinding("user");
	}
	
	protected String getSubject() {
		return (String) valueForBinding("subject");
	}
	
	public String getEmail() {
		User user = this.getUser();
		if(user != null)
			return user.getEmail();
		return null;
	}
	
	public String getMailToLink() {
		String link = "mailto:" + this.getEmail();
		try {
			link = link + "?subject=" + URLEncoder.encode(this.getSubject(), I18NUtil.EncodingUTF_8);
		} catch (UnsupportedEncodingException e) {
			Log.customer.error("MailLink: error", e);
		}
		return link;
	}
	
	public String getMailToString() {
		String fmt = AWLocal.localizedJavaString(1, "mail to %s", MailLink.class, requestContext());
		return Fmt.S(fmt, this.getUser().getName());
	}
}
