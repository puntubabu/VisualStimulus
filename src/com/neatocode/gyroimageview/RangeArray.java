package com.neatocode.gyroimageview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.util.Log;

/**
 *   __________________
 *  |  0 | 1 |  2 |  3 |
 *  |____|___|____|____|    ___ORIGIN
 * 	|  4 | 5 |  6 |  7 |  /
 * 	|____|___|____|____| / 
 * 8|  8 | 9 | 10 | 11 |------------
 * 0|____|___|____|____|glass view  |360px
 * 0| 12 |13 | 14 | 15 |____________|
 * p|____|___|____|____|	 640px
 * x| 16 | 17| 18 | 19 |
 * 	|____|___|____|____|
 *  | 20 |21 | 22 | 23 |
 *  |____|___|____|____|
 * 		    800px
 * Assume each sector is a 200px X 200px square
 * 
 * Assume 30px margin between each image
 * 
 */
public class RangeArray {
	private static final int NUMBER_OF_SECTORS = 24;
	private int count=0;
	private Range[] ranges = new Range[NUMBER_OF_SECTORS];
	private Integer[] imageIndicies;
	private List<Integer> rangeOrder;
	
	public RangeArray(){
		setList();
		setRanges();
		setOrder();
	}
	
	/**
	 * Get the range at the certain index.
	 * 
	 * @param i The index of the range array to retreive.
	 * @return The retrieved Range.
	 */
	public Range getRangeAtIndex(int i) {
		return ranges[i];
	}
	
	// set the x and y coordiantes of each range.
	private void setRanges() {
		int i = 0;
		//Add ranges to array
		//0
		addRange(-700, -400, i);
		i++;
		
		//1
		addRange(-500, -400, i);
		i++;
		
		//2
		addRange(-300, -400, i);
		i++;
		
		//3
		addRange(-100, -400, i);
		i++;
		
		//4
		addRange(-700, -200, i);
		i++;
		
		//5
		addRange(-500, -200, i);
		i++;
		
		//6
		addRange(-300, -200, i);
		i++;
		
		//7
		addRange(-100, -200, i);
		i++;
		
		//8
		addRange(-700, 0, i);
		i++;
		
		//9
		addRange(-500, 0, i);
		i++;
		
		//10
		addRange(-300, 0, i);
		i++;
		
		//11
		addRange(-100, 0, i);
		i++;
		
		//12
		addRange(-700, 360, i);
		i++;
		
		//13
		addRange(-500, 360, i);
		i++;
		
		//14
		addRange(-300, 360, i);
		i++;
		
		//15
		addRange(-100, 360, i);
		i++;
		
		//16
		addRange(-700, 560, i);
		i++;
		
		//17
		addRange(-500, 560, i);
		i++;
		
		//18
		addRange(-300, 560, i);
		i++;
		
		//19
		addRange(-100, 560, i);
		i++;
		
		//20
		addRange(-700, 760, i);
		i++;
		
		//21
		addRange(-500, 760, i);
		i++;
		
		//22
		addRange(-300, 760, i);
		i++;
		
		//23
		addRange(-100, 760, i);
	}
		
	

	private void setList() {
		imageIndicies = new Integer[NUMBER_OF_SECTORS];
		for (int i=0; i<NUMBER_OF_SECTORS; i++){
			imageIndicies[i] = i;
		}
	}
	
	/**
	 * Randomize the order of the shapes
	 */
	private void setOrder(){
		List<Integer> template = Arrays.asList(imageIndicies);
		Set<List<Integer>> seen = new HashSet<List<Integer>>();
		for (int i =0; i<NUMBER_OF_SECTORS; i++){
			rangeOrder = new ArrayList<Integer>(template);
			do{
				Collections.shuffle(rangeOrder);
			} while(!seen.add(rangeOrder));
		}
	}
	
	public int[] getRange(){
		Integer next;
		int[] range = new int[2];
		
				if(rangeOrder.size() > 0){
					next = rangeOrder.get(0);
					for (Range r : ranges){
						if(r.getSector() == next){
							range[0] = r.getXCoordinate();
							range[1] = r.getYCoordinate();
							rangeOrder.remove(0);
						}
					}
		}
				else{
					setOrder();
				}
			return range;
	}
	
	/**
	 * Resets each range in the range array to unused.
	 */
	public void clearUsedRanges(){
		for (Range r : ranges){
			r.setAsUnused();
		}
	}
	
	/**
	 * Add a range to the range array.
	 *
	 * @param x The x coordinate of the range.
	 * @param y The y coordinate of the range.
	 * @param s The sector number of the range.
	 */
	private void addRange(int x, int y, int s){
		Range range = new Range();
		range.setCoordinates(x, y, s);
		ranges[count%NUMBER_OF_SECTORS]=range;
		count++;
	}
	

}
