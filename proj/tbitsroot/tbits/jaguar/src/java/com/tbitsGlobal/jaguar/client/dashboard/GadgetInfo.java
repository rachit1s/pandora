package com.tbitsGlobal.jaguar.client.dashboard;

import java.io.Serializable;

/**
 * Class to carry info of a gadget
 * 
 * @author sourabh
 *
 */
public class GadgetInfo implements Serializable{
	private int gadgetId;
	private String caption;
	private String reportFile;
	private int column;
	private int height = 200;
	private int width = 200;
	private boolean isVisible = true;
	private boolean isMinimized = false;
	private int refreshRate = 0;
//	private int left = 0;
//	private int top = 0;
	
	/**
	 * Constructor. Don't remove. Required for {@link Serializable}
	 */
	public GadgetInfo() {
	}
	
	public int getGadgetId() {
		return gadgetId;
	}

	public void setGadgetId(int gadgetId) {
		this.gadgetId = gadgetId;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getReportFile() {
		return reportFile;
	}

	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setIsVisble(boolean isVisble) {
		this.isVisible = isVisble;
	}

	public boolean isMinimized() {
		return isMinimized;
	}

	public void setIsMinimized(boolean isMinimized) {
		this.isMinimized = isMinimized;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

//	public void setLeft(int left) {
//		this.left = left;
//	}
//
//	public int getLeft() {
//		return left;
//	}
//
//	public void setTop(int top) {
//		this.top = top;
//	}
//
//	public int getTop() {
//		return top;
//	}
}
