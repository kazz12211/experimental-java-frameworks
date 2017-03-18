package workflow.analysis;

import java.util.Date;
import java.util.List;

import core.util.DateUtils;
import workflow.model.AnalysisDataWorkflow;
import workflow.model.Status;
import workflow.model.Workflow;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.util.core.ListUtil;
import ariba.util.log.Log;

public class WorkflowCollector extends Collector {

	public WorkflowCollector() {
		super();
	}
	
	private List<Workflow> workflowsOfStatus(Status status, Date start, Date end) {
		Date s = DateUtils.startTimeOfTheDay(start);
		Date e = DateUtils.endTimeOfTheDay(end);
		List<Predicate> predicates = ListUtil.list();
		predicates.add(new Predicate.KeyValue("status", status));
		//predicates.add(new Predicate.KeyValue("deleted", new Boolean(false)));
		predicates.add(new Predicate.KeyValue("submittedDate", s, Predicate.Operator.Gte));
		predicates.add(new Predicate.KeyValue("submittedDate", e, Predicate.Operator.Lt));
		QuerySpecification spec = new QuerySpecification(Workflow.class.getName(), new Predicate.And(predicates));
		return ObjectContext.get().executeQuery(spec);
	}
	
	public List<Workflow> collectCompletedWorkflows(Date start, Date end) {
		return this.workflowsOfStatus(Status.get(Status.COMPLETED), start, end);
	}
	public List<Workflow> collectRejectedWorkflows(Date start, Date end) {
		return this.workflowsOfStatus(Status.get(Status.REJECTED), start, end);
	}
	
	@Override
	protected void collect(Date start, Date end) {
		if(AnalysisDataWorkflow.deleteRange(start, end)) {
			List<Workflow> workflows;
			Log.customer.info("WorkflowCollector: collecting completed workflow data from " + start + " to " + end);
			workflows = this.collectCompletedWorkflows(start, end);
			record(workflows);
			
			Log.customer.info("WorkflowCollector: collecting rejected workflow data from " + start + " to " + end);
			workflows = this.collectRejectedWorkflows(start, end);
			record(workflows);
		}
	}

	private void record(List<Workflow> workflows) {
		ObjectContext oc = ObjectContext.get();
		int batch = 1;
		for(int i = 1; i <= workflows.size(); i++) {
			Workflow workflow = workflows.get(i-1);
			int comps[] = DateUtils.dateComponents(workflow.getSubmittedDate());
			AnalysisDataWorkflow data = oc.create(AnalysisDataWorkflow.class);
			
			data.setCreatorId(workflow.getCreator().getUniqueName());
			data.setDay(new Integer(comps[2]));
			data.setModelClass(workflow.getClassName());
			data.setModelName(workflow.getName());
			data.setMonth(new Integer(comps[1]));
			data.setNumberOfRequests(workflow.getRequests().size());
			if(Status.COMPLETED.equals(workflow.getStatus().getCode()))
				data.setProcessedDate(workflow.getCompletedDate());
			else if(Status.REJECTED.equals(workflow.getStatus().getCode()))
				data.setProcessedDate(workflow.getRejectedDate());
			data.setProcessedStatusCode(workflow.getStatus().getCode());
			data.setProcessTime(workflow.handledTime());
			data.setSubmittedDate(workflow.getSubmittedDate());
			data.setYear(new Integer(comps[0]));
			
			if(i % 10 == 0) {
				Log.customer.info("WorkflowCollector:    saving batch " + batch);
				oc.save();
				batch++;
			}
		}
		Log.customer.info("WorkflowCollector:    saving batch " + batch);
		oc.save();
	}
	
}
