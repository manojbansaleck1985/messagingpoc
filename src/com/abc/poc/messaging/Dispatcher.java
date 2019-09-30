package com.abc.poc.messaging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abc.poc.messaging.vo.IABTask;
import com.abc.poc.messaging.vo.ICDTask;
import com.abc.poc.messaging.vo.IGlobalTask;

public class Dispatcher implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);
	
	private BlockingQueue<IGlobalTask> tasks;
	
	private BlockingQueue<IGlobalTask> abTasks;
	
	private BlockingQueue<IGlobalTask> cdTasks;
	
	private int totalTasks;
	
	public Dispatcher(BlockingQueue<IGlobalTask> tasks, int totalTasks, BlockingQueue<IGlobalTask> abTasks, BlockingQueue<IGlobalTask> cdTasks) {
		this.tasks = tasks;
		this.totalTasks = totalTasks;
		this.abTasks = abTasks;
		this.cdTasks = cdTasks;
	}

	@Override
	public void run() {
		for (int i = 0; i < totalTasks; i++) {
			try {
				IGlobalTask task = tasks.poll(1, TimeUnit.MINUTES);
				if(task instanceof IABTask)
					abTasks.put(task);
				else if(task instanceof ICDTask)
					cdTasks.put(task);
				else
					LOGGER.error("Object not supported by dispatcher");
			} catch (InterruptedException e) {
				LOGGER.error("Unable to put message to the consumers queue.");
//				No need to throw exception as we want to process other messages.
			}
		}
	}

}
