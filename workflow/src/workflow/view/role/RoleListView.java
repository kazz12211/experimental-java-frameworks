package workflow.view.role;

import java.util.List;

import core.util.ListUtils;
import workflow.model.Role;
import workflow.view.CRUDListView;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;

public class RoleListView extends CRUDListView {

	@Override
	protected void doQuery() {
		if(keyword == null)
			keyword = "";
		List<Predicate> predicates = ListUtils.list();
		predicates.add(new Predicate.KeyValue("name", "*"+keyword+"*"));
		predicates.add(new Predicate.KeyValue("uid", "*"+keyword+"*"));
		objects = ObjectContext.get().executeQuery(new QuerySpecification(this.entityClass().getName(), new Predicate.Or(predicates)));
		displayGroup.setObjectArray(objects);
	}

	@Override
	protected Class entityClass() {
		return Role.class;
	}

}
