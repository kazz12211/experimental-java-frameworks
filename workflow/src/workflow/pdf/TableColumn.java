package workflow.pdf;

import org.w3c.dom.Element;

import ariba.ui.widgets.XMLUtil;

public class TableColumn {
	private int align;
	private String fontName;
	private float width;
	private String label;
	private String name;
	private int colspan;
	private int rowspan;
	private int row;
	
	public int getAlignment() {
		return align;
	}
	public String getFontName() {
		return fontName;
	}
	public float getWidth() {
		return width;
	}
	public String getLabel() {
		return label;
	}
	public String getName() {
		return name;
	}
	public int getColspan() {
		return colspan;
	}
	public int getRowspan() {
		return rowspan;
	}
	public int getRow() {
		return row;
	}
	
	private int parseAlignment(String str) {
		if(str == null)
			return com.itextpdf.text.Element.ALIGN_UNDEFINED;
		String comps[] = str.split("\\|");
		
		if(comps.length == 0)
			return com.itextpdf.text.Element.ALIGN_UNDEFINED;
		if(comps.length == 1) {
			return parseOneAlignment(comps[0]);
		} else {
			int a = com.itextpdf.text.Element.ALIGN_UNDEFINED;
			for(String s : comps) {
				int b = this.parseOneAlignment(s);
				if(b == com.itextpdf.text.Element.ALIGN_UNDEFINED)
					continue;
				if(a == com.itextpdf.text.Element.ALIGN_UNDEFINED)
					a = b;
				else
					a |= b;
			}
			return a;
		}
	}
	
	private int parseOneAlignment(String str) {
		if("baseline".equals(str))
			return com.itextpdf.text.Element.ALIGN_BASELINE;
		else if("bottom".equals(str))
			return com.itextpdf.text.Element.ALIGN_BOTTOM;
		else if("center".equals(str))
			return com.itextpdf.text.Element.ALIGN_CENTER;
		else if("justified".equals(str))
			return com.itextpdf.text.Element.ALIGN_JUSTIFIED;
		else if("justified_all".equals(str))
			return com.itextpdf.text.Element.ALIGN_JUSTIFIED_ALL;
		else if("left".equals(str))
			return com.itextpdf.text.Element.ALIGN_LEFT;
		else if("middle".equals(str))
			return com.itextpdf.text.Element.ALIGN_MIDDLE;
		else if("right".equals(str))
			return com.itextpdf.text.Element.ALIGN_RIGHT;
		else if("right".equals(str))
			return com.itextpdf.text.Element.ALIGN_TOP;
		else
			return com.itextpdf.text.Element.ALIGN_UNDEFINED;
	}
	
	public void init(Element colElement) throws Exception {			
		String alignStr = colElement.getAttribute("align");
		align = this.parseAlignment(alignStr);
		fontName = colElement.getAttribute("font");
		if(fontName == null || fontName.isEmpty())
			fontName = "default";
		String widthStr = colElement.getAttribute("width");
		if(widthStr == null || widthStr.isEmpty())
			width = -1;
		else
			width = Float.parseFloat(widthStr);
		label = XMLUtil.getText(colElement, "");
		name = colElement.getAttribute("name");
		if(name == null || name.isEmpty())
			name = label;
		String span = colElement.getAttribute("colspan");
		if(span == null || span.isEmpty())
			colspan = 1;
		else
			colspan = Integer.parseInt(span);
		span = colElement.getAttribute("rowspan");
		if(span == null || span.isEmpty())
			rowspan = 1;
		else
			rowspan = Integer.parseInt(span);
		row = 0;
		String rowStr = colElement.getAttribute("row");
		if(rowStr != null && !rowStr.isEmpty())
			row = Integer.parseInt(rowStr);
	}

}
