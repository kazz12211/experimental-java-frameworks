package workflow.controller.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.WorkflowDef;
import workflow.controller.rule.WorkflowRule;
import workflow.controller.rule.flow.PathDef;
import workflow.controller.rule.flow.RuleDef;
import workflow.model.Activity;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.util.AWFileResourceDirectory;
import ariba.ui.aribaweb.util.AWMultiLocaleResourceManager;
import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.aribaweb.util.AWResourceDirectory;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.util.core.IOUtil;
import ariba.util.log.Log;

public class Rules extends WorkflowManagerHelper {

	WorkflowRule workflowRule;
	private static final String RULE_FILE_NAME = "Rule.xml";
	private static final String RULE_DIR_NAME = "rule";
	private static final String BACKUP_DIR_NAME = "rule-backup";
	private static final String TIMESTAMP_FILE_NAME = "rule_backup_timestamp.txt";
	private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	
	public Rules(WorkflowManager manager) {
		super(manager);
		workflowRule = new WorkflowRule(RULE_FILE_NAME);
		
		if(workflowRule.isValid()) {
			backupRuleFile(RULE_FILE_NAME);
		}
	}
	
	public WorkflowRule getWorkflowRule() {
		return workflowRule;
	}
	
	public List<WorkflowDef> availableWorkflowModels(User user) {
		return workflowRule.availableWorkflowModelsForUser(user);
	}
		
	public List<String> availableWorkflowModelNames(User user) {
		List<String> availableModelNames = ListUtils.list();
		for (WorkflowDef def : this.availableWorkflowModels(user)) {
			availableModelNames.add(def.getName());
		}
		return availableModelNames;
	}

	private boolean findActivityType(List<ActivityDef> activities, ActivityDef activity) {
		for(ActivityDef def : activities) {
			if(def.getModelName().equals(activity.getModelName()))
				return true;
		}
		return false;
	}
	
	public List<ActivityDef> availableActivityModels(WorkflowDef workflowDef) {
		List<ActivityDef> activities = ListUtils.list();
		for(ActivityDef def : workflowDef.getActivities()) {
			if(this.findActivityType(activities, def) == false)
				activities.add(def);
		}
		return activities;
	}
	
	public List<ActivityDef> availableActivityModels() {
		List<ActivityDef> activities = ListUtils.list();
		for(WorkflowDef workflow : workflowRule.getWorkflows()) {
			List<ActivityDef> defs = this.availableActivityModels(workflow);
			for(ActivityDef def : defs) {
				if(this.findActivityType(activities, def) == false)
					activities.add(def);
			}
		}
		return activities;
	}
	
	public WorkflowDef workflowDefForModel(Workflow workflow) {
		return workflowRule.getWorkflowForModel(workflow.getClass());
	}

	public WorkflowDef workflowDefForModel(
			Class<? extends Workflow> workflowClass) {
		return workflowRule.getWorkflowForModel(workflowClass);
	}
	
	public RuleDef ruleDefForModel(Workflow workflow) {
		WorkflowDef def = this.workflowDefForModel(workflow);
		if (def != null)
			return def.getRule();
		return null;
	}

	public ActivityDef lookupActivityDef(Workflow workflow, String activityId) {
		WorkflowDef def = this.workflowDefForModel(workflow);
		return def.lookupActivity(activityId);
	}

	public ActivityDef lookupActivityDef(WorkflowDef workflowDef, String activityId) {
		return workflowDef.lookupActivity(activityId);
	}
	
	public ActivityDef lookupActivityDefOfClass(Workflow workflow, String activityClassName) {
		WorkflowDef def = this.workflowDefForModel(workflow);
		return def.lookupActivityOfClass(activityClassName);
	}
	public ActivityDef lookupActivityDefOfClass(WorkflowDef workflowDef, String activityClassName) {
		return workflowDef.lookupActivityOfClass(activityClassName);
	}
	
	public ActivityDef lookupActivityDefForActorRole(Class<? extends Workflow> workflowClass,
			String roleName) {
		WorkflowDef workflowDef = this.workflowDefForModel(workflowClass);
		return workflowDef.lookupActivityForActorRole(roleName);
	}
	
