package com.neatocode.gyroimageview;

import java.util.Timer;
import java.util.TimerTask;

import com.glassrehab.mainM1M2.MainActivity;
import org.hitlabnz.helloworld.R;
import com.glassrehab.mainM1M2.sqlAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.polites.android.GestureImageView;
import com.polites.android.MoveAnimation;
import com.polites.android.MoveAnimationListener;

/**
 * View an image, scrolling it with head movements.
 * 
 */
public class ViewActivity extends Activity implements
		FilteredOrientationTracker.Listener {

	private static final int ANIMATION_DURATION_MS = 100;
	private static final float GYRO_TO_X_PIXEL_DELTA_MULTIPLIER = 80;
	private static final float GYRO_TO_Y_PIXEL_DELTA_MULTIPLIER = 80;
	private static int NUM_OF_IMAGES;
	private static final String TAG = "ViewActivity";

	private Timer t;
	private GameSession session;
	private GestureImageView crosshair, arrow;
	private AudioManager audio;

	// Arrays
	private GestureImageView[] images;
	private MoveAnimation[] moveAnimations;
	private RangeArray rangeArray;

	// Single variables
	private MoveAnimation moveAnimation;
	private FilteredOrientationTracker tracker;
	private int xCoor, yCoor;
	private int[] range;
	private Boolean isWaitingForGameReset;
	
	//TTS
	private TextToSpeech tts;
	
	// initialize sqlAdapter
	sqlAdapter sqlHelper;
	private String name;
	private SharedPreferences sharedPreferences;
	
	// Holds nextX and nextY coordinates
	private float[] nextX;
	private float[] nextY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	getWindow().setFormat(PixelFormat.RGB_565);
    	ScreenOn.run(this);
       	setContentView(R.layout.view_activity);
       	
    	//Initialize GameSession, AudioManager, TextToSpeech, Database Helper
    	session = new GameSession();
    	NUM_OF_IMAGES = session.getNumOfImages();
    	audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	
    	//DB Init
		sqlHelper = new sqlAdapter(this);
		sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES,
				Context.MODE_PRIVATE);
		name = sharedPreferences.getString(MainActivity.name, "NA");
    	
    	//Start 5 minute timer
    	initializeTimer();
    	
    	//Start Session
       	startSession();
    }
    
    private void initializeTimer(){
    	t = new Timer();
    	t.schedule(new TimerTask() {

            @Override
            public void run() {
            	//Save information to Database
            	
            	//Exit Activity
            	Toast.makeText(getApplicationContext(), "Session Complete", Toast.LENGTH_LONG).show();
            	ViewActivity.this.finish();
            }
     }, 300000);
    }
    
	private void initializeTextToSpeech(){
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int ttsStatus) {
				if(ttsStatus == TextToSpeech.ERROR){
					Toast.makeText(getApplicationContext(), "TTS not working.", Toast.LENGTH_SHORT).show();
				}
				else{
					setNextImageMessage();
				}
			}
		});
		tts.setSpeechRate(0.125f);
	}
    
    private void startSession(){
    	
       	//Initialize arrays
       	moveAnimations = new MoveAnimation[NUM_OF_IMAGES];
       	rangeArray = new RangeArray();
       	
       	//Load and set positions of images
       	initializeImages();

		//Load crosshair and arrow onto screen
		crosshair = (GestureImageView) findViewById(R.id.crosshair);
		arrow = (GestureImageView) findViewById(R.id.arrow);
		crosshair.setPosition(320, 180);
		arrow.setPosition(320, 75);
		arrow.setRotateAroundXY(crosshair.getImageX(), crosshair.getImageY());
		arrow.setImageRectF(2);

		// Initialize and set Crosshair and Arrow Rect variable
		setCrosshairRectVariable();

		// Set initial values for image Rect variables
		updateImageRectVariables();

		//Initialize Move Animations
		initMoveAnim();
		
		//TTS
    	initializeTextToSpeech();
    	
    	//Misc
    	isWaitingForGameReset = false;
		
		tracker = new FilteredOrientationTracker(this, this);
	}

    private void initMoveAnim(){
		// Initialize Move Animation variables
		for (int i = 0; i < NUM_OF_IMAGES; i++) {
			moveAnimation = new MoveAnimation();
			moveAnimation.setAnimationTimeMS(ANIMATION_DURATION_MS);
			moveAnimation.setMoveAnimationListener(new MoveAnimationListener() {
				@Override
				public void onMove(final float x, final float y) {
					for (int i = 0; i < NUM_OF_IMAGES; i++) {
						images[i].setPosition(moveAnimations[i].getTargetX(),
								moveAnimations[i].getTargetY());
						images[i].redraw();
					}
				}
			});
			moveAnimations[i] = moveAnimation;
		}
    }
    
	private void setNextImageMessage() {
		tts.speak("Find the "+session.getNextImageName(), TextToSpeech.QUEUE_FLUSH, null);

		Toast.makeText(this, "Find the " + session.getNextImageName(), Toast.LENGTH_LONG)
				.show();
	}
	
	private void setCenterHeaadMessage(){
		tts.speak("Please move your head to a level position. Then, tap the Glass to continue.", TextToSpeech.QUEUE_FLUSH, null);
		Toast.makeText(this, "Please move your head to a level position. Then, tap the Glass to continue.", Toast.LENGTH_LONG).show();
	}
	
	/*
	 * When the user taps the DPAD center, the images will reset to random locations
	 * 
	 */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
        	if(session.getImagesStack().size() == 0){
			initializeImages();
			updateImageRectVariables();
			session.resetGame();
			setNextImageMessage();
			initMoveAnim();
			isWaitingForGameReset = false;
        	return true;
        }
        else{
    		tts.speak("Please find. more. images.", TextToSpeech.QUEUE_FLUSH, null);	
        }
       }
        super.onKeyDown(keycode, event);
        return true;
    }

	private void initializeImages() {
		// Load Images into Array and set names
       	images = new GestureImageView[NUM_OF_IMAGES];
		
		images[0] = (GestureImageView) findViewById(R.id.square);
		images[0].setImageName("Square");

		images[1] = (GestureImageView) findViewById(R.id.triangle);
		images[1].setImageName("Triangle");

		images[2] = (GestureImageView) findViewById(R.id.image);
		images[2].setImageName("Picture");

		//Send to session
		session.setImagesArray(images);

		// Set Positions
		for (GestureImageView i : images) {
			// For each image, get random range, set image's X and Y coordinates
			range = rangeArray.getRange();
			xCoor = range[0];
			yCoor = range[1];
			Log.e(TAG, "Name: "+i.getImageName()+" X: " + xCoor + " Y: " + yCoor);
			i.setPosition(xCoor, yCoor);
			
			//Set visibility to true, applicable when displaying next 3 images
			i.setGIVAlpha(255);
		}
	}

	private void setCrosshairRectVariable() {
		// hit detection area is 25px X 25px (200px / 8px)
		crosshair.setImageRectF(8);
	}

	private void updateImageRectVariables() {
		for (GestureImageView image : images){
			if (!(image.getImageRect() == null)){
				//Parameter for setImageRect specifies division (in this case, divide by 2)
				image.setImageRectF(2);
			}
		}
		arrow.matchImageRectFWithImageView();
	}

	@Override
	public void onResume() {
		super.onResume();
		tracker.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		tracker.onPause();
	}
	
	@Override
	protected void onDestroy(){
		tts.shutdown();
		super.onDestroy();
	}

	// On gyro motion, start an animated scroll that direction.
	@Override
	public void onUpdate(float[] aGyro, float[] aGyroSum) {
		if (!isWaitingForGameReset){
		// Retrieve Gyro X and Y values, multiply with constant
		// call animateTo with resulting product
		final float xGyro = aGyro[0];
		final float yGyro = aGyro[1];
		final float deltaX = GYRO_TO_X_PIXEL_DELTA_MULTIPLIER * yGyro;
		final float deltaY = GYRO_TO_Y_PIXEL_DELTA_MULTIPLIER * xGyro;

		animateTo(deltaX, deltaY);
		arrow.rotateTowardsObject(session.getNextImageX(), session.getNextImageY(), crosshair.getImageX(), crosshair.getImageY());
		updateImageRectVariables();

		// If image intersects with crosshair, for each image and each onUpdate
		// call decrement the alpha of the image by 2.
		// When there is no intersection and the alpha of an image
		// has been decremented (but alpha is greater than 3), reset the image alpha
		// to 255
		
			if (session.getImagesStack().size()>0){
				int currentAlpha = session.getNextImage().getGIVAlpha();
				
				if (session.getNextImageRectF().intersect(crosshair.getImageRectF())) {
					session.getNextImage().setGIVAlpha((currentAlpha - 2));
					currentAlpha = session.getNextImage().getGIVAlpha();

					// If alpha is less than or equal to 3 after decrement, set
					// the rect to empty
					if (session.getNextImage().getGIVAlpha() <= 3) {
						session.incrementNumHit();
						
						//Set image's visibility to gone, pop image from stack,
						//Toast message will display which item is next
						//session.getNextImage().setVisibility(View.GONE);
						session.getNextImage().setGIVAlpha(0);
						session.popImage();
						audio.playSoundEffect(Sounds.SUCCESS);
						if (session.getImagesStack().size() > 0) {
							setNextImageMessage();
						}
						else {
							images = null;
							setCenterHeaadMessage();
							isWaitingForGameReset = true;
							for (MoveAnimation m : moveAnimations){
								m.setMoveAnimationListener(null);
							}
						}
					}
				}
				
				if(!isWaitingForGameReset){
				// If the image is still visible and is not intersecting with
				// the crosshair, reset the alpha to 255
				if ((session.getNextImage().getGIVAlpha() < 255)
						&& !session.getNextImageRectF().intersect(crosshair.getImageRectF())) {
					session.getNextImage().setGIVAlpha(session.getNextImage().getGIVAlpha() + 2);
				}
			
				//When the arrow intersects the next image, it will disappear
				if (arrow.getImageRectF().intersect(session.getNextImageRectF())){
					arrow.setGIVAlpha(arrow.getGIVAlpha() - 20);
					if(arrow.getGIVAlpha() < 30){
						arrow.setGIVAlpha(0);
					}
				}
				else{
					arrow.setGIVAlpha(arrow.getGIVAlpha() + 20);
					if (arrow.getGIVAlpha() > 200){
						arrow.setGIVAlpha(255);
					}
				}
			}
			}
		}
	}


	// Animate to a given offset
	private void animateTo(final float animationOffsetX,
		final float animationOffsetY) {
		nextX = new float[NUM_OF_IMAGES];
		nextY = new float[NUM_OF_IMAGES];

		// Set image offsets
		for (int i = 0; i < NUM_OF_IMAGES; i++) {
			nextX[i] = images[i].getImageX() + animationOffsetX;
			nextY[i] = images[i].getImageY() + animationOffsetY;

			moveAnimations[i].reset();
			moveAnimations[i].setTargetX(nextX[i]);
			moveAnimations[i].setTargetY(nextY[i]);
		}

		// Start animation
		for (int i = 0; i < NUM_OF_IMAGES; i++) {
			images[i].animationStart(moveAnimations[i]);
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

	}

}