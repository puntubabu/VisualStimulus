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
 * Module 2. Instead of 30 float square, one picture, half the width and length of the viewport, continuously
 * floats from right to left.

 */
public class MovingImage extends PApplet {

	// DECLARE
	Ball[] ballCollection = new Ball[1];
	sqlAdapter sqlHelper; // initialize sqlAdapter
	ArrayList<Integer> randomSpeedArray = new ArrayList<>(); // initialize
																// ArrayList
	PImage img;

	// TIME GLOBAL VARIABLES
	int treatmentTime = 600000; // 10 minutes in millis
	int pausedTime;
	int startTime;
	int passedTime;
	int elapsedTime;

	int lastTime; // last time that change speed
	int startTimeRandom; // start time for random
	int endTime = 15000; // 30 sec
	int x = 5; // intial speed

	// PREFERENCES ITEMS VARIBLES
	String speed;
	String bg;
	String name;
	String pic;

	private SharedPreferences sharedPreferences;

	public void setup() {
		noLoop();
		frameRate(60);
		smooth();

		startTime = millis(); // start time
		startTimeRandom = millis(); // start time for random

		// Getting shared preferences
		sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
				Context.MODE_PRIVATE);
		speed = sharedPreferences.getString(MainActivity.speed, "NA");
		bg = sharedPreferences.getString(MainActivity.bg, "NA");
		name = sharedPreferences.getString(MainActivity.name, "NA");
		pic = sharedPreferences.getString(MainActivity.pic, "NA");

		// update API
		// since the gdk currently has a bug that doesn't allow pictures to be taken from the glass,
		// use a default picture
		img = loadImage("bridge.jpg");

		for (int i = 0; i < ballCollection.length; i++) {
			ballCollection[i] = new Ball(width, 90);
		}
	}

	public void draw() {
		passedTime = millis() - startTime;
		if (passedTime < treatmentTime) { // if more then treatementTime then
											// exit
			switch (bg) {
			case "Grey":
				background(102, 102, 102);
				break;
			default:
				background(0); // Black
				break;
			}
			// CALL FUNCTIONALITY
			for (int i = 0; i < ballCollection.length; i++) {
				ballCollection[i].run(x);
				randomize();
			}
		} else
			exit();
	}

	public void randomize() {

		lastTime = millis() - startTimeRandom;
		if (lastTime >= endTime) {
			x = (int) random(5, 21); // random from 5 to 21 not including 21
			randomSpeedArray.add(x); // save random speed to array list
			 sqlHelper.updateM2(name, randomSpeedArray.toString());
			startTimeRandom = millis();
			// redraw();
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

	public void mouseClicked() {
		if (looping) {
			pausedTime = millis();
			noLoop();
			// log all speed in randomSpeed
			println(randomSpeedArray.size());
			for (int i = 0; i < randomSpeedArray.size(); i++) {
				Log.d("speed: ", randomSpeedArray.get(i).toString());
			}
			Log.d("name: ", name);

		} else {
			elapsedTime = millis() - pausedTime;
			treatmentTime += elapsedTime;
			loop();
		}

	}

	class Ball {
		float x = 0;
		float y = 0;
		int speedX = 0;

		Ball(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void run(int randomSpeed) {
			display();
			move(randomSpeed);
		}

		public void move(int randomSpeed) {
			switch (speed) {
			case "Slow":
				speedX = 5;
				break;
			case "Medium":
				speedX = 10;
				break;
			case "Fast":
				speedX = 15;
				break;
			case "Extra Fast":
				speedX = 20;
				break;
			default:
				speedX = randomSpeed;
				break;
			}

			//sqlHelper.updateM2(name, Integer.toString(speedX));

			x -= speedX;

			// if the picture is totally off the screen, position the picture back on the right side.
			if (x < -width / 2) {
				x = width;
			}
		}

		public void display() {
			image(img, x, y, width / 2, height / 2);
		}
	}

	public int sketchWidth() {
		return 640;
	}

	public int sketchHeight() {
		return 360;
	}
}