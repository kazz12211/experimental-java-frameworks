package workflow.controller.rule.flow;

import java.util.List;
import java.util.Map;

import workflow.controller.rule.WorkflowRule;
import workflow.model.Request;
import workflow.model.Workflow;
import core.util.ListUtils;
import core.util.MapUtils;
import ariba.util.log.Log;

public class RuleDef {

	List<ActivityRef> activityRefs = ListUtils.list();
	List<TransitionDef> transitions = ListUtils.list();
	SourceDestinationMap map = new SourceDestinationMap();
	
	public List<ActivityRef> getActivityRefs() {
		return activityRefs;
	}
	
	public void addActivityRef(ActivityRef activityRef) {
		this.activityRefs.add(activityRef);
	}
	
	public List<TransitionDef> getTransitions() {
		return transitions;
	}
	public void addTransition(TransitionDef transition) {
		if(WorkflowRule.debugMode)
		Log.customer.debug("Adding " + transition.toString());
		this.transitions.add(transition);
		this.addToMap(transition);
	}

	private void addToMap(TransitionDef transition) {
		String sourceId = transition.getSourceId();
		for(PathDef path : transition.getPaths()) {
			if(!path.isExit()) {
				String destId = path.getDestinationId();
				map.addSourceAndDestination(sourceId, destId);
			}
		}
	}

	public boolean isActive(Workflow workflow, ActivityRef activityRef) {
		List<ActivityRef> activeActivities = this.getAvailableActivityRefs(workflow);
		for(ActivityRef ref : activeActivities) {
			if(ref.getId().equals(activityRef.getId()))
				return true;
		}
		return false;
	}
	
	public List<ActivityRef> getFirstActivityRefs(Workflow workflow) {
		List<ActivityRef> activities = ListUtils.list();
		for(ActivityRef ref : this.getAvailableActivityRefs(workflow)) {
			List<ActivityRef> sources = this.findSourcesForActivity(ref);
			boolean good = false;
			if(sources == null || sources.isEmpty()) {
				good = true;
			} else {
				good = true;
				for(ActivityRef source : sources) {
					if(isActive(workflow, source)) {
						good = false; break;
					}
				}
			}
			if(good && ref.matches(workflow))
				this.addToActivityList(activities, ref);
		}
		return activities;
	}
	
	public List<ActivityRef> getAvailableActivityRefs(Workflow workflow) {
		List<ActivityRef> activities = ListUtils.list();
		for(ActivityRef ref : activityRefs) {
			if(ref.matches(workflow))
				this.addToActivityList(activities, ref);
		}
		return activities;
	}
	
	
	// don't add duplicated activity-ref
	private void addToActivityList(List<ActivityRef> list, ActivityRef act) {
		ActivityRef found = null;
		for(ActivityRef ref : list) {
			if(act.getId().equals(ref.getId())) {
				found = ref;
				break;
			}
		}
		if(found == null)
			list.add(act);
	}

	public List<TransitionDef> findTransitions(Request request) {
		List<TransitionDef> nextTransitions = ListUtils.list();
		for(TransitionDef td : transitions) {
			if(request.getActivityRefId().equals(td.getSourceId())) {
				nextTransitions.add(td);
			}
		}
		return nextTransitions;
	}
	public List<PathDef> findPaths(Request request) {
		List<TransitionDef> nextTransitions = this.findTransitions(request);
		List<PathDef> paths = ListUtils.list();
		for(TransitionDef td : nextTransitions) {
			for(PathDef path : td.getPaths()) {
				if(path.evaluate(request)) {
					if(WorkflowRule.debugMode)
					Log.customer.debug("RuleDef: path {sourceId=" + request.getActivityRefId() + "; destId=" + path.destinationId + "} evaluated with condition " + path.getCondition());
					paths.add(path);
				}
			}
		}
		return paths;
	}
	
	public List<ActivityRef> findSourcesForActivity(ActivityRef act) {
		List<ActivityRef> sources = ListUtils.list();
		for(TransitionDef transition : transitions) {
			for(PathDef path : transition.getPaths()) {
				String destId = path.getDestinationId();
				if(destId != null && act.getId().equals(destId)) {
					ActivityRef ref = this.lookupActivityRef(transition.getSourceId());
					if(ref != null) {
						sources.add(ref);
					}
					break;
				}
			}
		}
		return sources;
	}

	
	private ActivityRef lookupActivityRef(String id) {
		for(ActivityRef ref : activityRefs) {
			if(id.equals(ref.getId()))
				return ref;
		}
		return null;
	}


	class SourceDestinationMap {
		Map<String, List<String>> sourceToDestination = MapUtils.map();
		Map<String, List<String>> destinationToSource = MapUtils.map();
		
		public void addSourceAndDestination(String sourceId, String destId) {
			List<String> dests = sourceToDestination.get(sourceId);
			if(dests == null) {
				dests = ListUtils.list();
				sourceToDestination.put(sourceId, dests);
			}
			dests.add(destId);
			List<String> srcs = destinationToSource.get(destId);
			if(srcs == null) {
				srcs = ListUtils.list();
				destinationToSource.put(destId, srcs);
			}
			srcs.add(sourceId);
		}

		public List<String> getDestinationIds(String activityId) {
			return sourceToDestination.get(activityId);
		}

		public boolean isFirstActivity(String refId) {
			return (destinationToSource.get(refId) == null);
		}
	}


}
