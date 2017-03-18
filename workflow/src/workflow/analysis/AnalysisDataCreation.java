package workflow.analysis;

import java.util.Date;

import core.util.DateUtils;
import workflow.model.Global;
import ariba.ui.meta.persistence.ObjectContext;

public class AnalysisDataCreation {
	
	public static final String GLOBAL_ANALYSYS_DATA_CREATION_KEY = "AnalysisDataCreation";

	public AnalysisDataCreation() {
		ObjectContext.bindNewContext();
	}
	
	public void create() {
		Date fromDate = this.fromDate();
		
		new WorkflowSummarizer().summarize(fromDate);
		new ActivitySummarizer().summarize(fromDate);
		
		this.recordDate();
	}

	private Date fromDate() {
		Global global = Global.findOrCreate(GLOBAL_ANALYSYS_DATA_CREATION_KEY);
		long timestamp = global.getLongValue();
		if(timestamp == 0)
			return DateUtils.dateWithComponents(2014, 5, 1, 0, 0, 0, 0);
		else
			return new Date(timestamp - (1000 * 60 * 60 * 24 * 14)); // from 2 weeks before
	}
	
	private void recordDate() {
		Global global = Global.find(GLOBAL_ANALYSYS_DATA_CREATION_KEY);
		if(global == null) {
			global = Global.create(GLOBAL_ANALYSYS_DATA_CREATION_KEY);
		}
		global.setLongValue(System.currentTimeMillis());
		ObjectContext.get().save();
	}
}
