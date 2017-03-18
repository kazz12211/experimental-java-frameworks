package workflow.analysis;

import java.util.List;

import workflow.model.AnalysisSummaryActivity;
import workflow.model.AnalysisSummaryWorkflow;

public class Summary {

	public long maxProcessTime = 0;
	public long minProcessTime = 0;
	public long avgProcessTime = 0;
	public long totalProcessTime = 0;
	public long count = 0;
	
	
	public static Summary workflowSummary(List<AnalysisSummaryWorkflow> workflows) {
		Summary sum = new Summary();
		
		for(AnalysisSummaryWorkflow workflow : workflows) {
			if(sum.minProcessTime == 0)
				sum.minProcessTime = workflow.getMinProcessTime().longValue();
			if(workflow.getMinProcessTime().longValue() < sum.minProcessTime)
				sum.minProcessTime = workflow.getMinProcessTime().longValue();
			if(sum.maxProcessTime == 0)
				sum.maxProcessTime = workflow.getMaxProcessTime().longValue();
			else if(workflow.getMaxProcessTime().longValue() > sum.maxProcessTime)
				sum.maxProcessTime = workflow.getMaxProcessTime().longValue();
			sum.count += workflow.getNumberOfProcess().longValue();
			sum.totalProcessTime += workflow.getProcessTimeTotal().longValue();
		}
		sum.avgProcessTime = sum.totalProcessTime / sum.count;
		return sum;
	}
	
	public static Summary activitySummary(List<AnalysisSummaryActivity> activities) {
		Summary sum = new Summary();

		for(AnalysisSummaryActivity activity : activities) {
			if(activity.getMinProcessTime() == null || activity.getMaxProcessTime() == null || activity.getProcessTimeTotal() == null)
				continue;
			if(sum.minProcessTime == 0)
				sum.minProcessTime = activity.getMinProcessTime().longValue();
			if(activity.getMinProcessTime().longValue() < sum.minProcessTime)
				sum.minProcessTime = activity.getMinProcessTime().longValue();
			if(sum.maxProcessTime == 0)
				sum.maxProcessTime = activity.getMaxProcessTime().longValue();
			else if(activity.getMaxProcessTime().longValue() > sum.maxProcessTime)
				sum.maxProcessTime = activity.getMaxProcessTime().longValue();
			sum.count += activity.getNumberOfActivity().longValue();
			sum.totalProcessTime += activity.getProcessTimeTotal().longValue();
		}
		sum.avgProcessTime = sum.totalProcessTime / sum.count;
		return sum;
		
	}
}
