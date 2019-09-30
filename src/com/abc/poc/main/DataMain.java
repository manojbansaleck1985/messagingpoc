package com.abc.poc.main;

public class DataMain {

	public static void main(String[] args) throws Exception {

		MessagingEngine engine = new MessagingEngine();
		/**
		 * Use all the cores of the CPU effectively.
		 */
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		engine.start(100, availableProcessors);
		
	}


}
