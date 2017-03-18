package workflow.controller.rule;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import workflow.controller.rule.cond.Condition;
import workflow.controller.rule.cond.ConditionParser;
import workflow.controller.rule.flow.ActivityRef;
import workflow.controller.rule.flow.PathDef;
import workflow.controller.rule.flow.RuleDef;
import workflow.controller.rule.flow.TransitionDef;
import workflow.controller.rule.trigger.TriggerDef;
import workflow.controller.trigger.Trigger;
import workflow.model.Activity;
import workflow.model.User;
import workflow.model.Workflow;
import core.util.ClassUtils;
import core.util.ListUtils;
import core.util.MapUtils;
import core.util.NumberUtils;
import core.util.XMLConfig;
import ariba.ui.widgets.XMLUtil;
import ariba.util.log.Log;

public class WorkflowRule extends XMLConfig {

	public static boolean debugMode = false;
	
	private List<WorkflowDef> workflows;
	private String fileName;

	public WorkflowRule(String fileName) {
		this.fileName = fileName;
		this.init();
	}
	
	public void reload() {
		this.init();
	}
	
	public WorkflowDef getWorkflowDefForModel(Workflow workflow) {
		String modelName = workflow.getClass().getName();
		return getWorkflowDefForModel(modelName);
	}
	public WorkflowDef getWorkflowDefForModel(String modelName) {
		for(WorkflowDef wd : workflows) {
			if(modelName.equals(wd.getModelName()))
				return wd;
		}
		return null;
	}
	
	public String getControllerNameForWorkflow(String modelName) {
		WorkflowDef def = this.getWorkflowDefForModel(modelName);
		if(def != null)
			return def.getControllerName();
		return null;
	}

	public String getControllerNameForActivity(Workflow workflow,
			String modelName) {
		WorkflowDef def = this.getWorkflowDefForModel(workflow);
		if(def != null) {
			ActivityDef adef = def.getActivityForModel(modelName);
			if(adef != null) {
				return adef.getControllerName();
			}
		}
		return null;
	}


	public List<WorkflowDef> availableWorkflowModelsForUser(User user) {
		List<WorkflowDef> workflowDefs = ListUtils.list();
		if(user != null) {
			for(WorkflowDef wd : workflows) {
				if(wd.isCreator(user))
					workflowDefs.add(wd);
			}
		}
		return workflowDefs;
	}

	public WorkflowDef getWorkflowForModel(Class<? extends Workflow> workflowClass) {
		for(WorkflowDef wd : workflows) {
			if(wd.getModelName().equals(workflowClass.getName()))
				return wd;
		}
		return null;
	}


	
	private Element load() throws MalformedURLException {
		String path = super.getResourcePath(fileName);
		URL url = new URL(path);
		Element docElem = XMLUtil.document(url, false, false, null).getDocumentElement();
		if(docElem.getNodeName().equals("workflows"))
			return docElem;
		return null;
	}
	
	private void init() {
		workflows = ListUtils.list();
		try {
			Element docElem = this.load();
			Element[] workflowElems = this.elementsNamed(docElem, "workflow");
			for(Element workflowElem : workflowElems) {
				parseWorkflowDef(workflowElem);
			}
		} catch (Exception e) {
			Log.customer.error("WorkflowRule: error occured during parsing " + fileName, e);
		}
	}
	
	public boolean isValid() {
		if(workflows.isEmpty())
			return false;
		for(WorkflowDef workflow : workflows) {
			if(workflow.isValid() == false)
				return false;
		}
		return true;
	}
	
	private void parseWorkflowDef(Element workflowElem) {
		String name = workflowElem.getAttribute("name");
		String modelName = workflowElem.getAttribute("model");
		String controllerName = workflowElem.getAttribute("controller");
		
		if(debugMode)
		Log.customer.debug("WorkflowRule: workflow-def(" + name + ", " + modelName + ", " + controllerName + " parsed.");

		WorkflowDef workflowDef = new WorkflowDef(name, modelName, controllerName);
		Element[] creatorRoleElems = this.elementsNamed(workflowElem, "creator-role");
		if(creatorRoleElems != null) {
			for(Element creatorRoleElem : creatorRoleElems) {
				parseCreatorRoleDef(workflowDef, creatorRoleElem);
			}
		}
		
		Element activitiesElem = this.elementNamed(workflowElem, "activities");
		if(activitiesElem != null) {
			Element[] activityElems = this.elementsNamed(activitiesElem, "activity");
			for(Element activityElem : activityElems) {
				parseActivityDef(workflowDef, activityElem);
			}
		}
		
		Element ruleElem = this.elementNamed(workflowElem, "rule");
		if(ruleElem != null) {
			this.parseRule(workflowDef, ruleElem);
		}
		
		Element triggersElem = this.elementNamed(workflowElem,"triggers");
		if(triggersElem != null) {
			Element[] triggerElems = this.elementsNamed(triggersElem, "trigger");
			for(Element triggerElem : triggerElems) {
				parseTriggerDef(workflowDef, triggerElem);
			}
		}
		
		Element userInfoElem = this.elementNamed(workflowElem, "user-info");
		if(userInfoElem != null) {
			this.parseUserInfo(workflowDef, userInfoElem);
		}
		
		workflows.add(workflowDef);
	}


