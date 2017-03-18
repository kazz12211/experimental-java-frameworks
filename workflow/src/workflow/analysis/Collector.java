package workflow.analysis;

import java.util.Date;

import core.util.DateUtils;

public abstract class Collector {
	
	protected Collector() {
	}
	
	protected abstract void collect(Date start, Date end);
	
	public void collect(Date start, int days) {
		this.collect(start, DateUtils.dateByAddingDays(start, days));
	}
	
	public void collect(Date start) {
		Date s = start;
		Date now = new Date();
		while(true) {
			this.collect(s, 7);
			s = DateUtils.dateByAddingDays(s, 7);
			if(s.after(now))
				break;
		}
	}
	
}
