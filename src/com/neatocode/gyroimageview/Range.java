package com.neatocode.gyroimageview;

/**
 * This class is used by RangeArray to determine a specific's range's sector within the array,
 * as well as the x and y coordinate of that sector. The used boolean is to tell RangeArray if the
 * Range has already been used by a currently drawn shape.
 *
 */
public class Range {
	private int sector;
	private int xCoord;
	private int yCoord;
	private boolean used;

	public Range(){
		
	}
	
	/**
	 * Set the coordinates and the sector for the range.
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @param s The sector number.
	 */
	public void setCoordinates(int x, int y, int s){
		this.used = false;
		this.xCoord = x;
		this.yCoord = y;
		this.sector = s;
	}
	

	public int getXCoordinate() {
		return this.xCoord;
	}
	
	public int getYCoordinate() {
		return this.yCoord;
	}
	
	public boolean isUsed() {
		return this.used;
	}
	
	/**
	 * Set the range's used boolean to true.
	 */
	public void setAsUsed() {
		this.used = true;
	}
	
	/**
	 * Set the range's used boolean to false.
	 */
	public void setAsUnused() {
		this.used = false;
	}
	
	public int getSector() {
		return this.sector;
	}
}