	public PathDef exitPath(List<PathDef> paths) {
		for (PathDef path : paths) {
			if (path.isExit())
				return path;
		}
		return null;
	}

	public List<WorkflowDef> allWorkflowModels() {
		return workflowRule.getWorkflows();
	}

	public void reload() {
		this.workflowRule.reload();
	}
	
	public Map<String, Object> userInfoForModel(Workflow workflow) {
		WorkflowDef def = this.workflowDefForModel(workflow);
		return this.userInfoForModel(def);
	}
	public Map<String, Object> userInfoForModel(WorkflowDef workflow) {
		return workflow.userInfoDictionary();
	}
	
	public Map<String, Object> userInfoForModel(Workflow workflow, Activity activity) {
		ActivityDef def = this.lookupActivityDefOfClass(workflow, activity.getClass().getName());
		return def.userInfoDictionary();
	}
	
	public Map<String, Object> userInfoForModel(Workflow workflow, String activityId) {
		ActivityDef def = this.lookupActivityDef(workflow, activityId);
		return def.userInfoDictionary();
	}
	
	/// FOLLOWING METHODS ARE NOT FULLY IMPLEMENTED

	private String lastPathComponent(String path) {
		String[] components = path.split("/");
		return components[components.length-1];
	}
	
	private AWResourceDirectory ruleDirectory() {
		AWMultiLocaleResourceManager resourceManager = AWServletApplication.sharedInstance().resourceManager();
		List<AWResourceDirectory> directories = resourceManager.resourceDirectories();
		AWResourceDirectory resourceDirectory = null;
		for(AWResourceDirectory directory : directories) {
			if(lastPathComponent(directory.directoryPath()).equals(RULE_DIR_NAME)) {
				resourceDirectory = directory;
				break;
			}
		}
		return resourceDirectory;
	}
	
	private AWResourceDirectory backupDirectory() {
		AWMultiLocaleResourceManager resourceManager = AWServletApplication.sharedInstance().resourceManager();
		List<AWResourceDirectory> directories = resourceManager.resourceDirectories();
		AWResourceDirectory resourceDirectory = null;
		for(AWResourceDirectory directory : directories) {
			if(lastPathComponent(directory.directoryPath()).equals(BACKUP_DIR_NAME)) {
				resourceDirectory = directory;
				break;
			}
		}
		return resourceDirectory;
	}

	private void backupRuleFile(String fileName) {
		AWMultiLocaleResourceManager resourceManager = AWServletApplication.sharedInstance().resourceManager();
		AWResourceDirectory resourceDirectory = this.backupDirectory();
		if(resourceDirectory != null) {
			AWResource ruleFile = resourceManager.resourceNamed(RULE_FILE_NAME);
			Date timestamp = this.backupTimestamp();
			//Log.customer.debug("Rules: last backup time = " + timestamp);
			//Log.customer.debug("Rules: last modified of " + fileName + " = " + new Date(ruleFile.lastModified()));
			if(timestamp != null && ruleFile.lastModified() > timestamp.getTime()) {
				_backup(fileName);
			}
		}
	}
	
	private void _backup(String fileName) {
		Date timestamp = new Date();
		_copy(fileName, fileName + TIMESTAMP_FORMAT.format(timestamp));
		_updateBackupTimestamp(timestamp);
	}
	
	private void _copy(String from, String to) {
		Log.customer.debug("Rules: copying " + from + " to " + to);
	}

	private void _updateBackupTimestamp(Date timestamp) {
	
	}

	private Date backupTimestamp() {
		AWResource file = this.backupTimestampFile();
		if(file != null) {
			InputStream in = file.inputStream();
			try {
				String line = IOUtil.readLine(in);
				if(line != null && line.length() >= 19) {
					return TIMESTAMP_FORMAT.parse(line.substring(0, 19));
				}
			} catch (IOException e) {
				Log.customer.error("Rules: could not read backup timestamp file", e);
			} catch (ParseException e) {
				Log.customer.error("Rules: could not parse backup timestamp", e);
			}
		}
		return null;
	}
	private AWResource backupTimestampFile() {
		AWMultiLocaleResourceManager resourceManager = AWServletApplication.sharedInstance().resourceManager();
		return resourceManager.resourceNamed(TIMESTAMP_FILE_NAME);
	}
}
