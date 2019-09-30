package com.abc.poc.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abc.poc.messaging.Consumer;
import com.abc.poc.messaging.Dispatcher;
import com.abc.poc.messaging.StreamProducer;
import com.abc.poc.messaging.vo.IGlobalTask;
import com.abc.poc.messaging.vo.TaskAB;
import com.abc.poc.messaging.vo.TaskCD;

public class MessagingEngine {
	
	private static final Logger LOGGER  = LoggerFactory.getLogger(MessagingEngine.class);
	
	/**
	 * THis map is used for unit testing.
	 * Here in this map, system will store processed value and the index position in final queue (finalResultSetQueue below) 
	 */
	private Map<String, Integer> outputValueVsIndexPosition = new LinkedHashMap<String, Integer>();

	public void start(int totalTaskByAllProducers, int totalThreadsForProcessing) throws Exception {
		/**
		 * This is the common queue in which all producers will put their task randomly.
		 */
		BlockingQueue<IGlobalTask> commonQueue = new LinkedBlockingQueue<IGlobalTask>(totalTaskByAllProducers);
		
		/**
		 * This abQueue is the queue in which dispatcher will put all tasks from producer A and producer B.
		 */
		BlockingQueue<IGlobalTask> abQueue = new LinkedBlockingQueue<IGlobalTask>(totalTaskByAllProducers/2);
		
		/**
		 * This cdQueue is the queue in which dispatcher will put all tasks from producer C and producer D.
		 */
		BlockingQueue<IGlobalTask> cdQueue = new LinkedBlockingQueue<IGlobalTask>(totalTaskByAllProducers/2);
		
		/**
		 * This finalResultSetQueue is the queue in which consumerAB and consumerCD will put task object after processing concurrently.
		 * Used LinkedBlockingQueue to maintain the insertion order.
		 */
		BlockingQueue<Future<IGlobalTask>> finalResultSetQueue = new LinkedBlockingQueue<Future<IGlobalTask>>(totalTaskByAllProducers);

		/**
		 * consumerAB will submit its task to the abConsumerWorkerService for processing.
		 */
		ExecutorService abConsumerWorkerService = Executors.newFixedThreadPool(totalThreadsForProcessing);
		
		/**
		 * consumerCD will submit its task to the cdConsumerWorkerService for processing.
		 */
		ExecutorService cdConsumerWorkerService = Executors.newFixedThreadPool(totalThreadsForProcessing);

		ExecutorService executorService = Executors.newFixedThreadPool(2);
		try {
			
			/**
			 * Start consumers first for the processing. 
			 * System should not wait for consumers to start for processing any task object. Task should process immediately.
			 */
			
			startConsumerAndProcessTask(abQueue, abConsumerWorkerService, finalResultSetQueue, totalTaskByAllProducers, executorService);
			startConsumerAndProcessTask(cdQueue, cdConsumerWorkerService, finalResultSetQueue, totalTaskByAllProducers, executorService);

			/**
			 * Dispatchers should also be ready before any task produced by any producer
			 */
			Thread dispatcherThread = new Thread(new Dispatcher(commonQueue, totalTaskByAllProducers, abQueue, cdQueue));
			dispatcherThread.start();

			/**
			 * now start producers to produce tasks for processing.
			 */
			startProducers(commonQueue, totalTaskByAllProducers);

			/**
			 * finally retrieve the output from finalResultSetQueue.
			 */
			for (int i = 0; i < totalTaskByAllProducers; i++) {
				try {
					String processedValue = finalResultSetQueue.poll(1, TimeUnit.MINUTES).get().getProcessedValue();
					LOGGER.info("{}  output ==> {} ",Thread.currentThread().getName() ,processedValue);
					outputValueVsIndexPosition.put(processedValue, i);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getCause());
		} finally {
			executorService.shutdown();
			abConsumerWorkerService.shutdown();
			cdConsumerWorkerService.shutdown();
		}

	}

	private void startConsumerAndProcessTask(BlockingQueue<IGlobalTask> queue,
			ExecutorService consumerWorkerService, BlockingQueue<Future<IGlobalTask>> finalResultSetQueue,
			int totalTaskByAllProducers, ExecutorService executorService) {
		executorService.submit(new Consumer(queue, consumerWorkerService, finalResultSetQueue, totalTaskByAllProducers));
	}

	/**
	 * @param commonQueue
	 * @param totalTaskByAllProducers
	 * 
	 * Here is an assumption that each producer will produce same number of task objects.
	 */
	private void startProducers(BlockingQueue<IGlobalTask> commonQueue, int totalTaskByAllProducers) {
		/**
		 * create producers
		 */
		StreamProducer producerA = new StreamProducer(commonQueue, createTask("A", totalTaskByAllProducers/4, true));
		StreamProducer producerB = new StreamProducer(commonQueue, createTask("B", totalTaskByAllProducers/4, true));
		StreamProducer producerC = new StreamProducer(commonQueue, createTask("C", totalTaskByAllProducers/4, false));
		StreamProducer producerD = new StreamProducer(commonQueue, createTask("D", totalTaskByAllProducers/4, false));
		/**
		 * Finally start all producers 
		 */
		producerA.start();
		producerB.start();
		producerC.start();
		producerD.start();
	}

	/**
	 * @param id
	 * @param totalTask
	 * @param flagForAB
	 * @return List<IGlobalTask> 
	 * 
	 * This  method will create a task object of type TaskAB or TaskCD.
	 */
	private List<IGlobalTask> createTask(String id, int totalTask, boolean flagForAB) {
		List<IGlobalTask> list = new ArrayList<IGlobalTask>();
		for (int i = 0; i < totalTask; i++) {
			if(flagForAB)
				list.add(new TaskAB(id+(i+1)));
			else
				list.add(new TaskCD (id+(i+1)));
		}
		return list;
	}

	public Map<String, Integer> getOutputValueVsIndexPosition() {
		return outputValueVsIndexPosition;
	}
}
