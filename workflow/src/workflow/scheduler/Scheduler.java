package workflow.scheduler;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import workflow.app.AppConfigManager;
import core.util.Timer;
import core.util.TimerListener;
import ariba.ui.aribaweb.util.AWChangeNotifier;
import ariba.ui.aribaweb.util.CSVReader;
import ariba.util.core.ClassUtil;
import ariba.util.core.ListUtil;
import ariba.util.io.CSVConsumer;

public class Scheduler extends ThreadedServer implements TaskCompleteListener, TimerListener {

	private List<ScheduledTask> tasks = ListUtil.list();
	private List<ScheduleDescription> scheduleDescriptions = new ArrayList<ScheduleDescription>();
	
	private static final String SCHEDULE_DESC_FILE_PATH = AppConfigManager.RESOURCE_PATH + "schedule" + "/" + "schedule.csv";
	
	private Timer timer;
	private String scheduleFilePath;
		
	private List<AWChangeNotifier> notifiers = ListUtil.list();
	
	public Scheduler() {
		this(SCHEDULE_DESC_FILE_PATH);
	}
	
	public Scheduler(String scheduleFilePath) {
		super("Scheduler", 1);
		timer = new Timer(this, 5000);
		this.scheduleFilePath = scheduleFilePath;
	}
	
