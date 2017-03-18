package workflow.app;

import java.util.Map;

import workflow.model.User;
import core.util.MapUtils;
import ariba.ui.aribaweb.core.AWSession;
import ariba.ui.aribaweb.core.AWSession.LifecycleListener;
import ariba.ui.meta.persistence.ObjectContext;

public abstract class WorkflowUserBinder implements LifecycleListener {
	static boolean _DidReg = false;
	static final String _SessionUIDKey = "_OC_USER_ID";
	static final String _SessionContextKey = "_OC_CONTEXT_ID";
	static ObjectContext oc = ObjectContext.createContext();

	protected WorkflowUserBinder() {}
	
	public void init() {
		if(!_DidReg) {
			_DidReg = true;
			AWSession.registerLifecycleListener(this);
		}
	}
	
	static ThreadLocal<String> _ThreadLocalUserId = new ThreadLocal<String>();
	
	public static User currentUser() {
		String uid = (String)_ThreadLocalUserId.get();
		if(uid == null)
			return null;
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put("ldapUID", uid);
		return oc.findOne(User.class, fieldValues);
	}

	public static boolean isLoggedIn() {
		User user = currentUser();
		return user != null;
	}

	@SuppressWarnings("unchecked")
	public static void bindUserToSession(User user, AWSession session) {
		session.dict().put(_SessionUIDKey, user.getUniqueName());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void sessionWillAwake(AWSession session) {
		_ThreadLocalUserId.set((String) session.dict().get(_SessionUIDKey));
		if(ObjectContext.peek() == null) {
			ObjectContext ctx = (ObjectContext) session.dict().get(_SessionContextKey);
			if(ctx == null) {
				ObjectContext.bindNewContext(session.sessionId());
				ctx = ObjectContext.get();
				session.dict().put(_SessionContextKey, ctx);
			} else {
				ObjectContext.bind(ctx);
			}
		}
	}
	
	@Override
	public void sessionWillSleep(AWSession session) {
		_ThreadLocalUserId.set(null);
		ObjectContext.unbind();
	}

}
