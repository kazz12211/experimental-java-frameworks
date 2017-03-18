package workflow.view.pref;

import workflow.model.UserPreference;


public class DisplaySettingView extends AbstractPreferenceView {

	public void setShowCounts(Boolean value) {
		this.getUserPreference(UserPreference.KEY_SHOW_COUNTS).setBooleanValue(value);
	}
	public Boolean getShowCounts() {
		return this.getUserPreference(UserPreference.KEY_SHOW_COUNTS).getBooleanValue();
	}

}
