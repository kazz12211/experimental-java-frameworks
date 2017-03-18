package workflow.view.role;

import java.util.List;

import core.util.ListUtils;
import workflow.model.Role;
import workflow.model.User;
import workflow.view.CRUDListView;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;

public class UserListView extends CRUDListView {
	
	@Override
	protected void doQuery() {
		if(keyword == null)
			keyword = "";
		List<Predicate> predicates = ListUtils.list();
		if(!keyword.isEmpty()) {
			predicates.add(new Predicate.KeyValue("name", "*"+keyword+"*"));
			predicates.add(new Predicate.KeyValue("email", "*"+keyword+"*"));
			predicates.add(new Predicate.KeyValue("ldapUID", "*"+keyword+"*"));
		}
		objects = ObjectContext.get().executeQuery(new QuerySpecification(this.entityClass().getName(), new Predicate.Or(predicates)));
		displayGroup.setObjectArray(objects);
	}

	@Override
	protected Class entityClass() {
		return User.class;
	}

	public List<User> users() {
		return User.allEmployees();
	}
	
	public List<Role> roles() {
		return Role.listAll();
	}
		
}
