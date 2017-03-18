package workflow.pdf;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import ariba.ui.widgets.XMLUtil;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

public class Fonts extends DefAccessor {
	Font defaultFont;
	Map<String, Font> fonts;
	Colors colors;
	
	public Fonts(Element element) {
		super(element);
		fonts = new HashMap<String, Font>();
		colors = new Colors();
		try {
			defaultFont = new Font(BaseFont.createFont());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Font createFont(String name, String encoding, String size, String style, String color) {
		BaseFont base = null;
		try {
			base = BaseFont.createFont(name, encoding, BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (base == null) {
			try {
				base = BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		int sizeValue = (size != null) ? Integer.parseInt(size)
				: Font.DEFAULTSIZE;
		int styleValue = (style != null) ? Integer.parseInt(style)
				: Font.NORMAL;
		BaseColor col = null;
		Font font = new Font(base, sizeValue, styleValue);
		if (color != null) {
			col = colors.getColor(color.toLowerCase());
		}
		if(col == null)
			col = BaseColor.BLACK;
		font.setColor(col);
		return font;
	}
	
	public Font getFont(String fontName) {
		if(fonts.size() == 0) {
			Element elements[] = XMLUtil.getAllChildren(root, "font");
			for(Element element : elements) {
				String name = XMLUtil.stringAttribute(element, "name", null);
				String fName = XMLUtil.stringAttribute(element, "fontName", null);
				String encoding = XMLUtil.stringAttribute(element, "encoding", null);
				String size = XMLUtil.stringAttribute(element, "size", null);
				String style = XMLUtil.stringAttribute(element, "style", null);
				String color = XMLUtil.stringAttribute(element, "color", null);
				if(name != null && fName != null && encoding != null) {
					Font font = this.createFont(fName, encoding, size, style, color);
					fonts.put(name, font);
					if(name.equals("default")) {
						defaultFont = font;
					}
				}
			}
		}
		
		Font f = fonts.get(fontName);
		if(f == null) {
			f = defaultFont;
		}
		return f;
	}

}
