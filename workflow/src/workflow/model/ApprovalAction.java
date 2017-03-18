package workflow.model;

import javax.persistence.Entity;

@Entity
public class ApprovalAction extends Activity {

	Boolean approved;
	
	public Boolean getApproved() {
		return approved;
	}
	
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
}
