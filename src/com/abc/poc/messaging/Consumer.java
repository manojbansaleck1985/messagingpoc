package com.abc.poc.messaging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abc.poc.messaging.vo.IGlobalTask;

public class Consumer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
	
	/**
	 * queue will hold either task objects produced by A and B or task objects produced by C and D.
	 */
	private BlockingQueue<IGlobalTask> queue;
	
	/**
	 * finalResultSetQueue will hold all the future object after processing all the task objects produced by all producers 
	 */
	private BlockingQueue<Future<IGlobalTask>> finalResultSetQueue;
	
	/**
	 * totalTaskByAllProducers : number of all tasks produced by all producers 
	 */
	private int totalTaskByAllProducers;
	
	private ExecutorService executorService;

	public Consumer(BlockingQueue<IGlobalTask> queue, ExecutorService executorService, BlockingQueue<Future<IGlobalTask>> finalResultSetQueue, int totalTaskByAllProducers) {
		this.queue = queue;
		this.executorService = executorService;
		this.finalResultSetQueue = finalResultSetQueue;
		this.totalTaskByAllProducers = totalTaskByAllProducers;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < totalTaskByAllProducers/2; i++) {
			try {
				IGlobalTask task = queue.poll(1, TimeUnit.MINUTES);
				/**
				 * If we want to return different object that can be done through @Callable interface
				 * Here we are returning the same object so we can use @Runnable interface and can set the processed value to the same object.
				 */
				finalResultSetQueue.put(executorService.submit(new Callable<IGlobalTask>() {
					@Override
					public IGlobalTask call() throws Exception {
						LOGGER.info("Processing task received by the consumer AB or consumer CD.");
						task.setProcessedValue(String.join(" ", task.getValue(), "Processed"));
						return task;
					}
				}));
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOGGER.error("Unable to put message to the consumers queue.");
//				No need to throw exception as we want to process other messages.
			}
		}

	}

}
