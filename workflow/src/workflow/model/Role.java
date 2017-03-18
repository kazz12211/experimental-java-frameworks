package workflow.model;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import core.util.ListUtils;
import core.util.MapUtils;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;

@Entity
public class Role extends Actor {

	@Column(name="ldapuid")
	String uid;

	@ManyToMany
	@JoinTable(name="user_role",
			joinColumns={@JoinColumn(name="role_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="user_id", referencedColumnName="id")})
	List<User> users;

	Boolean isDefault;

	String description;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public List<User> getUsers() {
		if(users == null)
			users = ListUtils.list();
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}

	public User addUser(User user) {
		if(users.contains(user) == false) {
			users.add(user);
			List<Role> roles = user.getRoles();
			if(roles.contains(this) == false)
				roles.add(this);
		}
		return user;
	}
	public User removeUser(User user) {
		if(users.contains(user)) {
			users.remove(user);
			List<Role> roles = user.getRoles();
			if(roles.contains(this))
				roles.remove(this);
		}
		return user;
	}
	
	public static List<Role> defaultRoles() {
		Predicate pred = new Predicate.KeyValue("isDefault", new Boolean(true));
		QuerySpecification spec = new QuerySpecification(Role.class.getName(), pred);
		return ObjectContext.get().executeQuery(spec);
	}

	public static List<Role> listAll() {
		return ObjectContext.get().executeQuery(new QuerySpecification(Role.class.getName(), null));
	}
	
	public static Role getRole(String roleName) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("uid", roleName);
		return ObjectContext.get().findOne(Role.class, fieldValues);
	}
	
	@Override
	public String getUniqueName() {
		return this.uid;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUserNames() {
		List<String> names = ListUtils.list();
		for(User user : getUsers()) {
			names.add(user.getName());
		}
		return ListUtils.listToString(names, ",");
	}
	
	private static String[] administrativeRoleNames = {"SystemAdministration", "IT"};
			
	public static List<Role> administrativeRoles() {
		List<Role> roles = ListUtils.list();
		for(String roleName : administrativeRoleNames) {
			Role r = Role.getRole(roleName);
			if(r != null)
				roles.add(r);
		}
		return roles;
	}
	
	public static List<User> administrativeUsers() {
		List<Role> roles = Role.administrativeRoles();
		List<User> users = ListUtils.list();
		if(ListUtils.nullOrEmpty(roles)) {
			users.addAll(User.administrativeUsers());
		} else {
			for(Role r : roles) {
				List<User> us = r.getUsers();
				if(ListUtils.nullOrEmpty(us) == false) {
					users.addAll(us);
				}
			}
		}
		return users;
	}
}
