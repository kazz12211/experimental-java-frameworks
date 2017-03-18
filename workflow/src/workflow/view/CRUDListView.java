package workflow.view;

import java.util.List;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;

public abstract class CRUDListView extends AWComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Object> objects;
	public Object currentObject;
	public String keyword;
	
	@Override
	public void init() {
		super.init();
		this.doQuery();
	}
	
	@Override
	public boolean isStateless() { return false; }
	
	protected abstract void doQuery();
	protected abstract Class entityClass();
	
	public AWResponseGenerating fetch() {
		this.doQuery();
		return null;
	}
	
	public AWResponseGenerating insertNew() {
		Object object = ObjectContext.get().create(this.entityClass());
		ObjectContext.get().recordForInsert(object);
		objects.add(object);
		displayGroup.setSelectedObject(object);
		return null;
	}
	
	public AWResponseGenerating delete() {
		Object object = displayGroup.selectedObject();
		objects.remove(object);
		ObjectContext.get().remove(object);
		return null;
	}
	
	public AWResponseGenerating revert() {
		Object pk = ObjectContext.get().getPrimaryKey(displayGroup.selectedObject());
		Object obj = ObjectContext.get().find(this.entityClass(), pk);
		displayGroup.setSelectedObject(null);
		return null;
	}
	
	public AWResponseGenerating saveChanges() {
		ObjectContext.get().save();
		return null;
	}
}
