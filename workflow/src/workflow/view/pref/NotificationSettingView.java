package workflow.view.pref;

import workflow.model.UserPreference;

public class NotificationSettingView extends AbstractPreferenceView {

	public void setRequestNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_REQUEST).setBooleanValue(value);
	}
	public Boolean getRequestNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_REQUEST).getBooleanValue();
	}
	
	public void setActionNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_ACTION).setBooleanValue(value);
	}
	public Boolean getActionNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_ACTION).getBooleanValue();
	}
	
	public void setCompleteNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_COMPLETE).setBooleanValue(value);
	}
	public Boolean getCompleteNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_COMPLETE).getBooleanValue();
	}
	
	public void setErrorNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_ERROR).setBooleanValue(value);
	}
	public Boolean getErrorNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_ERROR).getBooleanValue();
	}
	
	public void setRejectNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_REJECT).setBooleanValue(value);
	}
	public Boolean getRejectNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_REJECT).getBooleanValue();
	}
	
	public void setExpireNotif(Boolean value) {
		this.getUserPreference(UserPreference.KEY_NOTIF_EXPIRE).setBooleanValue(value);
	}
	public Boolean getExpireNotif() {
		return this.getUserPreference(UserPreference.KEY_NOTIF_EXPIRE).getBooleanValue();
	}


}
