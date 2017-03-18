package workflow.view.analysis;

import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.DateUtils;
import core.util.ListUtils;
import core.util.MapUtils;
import workflow.analysis.Summary;
import workflow.controller.WorkflowManager;
import workflow.model.Actor;
import workflow.model.AnalysisSummaryActivity;
import workflow.model.Global;
import workflow.view.ActivityModelChooser.WorkflowAndActivityDef;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class SummaryByActivity extends AWComponent {
	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public WorkflowAndActivityDef selectedModel;
	public List<Integer> startYears;
	public Integer selectedStartYear;
	public List<Integer> endYears;
	public Integer selectedEndYear;
	private WorkflowManager manager;
	public List<SummaryData> summaries;
	public List<PivotLayout> layouts;
	public PivotLayout selectedLayout;
	public PivotLayout iter;
	public boolean layoutChangeLatch;

	@Override
	public void init() {
		super.init();
		manager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		selectedModel = null;
		
		int comps[] = DateUtils.dateComponents();
		
		startYears = ListUtils.list();
		endYears = ListUtils.list();

		for(int year = 2014; year <= comps[0]; year++) {
			startYears.add(new Integer(year));
			endYears.add(new Integer(year));
		}

		layouts = ListUtils.list();
		PivotLayout layout;
		layout = new PivotLayout(
				AWLocal.localizedJavaString(1, "By Activity", SummaryByActivity.class, requestContext()),
				new String[] {"modelName", "userName"},
				new String[] {"year","month", "status"},
				new String[] {"minProcessTime", "maxProcessTime", "averageProcessTime", "count"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(2, "By Actor", SummaryByActivity.class, requestContext()),
				new String[] {"userName", "modelName"},
				new String[] {"year","month", "status"},
				new String[] {"minProcessTime", "maxProcessTime", "averageProcessTime", "count"}
				);
		layouts.add(layout);
		selectedLayout = layouts.get(0);
	}
	
	public AWResponseGenerating layoutChanged() {
		layoutChangeLatch = true;
		return null;
	}

	public AWResponseGenerating search() {
		List<Predicate> predicates = ListUtils.list();
		if(selectedModel != null) {
			predicates.add(new Predicate.KeyValue("modelClass", selectedModel.activityDef.getModelName()));
			predicates.add(new Predicate.KeyValue("workflowClass", selectedModel.workflowDef.getModelName()));
		}
		if(selectedStartYear != null)
			predicates.add(new Predicate.KeyValue("year", selectedStartYear, Predicate.Operator.Gte));
		if(selectedEndYear != null)
			predicates.add(new Predicate.KeyValue("year", selectedEndYear, Predicate.Operator.Lte));
		QuerySpecification spec = new QuerySpecification(AnalysisSummaryActivity.class.getName(), new Predicate.And(predicates));
		List<AnalysisSummaryActivity> activities = ObjectContext.get().executeQuery(spec);
		summaries(activities);
		return null;
	}

	private void summaries(List<AnalysisSummaryActivity> activities) {
		summaries = ListUtils.list();
		
		Map<MultiKey, List<AnalysisSummaryActivity>> map = MapUtils.map();
		for(AnalysisSummaryActivity activity : activities) {
			Actor actor = activity.getActor();
			MultiKey key;
			if(actor == null) {
				key = new MultiKey(
						activity.getModelName(), 
						"",
						activity.getYear(),
						activity.getMonth(),
						activity.getStatus().getLabel());
			} else {
				key = new MultiKey(
						activity.getModelName(), 
						activity.getActor().getName(),
						activity.getYear(),
						activity.getMonth(),
						activity.getStatus().getLabel());
			}
			
			List<AnalysisSummaryActivity> value = findKey(map, key);
			if(value == null) {
				value = ListUtils.list();
				map.put(key, value);
			}
			value.add(activity);
		}
		
		for(MultiKey key : map.keySet()) {
			List<AnalysisSummaryActivity> value = map.get(key);
			Summary sum = Summary.activitySummary(value);
			SummaryData data = new SummaryData();
			data.modelName = key.modelName;
			data.userName = key.userName;
			data.year = key.year;
			data.month = key.month;
			data.status = key.status;
			data.averageProcessTime = sum.avgProcessTime;
			data.minProcessTime = sum.minProcessTime;
			data.maxProcessTime = sum.maxProcessTime;
			data.count = sum.count;
			summaries.add(data);
		}
	}

	
	private List<AnalysisSummaryActivity> findKey(
			Map<MultiKey, List<AnalysisSummaryActivity>> map, MultiKey key) {
		for(Map.Entry<MultiKey, List<AnalysisSummaryActivity>> entry: map.entrySet()) {
			if(entry.getKey().matches(key))
				return entry.getValue();
		}
		return null;
	}

	public Date lastModifiedDate() {
		Date date = null;
		Global global = Global.find("AnalysisDataCreation");
		if(global != null) {
			date = new Date(global.getLongValue());
		}
		return date;
	}

	@Override
	public boolean isStateless() { return false; }
}
