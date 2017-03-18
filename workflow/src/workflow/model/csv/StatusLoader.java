package workflow.model.csv;

import java.util.List;
import java.util.Map;

import workflow.model.Status;
import core.util.MapUtils;
import ariba.ui.meta.persistence.ObjectContext;

public class StatusLoader extends Loader {

	public StatusLoader(ObjectContext oc) {
		super(oc);
	}

	@Override
	public void consumeLineOfTokens(String path, int rowIndex, List<String> record)
			throws Exception {
		if(record.size() != 2)
			return;
		
		String code = record.get(0);
		
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put("code", code);
		Status status = oc.findOne(Status.class, fieldValues);
		if(status == null) {
			status = oc.create(Status.class);
			status.setCode(code);
		}
		status.setLabel(record.get(1));
	}

	@Override
	protected String getResourceName() {
		return "Status.csv";
	}

}
