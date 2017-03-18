package workflow.schedule;

import java.util.Date;
import java.util.List;

import workflow.WorkflowException;
import workflow.controller.WorkflowManager;
import workflow.model.Request;
import workflow.model.Status;
import workflow.scheduler.ThreadedServer;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.util.core.ListUtil;
import ariba.util.log.Log;

public class ExpirationWatch extends ThreadedServer {

	public ExpirationWatch() {
		super("ExpirationWatch", 1);
	}

	@Override
	public void run() {
		ObjectContext oc = ObjectContext.createContext();
		ObjectContext.bind(oc);
		
		WorkflowManager manager = new WorkflowManager();

		Date now = new Date();
		
		try {
			List<Predicate> predicates = ListUtil.list();
			predicates.add(new Predicate.KeyValue("expirationDate", now, Predicate.Operator.Lt));
			predicates.add(new Predicate.KeyValue("workflow.deleted", new Boolean(false)));
			predicates.add(new Predicate.KeyValue("workflow.archived", new Boolean(false)));
			predicates.add(new Predicate.KeyValue("workflow.hidden", new Boolean(false)));
			predicates.add(new Predicate.KeyValue("status", Status.get(Status.REQUESTED)));
			QuerySpecification spec = new QuerySpecification(Request.class.getName(), new Predicate.And(predicates));
			List<Request> requests = ObjectContext.get().executeQuery(spec);
			
			for(Request request : requests) {
				try {
					manager.expireAndSave(request);
				} catch (WorkflowException e) {
					Log.customer.error("ExpirationWatch failed", e);
				}
			}
		} catch (Exception oe) {
			Log.customer.error("ExpirationWatch failed", oe);
		} finally {
			ObjectContext.unbind();
		}
	}

}
