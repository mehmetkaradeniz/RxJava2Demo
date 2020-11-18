package com.lbs.demo.rx.basic.domain;

import java.io.Serializable;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
public class SomeThing implements Serializable {

	private String thing;

	public SomeThing() {
	}

	public SomeThing(String thing) {
		this.thing = thing;
	}

	public String getThing() {
		return thing;
	}

	public void setThing(String thing) {
		this.thing = thing;
	}

	@Override
	public String toString() {
		return Thread.currentThread().getName() + "-SomeThing{" +
				"thing='" + thing + '\'' +
				'}';
	}
}
