package workflow.view;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;

public abstract class CodeTableList extends AWComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	
	@Override
	public void init() {
		this.loadAll(ObjectContext.get());
		super.init();
	}
	
	protected abstract void loadAll(ObjectContext oc);
	
	public AWResponseGenerating refresh() {
		this.loadAll(ObjectContext.get());
		return null;
	}
	
	public boolean isStateless() { return false; }
	

}
