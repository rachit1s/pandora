package org.primefaces.examples.view;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name="counterBean")
@ViewScoped
public class CounterBean implements Serializable {

	private int count;
	private int clickCount;

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void increment() {
		count++;
	}
	
	public void clickButtonHandler()
	{
		clickCount++;
	}
}
