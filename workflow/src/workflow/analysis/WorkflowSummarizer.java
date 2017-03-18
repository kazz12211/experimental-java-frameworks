package workflow.analysis;

import java.util.Date;
import java.util.List;

import workflow.model.AnalysisDataWorkflow;
import workflow.model.AnalysisSummaryWorkflow;
import ariba.ui.meta.persistence.ObjectContext;

public class WorkflowSummarizer {

	public void summarize(Date fromDate) {
		if(AnalysisSummaryWorkflow.deleteAll()) {
			new WorkflowCollector().collect(fromDate);

			List<?> objects = AnalysisDataWorkflow.sums();
			for(int i = 0; i < objects.size(); i++) {
				Object[] row = (Object[]) objects.get(i);
				AnalysisSummaryWorkflow sum = ObjectContext.get().create(AnalysisSummaryWorkflow.class);
				sum.setValueForKey(row[0], "year");
				sum.setValueForKey(row[1], "month");
				sum.setValueForKey(row[2], "day");
				sum.setValueForKey(row[3], "modelClass");
				sum.setValueForKey(row[4], "modelName");
				sum.setValueForKey(row[5], "statusCode");
				sum.setValueForKey(row[6], "creatorId");
				sum.setValueForKey(row[7], "numberOfRequests");
				sum.setValueForKey(row[8], "processTimeTotal");
				sum.setValueForKey(row[9], "maxProcessTime");
				sum.setValueForKey(row[10], "minProcessTime");
				sum.setValueForKey(row[11], "numberOfProcess");
				ObjectContext.get().save();
			}
		}
	}
}
