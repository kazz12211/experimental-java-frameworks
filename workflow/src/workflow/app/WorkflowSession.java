package workflow.app;

import java.util.Map;

import core.util.Notification;
import core.util.NotificationCenter;
import core.util.NotificationObserver;
import rdbms.DbContext;
import workflow.aribaweb.hibernate.HibernateUtil;
import workflow.controller.WorkflowManager;
import workflow.controller.helper.ChangeWatcher;
import workflow.dashboard.DashboardManager;
import workflow.model.User;
import workflow.schedule.MissingRequestLinkWatch;
import ariba.ui.aribaweb.core.AWApplication;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.core.AWSession;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.widgets.MessageBanner;
import ariba.util.core.Fmt;
import ariba.util.log.Log;

public abstract class WorkflowSession extends AWSession implements NotificationObserver {

	WorkflowManager workflowManager;
	protected User user;		
	ChangeWatcher changeWatcher;
	TableConfigManager tableConfigManager;
	ViewPreferenceManager viewPreferenceManager;
	DashboardManager dashboardManager;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
		tableConfigManager = new TableConfigManager(user.getUniqueName());
		tableConfigManager.load();
		viewPreferenceManager = new ViewPreferenceManager(user.getUniqueName());
		viewPreferenceManager.load();
		dashboardManager = new DashboardManager(user.getUniqueName());
		dashboardManager.load();
		
		if(user.hasRole("SystemAdministration")) {
			NotificationCenter.defaultCenter().addObserver(this, MissingRequestLinkWatch.MissingRequestLinkFoundNotification, null);
		}
	}
			
	@Override
	public void init(AWApplication application, AWRequestContext requestContext) {
		super.init(application, requestContext);
		workflowManager = new WorkflowManager();
		changeWatcher = new ChangeWatcher();
		workflowManager.addCallback(changeWatcher);
	}
	
	public WorkflowManager getWorkflowManager() {
		return workflowManager;
	}
	
	public ChangeWatcher getChangeWatcher() {
		return changeWatcher;
	}
	
	public void reloadUser() {
		try {
			user = (User) HibernateUtil.reloadObject(user, ObjectContext.get());
		} catch (Exception e) {
			Log.customer.error("WorkflowSession: reloadUser() failed.", e);
		}
	}
	
	public TableConfigManager getTableConfigManager() {
		return tableConfigManager;
	}
	
	public ViewPreferenceManager getViewPreferenceManager() {
		return viewPreferenceManager;
	}
		
	public DashboardManager getDashboardManager() {
		return dashboardManager;
	}
	
	public Object getConfigValue(String key) {
		Object value = null;
		if(user != null)
			value = AppConfigManager.getInstance().get(user.getLdapUID(), key);
		if(value == null)
			value = AppConfigManager.getInstance().get(key);
		return value;
	}
	
	public void setConfigValue(String key, Object value) {
		if(user != null) {
			AppConfigManager.getInstance().set(user.getLdapUID(), key, value);
		}
	}
	@Override
	public void terminate() {
		if(user != null && user.hasRole("SystemAdministration")) {
			NotificationCenter.defaultCenter().removeObserver(this, MissingRequestLinkWatch.MissingRequestLinkFoundNotification, null);
		}
		super.terminate();
	}
	
	// Notification observer method
	public void notify(Notification notif) {
		Map<String, Object> userInfo = notif.getUserInfo();
		Integer num = (Integer) userInfo.get(MissingRequestLinkWatch.NumberOfMissingRequestLinksKey);
		if(num != null) {
			String fmt = AWLocal.localizedJavaString(1, "%s missing request links found", WorkflowSession.class, requestContext());
			String message = Fmt.S(fmt, num.toString());
			MessageBanner.setMessage(message, this);
		}
	}

	
	@Override
	protected void awake() {
		super.awake();
		if(DbContext.peek() == null)
			DbContext.bindNewContext();
	}
	
	public User cachedLoggedInUser() {
		if(((WorkflowApplication) application()).getUserBinder().isLoggedIn()) {
			return ((WorkflowApplication) application()).getUserBinder().currentUser();
		}
		return null;
	}
	
	public boolean isMemberOf(String group) {
		return isMemberOf(((WorkflowApplication) application()).getUserBinder().currentUser(), group);
	}
	
	public boolean isMemberOf(User user, String group) {
		if(user == null)
			return false;
		return user.hasRole(group);
	}
	
	public boolean isMemberOf(User user, String[] groups) {
		if(user == null)
			return false;
		
		for(String group : groups) {
			if(user.hasRole(group))
				return true;
		}
		return false;
	}

	public boolean isLoggedIn() {
		return ((WorkflowApplication) application()).getUserBinder().isLoggedIn();
	}

}
