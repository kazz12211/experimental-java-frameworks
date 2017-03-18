package workflow.view.pref;

import workflow.model.User;
import workflow.model.UserPreference;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.util.fieldvalue.FieldValue;

public abstract class AbstractPreferenceView extends AWComponent {

	public UserPreference getUserPreference(String key) {
		User user = (User) FieldValue.getFieldValue(session(), "user");
		return user.getUserPreference(key);
	}
	
	@Override
	public boolean isStateless() { return false; }


}
