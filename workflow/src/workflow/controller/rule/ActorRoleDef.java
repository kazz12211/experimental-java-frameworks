package workflow.controller.rule;

import core.util.ClassUtils;
import workflow.controller.Trace;
import workflow.model.Actor;
import workflow.model.Role;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.util.fieldvalue.FieldValue;
import ariba.util.log.Log;

public class ActorRoleDef extends RoleDef {
	// type are; role, user, relative
	ActorFetcher fetcher;
	
	public ActorRoleDef(String name, String type) {
		super(name, type);
		if("role".equals(type))
			fetcher = new RoleFetcher(name);
		else if("user".equals(type))
			fetcher = new UserFetcher(name);
		else if("relative".equals(type))
			fetcher = new RelativeFetcher(name);
		else if("custom".equals(type))
			fetcher = createCustomFetcher();
	}
	public ActorFetcher getFetcher() {
		return fetcher;
	}
	
	
	private ActorFetcher createCustomFetcher() {
		Class<?> fetcherClass = null;
		
		try {
			if(WorkflowRule.debugMode)
				Trace.writeLog("ActorRoleDef: instantiating ActorFetcher of class '" + name + "'");
			fetcherClass = ClassUtils.classForName(name, ActorFetcher.class);
			ActorFetcher fetcher = (ActorFetcher) fetcherClass.newInstance();
			return fetcher;
		} catch (Exception e) {
			Log.customer.error("ActorRoleDef: could not instantiate ActorFetcher of class '" + name + "'");
			Trace.writeLog("ActorRoleDef: could not instantiate ActorFetcher of class '" + name + "'");
			return null;
		}
	}
	
	public Actor getActor(Workflow workflow) throws Exception {
		return this.fetcher.getActor(workflow);
	}

	public interface ActorFetcher {
		public abstract Actor getActor(Workflow workflow) throws Exception;
	}
	
	public class RoleFetcher implements ActorFetcher {

		String roleName;
		
		public RoleFetcher(String roleName) {
			this.roleName = roleName;
		}

		@Override
		public Actor getActor(Workflow workflow) throws Exception {
			Trace.writeLog(this.getClass().getName() + ": fetching role(" + roleName + ")");
			Role role = Role.getRole(roleName);
			if(role == null) {
				Trace.writeLog(this.getClass().getName()+ ": Role with unique name '" + roleName + "' does not exist");
				throw new IllegalArgumentException(this.getClass().getName()+ ": Role with unique name '" + roleName + "' does not exist");
			}
			Trace.writeLog(this.getClass().getName() + ": " + " returning Role(" + role.getUniqueName() + ")");
			return role;
		}
		
	}
	public class UserFetcher implements ActorFetcher {

		String uniqueName;
		public UserFetcher(String uniqueName) {
			this.uniqueName = uniqueName;
		}
		@Override
		public Actor getActor(Workflow workflow) throws Exception {
			Trace.writeLog(this.getClass().getName() + ": fetching user(" + uniqueName + ")");
			User user = User.userWithUUID(uniqueName);
			if(user == null) {
				Trace.writeLog(this.getClass().getName()+ ": User with unique name '" + uniqueName + "' does not exist");
				throw new IllegalArgumentException(this.getClass().getName()+ ": User with unique name '" + uniqueName + "' does not exist");
			}
			Trace.writeLog(this.getClass().getName() + ": " + " returning User(" + user.getUniqueName() + ")");
			return user;
		}
		
	}
	public class RelativeFetcher implements ActorFetcher {

		String relative;
		
		public RelativeFetcher(String relative) {
			this.relative = relative;
		}

		@Override
		public Actor getActor(Workflow workflow) throws Exception {
			Trace.writeLog(this.getClass().getName() + ": fetching relative(" + relative + ")");
			Object value = FieldValue.getFieldValue(workflow, relative);
			if(value == null) {
				Trace.writeLog(this.getClass().getName()+ ": relative path '" + relative + "' does not work");
				throw new IllegalArgumentException(this.getClass().getName()+ ": relative path '" + relative + "' does not work");
			}
			if(value instanceof Actor) {
				Trace.writeLog(this.getClass().getName() + ": " + " returning Actor(" + ((Actor) value).getUniqueName() + ")");
				return (Actor) value;
			}
			Trace.writeLog(this.getClass().getName()+ ": Invalid relative path '" + relative + "'");
			throw new IllegalArgumentException(this.getClass().getName()+ ": Invalid relative path '" + relative + "'");
		}
		
	}
}
