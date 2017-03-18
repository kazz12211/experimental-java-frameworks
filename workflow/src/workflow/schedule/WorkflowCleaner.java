package workflow.schedule;

import java.util.List;

import workflow.controller.WorkflowManager;
import workflow.model.Workflow;
import workflow.scheduler.ThreadedServer;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.util.log.Log;

public class WorkflowCleaner extends ThreadedServer {

	public WorkflowCleaner() {
		super("WorkflowCleaner", 1);
	}
	
	@Override
	public void run() {
		ObjectContext oc = ObjectContext.createContext();
		ObjectContext.bind(oc);

		WorkflowManager manager = new WorkflowManager();

		Predicate predicate = new Predicate.KeyValue("hidden", new Boolean(true));
		QuerySpecification spec = new QuerySpecification(Workflow.class.getName(), predicate);
		List<Workflow> workflows = ObjectContext.get().executeQuery(spec);
		
		for(Workflow workflow : workflows) {
			try {
				manager.reallyRemove(workflow);
				ObjectContext.get().save();
			} catch (Exception e) {
				Log.customer.error("WorkflowCleaner: failed to clean workflow id (" + workflow.getId() + ")", e);
			}
		}
		
		ObjectContext.unbind();
	}

}
