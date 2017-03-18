package workflow.controller;

import java.lang.reflect.Constructor;

import workflow.aribaweb.hibernate.HibernateUtil;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Request;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.wizard.component.WizardUtil;
import ariba.util.core.ClassUtil;
import ariba.util.log.Log;

public class WizardLauncher {

	public static AWResponseGenerating startWorkflowWizard(
			String mode, 
			Workflow model, 
			AWComponent caller, 
			WorkflowManager workflowManager) {
		
		try {
			HibernateUtil.reloadObject(model, ObjectContext.get());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: failed to reload workflow object", e);
		}
		
		String controllerName = workflowManager.controllerNameForModel(model);
		Class<?> controllerClass = ClassUtil.classForName(controllerName);
		Log.customer.debug("Creating wizard in '" + mode + "' mode for model '" + model.getClass().getName() + "' using controller '" + controllerName + "'");
		try {
			Constructor<?> constructor = controllerClass.getConstructor(new Class[] {String.class, Workflow.class, AWRequestContext.class, AWResourceManager.class});
			WorkflowWizard wizard = (WorkflowWizard) constructor.newInstance(mode, model, caller.requestContext(), caller.resourceManager());
			wizard.setCaller(caller.pageComponent());
			return WizardUtil.startWizard(wizard, caller.requestContext());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: could not create workflow wizard instance for '" + model.getName() + "' in mode '" + mode + "'", e);
		}
		return null;
	}

	public static AWResponseGenerating copyWorkflowWizard(
			User creator,
			Workflow workflow,
			String title, 
			AWComponent caller,
			WorkflowManager workflowManager) {
		String controllerName = workflowManager.controllerNameForModel(workflow);
		Class<?> controllerClass = ClassUtil.classForName(controllerName);
		Log.customer.debug("Creating wizard in 'new' mode for copied model '" + workflow.getClass().getName() + "' using controller '" + controllerName + "'");
		try {
			Constructor<?> constructor = controllerClass.getConstructor(new Class[] {User.class, User.class, Workflow.class, String.class, String.class, AWRequestContext.class, AWResourceManager.class});
			WorkflowWizard wizard = (WorkflowWizard) constructor.newInstance(creator, null, workflow, title, WorkflowWizard.MODE_NEW, caller.requestContext(), caller.resourceManager());
			wizard.setCaller(caller.pageComponent());
			return WizardUtil.startWizard(wizard, caller.requestContext());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: could not create workflow wizard instance for '" + workflow.getName() + "' in mode 'new'", e);
		}
		return null;
	}
	
	public static AWResponseGenerating startWorkflowWizard(
			User creator, 
			WorkflowDef modelDef, 
			AWComponent caller, 
			WorkflowManager workflowManager) {
		
		String modelName = modelDef.getModelName();
		String controllerName = modelDef.getControllerName();
		Class<?> controllerClass = ClassUtil.classForName(controllerName);
		Log.customer.debug("Creating wizard in new mode for model '" + modelName + "' using controller '" + controllerName + "'");
		try {
			Constructor<?> constructor = controllerClass.getConstructor(new Class[] {User.class, User.class, String.class, AWRequestContext.class, AWResourceManager.class});
			WorkflowWizard wizard = (WorkflowWizard) constructor.newInstance(creator, null, WorkflowWizard.MODE_NEW, caller.requestContext(), caller.resourceManager());
			wizard.setCaller(caller.pageComponent());
			return WizardUtil.startWizard(wizard, caller.requestContext());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: could not create workflow wizard instance for '" + modelName + "' in mode '" + WorkflowWizard.MODE_NEW + "'", e);
		}
		return null;
	}

	public static AWResponseGenerating startActivityWizard(
			User actor, 
			String mode, 
			Request model, 
			AWComponent caller, 
			WorkflowManager workflowManager) {
		
		try {
			HibernateUtil.reloadObject(model, ObjectContext.get());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: failed to reload request object", e);
		}

		String controllerName = workflowManager.controllerNameForModel(model);
		Class<?> controllerClass = ClassUtil.classForName(controllerName);
		Log.customer.debug("Creating wizard '" + controllerName +  "' in '" + mode + "' mode for model '" + model.getActivityClassName() + "' using controller '" + controllerName + "'");
		try {
			Constructor<?> constructor = controllerClass.getConstructor(
					new Class[] {
							User.class, String.class, Request.class, AWRequestContext.class, AWResourceManager.class
					}
					);
			ActivityWizard wizard = (ActivityWizard) constructor.newInstance(
					actor, mode, model, caller.requestContext(), caller.resourceManager());
			wizard.setCaller(caller.pageComponent());
			return WizardUtil.startWizard(wizard, caller.requestContext());
		} catch (Exception e) {
			Log.customer.error("WizardLauncher: could not create activity wizard instance for '" + model.getActivityClassName() + "' in mode '" + WorkflowWizard.MODE_NEW + "'", e);
		}
		return null;
	}
}