	private void parseCreatorRoleDef(WorkflowDef workflowDef, Element creatorElem) {
		if(creatorElem != null) {
			String name = creatorElem.getAttribute("name");
			String type = creatorElem.getAttribute("type");
			if(debugMode)
			Log.customer.debug("WorkflowRule: creator-role (" + name + ", " + type +") parsed.");
			CreatorRoleDef roleDef = new CreatorRoleDef(name, type);
			workflowDef.addCreatorRole(roleDef);
		}
	}

	private void parseActivityDef(WorkflowDef workflowDef, Element activityElem) {
		String id = activityElem.getAttribute("id");
		String name = activityElem.getAttribute("name");
		String modelName = activityElem.getAttribute("model");
		String controllerName = activityElem.getAttribute("controller");
		if(debugMode)
		Log.customer.debug("WorkflowRule: activity-def(" + id + ", " + name + ", " + modelName + ", " + controllerName + ") parsed.");
		ActivityDef activityDef = new ActivityDef(id, name, modelName, controllerName);
		
		Element actorRoleElems[] = this.elementsNamed(activityElem, "actor-role");
		if(actorRoleElems != null) {
			for(Element actorRoleElem : actorRoleElems) {
				parseActorRoleDef(activityDef, actorRoleElem);
			}
		}
		
		parseExpirationDef(activityDef, this.elementNamed(activityElem, "expiration"));
		
		Element triggersElem = this.elementNamed(activityElem, "triggers");
		if(triggersElem != null) {
			Element[] triggerElems = this.elementsNamed(triggersElem, "trigger");
			for(Element triggerElem : triggerElems) {
				this.parseTriggerDef(activityDef, triggerElem);
			}
		}
		
		Element userInfoElem = this.elementNamed(activityElem, "user-info");
		if(userInfoElem != null) {
			this.parseUserInfo(activityDef, userInfoElem);
		}
		
		workflowDef.addActivityDef(activityDef);
	}

	private void parseExpirationDef(ActivityDef activityDef, Element element) {
		if(element != null) {
			String type = element.getAttribute("type");
			String value = element.getAttribute("value");
			if(debugMode)
			Log.customer.debug("WorkflowRule: expiration (" + type + ", " + value +") parsed.");
			ExpirationDef expDef = new ExpirationDef();
			expDef.setType(type);
			expDef.setValue(value);
			activityDef.setExpiration(expDef);
		}
	}

	private void parseActorRoleDef(ActivityDef activityDef, Element actorElem) {
		if(actorElem != null) {
			String name = actorElem.getAttribute("name");
			String type = actorElem.getAttribute("type");
			if(debugMode)
			Log.customer.debug("WorkflowRule: actor-role (" + name + ", " + type +") parsed.");
			ActorRoleDef roleDef = new ActorRoleDef(name, type);
			activityDef.addActorRole(roleDef);
		}
	}


	private void parseRule(WorkflowDef workflowDef, Element ruleElem) {
		Element additionElem = this.elementNamed(ruleElem, "addition");
		Element dependencyElem = this.elementNamed(ruleElem, "dependency");
		if(additionElem == null || dependencyElem == null) {
			Log.customer.warn("Needs addition rules and dependency rules");
			return;
		}
		
		Element[] refElems = this.elementsNamed(additionElem, "activity-ref");
		Element[] transitionElems = this.elementsNamed(dependencyElem, "transition");
		if(refElems == null || refElems.length == 0) {
			Log.customer.warn("Needs activity-ref in addition");
			return;
		}
		if(transitionElems == null || transitionElems.length == 0) {
			Log.customer.warn("Needs transitions in dependency");
			return;
		}
		RuleDef ruleDef = new RuleDef();
		this.parseAdditions(ruleDef, refElems);
		this.parseTransitions(ruleDef, transitionElems);
		
		workflowDef.setRule(ruleDef);
	}
	
	
	private void parseAdditions(RuleDef ruleDef, Element[] refElems) {
		for(Element elem : refElems) {
			this.parseAddition(ruleDef, elem);
		}
	}

	private void parseAddition(RuleDef ruleDef, Element elem) {
		String id = elem.getAttribute("id");
		if(id == null) {
			Log.customer.warn("Missing id attribute in activity-ref");
			return;
		}
		ActivityRef activityRef = new ActivityRef();
		activityRef.setId(id);
		if(debugMode)
		Log.customer.debug("WorkflowRule: activity-ref(" + id + ") parsed.");
		ruleDef.addActivityRef(activityRef);
		Element conditionElem = this.elementNamed(elem, "condition");
		if(conditionElem != null) {
			this.parseCondition(activityRef, conditionElem);
		}
	}

