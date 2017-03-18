package workflow.model;

import workflow.model.csv.RoleLoader;
import workflow.model.csv.StatusLoader;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.log.Log;

public class Initialization {

	public static void initialize() {
		ObjectContext.bindNewContext();
		
		try {
			if(Role.listAll().isEmpty()) {
				RoleLoader loader = new RoleLoader(ObjectContext.get());
				loader.load();
				ObjectContext.get().save();
			}
			if(Status.get(Status.SAVED) == null) {
				StatusLoader loader = new StatusLoader(ObjectContext.get());
				loader.load();
				ObjectContext.get().save();
			}
		} catch (Exception e) {
			Log.customer.error("Initialization (workflow.model): failed", e);
		}
	}
}
