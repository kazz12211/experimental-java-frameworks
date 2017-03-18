package workflow.tools;

import java.util.Date;
import java.util.List;
import java.util.Map;

import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import core.util.DateUtils;
import core.util.ListUtils;
import core.util.StringUtils;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.util.core.Fmt;

public class MissingRequestLink {

	ObjectContext oc;
	Date dateFrom;
	Date dateTo;
	String workflowClassName;
	boolean showDeleted;
	boolean showArchived;
	
	
	public MissingRequestLink(ObjectContext oc) {
		this.oc = oc;
	}
	
	public void setDateRange(Date from, Date to) {
		if(from != null) {
			dateFrom = DateUtils.startTimeOfTheDay(from);
		}
		if(to != null) {
			dateTo = DateUtils.endTimeOfTheDay(to);
		}
	}
	
	public void setWorkflowClassName(String className) {
		this.workflowClassName = className;
	}
	
	public void setShowDeleted(boolean flag) {
		this.showDeleted = flag;
	}
	public void setShowArchived(boolean flag) {
		this.showArchived = flag;
	}
	
	public List<Request> search(Map<Long, String> reasons) {
		return this.search(reasons, null);
	}

	public List<Request> search(Map<Long, String> results, AWRequestContext requestContext) {
		List<Predicate> predicates = ListUtils.list();
		if(StringUtils.nullOrEmptyOrBlank(workflowClassName) == false)
			predicates.add(new Predicate.KeyValue("workflow.className", workflowClassName));
		if(dateFrom != null)
			predicates.add(new Predicate.KeyValue("createdDate", dateFrom, Predicate.Operator.Gte));
		if(dateTo != null)
			predicates.add(new Predicate.KeyValue("createdDate", dateTo, Predicate.Operator.Lte));
		predicates.add(new Predicate.KeyValue("status", Status.get(Status.SUBMITTED), Predicate.Operator.Neq));
		predicates.add(new Predicate.KeyValue("workflow.status", Status.get(Status.SUBMITTED)));
		if(showDeleted == false)
			predicates.add(new Predicate.KeyValue("workflow.deleted", new Boolean(false)));
		if(showArchived == false)
			predicates.add(new Predicate.KeyValue("workflow.archived", new Boolean(false)));
		QuerySpecification spec = new QuerySpecification(Request.class.getName(), new Predicate.And(predicates));
		List<Request> rs = oc.executeQuery(spec);

		List<Request> requests = filterUnknownAddress(rs, results, requestContext);
		
		return requests;
	}
	
	private List<Request> filterUnknownAddress(List<Request> rs, Map<Long, String> results, AWRequestContext requestContext) {
		List<Request> requests = ListUtils.list();
		for(Request r : rs) {
			if(r.isMissingActivityClass()) {
				String message = "No activity class";
				if(requestContext != null)
					message = AWLocal.localizedJavaString(1, "No activity class", MissingRequestLink.class, requestContext);
				results.put(r.getId(), message);
				requests.add(r);
				continue;
			}
			
			if(r.isMissingActor()) {
				String message = "No actor assigned";
				if(requestContext != null)
					message = AWLocal.localizedJavaString(2, "No actor assigned", MissingRequestLink.class, requestContext);
				results.put(r.getId(), message);
				requests.add(r);
				continue;
			}

			// no request sent to users
			List<User> expectedUsers = r.expectedActingUsers();
			for(User u : expectedUsers) {
				if(r.isNotSentTo(u)) {
					String message = Fmt.S("No request sent to %s", u.getName());
					if(requestContext != null) {
						String fmt = AWLocal.localizedJavaString(3, "No request sent to %s", MissingRequestLink.class, requestContext);
						message = Fmt.S(fmt, u.getName());
					}
					results.put(r.getId(), message);
					requests.add(r);
					break;
				}
			}
			
		}
		return requests;
	}

}
