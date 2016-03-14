package com.glassrehab.mainM1M2;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

/**
 * Module 1. This module is attempting to to replicate the Kerkhoff study regarding optokinetic visual
 * stimulus. It displays 30 squares, of size 30x30 pixels, distributed randomly yet evenly throughout Glass'
 * viewport. Each square moves uniformly from right-to-left in the viewport. Once a square has reach the left
 * edge of the viewport, it wraps back around the right side to continue it's right-to-left path.
 *
 * Every thirty seconds, all of the squares randomly change to a speed form 5 to 20 pixels/second. The session
 * continues for 10 minutes before stopping.
 */
public class MovingBall extends PApplet {

	Ball[] ballCollection;// = new Ball[30];
	sqlAdapter sqlHelper;
	ArrayList<Integer> randomSpeedArray = new ArrayList<>();


	// TIME GLOBAL VARIABLES
	int treatmentTime = 600000; // 10 minutes in milliseconds

	/**
	 * Holds the time of when module 1 was paused.
	 */
	int pausedTime;
	int startTime;
	int passedTime;
	int elapsedTime;


	int lastTime;

	/**
	 * Records the last time the square speed was randomized. In conjunction with endTime, used to determine
	 * the next time square speed will be randomized.
	 */
	int startTimeRandom;
	
	/**
	 * The time interval that determines when the moving squares will change their speed.
	 */
	int endTime = 30000; // 30 seconds
	int x = 5; // initial speed

	// PREFERENCES ITEMS VARIABLES
	String shape;
	String sizeB;
	String speed;
	String color;
	String bg;
	String numberOfSquare;
	String name;
	private SharedPreferences sharedPreferences;


	/**
	 * Prepare the canvas before the first initial draw cycle.
	 */
	public void setup() {
		noLoop();
		frameRate(60);
		smooth();

		// initialize the start time of Module 1 to the current time
		startTime = millis();
		
		// initialize the interval that determines when to randomize square speed
		startTimeRandom = millis();

		/*
		 * Get share preferences
		 */
		sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
				Context.MODE_PRIVATE);
		color = sharedPreferences.getString(MainActivity.color, "NA");
		speed = sharedPreferences.getString(MainActivity.speed, "NA");
		shape = sharedPreferences.getString(MainActivity.shape, "NA");
		sizeB = sharedPreferences.getString(MainActivity.size, "NA");
		bg = sharedPreferences.getString(MainActivity.bg, "NA");
		numberOfSquare = sharedPreferences.getString(MainActivity.num, "NA");
		name = sharedPreferences.getString(MainActivity.name, "NA");

		switch (numberOfSquare) {
		case "Medium":
			ballCollection = new Ball[30];
			break;
		case "Max":
			ballCollection = new Ball[50];
			break;
		default:
			ballCollection = new Ball[20]; // default
			break;
		}

		// initialize each ball in the ball array with a random x-y positon
		for (int i = 0; i < ballCollection.length; i++) {
			ballCollection[i] = new Ball(random(0, width), random(20,
					height - 20));
		}
	}

	/**
	 * The draw cycle happens continuously. When you see the squares moving from right-to-left, it
	 * is because this class is drawing the squares on each cycle.
	 */
	public void draw() {
		passedTime = millis() - startTime;
		
		// the session goes until the current time is greater than the prescribed
		// treatment time.
		if (passedTime < treatmentTime) {
			switch (this.bg) {
			case "Grey":
				background(102, 102, 102);
				break;

			default:
				// black background
				background(0);
				break;
			}
			
			// the square's color
			switch (this.color) {
			case "Red":
				fill(187, 0, 0);
				break;
			case "Yellow":
				fill(255, 255, 0);
				break;
			default:
				// white
				fill(255, 255, 255);
				break;
			}

			// draw each ball in the ball array
			for (int i = 0; i < ballCollection.length; i++) {
				ballCollection[i].run(x);
				this.randomize();
			}
		} else {
			exit();
		}
	}

	/**
	 * Randomizes the squares' speeds every on an interval determined by endTime.
	 */
	public void randomize() {

		lastTime = millis() - startTimeRandom;
		if (lastTime >= endTime) {
			x = (int) random(5, 21); // random number from 5 inclusive to 21 exclusive
			randomSpeedArray.add(x); // save random speed to array list
			sqlHelper.updateM1(name, Integer.toString(x));
			startTimeRandom = millis(); // save the last time the square speed was randomized
		}

	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		sqlHelper = new sqlAdapter(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			mouseClicked();
			return true; // return true if you handled this event
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * When the user taps the Glass' dpad, the module will either pause/resume the module 1 session.
	 */
	public void mouseClicked() {

		// if module 1 is currently playing, stop the draw cycle
		if (looping) {
			pausedTime = millis();
			noLoop();
			// log all speed in randomSpeed
			for (int i = 0; i < randomSpeedArray.size(); i++) {
				Log.d("speed: ", randomSpeedArray.get(i).toString());
			}
			Log.d("name: ", name);
		}
		// if module 1 is paused, restart the draw cycle
		else {
			elapsedTime = millis() - pausedTime;
			treatmentTime += elapsedTime;
			loop();
		}

	}

	/**
	 * Each square in the glass viewport is an instance of the Ball class. It keeps track of a square's
	 * x and y coordinate, as well as it's x-speed.
	 *
	 */
	class Ball {
		float x = 0;
		float y = 0;
		int speedX = 0;

		Ball(float x, float y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * Draws and moves the ball.
		 * 
		 * @param randomSpeed Random speed for the ball.
		 */
		public void run(int randomSpeed) {
			display();
			move(randomSpeed);
		}

		public void move(int randomSpeed) {
			switch (speed) {
			case "Slow":
				this.speedX = 5;
				break;
			case "Medium":
				this.speedX = 10;
				break;
			case "Fast":
				this.speedX = 15;
				break;
			case "Extra Fast":
				this.speedX = 20;
				break;
			default:
				this.speedX = randomSpeed;
				//sqlHelper.updateM1(name, randomSpeedArray.toString());
				break;
			}

			//sqlHelper.updateM1(name, Integer.toString(speedX));

			// displace the x position of the square by however fast the square is supposed to be 
			x -= speedX;

			// once the square has move totally off the left screen of the app and is no longer drawn,
			// reset it back on the right side with a random height
			if (x < -35) {
				x = width;
				y = random(20, height - 20);
			}
		}

		/**
		 * Display the square.
		 */
		public void display() {

			int sizeOfBall = 0;

			switch (sizeB) {

			case "Small":
				sizeOfBall = 25;
				break;
			case "Medium":
				sizeOfBall = 30;
				break;
			default:
				sizeOfBall = 35;
				break;
			}

			switch (shape) {

			case "Rectangle":
				rect(x, y, sizeOfBall, sizeOfBall, 7);
				break;
			default:
				ellipse(x, y, sizeOfBall, sizeOfBall);
				break;
			}
		}

	}

	public int sketchWidth() {
		return 640;
	}

	public int sketchHeight() {
		return 360;
	}
}