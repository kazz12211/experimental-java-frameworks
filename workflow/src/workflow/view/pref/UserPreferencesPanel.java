package workflow.view.pref;

import workflow.aribaweb.component.ModalPage;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;

public class UserPreferencesPanel extends ModalPage {

	public AWResponseGenerating done() {
		return super.close();
	}
	public AWResponseGenerating close() {
		return super.close();
	}
	public AWResponseGenerating apply() {
		ObjectContext.get().save();
		return null;
	}
	public AWResponseGenerating save() {
		ObjectContext.get().save();
		return super.close();
	}
}