	private void parseTransitions(RuleDef ruleDef, Element[] transitionElems) {
		for(Element elem : transitionElems) {
			this.parseTransition(ruleDef, elem);
		}
	}

	private void parseTransition(RuleDef ruleDef, Element elem) {
		Element sourceElem = this.elementNamed(elem, "source");
		if(sourceElem == null) {
			Log.customer.warn("Missing source in transition");
			return;
		}
		Element[] pathElems = this.elementsNamed(elem, "path");
		if(pathElems == null || pathElems.length == 0) {
			Log.customer.warn("Missing paths in transition");
			return;
		}
		String sourceId = sourceElem.getAttribute("id");
		if(debugMode)
		Log.customer.debug("WorkflowRule: transition-def(" + sourceId + ") parsed.");
		TransitionDef transition = new TransitionDef();
		transition.setSourceId(sourceId);
		for(Element pathElem : pathElems) {
			parsePath(transition, pathElem);
		}
		ruleDef.addTransition(transition);
	}
	
	private void parsePath(TransitionDef transition, Element elem) {
		Element conditionElem = this.elementNamed(elem, "condition");
		Element destinationElem = this.elementNamed(elem, "destination");
		Element exitElem = this.elementNamed(elem, "exit");
		if(destinationElem != null) {
			String destinationId = destinationElem.getAttribute("id");
			if(destinationId == null) {
				Log.customer.warn("Missing destination in path");
				return;
			}
			if(debugMode)
			Log.customer.debug("WorkflowRule: path-def(" + destinationId + ") parsed.");
			PathDef path = new PathDef();
			path.setDestinationId(destinationId);
			transition.addPath(path);
			if(conditionElem != null) {
				parseCondition(path, conditionElem);
			}
		} else if(exitElem != null) {
			String exitStatus = exitElem.getAttribute("status");
			PathDef path = new PathDef();
			if(debugMode)
			Log.customer.debug("WorkflowRule: path-def(" + exitStatus + ") parsed.");
			path.setExitStatus(exitStatus);
			transition.addPath(path);
			if(conditionElem != null) {
				parseCondition(path, conditionElem);
			}
		}
	}

	private void parseCondition(ActivityRef activityRef, Element conditionElem) {
		Element elem;
		Condition condition = null;
		elem = this.elementNamed(conditionElem, "key-value");
		if (elem != null) {
			condition = ConditionParser.KeyValue.parse(elem);
		} else {
			elem = this.elementNamed(conditionElem, "or");
			if (elem != null) {
				condition = ConditionParser.Or.parse(elem);
			} else {
				elem = this.elementNamed(conditionElem, "and");
				if (elem != null) {
					condition = ConditionParser.And.parse(elem);
				} else {
					elem = this.elementNamed(conditionElem, "not");
					if (elem != null) {
						condition = ConditionParser.Not.parse(elem);
					}
				}
			}
		}

		if (condition != null) {
			activityRef.setCondition(condition);
		}
	}

	private void parseCondition(PathDef path, Element conditionElem) {
		Element elem;
		Condition condition = null;
		elem = this.elementNamed(conditionElem, "key-value");
		if (elem != null) {
			condition = ConditionParser.KeyValue.parse(elem);
		} else {
			elem = this.elementNamed(conditionElem, "or");
			if (elem != null) {
				condition = ConditionParser.Or.parse(elem);
			} else {
				elem = this.elementNamed(conditionElem, "and");
				if (elem != null) {
					condition = ConditionParser.And.parse(elem);
				} else {
					elem = this.elementNamed(conditionElem, "not");
					if (elem != null) {
						condition = ConditionParser.Not.parse(elem);
					}
				}
			}
		}

		if (condition != null) {
			path.setCondition(condition);
		}
	}

	
	private void parseTriggerDef(ModelDef modelDef, Element triggerElem) {
		String stage = triggerElem.getAttribute("stage");
		String triggerClass = triggerElem.getAttribute("triggerClass");
		if(stage != null && triggerClass != null) {
			if(debugMode)
			Log.customer.debug("WorkflowRule: trigger-def(" + stage + ", " + triggerClass + ") parsed.");
			TriggerDef trigger = new TriggerDef(stage, triggerClass, null);
			modelDef.addTrigger(trigger);
		}
	}
	

	public List<WorkflowDef> getWorkflows() {
		return workflows;
	}

