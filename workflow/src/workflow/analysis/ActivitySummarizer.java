package workflow.analysis;

import java.util.Date;
import java.util.List;

import workflow.model.AnalysisDataActivity;
import workflow.model.AnalysisSummaryActivity;
import ariba.ui.meta.persistence.ObjectContext;

public class ActivitySummarizer {

	public void summarize(Date fromDate) {
		if(AnalysisSummaryActivity.deleteAll()) {
			new ActivityCollector().collect(fromDate);
			
			List<?> list = AnalysisDataActivity.sums();
			for(int i = 0; i < list.size(); i++) {
				Object[] row = (Object[]) list.get(i);
				AnalysisSummaryActivity sum = ObjectContext.get().create(AnalysisSummaryActivity.class);
				sum.setValueForKey(row[0], "year");
				sum.setValueForKey(row[1], "month");
				sum.setValueForKey(row[2], "day");
				sum.setValueForKey(row[3], "workflowClass");
				sum.setValueForKey(row[4], "modelClass");
				sum.setValueForKey(row[5], "modelName");
				sum.setValueForKey(row[6], "actorId");
				sum.setValueForKey(row[7], "statusCode");
				sum.setValueForKey(row[8], "processTimeTotal");
				sum.setValueForKey(row[9], "maxProcessTime");
				sum.setValueForKey(row[10], "minProcessTime");
				sum.setValueForKey(row[11], "numberOfActivity");
				ObjectContext.get().save();
			}
		}
	}
}
