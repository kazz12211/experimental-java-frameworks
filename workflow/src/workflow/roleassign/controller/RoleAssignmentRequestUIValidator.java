package workflow.roleassign.controller;

import java.util.List;

import core.util.ListUtils;
import core.util.StringUtils;
import workflow.controller.AbstractFrameContent;
import workflow.controller.UIValidator;
import workflow.model.Role;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;

public class RoleAssignmentRequestUIValidator implements UIValidator {

	@Override
	public void validateInContent(AbstractFrameContent frameContent,
			AWRequestContext requestContext) {

		String title = (String) frameContent.getValidationTargetValue("title");
		if(StringUtils.nullOrEmptyOrBlank(title)) {
			frameContent.recordValidationError(
					"title", 
					AWLocal.localizedJavaString(1, "Missing title", RoleAssignmentRequestUIValidator.class, requestContext), 
					title);
		}
		
		@SuppressWarnings("unchecked")
		List<Role> requestedRoles = (List<Role>) frameContent.getValidationTargetValue("requestedRoles");
		if(ListUtils.nullOrEmpty(requestedRoles)) {
			frameContent.recordValidationError(
					"requestedRoles", 
					AWLocal.localizedJavaString(2, "Empty requested roles", RoleAssignmentRequestUIValidator.class, requestContext), 
					requestedRoles);
		}
	}

}
