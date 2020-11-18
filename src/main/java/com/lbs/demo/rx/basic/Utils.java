package com.lbs.demo.rx.basic;

public class Utils {

	public static void sleep(long time) {
		try{
			Thread.currentThread().sleep(time);
		} catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	private static long start = System.currentTimeMillis();
}
