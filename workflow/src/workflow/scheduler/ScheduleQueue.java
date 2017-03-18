package workflow.scheduler;


import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class ScheduleQueue extends LinkedList<Schedule> {

	private static final long serialVersionUID = 1L;
	private static final ScheduleComparator SCHEDULE_COMPARATOR = new ScheduleComparator();

	public Schedule getFirstAvailableItem() {
		Iterator<Schedule> iter = this.iterator();
		while(iter.hasNext()) {
			Schedule schedule = iter.next();
			if(schedule.isInited())
				return schedule;
		}
		return null;
	}

	
	@Override
	public boolean add(Schedule e) {
		int index = -1;
		int i = 0;
		for(Schedule sched : this) {
			if(sched.isEqualToSchedule(e)) {
				index = i;
				break;
			}
			i++;
		}
		boolean flag = false;
		
		if(index == -1) {
			flag = super.add(e);
		} else {
			flag = true;
			Schedule sched = this.get(index);
			if(sched.isInited()) { 
				sched.setFireTime(e.getFireTime());
				sched.setName(e.getName());
				sched.setTaskClass(e.getTaskClass());
			}
		}
		Collections.sort(this, SCHEDULE_COMPARATOR);
		
		return flag;
	}


	static class ScheduleComparator implements Comparator<Object> {

		@Override
		public int compare(Object arg0, Object arg1) {
			Schedule s1 = (Schedule) arg0;
			Schedule s2 = (Schedule) arg1;
			if(s1.getFireTime() == null || s2.getFireTime() == null)
				return 0;
			if(s1.getFireTime().before(s2.getFireTime()))
				return -1;
			else if(s1.getFireTime().after(s2.getFireTime()))
				return 1;
			return 0;
		}
		
	}

}
