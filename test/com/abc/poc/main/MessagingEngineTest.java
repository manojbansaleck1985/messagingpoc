package com.abc.poc.main;

import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;

public class MessagingEngineTest {
	
	/**
	 * This test will check whether sequence of message produced by individual producer 
	 * and sequence of the message received by final joiner queue is same or not.
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception {
		MessagingEngine engine = new MessagingEngine();
		engine.start(16, 2);
		Map<String, Integer> outputValueVsIndexPosition = engine.getOutputValueVsIndexPosition();
		Assert.assertTrue( outputValueVsIndexPosition.get("A1 Processed") < outputValueVsIndexPosition.get("A2 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("A2 Processed") < outputValueVsIndexPosition.get("A3 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("A3 Processed") < outputValueVsIndexPosition.get("A4 Processed"));
		

		Assert.assertTrue( outputValueVsIndexPosition.get("B1 Processed") < outputValueVsIndexPosition.get("B2 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("B2 Processed") < outputValueVsIndexPosition.get("B3 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("B3 Processed") < outputValueVsIndexPosition.get("B4 Processed"));
		

		Assert.assertTrue( outputValueVsIndexPosition.get("C1 Processed") < outputValueVsIndexPosition.get("C2 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("C2 Processed") < outputValueVsIndexPosition.get("C3 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("C3 Processed") < outputValueVsIndexPosition.get("C4 Processed"));
		

		Assert.assertTrue( outputValueVsIndexPosition.get("D1 Processed") < outputValueVsIndexPosition.get("D2 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("D2 Processed") < outputValueVsIndexPosition.get("D3 Processed"));
		Assert.assertTrue( outputValueVsIndexPosition.get("D3 Processed") < outputValueVsIndexPosition.get("D4 Processed"));
	}

	@Test
	public void test2() throws Exception {
		MessagingEngine engine = new MessagingEngine();
		engine.start(1000, 4);
	}
	
	@Test
	public void test3() throws Exception {
		MessagingEngine engine = new MessagingEngine();
		engine.start(10000, 10);
	}
	
	@Test
	public void test4() throws Exception {
		MessagingEngine engine = new MessagingEngine();
		engine.start(40000, 25);
	}
}
