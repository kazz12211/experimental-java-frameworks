package workflow.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;

import core.util.ListFilter;
import core.util.ListUtils;
import core.util.MapUtils;
import workflow.model.listener.UserListener;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;

@Entity
@EntityListeners(UserListener.class)
@NamedNativeQueries({
	@NamedNativeQuery(
		name="RequestsOfStatus",
		query="select b.* from workflow.actor_request a, workflow.request b, workflow.status c where a.requests_id = b.id and c.id = b.status_id and c.code= :statusCode and a.actor_id= :actorId",
		resultClass=Request.class
	),
	@NamedNativeQuery(
			name="AllRequests",
			query="select b.* from workflow.actor_request a, workflow.request b, workflow.status c where a.requests_id = b.id and c.id = b.status_id and a.actor_id= :actorId",
			resultClass=Request.class
		),
})
@NamedQueries({
	@NamedQuery(
			name="WorkflowsOfStatus",
			query="select w from Workflow w where creator = :creator and archived = false and deleted = false and hidden = false and status.code = :statusCode"),
	@NamedQuery(
			name="CountWorkflowsOfStatus",
			query="select count(w) from Workflow w where creator = :creator and archived = false and deleted = false and hidden = false and status.code = :statusCode")
})

public class User extends Actor {

	String ldapUID;
	String email;
	String phoneNumber;
	String mobileEmail;
	String mobilePhoneNumber;
	@ManyToMany
	@JoinTable(
			name="user_role",
			joinColumns={@JoinColumn(name="user_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="id")})
	List<Role> roles;

	@ManyToOne
	User reportTo;
	@ManyToOne
	User delegator;
	Date createdDate;
	Date modifiedDate;
	@OneToMany
	List<Request> requests;
	@OneToMany
	List<Activity> activities;
	@OneToMany
	List<Workflow> workflows;
	Boolean isEmployee;
	@OneToMany
	List<UserPreference> userPreferences;
	@OneToMany
	List<Folder> folders;
	
	// added for demo
	String password;
	
	public String getLdapUID() {
		return ldapUID;
	}
	public void setLdapUID(String ldapUID) {
		this.ldapUID = ldapUID;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getMobileEmail() {
		return mobileEmail;
	}
	public void setMobileEmail(String mobileEmail) {
		this.mobileEmail = mobileEmail;
	}
	
	public String getMobilePhoneNumber() {
		return mobilePhoneNumber;
	}
	public void setMobilePhoneNumber(String mobilePhoneNumber) {
		this.mobilePhoneNumber = mobilePhoneNumber;
	}
	
	public List<Role> getRoles() {
		if(roles == null)
			roles = ListUtils.list();
		return roles;
	}
	public void setRoles(List<Role> newRoles) {
		List<Role> deleted = ListUtils.list();
		List<Role> added = ListUtils.list();
		
		for(Role r : this.getRoles()) {
			if(newRoles.contains(r) == false) {
				deleted.add(r);
			}
		}
		for(Role r : newRoles) {
			if(this.getRoles().contains(r) == false) {
				added.add(r);
			}
		}
		
		for(Role r : deleted) {
			r.removeUser(this);
		}
		for(Role r : added) {
			r.addUser(this);
		}
		//this.roles = newRoles;
	}

	public User getReportTo() {
		return reportTo;
	}
	public void setReportTo(User reportTo) {
		this.reportTo = reportTo;
	}
	public User getDelegator() {
		return delegator;
	}
	public void setDelegator(User delegator) {
		this.delegator = delegator;
	}

	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public List<Request> getRequests() {
		if(requests == null)
			requests = ListUtils.list();
		return requests;
	}
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}
	
	private boolean hasRequest(Request request) {
		for(Request r : this.getRequests()) {
			if(r.equalsTo(request))
				return true;
		}
		return false;
	}
	
	public void addRequest(Request request) {
		if(request != null && this.hasRequest(request) == false)
			this.getRequests().add(request);
	}
	public void removeRequest(Request request) {
		if(request != null && getRequests().contains(request))
			this.getRequests().remove(request);
	}
	
	public List<Activity> getActivities() {
		if(activities == null)
			activities = ListUtils.list();
		return activities;
	}
	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}
	public void addActivity(Activity activity) {
		if(activity != null && this.getActivities().contains(activity) == false) {
			this.getActivities().add(activity);
			activity.setActor(this);
		}
	}
	public void removeActivity(Activity activity) {
		if(activity != null && this.getActivities().contains(activity)) {
			this.getActivities().remove(activity);
			activity.setActor(null);
		}
	}
	
	public List<Workflow> getWorkflows() {
		if(workflows == null)
			workflows = ListUtils.list();
		return workflows;
	}
	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
	public void addWorkflow(Workflow workflow) {
		if(workflow != null) {
			this.getWorkflows().add(workflow);
			workflow.setCreator(this);
		}
	}
	public void removeWorkflow(Workflow workflow) {
		if(workflow != null) {
			this.getWorkflows().remove(workflow);
			workflow.setCreator(null);
		}
	}
	public Boolean getIsEmployee() {
		return isEmployee;
	}
	public void setIsEmployee(Boolean isEmployee) {
		this.isEmployee = isEmployee;
	}

	public List<UserPreference> getUserPreferences() {
		if(userPreferences == null)
			userPreferences = ListUtils.list();
		return userPreferences;
	}
	public void setUserPreferenecs(List<UserPreference> userPreferences) {
		this.userPreferences = userPreferences;
	}
	
	public List<Folder> getFolders() {
		if(folders == null)
			folders = ListUtils.list();
		return folders;
	}
	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}
	public void addFolder(Folder folder) {
		if(this.getFolders().contains(folder) == false) {
			this.getFolders().add(folder);
			folder.setOwner(this);
		}
	}
	public void removeFolder(Folder folder) {
		if(this.getFolders().contains(folder)) {
			this.getFolders().remove(folder);
			folder.setOwner(null);
		}
	}

	public static User userWithUUID(String uid) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("ldapUID", uid);
		List<User> result = ObjectContext.get().executeQuery(User.class, fieldValues);
		if(result != null && result.size() > 0)
			return result.get(0);
		return null;
	}
	
