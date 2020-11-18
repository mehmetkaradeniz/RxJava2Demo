package com.lbs.demo.rx.tests.basicdomain;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
public class SomeThing {

	private String thing;

	public SomeThing(String thing) {
		this.thing = thing;
	}

	@Override
	public String toString() {
		return Thread.currentThread().getName() + "-SomeThing{" +
				"thing='" + thing + '\'' +
				'}';
	}
}
