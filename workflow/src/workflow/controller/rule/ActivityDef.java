package workflow.controller.rule;

import java.util.List;

import ariba.util.core.ListUtil;

public class ActivityDef extends ModelDef {
	String id;
	List<ActorRoleDef> actorRoles;
	ExpirationDef expirationDef;
	
	public ActivityDef(String id, String name, String modelName, String controllerName) {
		super(name, modelName, controllerName);
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setActorRoles(List<ActorRoleDef> actorRoles) {
		this.actorRoles = actorRoles;
	}
	public List<ActorRoleDef> getActorRoles() {
		return actorRoles;
	}
	public void addActorRole(ActorRoleDef roleDef) {
		if(actorRoles == null)
			actorRoles = ListUtil.list();
		actorRoles.add(roleDef);
	}

	public ExpirationDef getExpiration() {
		return expirationDef;
	}
	public void setExpiration(ExpirationDef expirationDef) {
		this.expirationDef = expirationDef;
	}

	public boolean hasActorRole(String roleName) {
		if(actorRoles == null)
			return false;
		for(ActorRoleDef actorRole : actorRoles) {
			if(actorRole.getName().equals(roleName))
				return true;
		}
		return false;
	}

	
}
