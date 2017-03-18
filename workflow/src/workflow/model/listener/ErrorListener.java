package workflow.model.listener;

import java.util.Date;

import javax.persistence.PrePersist;
import workflow.model.Error;

public class ErrorListener {

	@PrePersist
	public void onPrePersist(Object object) {
		Error error = (Error) object;
		error.setTimestamp(new Date());
	}
}