	public List<Trigger> getTriggerForModelAndStage(Object model, String stage) {
		List<Trigger> triggers = ListUtils.list();
		List<TriggerDef> triggerDefs;
		List<TriggerDef> list = ListUtils.list();
		if(model instanceof Workflow) {
			Class workflowClass = model.getClass();
			WorkflowDef workflowDef = this.getWorkflowForModel(workflowClass);
			triggerDefs = workflowDef.getTriggersForStage(stage);
			list.addAll(triggerDefs);
		} else if(model instanceof Activity) {
			Workflow workflow = ((Activity)model).getRequest().getWorkflow();
			WorkflowDef workflowDef = this.getWorkflowForModel(workflow.getClass());
			for(ActivityDef ad : workflowDef.getActivities()) {
				if(ad.getModelName().equals(model.getClass().getName())) {
					triggerDefs = ad.getTriggersForStage(stage);
					for(TriggerDef def : triggerDefs) {
						if(ad.getId().equals(def.getId())) {
							list.add(def);
						}
					}
				}
			}
		}
		
		for(TriggerDef def : list) {
			Class<?> triggerClass = ClassUtils.classForName(def.getTriggerClass(), Trigger.class);
			if(triggerClass != null) {
				Trigger trigger = (Trigger) ClassUtils.newInstance(triggerClass);
				triggers.add(trigger);
			}
		}
		return triggers;
	}

	private void parseUserInfo(ModelDef modelDef, Element element) {
		try {
			Map<String, Object> map = null; 
			Element dictElement = this.elementNamed(element, "dict");
			if(dictElement != null) {
				map = this.parseDictionary(dictElement);
			}
			if(map != null) {
				UserInfoDef userInfo = new UserInfoDef();
				userInfo.setDictionary(map);
				modelDef.setUserInfo(userInfo);
			}
		} catch (Exception e) {
			Log.customer.error("WorkflowRule: error occured while parsing userInfo for model " + modelDef.getModelName(), e); 
		}
	}
	
	
	private static final String DICT_TYPE = "dict";
	private static final String ARRAY_TYPE = "array";
	private static final String STRING_TYPE = "string";
	private static final String NUMBER_TYPE = "number";
	private static final String DATE_TYPE = "date";
	private static final String BOOLEAN_TYPE = "boolean";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	private Map<String, Object> parseDictionary(Element element) throws Exception {
		Element elements[] = XMLUtil.getAllChildren(element, null);
		Map<String, Object> map = MapUtils.map();
		int size = elements.length;
		for(int i = 0; i < size; ) {
			Element keyElement = elements[i];
			Element valueElement = elements[i+1];
			if(keyElement.getNodeName().equals("key") && isValidValueElement(valueElement)) {
				String key = XMLUtil.getText(keyElement, null);
				Object value = objectValue(valueElement);
				if(key != null && value != null) {
					map.put(key, value);
				}
			}
			
			
			i += 2;
		}
		return map;
	}
	
	private Object objectValue(Element element) throws Exception {

		String type = element.getNodeName();
		if(DICT_TYPE.equals(type)) {
			return parseDictionary(element);
		} else if(ARRAY_TYPE.equals(type)) {
			return parseArray(element);
		} else if(STRING_TYPE.equals(type)) {
			return XMLUtil.getText(element, null);
		} else if(NUMBER_TYPE.equals(type)) {
			return parseNumber(element);
		} else if(DATE_TYPE.equals(type)) {
			return parseDate(element);
		} else if(BOOLEAN_TYPE.equals(type)) {
			return parseBoolean(element);
		}
		return null;

		
	}
	
	private List<Object> parseArray(Element element) throws Exception {
		Element elements[] = XMLUtil.getAllChildren(element, null);
		List<Object> list = ListUtils.list();
		int size = elements.length;
		for(int i = 0; i < size; i++) {
			Object value = this.objectValue(elements[i]);
			if(value != null)
				list.add(value);
		}
		return list;
	}
	
	private Number parseNumber(Element element) {
		String value = XMLUtil.getText(element, null);
		if(value == null)
			return null;
		return NumberUtils.toDouble(value);
	}

	private Date parseDate(Element element) throws ParseException {
		String value = XMLUtil.getText(element, null);
		if(value == null) {
			return null;
		}
		Date date = DATE_FORMAT.parse(value);
		return date;
	}
	
	private Boolean parseBoolean(Element element) {
		String value = XMLUtil.getText(element, null);
		if(value == null) {
			return null;
		}
		if("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
			return new Boolean(true);
		else if("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value))
			return new Boolean(false);
		return null;
	}

	private boolean isValidValueElement(Element element) {
		String type = element.getNodeName();
		if(DICT_TYPE.equals(type) || 
				ARRAY_TYPE.equals(type) || 
				STRING_TYPE.equals(type) || 
				NUMBER_TYPE.equals(type) || 
				DATE_TYPE.equals(type) || 
				BOOLEAN_TYPE.equals(type))
			return true;
		return false;
	}

}
