package workflow.view.management;

import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.DateUtils;
import core.util.ListUtils;
import core.util.MapUtils;
import workflow.WorkflowException;
import workflow.app.DataTableComponent;
import workflow.aribaweb.hibernate.HibernateUtil;
import workflow.controller.WizardLauncher;
import workflow.controller.WorkflowManager;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.Request;
import workflow.model.Status;
import workflow.model.User;
import workflow.tools.MissingRequestLink;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.fieldvalue.FieldValue;

public class UnknownAddressRequestSearch extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<Request> requests;
	public WorkflowDef selectedModel;
	public Date createdFrom;
	public Date createdTo;
	private WorkflowManager workflowManager;
	public Map<Long, String> unknownReasons;
	public Request currentItem;
	public List<User> expectedUsers = ListUtils.list();
	public User selectedUser;

	@Override
	public void init() {
		super.init();
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
		createdTo = DateUtils.endTimeOfTheDay(new Date());
		createdFrom = DateUtils.startTimeOfTheDay(DateUtils.dateByAddingDays(new Date(), -7));
	}

	public AWResponseGenerating search() {
		MissingRequestLink finder = new MissingRequestLink(ObjectContext.get());
		finder.setDateRange(createdFrom, createdTo);
		finder.setShowArchived(true);
		finder.setShowDeleted(false);
		finder.setWorkflowClassName(selectedModel != null ? selectedModel.getModelName() : null);
		unknownReasons = MapUtils.map();
		requests = finder.search(unknownReasons, requestContext());		
		return null;
	}

	
	public String getReason() {
		Request r = (Request) displayGroup.currentItem();
		return unknownReasons.get(r.getId());
	}
	
	public AWResponseGenerating inspect() {
		Request request = (Request) displayGroup.currentItem();
		return WizardLauncher.startWorkflowWizard(
				WorkflowWizard.MODE_INSPECT, 
				request.getWorkflow(), 
				this, 
				workflowManager);
	}

	public boolean itemSelectable() {
		return (!currentItem.isMissingActivityClass() && !currentItem.isMissingActor());
	}
	
	
	@Override
	public void applyValues(AWRequestContext requestContext,
			AWComponent component) {
		super.applyValues(requestContext, component);
		Request r = (Request) displayGroup.selectedObject();
		if(r != null) {
			expectedUsers.clear();
			expectedUsers.addAll(r.expectedActingUsers());
			User me = (User) session().getFieldValue("user");
			if(expectedUsers.contains(me) == false)
				expectedUsers.add(me);
		}
	}

	public AWResponseGenerating solve() {
		Request r = (Request) displayGroup.selectedObject();
		try {
			HibernateUtil.reloadObject(r, ObjectContext.get());
		} catch (Exception ignore) {
		}
		if(selectedUser != null && r.isNotSentTo(selectedUser) && r.getAction() == null) {
			selectedUser.getRequests().add(r);
			r.setRequestedDate(new Date());
			r.setStatusCode(Status.REQUESTED);
			ObjectContext.get().save();
			try {
				workflowManager.resend(r);
			} catch (WorkflowException ignore) {
			}
		}
		
		return search();
	}
	
	public AWResponseGenerating solveAll() {
		for(Request r : requests) {
			try {
				HibernateUtil.reloadObject(r, ObjectContext.get());
			} catch (Exception ignore) {
			}
			List<User> expectingUsers = ListUtils.list();
			expectingUsers.addAll(r.expectedActingUsers());
			for(User user : expectingUsers) {
				if(r.isNotSentTo(user)) {
					user.getRequests().add(r);
					r.setRequestedDate(new Date());
					r.setStatusCode(Status.REQUESTED);
					try {
						ObjectContext.get().save();
						workflowManager.resend(r);
					} catch (WorkflowException ignore) {
						ignore.printStackTrace();
					}
				}
			}
		}
		return search();
	}

	@Override
	public boolean isStateless() { return false; }

}
