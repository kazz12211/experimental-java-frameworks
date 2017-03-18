package workflow.schedule;

import java.util.Map;

import core.util.NotificationCenter;
import workflow.scheduler.ThreadedServer;
import workflow.tools.MissingRequestLink;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.MapUtil;

public class MissingRequestLinkWatch extends ThreadedServer {

	public MissingRequestLinkWatch() {
		super("MissingRequestLinkWatch", 1);
	}

	public static final String MissingRequestLinkFoundNotification = "MissingRequestLinkFoundNotification";
	public static final String NumberOfMissingRequestLinksKey = "numberOfMissingRequestLinks";
	
	@Override
	public void run() {
		ObjectContext oc = ObjectContext.createContext();
		ObjectContext.bind(oc);
		MissingRequestLink missing = new MissingRequestLink(ObjectContext.get());
		Map<Long, String> reasons = MapUtil.map();
		missing.search(reasons);
		ObjectContext.unbind();
		if(reasons.size() > 0) {
			NotificationCenter.defaultCenter().postNotification(MissingRequestLinkFoundNotification, this, NumberOfMissingRequestLinksKey, new Integer(reasons.size()));
		}
	}

}
