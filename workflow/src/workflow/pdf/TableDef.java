package workflow.pdf;

import java.util.List;

import org.w3c.dom.Element;

import ariba.ui.widgets.XMLUtil;
import ariba.util.core.ListUtil;

public class TableDef extends DefAccessor {
	public TableDef(Element element) {
		super(element);
	}
	
	private TableColumn[] parseColumns(String type) {
		Element columnsElements[] = elementsNamed(root, "columns");
		List<TableColumn> colList = ListUtil.list();
		Element columnsElement = null;
		
		if(type == null) {
			if(columnsElements != null && columnsElements.length > 0)
				columnsElement = columnsElements[0];
		} else {
			for(Element elem : columnsElements) {
				if(type.equals(elem.getAttribute("type"))) {
					columnsElement = elem;
					break;
				}
			}
		}
		if(columnsElement != null) {
			for(Element colElement : XMLUtil.getAllChildren(columnsElement, "column")) {
				TableColumn col = new TableColumn();
				try {
					col.init(colElement);
					colList.add(col);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		TableColumn[] columns = new TableColumn[colList.size()];
		int i = 0;
		for (TableColumn col : colList) {
			columns[i++] = col;
		}
		
		return columns;
	}
	
	public TableColumn[] getColumns(int row, String type) {
		TableColumn[] columns = this.parseColumns(type);
		
		int rowCount = 0;
		for(TableColumn col : columns) {
			if(col.getRow() == row)
				rowCount++;
		}
		
		TableColumn cols[] = new TableColumn[rowCount];
		int i = 0;
		for(TableColumn col : columns) {
			if(col.getRow() == row)
				cols[i++] = col;
		}
		
		return cols;
	}
	public TableColumn[] getColumns(int row) {
		return this.getColumns(row, null);
	}
	
	public TableColumn[] getColumns(String type) {
		return getColumns(0, type);
	}
	
	public TableColumn[] getColumns() {
		return getColumns(0, null);
	}

	public float[] getColumnWidths(int row, String type) {
		TableColumn[] cols = this.getColumns(row);
		float widths[] = new float[cols.length];
		for(int i = 0; i < cols.length; i++) {
			if(cols[i].getWidth() == -1)
				continue;
			widths[i] = cols[i].getWidth();
		}
		return widths;
	}
	
	public float[] getColumnWidths(int row) {
		return this.getColumnWidths(row, null);
	}
	
	public float[] getColumnWidths(String type) {
		return getColumnWidths(0, type);
	}
	
	public float[] getColumnWidths() {
		return getColumnWidths(0, null);
	}

}
