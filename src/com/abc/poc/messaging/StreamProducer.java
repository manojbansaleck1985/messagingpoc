package com.abc.poc.messaging;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.abc.poc.messaging.vo.IGlobalTask;

public class StreamProducer extends Thread {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamProducer.class);

	private BlockingQueue<IGlobalTask> queue;
	
	List<IGlobalTask> taskList;
	
	public StreamProducer(BlockingQueue<IGlobalTask> queue, List<IGlobalTask> taskList) {
		super();
		this.queue = queue;
		this.taskList = taskList;
	}
	
	@Override
	public void run() {	
		/**
		 * Put all the task object in common queue.
		 */
		taskList.stream()
				.filter(task -> task != null)
				.filter(task -> !StringUtils.isEmpty(task.getValue()))
				.forEach(task -> {
					try {
						queue.put(task);
						if(LOGGER.isDebugEnabled())
							LOGGER.debug("Sent task object blocking queue successfully.");
					} catch (InterruptedException e) {
						e.printStackTrace();
						LOGGER.error("Unable to put message to the queue produced by producer.");
//						No need to throw exception as we want to process other messages.
					}
				});
	}
}