	@Override
	public void run() {
		while(this.isRunning()) {
			Schedule task = null;
			
			long wait = 300L;
			
			synchronized(queue) {
				if(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException ignore) {}
				}
				
				if(queue.size() > 0) {
					task = queue.getFirstAvailableItem();
					if(task != null) {
						long expireTime = System.currentTimeMillis() + 200L;
						if(task.getFireTime().getTime() > expireTime) {
							task = null;
						}
					}
				}
				
			}
			
			if(task != null) {
				this.processSchedule(task);
			}
			
			try {
				Thread.sleep(wait);
			} catch (InterruptedException ignore) {}
		}
	}

	
	@Override
	public void start() {
		try {
			this.loadSchedules();
			super.start();
			if(timer == null) {
				timer = new Timer(this, 5000);
			}
			timer.start();
		} catch (Exception e) {
			this.log("ERROR", "Unable to load schedules.", e);
		}
	}

	@Override
	public void stop() {
		if(timer.isAlive()) {
			timer.stop();
			timer = null;
		}
		super.stop();
		this.notifyChange();
	}
	
	private void loadSchedules() throws Exception {
		this.queue.clear();
		this.tasks.clear();
		this.reloadSchedules();
		this.notifyChange();
	}
	
	private void reloadSchedules() throws Exception {
		ScheduleDescriptionLoader loader = new ScheduleDescriptionLoader();
		loader.loadScheduleDescriptions();
		ScheduleGenerator generator = new ScheduleGenerator();
		synchronized(queue) {
			List<Schedule> toBeRemoved = ListUtil.list();
			for(Schedule schedule : queue) {
				if(schedule.isFinished()) {
					toBeRemoved.add(schedule);
				}
			}
			queue.removeAll(toBeRemoved);
			generator.generate();
			queue.notifyAll();
		}
	}
	
	public void refresh() throws Exception {
		boolean running = this.isRunning();
		if(running)
			this.stop();
		
		this.reloadSchedules();
		
		if(running)
			this.start();
	}
	
	public void add(Schedule schedule) {
		queue.add(schedule);
	}

	public void remove(Schedule schedule) {
		synchronized(queue) {
			queue.remove(schedule);
		}
	}


	private void processSchedule(Schedule schedule) {
		ScheduledTask task = null;
		Class<?> taskClass = null;
		
		log("INFO", "processing schedule " + schedule, null);
		
		taskClass = ClassUtil.classForName(schedule.getTaskClass());
		if(taskClass == null) {
			log("ERROR", "Class " + schedule.getTaskClass() + " is not found in runtime.", null);
			schedule.setStatus(Schedule.ERROR);
			schedule.setErrorString("Class " + schedule.getTaskClass() + " is not found in runtime.");
			return;
		}
		
		try {
			task = (ScheduledTask) taskClass.newInstance();
		} catch (Exception e) {
			log("ERROR", e.getMessage(), e);
			schedule.setStatus(Schedule.ERROR);
			schedule.setErrorString(e.getLocalizedMessage());
		}
		
		if(task == null)
			return;
		
		task.addTaskCompleteListener(this);
		task.setId(schedule.getId());
		task.setName(schedule.getName());
		task.setSchedule(schedule);
		task.setRunsOnBusinessDay(schedule.runsOnBusinessDay());
		tasks.add(task);
		schedule.setStartTime(new Date());
		schedule.setStatus(Schedule.RUNNING);
		
		log("DEBUG", "Start processing.", null);
		task.start();
		
		this.notifyChange();
	}
	
	@Override
	public void taskCompleted(ScheduledTask task, Throwable error) {
		log("DEBUG", "Stop processing.", null);
		tasks.remove(task);
		
		synchronized(queue) {
			Schedule sched = task.getSchedule();
			if(sched != null) {
				sched.setStopTime(new Date());
				if(error != null) {
					log("ERROR", error.getMessage(), error);
					sched.setStatus(Schedule.ERROR);
					sched.setErrorString(error.getLocalizedMessage());
				} else {
					sched.setStatus(Schedule.FINISHED);
				}
			}
		}
		
		try {
			this.reloadSchedules();
		} catch (Exception e) {
			log("ERROR", "Failed to reload schedule in taskCompleted()", e);
		}

		this.notifyChange();
	}
		
	public void registerChangeListener(AWChangeNotifier notifier) {
		notifiers.add(notifier);
	}
	
	private void notifyChange() {
		if(this.isRunning() == false)
			return;
		for(AWChangeNotifier notifier : notifiers) {
			notifier.notifyChange();
		}
	}
	
	@Override
	public void onTimeout() {
		if(timer != null)
			timer.reset();
		
		if(!this.isRunning())
			return;
		
		try {
			this.reloadSchedules();
		} catch (Exception e) {
			log("ERROR", "Failed to reload schedule in onTimeout()", e);
		}
		
		this.notifyChange();
	}
	
	class ScheduleDescriptionLoader implements CSVConsumer {
		
		public void loadScheduleDescriptions() throws IOException {
			CSVReader reader = new CSVReader(this);
			File file = new File(scheduleFilePath);
			if(!file.exists()) {
				file.createNewFile();
			}
			Scheduler.this.scheduleDescriptions.clear();
			
			FileInputStream is = new FileInputStream(file);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
			try {
				reader.read(buffer, "csv");
			} catch (IOException e) {
				log("ERROR", "Failed to read schedule csv.", e);
				throw e;
			} finally {
				buffer.close();
			}
		}

		@Override
		public void consumeLineOfTokens(String path, int lineNumber, List line) {
			if(line.size() != 11)
				return;
			
			String className = (String) line.get(9);
			Class<?> taskClass = null;
			
			taskClass = ClassUtil.classForName(className);
			
			if(taskClass == null) {
				log("ERROR", "Class " + className + " is not found in runtime.", null);
				return;
			}
			
			ScheduleDescription descr = new ScheduleDescription();
						
			descr.setYear((String) line.get(0));
			descr.setMonth((String) line.get(1));
			descr.setDay((String) line.get(2));
			descr.setHour((String) line.get(3));
			descr.setMinute((String) line.get(4));
			descr.setSecond((String) line.get(5));
			descr.setDayOfWeek((String) line.get(6));
			descr.setId((String) line.get(7));
			descr.setRunsOnBusinessDay((String) line.get(8));
			descr.setTaskClassname(className);
			descr.setName((String) line.get(10));
			
			Scheduler.this.scheduleDescriptions.add(descr);
		}
	}
	
	protected void log(String type, String message, Throwable e) {
		System.out.println(type + ": " +  message);
		if(e != null)
			e.printStackTrace();
	}
	
	class ScheduleGenerator {
		
		public void generate() {
			Date now = new Date();
			for(ScheduleDescription desc : Scheduler.this.scheduleDescriptions) {
				Schedule schedule = ScheduleFactory.getInstance().createScheduleWithDescription(desc);
				if(schedule != null && schedule.getFireTime() != null && schedule.getFireTime().after(now)) {
					Scheduler.this.add(schedule);
				}
			}

		}
	}

}
