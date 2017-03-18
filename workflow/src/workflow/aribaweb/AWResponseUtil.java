package workflow.aribaweb;

import java.util.Calendar;
import java.util.GregorianCalendar;

import core.util.DateUtils;
import ariba.ui.aribaweb.core.AWResponse;

public final class AWResponseUtil {

 	public static void setCacheExpireDate(AWResponse response,
			int seconds) {
		if (response != null) {
			Calendar cal = new GregorianCalendar();
			cal.roll(Calendar.SECOND, seconds);
			response.setHeaderForKey(DateUtils.htmlExpiresDateFormat().format(cal.getTime()), "Expires");
		}
	}

}
