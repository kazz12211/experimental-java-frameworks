package workflow.view.management;

import java.util.List;

import rdbms.DbContext;
import workflow.app.DataTableComponent;
import workflow.model.User;
import workflow.tools.ActorActivity;
import workflow.tools.MissingActivityLink;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;

public class MissingActivitySearch extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<ActorActivity> missedActivities;
	public List<User> users;
	public User selectedUser;
	
	@Override
	public void init() {
		super.init();
		users = User.allUsers();
	}
	
	public AWResponseGenerating search() {
		MissingActivityLink finder = new MissingActivityLink(ObjectContext.get(), DbContext.get());
		missedActivities = finder.search(selectedUser);
		return null;
	}
	
	public AWResponseGenerating solve() {
		MissingActivityLink finder = new MissingActivityLink(ObjectContext.get(), DbContext.get());
		finder.solve(selectedUser);
		return search();
	}
	
	public AWResponseGenerating selectUser() {
		return null;
	}
	
	public boolean isStateless() { return false; }
	
}