	public static User userWithEmail(String mailAddress) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("email", mailAddress);
		List<User> result = ObjectContext.get().executeQuery(User.class, fieldValues);
		if(result != null && result.size() > 0)
			return result.get(0);
		return null;
	}

	public static User create(String name, String email, String uid, List<Role> defaultRoles, Boolean isEmployee) {
		User newUser = ObjectContext.get().create(User.class);
		newUser.setName(name);
		newUser.setEmail(email);
		newUser.setLdapUID(uid);
		newUser.setIsEmployee(isEmployee);
		for(Role role : defaultRoles) {
			role.addUser(newUser);
		}
		ObjectContext.get().save();
		return newUser;
	}
	
	
	public static List<User> allUsers() {
		return ObjectContext.get().executeQuery(new QuerySpecification(User.class.getName(), null));
	}

	public static List<User> allEmployees() {
		Predicate p = new Predicate.KeyValue("isEmployee", new Boolean(true));
		return ObjectContext.get().executeQuery(new QuerySpecification(User.class.getName(), p));
	}
	
	public static List<User> allNonEmployees() {
		Predicate p = new Predicate.KeyValue("isEmployee", new Boolean(false));
		return ObjectContext.get().executeQuery(new QuerySpecification(User.class.getName(), p));
	}
	
	//
	// Workflow related methods
	//
	
	public List<Workflow> workflowsOfStatus(Folder folder, String status) {
		List<Workflow> list = ListUtils.list();
		if(workflows != null)
			for(Workflow w : workflows) {
				if(w.isViewableIn(folder) && status.equals(w.getStatus().getCode()) ) {
					list.add(w);
				}
			}
		return list;
	}
	
	public List<Workflow> workflowsOfStatus(String status) {
		//return workflowsOfStatus(null, status);
		return fetchWorkflowsOfStatus(status);
	}
	
	public List<Workflow> viewableWorkflows(Folder folder) {
		List<Workflow> list = ListUtils.list();
		if(workflows != null)
			for(Workflow w : workflows) {
				if(w.isViewableIn(folder)) {
					list.add(w);
				}
			}
		return list;
	}
	
	public List<Workflow> viewableWorkflows() {
		return viewableWorkflows(null);
	}
	
	
	public List<Workflow> deletedWorkflows(Folder folder) {
		List<Workflow> list = ListUtils.list();
		if(workflows != null)
			for(Workflow w: workflows) {
				if(!w.isArchived() && !w.isHidden() && w.isDeleted() && w.getFolder() == folder)
					list.add(w);
			}
		return list;
	}

	public List<Workflow> deletedWorkflows() {
		return this.deletedWorkflows(null);
	}
	
	@SuppressWarnings("unchecked")
	public List<Request> requestsOfStatus(final String status) {
		return (List<Request>) ListUtils.filteredList(this.getRequests(), new ListFilter<Request>() {
			@Override
			public boolean filter(Request request) {
				return (request.isViewable() && status.equals(request.getStatusCode()));
			}});
		//return this.fetchRequestOfStatus(status);
	}
	public List<Request> viewableRequests() {
		List<Request> list = ListUtils.list();
		if(requests != null)
		for(Request r : requests) {
			if(r.isViewable()) {
				list.add(r);
			}
		}
		return list;
	}
	
	public long workflowCountOfStatus(String statusCode) {
		Number value = this.fetchCountWorklowsOfStatus(statusCode);
		return (value != null) ? value.longValue() : 0;
	}
	public long savedWorkflowCount() {
		//return this.workflowsOfStatus(Status.SAVED).size();
		return this.workflowCountOfStatus(Status.SAVED);
	}
	public long submittedWorkflowCount() {
		//return this.workflowsOfStatus(Status.SUBMITTED).size();
		return this.workflowCountOfStatus(Status.SUBMITTED);
	}
	public long completedWorkflowCount() {
		//return this.workflowsOfStatus(Status.COMPLETED).size();
		return this.workflowCountOfStatus(Status.COMPLETED);
	}
	public long rejectedWorkflowCount() {
		//return this.workflowsOfStatus(Status.REJECTED).size();
		return this.workflowCountOfStatus(Status.REJECTED);
	}
	public long pendingWorkflowCount() {
		//return this.workflowsOfStatus(Status.PENDING).size();
		return this.workflowCountOfStatus(Status.PENDING);
	}
	public long expiredWorkflowCount() {
		//return this.workflowsOfStatus(Status.EXPIRED).size();
		return this.workflowCountOfStatus(Status.EXPIRED);
	}
	public long errorWorkflowCount() {
		//return this.workflowsOfStatus(Status.ERROR).size();
		return this.workflowCountOfStatus(Status.ERROR);
	}
	public long submittedRequestCount() {
		return this.requestsOfStatus(Status.SUBMITTED).size();
	}
	public long requestedRequestCount() {
		return this.requestsOfStatus(Status.REQUESTED).size();
	}
	public long rejectedRequestCount() {
		return this.requestsOfStatus(Status.REJECTED).size();
	}
	public long expiredRequestCount() {
		return this.requestsOfStatus(Status.EXPIRED).size();
	}

	public long savedWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.SAVED).size();
	}
	public long submittedWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.SUBMITTED).size();
	}
	public long completedWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.COMPLETED).size();
	}
	public long rejectedWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.REJECTED).size();
	}
	public long pendingWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.PENDING).size();
	}
	public long expiredWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.EXPIRED).size();
	}
	public long errorWorkflowCount(Folder folder) {
		return this.workflowsOfStatus(folder, Status.ERROR).size();
	}

	public String roleNames() {
		String s = "";
		for(Role r : this.getRoles()) {
			if(s.length() > 0)
				s += ", ";
			s += r.getName();
		}
		return s;
	}

	@Override
	public String getUniqueName() {
		return this.ldapUID;
	}
	
	public boolean hasRole(String roleName) {
		for(Role r : this.getRoles()) {
			if(r.getUniqueName().equals(roleName))
				return true;
		}
		return false;
	}
	public static User findOrCreate(String email, String name) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("email", email);
		//fieldValues.put("name", name);
		ObjectContext context = ObjectContext.get();
		User user = context.findOne(User.class, fieldValues);
		if(user == null) {
			user = context.create(User.class);
			user.setName(name);
			user.setEmail(email);
			user.setLdapUID(email);
		}
		return user;
	}
	
	public UserPreference getUserPreference(String prefKey) {
		for(UserPreference pref : this.getUserPreferences()) {
			if(pref.getKey().equals(prefKey)) {
				return pref;
			}
		}
		return null;
	}
	
	public User getManager() {
		if(reportTo != null) {
			if(reportTo.hasRole("Manager"))
				return reportTo;
			else
				return reportTo.getManager();
		} else {
			return null;
		}
	}
	
	public List<User> directSubordinates() {
		List<User> subordinates = ListUtils.list();
		Predicate p = new Predicate.KeyValue("reportTo", this);
		QuerySpecification spec = new QuerySpecification(User.class.getName(), p);
		subordinates.addAll(ObjectContext.get().executeQuery(spec));
		return subordinates;
	}
	
	public List<User> subordinates() {
		List<User> directs = this.directSubordinates();
		List<User> subordinates = ListUtils.list();
		subordinates.addAll(directs);
		for(User user : directs) {
			ListUtils.addElementsIfAbsent(subordinates, user.subordinates());
		}
		return subordinates;
	}
	
	@SuppressWarnings("unchecked")
	public List<Request> fetchRequestOfStatus(final String statusCode) {
		Long actorId = this.getId();
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		Query query = em.createNamedQuery("RequestsOfStatus");
		query.setParameter("statusCode", statusCode);
		query.setParameter("actorId", actorId);
		List<Request> requests = query.getResultList();
		return (List<Request>) ListUtils.filteredList(requests, new ListFilter<Request>() {
			@Override
			public boolean filter(Request object) {
				return object.isViewable();
			}});
	}
		
	@SuppressWarnings("unchecked")
	public List<Request> fetchAllRequests() {
		Long actorId = this.getId();
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		Query query = em.createNamedQuery("AllRequests");
		query.setParameter("actorId", actorId);
		List<Request> requests = query.getResultList();
		return (List<Request>) ListUtils.filteredList(requests, new ListFilter<Request>() {
			@Override
			public boolean filter(Request object) {
				return object.isViewable();
			}});
	}

	public List<Workflow> fetchWorkflowsOfStatus(String statusCode) {
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		Query query = em.createNamedQuery("WorkflowsOfStatus");
		query.setParameter("creator", this);
		query.setParameter("statusCode", statusCode);
		return query.getResultList();
	}
	
	public Number fetchCountWorklowsOfStatus(String statusCode) {
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		Query query = em.createNamedQuery("CountWorkflowsOfStatus");
		query.setParameter("creator", this);
		query.setParameter("statusCode", statusCode);
		return (Number) query.getSingleResult();
	}
	
	public Counts getCounts() {
		return Counts.find(this, ObjectContext.get());
	}
	
	public void updateCounts() {
		Counts.createOrUpdate(this, ObjectContext.get());
	}
	
	private static String[] administrativeUserNames = {"kazuo.tsubaki", "michael.dorrian", "hiroshi.hatakeyama"};
	
	public static List<User> administrativeUsers() {
		List<User> users = ListUtils.list();
		for(String uid : administrativeUserNames) {
			User u = User.userWithUUID(uid);
			if(u != null)
				users.add(u);
		}
		return users;
	}
	
	// added for demo
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public static User authenticate(String email, String password) {
		Map<String, Object> fieldValues = core.util.MapUtils.map();
		fieldValues.put("email", email);
		fieldValues.put("password", password);
		User user = ObjectContext.get().findOne(User.class, fieldValues);
		return user;
	}
 }

