package workflow.view.analysis;

import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.DateUtils;
import core.util.ListUtils;
import core.util.MapUtils;
import workflow.analysis.Summary;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.WorkflowDef;
import workflow.model.AnalysisSummaryWorkflow;
import workflow.model.Global;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QuerySpecification;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class SummaryByWorkflow extends AWComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public WorkflowDef selectedModel;
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
				AWLocal.localizedJavaString(1, "By Workflow", SummaryByWorkflow.class, requestContext()),
				new String[] {"modelName", "userName"},
				new String[] {"year","month", "status"},
				new String[] {"minProcessTime", "maxProcessTime", "averageProcessTime", "count"}
				);
		layouts.add(layout);
		layout = new PivotLayout(
				AWLocal.localizedJavaString(2, "By Creator", SummaryByWorkflow.class, requestContext()),
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
		if(selectedModel != null)
			predicates.add(new Predicate.KeyValue("modelClass", selectedModel.getModelName()));
		if(selectedStartYear != null)
			predicates.add(new Predicate.KeyValue("year", selectedStartYear, Predicate.Operator.Gte));
		if(selectedEndYear != null)
			predicates.add(new Predicate.KeyValue("year", selectedEndYear, Predicate.Operator.Lte));
		QuerySpecification spec = new QuerySpecification(AnalysisSummaryWorkflow.class.getName(), new Predicate.And(predicates));
		List<AnalysisSummaryWorkflow> workflows = ObjectContext.get().executeQuery(spec);
		summaries(workflows);
		return null;
	}

	private void summaries(List<AnalysisSummaryWorkflow> workflows) {
		summaries = ListUtils.list();
		
		Map<MultiKey, List<AnalysisSummaryWorkflow>> map = MapUtils.map();
		for(AnalysisSummaryWorkflow workflow : workflows) {
			MultiKey key = new MultiKey(
					workflow.getModelName(), 
					workflow.getCreator().getName(),
					workflow.getYear(),
					workflow.getMonth(),
					workflow.getStatus().getLabel());
			
			List<AnalysisSummaryWorkflow> value = findKey(map, key);
			if(value == null) {
				value = ListUtils.list();
				map.put(key, value);
			}
			value.add(workflow);
		}
		
		for(MultiKey key : map.keySet()) {
			List<AnalysisSummaryWorkflow> value = map.get(key);
			Summary sum = Summary.workflowSummary(value);
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

	private List<AnalysisSummaryWorkflow> findKey(
			Map<MultiKey, List<AnalysisSummaryWorkflow>> map, MultiKey key) {
		for(Map.Entry<MultiKey, List<AnalysisSummaryWorkflow>> entry: map.entrySet()) {
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
