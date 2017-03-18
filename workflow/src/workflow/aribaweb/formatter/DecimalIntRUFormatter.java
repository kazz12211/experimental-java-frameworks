package workflow.aribaweb.formatter;

import java.math.RoundingMode;

public class DecimalIntRUFormatter extends DecimalIntFormatter {

	public DecimalIntRUFormatter() {
		super();
		fmt.setRoundingMode(RoundingMode.UP);
	}

}
