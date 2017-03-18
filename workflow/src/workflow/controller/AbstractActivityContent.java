package workflow.controller;

import workflow.model.Activity;
import ariba.util.fieldvalue.FieldValue;

public class AbstractActivityContent extends WizardContentView {

	public Activity getActivity() {
		if(this.hasBinding("activity")) {
			return (Activity) valueForBinding("activity");
		} else {
			return (Activity) FieldValue.getFieldValue(this.getContext(), "activity");
		}
	}

}
