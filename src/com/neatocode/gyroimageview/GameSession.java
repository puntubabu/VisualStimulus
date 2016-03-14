package com.neatocode.gyroimageview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import android.graphics.RectF;
import com.polites.android.GestureImageView;

/**
 * Keeps track of the game session, including the number of images hit, the number of game sessions played,
 * as well as list of images.
 */
public class GameSession {
	private int numOfImagesHit, numGameSession;
	private Integer[] imageIndicies;
	private List<Integer> imageOrder;
	private static int NUM_OF_IMAGES = 3;
	private Stack<GestureImageView> imagesStack;
	private GestureImageView[] imagesArray;

	public GameSession(){
		numGameSession++;
		setList();
		setOrder();
	}
	
	
	public GestureImageView getNextImage(){
		return imagesStack.peek();
	}
	
	public int getNumOfImages(){
		return NUM_OF_IMAGES;
	}
	
	public void setImagesArray(GestureImageView[] images){
		this.imagesArray = images;
		createImagesStack();
	}
	
	private void setList(){
		imageIndicies = new Integer[NUM_OF_IMAGES];
		
		for ( int i=0; i<NUM_OF_IMAGES; i++ ){
			imageIndicies[i] = i;
		}
	}
	
	public RectF getNextImageRectF(){
		return imagesStack.peek().getImageRectF();
	}
	
	private void createImagesStack(){
		Integer randIndex;
		imagesStack = new Stack<GestureImageView>();
		
		for (int i=0; i<NUM_OF_IMAGES; i++){
			randIndex = getNextImageIndex();
			imagesStack.push(imagesArray[randIndex]);
		}
		
	}
	
	public Stack<GestureImageView> getImagesStack(){
		return this.imagesStack;
	}
	
	public float getNextImageX(){
		return imagesStack.peek().getImageX();
	}
	
	public float getNextImageY(){
		return imagesStack.peek().getImageY();
	}
	
	public String getNextImageName(){
		return imagesStack.peek().getImageName();
	}
	
	public void popImage(){
		imagesStack.pop();
	}
	
	private void setOrder(){
		
		// temporary variable
		List<Integer> template = Arrays.asList(imageIndicies);
		
		// a set of a list of a single integer: the index of the next shape to be drawn
		Set<List<Integer>> seen = new HashSet<List<Integer>>();
		
		for (int i : imageIndicies ){
			imageOrder = new ArrayList<Integer>(template);

			while(!seen.add(imageOrder)) {
				Collections.shuffle(imageOrder);
			}
		}
	}
	
	public void resetGame(){
		//reset order images
		setOrder();
		numGameSession++;
	}
	
	public int getNumGameSession(){
		return numGameSession;
	}
	
	public Integer getNextImageIndex(){
		Integer next;
		//Get first image number, then remove from List
		if (imageOrder.size() == 0) {
			resetGame();
		}
		next = imageOrder.remove(0);

		return next;
	}
	
	public void incrementNumHit(){
		this.numOfImagesHit++;
	}
	
	public int getNumHit(){
		return this.numOfImagesHit;
	}
}
