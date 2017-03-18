package workflow.analysis;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import workflow.model.AnalysisDataActivity;
import workflow.model.Request;
import workflow.model.Status;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.util.core.ListUtil;
import ariba.util.log.Log;

public class ActivityCollector extends Collector {

	public ActivityCollector() {
		super();
	}
	
	private List<Request> activitiesOfStatus(Status status, Date start, Date end) {
		Date s = DateUtils.startTimeOfTheDay(start);
		Date e = DateUtils.endTimeOfTheDay(end);
		List<Predicate> predicates = ListUtil.list();
		predicates.add(new Predicate.KeyValue("status", status));
		//predicates.add(new Predicate.KeyValue("workflow.deleted", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("requestedDate", s, Predicate.Operator.Gte));
		predicates.add(new Predicate.KeyValue("requestedDate", e, Predicate.Operator.Lt));
		QuerySpecification spec = new QuerySpecification(Request.class.getName(), new Predicate.And(predicates));
		return ObjectContext.get().executeQuery(spec);
	}

	@Override
	protected void collect(Date start, Date end) {
		if(AnalysisDataActivity.deleteRange(start, end)) {
			List<Request> activities;
			Log.customer.info("ActivityCollector: collecting submitted activity data from " + start + " to " + end);
			activities = this.activitiesOfStatus(Status.get(Status.SUBMITTED), start, end);
			record(activities);
			Log.customer.info("ActivityCollector: collecting rejected activity data from " + start + " to " + end);
			activities = this.activitiesOfStatus(Status.get(Status.REJECTED), start, end);
			record(activities);
		}
	}

	private void record(List<Request> activities) {
		ObjectContext oc = ObjectContext.get();
		int batch = 1;
		for(int i = 1; i <= activities.size(); i++) {
			Request request = activities.get(i-1);
			if(request.getWorkflow() == null)
				continue;
			int comps[] = DateUtils.dateComponents(request.getRequestedDate());
			AnalysisDataActivity data = oc.create(AnalysisDataActivity.class);
			if(request.getAction() != null)
				data.setActorId(request.getAction().getActor().getUniqueName());
			else
				data.setActorId(request.getRequestTo().getUniqueName());
			data.setDay(new Integer(comps[2]));
			data.setWorkflowClass(request.getWorkflow().getClassName());
			data.setModelClass(request.getActivityClassName());
			data.setModelName(request.getName());
			data.setMonth(new Integer(comps[1]));
			data.setProcessTime(request.handledTime());
			data.setRequestedDate(request.getRequestedDate());
			data.setSubmittedDate(request.getSubmittedDate());
			data.setProcessedStatusCode(request.getStatus().getCode());
			data.setYear(new Integer(comps[0]));
			if(i % 10 == 0) {
				Log.customer.info("ActivityCollector:    saving batch " + batch);
				oc.save();
				batch++;
			}
			Log.customer.info("ActivityCollector:    saving batch " + batch);
			oc.save();
		}
	}

}
