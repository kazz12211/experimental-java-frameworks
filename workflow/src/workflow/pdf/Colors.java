package workflow.pdf;

import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.BaseColor;

public class Colors {
	Map<String, BaseColor> colors;
	
	public Colors() {
		colors = new HashMap<String, BaseColor>();
		colors.put("black", BaseColor.BLACK);
		colors.put("blue", BaseColor.BLUE);
		colors.put("cyan", BaseColor.CYAN);
		colors.put("dark_gray", BaseColor.DARK_GRAY);
		colors.put("gray", BaseColor.GRAY);
		colors.put("green", BaseColor.GREEN);
		colors.put("light_gray", BaseColor.LIGHT_GRAY);
		colors.put("magenta", BaseColor.MAGENTA);
		colors.put("orange", BaseColor.ORANGE);
		colors.put("pink", BaseColor.PINK);
		colors.put("red", BaseColor.RED);
		colors.put("white", BaseColor.WHITE);
	}
	
	public Map<String, BaseColor> getColors() {
		return colors;
	}
	
	public BaseColor getColor(String colorName) {
		String trimmed = colorName.trim();
		BaseColor color = null;
		if(trimmed.startsWith("(") && trimmed.endsWith(")")) {
			String str = trimmed.substring(1, trimmed.length()-1);
			String rgb[] = str.split(",");
			if(rgb != null && rgb.length == 3) {
				int val[] = new int[3];
				val[0] = Integer.parseInt(rgb[0].trim());
				val[1] = Integer.parseInt(rgb[1].trim());
				val[2] = Integer.parseInt(rgb[2].trim());
				color = new BaseColor(val[0], val[1], val[2], val[3]);
			}
		} else {
			color = colors.get(colorName);
		}
		
		if(color == null) {
			return BaseColor.BLACK;
		} else {
			return color;
		}
	}
}
