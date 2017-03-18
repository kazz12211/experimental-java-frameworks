package workflow.aribaweb.formatter;

import java.math.RoundingMode;

public class DecimalDoubleRUFormatter extends DecimalDoubleFormatter {

	public DecimalDoubleRUFormatter() {
		super();
		fmt.setRoundingMode(RoundingMode.UP);
	}
}
