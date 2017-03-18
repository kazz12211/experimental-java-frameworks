package workflow.model;

import javax.persistence.Entity;

@Entity
public class ConfirmAction extends Activity {

	Boolean confirmed;
	
	public Boolean getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}
}
