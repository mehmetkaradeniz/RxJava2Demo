package com.lbs.demo.rx.tests.utils;

public class Utils {

	public static void sleep(long time) {
		try{
			Thread.sleep(time);
		} catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	private static long start = System.currentTimeMillis();

	public static Boolean isSlowTime() {
		boolean b = (System.currentTimeMillis() - start) % 30_000 >= 15_000;
		return b;
	}
}
