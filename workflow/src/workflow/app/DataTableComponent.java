package workflow.app;

import java.util.Map;

import ariba.ui.aribaweb.core.AWComponent;

public abstract class DataTableComponent extends AWComponent {

	public Map<String, Object> getTableConfig() {
		TableConfigManager manager = (TableConfigManager) session().getFieldValue("tableConfigManager");
		return manager.pop(this.name());
	}
	public void setTableConfig(Map<String, Object> tableConfig) {
		TableConfigManager manager = (TableConfigManager) session().getFieldValue("tableConfigManager");
		manager.push(tableConfig, this.name());
		manager.save();
	}

}
