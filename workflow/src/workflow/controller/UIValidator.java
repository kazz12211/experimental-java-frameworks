package workflow.controller;

import ariba.ui.aribaweb.core.AWRequestContext;

public interface UIValidator {

	public void validateInContent(AbstractFrameContent frameContent, AWRequestContext requestContext);
}
