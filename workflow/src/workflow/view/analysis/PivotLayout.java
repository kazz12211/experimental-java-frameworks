package workflow.view.analysis;

import java.util.List;

import ariba.util.core.ListUtil;

public class PivotLayout {
	String name;
	List<String> rowFields;
	List<String> columnFields;
	List<String> columnAttributes;
	
	public PivotLayout(String name, String[] rowFields, String[] columnFields, String[] columnAttributes) {
		this.name = name;
		this.rowFields = ListUtil.arrayToList(rowFields);
		this.columnFields = ListUtil.arrayToList(columnFields);
		this.columnAttributes = ListUtil.arrayToList(columnAttributes);
	}
	public String getName() {
		return name;
	}
	public List<String> getRowFields() {
		return rowFields;
	}
	public List<String> getColumnFields() {
		return columnFields;
	}
	public List<String> getColumnAttributes() {
		return columnAttributes;
	}
}
