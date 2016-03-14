package com.glassrehab.mainM1M2;

import java.io.File;
import java.util.List;

import org.hitlabnz.helloworld.R;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MainActivity extends Activity {

	public final static String TAG = MainActivity.class.getSimpleName();
	public static final String name= "name"; 
	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final String shape = "shapeKey"; 
	public static final String size = "sizeKey"; 
	public static final String speed = "speedKey"; 
	public static final String color = "colorKey"; 
	public static final String num = "numKey"; 
	public static final String bg = "bgKey"; 
	public static final String pic = "picKey"; 

	SharedPreferences sharedpreferences;
	TextView Tsize;
	TextView Tspeed;
	TextView Tcolor;
	TextView Tshape;
	TextView textView;
	TextView Tnum;
	TextView Tbg;
	GestureDetector gestureDetector;
	TextToSpeech tts;
	SoundPool soundPool;
	int soundID;
	sqlAdapter sqlHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


		// get text view inflated from the layout resource
		textView = (TextView) findViewById(R.id.textView1);
		Tsize = (TextView) findViewById(R.id.textSize);
		Tspeed= (TextView) findViewById(R.id.textSpeed);
		Tcolor= (TextView) findViewById(R.id.textColor);
		Tshape= (TextView) findViewById(R.id.textShape);
		Tnum= (TextView) findViewById(R.id.textNum);
		Tbg= (TextView) findViewById(R.id.textBg);
		initialSetting("Red","Medium","Random","Circle","Medium","Black");


		// create gesture detector
		gestureDetector = createGestureDetector();


		// create tts
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status == TextToSpeech.ERROR)
					Toast.makeText(getApplicationContext(), "TTS not working.", Toast.LENGTH_SHORT).show();
			}
		});

		// play opening sound
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundID = soundPool.load(this, R.raw.bell, 1);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
				soundPool.play(soundID, 1, 1, 1, 0, 1);
			}
		});
		
		//sql database
		sqlHelper= new sqlAdapter(this);
	}

	private void initialSetting(String Color, String Size, String Speed,
			String Shape, String Num, String Bg) {
		Editor editor = sharedpreferences.edit();
		editor.putString(color, Color);
		editor.putString(speed, Speed);
		editor.putString(size, Size);
		editor.putString(shape, Shape);
		editor.putString(num, Num);
		editor.putString(bg, Bg);
		editor.commit();
		if (sharedpreferences.contains(color))
		{
			Tcolor.setText("Color: \n"+sharedpreferences.getString(color, ""));

		}
		if (sharedpreferences.contains(shape))
		{
			Tshape.setText("Shape: \n"+sharedpreferences.getString(shape, ""));

		}
		if (sharedpreferences.contains(speed))
		{
			Tspeed.setText("Speed: \n"+sharedpreferences.getString(speed, ""));

		}
		if (sharedpreferences.contains(size))
		{
			Tsize.setText("Size: \n"+sharedpreferences.getString(size, ""));

		}
		if (sharedpreferences.contains(num))
		{
			Tnum.setText("Number:\n"+sharedpreferences.getString(num, ""));

		}
		if (sharedpreferences.contains(bg))
		{
			Tbg.setText("Background:\n"+sharedpreferences.getString(bg, ""));

		}
	}

	@Override
	protected void onDestroy() {
		// Add static card with the current hello message
		Card card = new Card(this);
		card.setText((String)textView.getText());
		card.setFootnote("From the Glass Class");

		/*	TimelineManager tm = TimelineManager.from(this);
		tm.insert(card);
		 */
		// shutdown tts
		tts.shutdown();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_Log_In:
			onLogin();
			return true;

		case R.id.rectangle:

			showMessageAndSpeak(Tshape,"Shape: \n"+item.toString());
			putSharedData(shape, item.toString());

			Log.d(TAG, item.toString());
		case R.id.circle:

			showMessageAndSpeak(Tshape,"Shape: \n"+item.toString());
			putSharedData(shape, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.triangle:
			showMessageAndSpeak(Tshape,"Shape: \n"+item.toString());
			putSharedData(shape, item.toString());
			Log.d(TAG, item.toString());

			return true;
		case R.id.random:
			showMessageAndSpeak(Tshape,"Shape: \n"+item.toString());
			putSharedData(shape, item.toString());
			Log.d(TAG, item.toString());

			return true;	
		case R.id.yellow:

			showMessageAndSpeak(Tcolor,"Color: \n"+item.toString());
			putSharedData(color, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.red:

			showMessageAndSpeak(Tcolor,"Color: \n"+item.toString());
			putSharedData(color, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.white:

			showMessageAndSpeak(Tcolor,"Color: \n"+item.toString());
			putSharedData(color, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.small:

			showMessageAndSpeak(Tsize,"Size: \n"+item.toString());
			putSharedData(size, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.medium:

			showMessageAndSpeak(Tsize,"Size: \n"+item.toString());
			putSharedData(size, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.large:

			showMessageAndSpeak(Tsize,"Size: \n"+item.toString());
			putSharedData(size, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.extra:

			showMessageAndSpeak(Tsize,"Size: \n"+item.toString());
			putSharedData(size, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.extra2:

			showMessageAndSpeak(Tspeed,"Speed: \n"+item.toString());
			putSharedData(speed, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.slow:

			showMessageAndSpeak(Tspeed,"Speed: \n"+item.toString());
			putSharedData(speed, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.medium2:

			showMessageAndSpeak(Tspeed,"Speed: \n"+item.toString());
			putSharedData(speed, item.toString());

			Log.d(TAG, item.toString());

			return true;
		case R.id.fast:

			showMessageAndSpeak(Tspeed,"Speed: \n"+item.toString());
			putSharedData(speed, item.toString());

			Log.d(TAG,item.toString());

			return true;
		case R.id.random2:

			showMessageAndSpeak(Tspeed,"Speed: \n"+item.toString());
			putSharedData(speed, item.toString());

			Log.d(TAG,item.toString());

			return true;

		case R.id.min:

			showMessageAndSpeak(Tnum,"Number:\n"+item.toString());
			putSharedData(num, item.toString());

			Log.d(TAG,item.toString());

			return true;
		case R.id.medium3:

			showMessageAndSpeak(Tnum,"Number:\n"+item.toString());
			putSharedData(num, item.toString());

			Log.d(TAG,item.toString());

			return true;
		case R.id.max:

			showMessageAndSpeak(Tnum,"Number:\n"+item.toString());
			putSharedData(num, item.toString());

			Log.d(TAG,item.toString());

			return true;
		case R.id.black:

			showMessageAndSpeak(Tbg,"Background:\n"+item.toString());
			putSharedData(bg, item.toString());

			Log.d(TAG,item.toString());

			return true;
		case R.id.grey:

			showMessageAndSpeak(Tbg,"Background:\n"+item.toString());
			putSharedData(bg, item.toString());

			Log.d(TAG,item.toString());

			return true;	
		case R.id.menu_m1:
			//			showMessageAndSpeak("module 1 starts!");
			startActivity(new Intent(MainActivity.this, MovingBall.class));
			return true;
		case R.id.menu_m2:
			//			showMessageAndSpeak("module 1 starts!");
			startActivity(new Intent(MainActivity.this, MovingImage.class));
			return true;
		// module 3
		case R.id.menu_m3:
			//			showMessageAndSpeak("module 1 starts!");
			startActivity(new Intent(MainActivity.this, com.neatocode.gyroimageview.ViewActivity.class));
			return true;

		// take a picture
		case R.id.menu_item_camera:
			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 200);
			break;

		// reset the configurations to their default settings
		case R.id.menu_item_reset:
			showMessageAndSpeak(textView,"Reset");
			this.findViewById(R.id.backgroundLayout).setBackgroundDrawable(null);
			initialSetting("Red","Medium","Fast","Circle","Medium","Black");
			showMessageAndSpeak(textView,"Reset");
			int count= sqlHelper.deleteRow(sharedpreferences.getString(name, "NA"));
			return true;

		// user has clicked the about menu card
		case R.id.menu_item_about:
			startActivity(new Intent(this, InfoActivity.class));
			return true;}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			Log.d(TAG, "Tapped (DPAD_CENTER)");
			openOptionsMenu(); // open the option menu on tap
			return true; // return true if you handled this event
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, String.format("Motion ACTION_DOWN: %.1f / %.1f", event.getX(), event.getY()));
			// return true if you handled this event
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, String.format("Motion ACTION_MOVE: %.1f / %.1f", event.getX(), event.getY()));
			// return true if you handled this event
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, String.format("Motion ACTION_UP: %.1f / %.1f", event.getX(), event.getY()));
			// return true if you handled this event
			break;
		}

		if(gestureDetector.onMotionEvent(event))
			return true;

		return super.onGenericMotionEvent(event);
	}

	private GestureDetector createGestureDetector() {
		GestureDetector gestureDetector = new GestureDetector(this);
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.d(TAG, "Gesture.TAP");
					// return true if you handled this event
				} else if (gesture == Gesture.TWO_TAP) {
					Log.d(TAG, "Gesture.TWO_TAP");
					// return true if you handled this event
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					Log.d(TAG, "Gesture.SWIPE_RIGHT");
					// return true if you handled this event
				} else if (gesture == Gesture.SWIPE_LEFT) {
					Log.d(TAG, "Gesture.SWIPE_LEFT");
					// return true if you handled this event
				}

				return false;
			}
		});
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				Log.d(TAG, String.format("Finger prev:%d curr:%d", previousCount, currentCount));
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta, float velocity) {
				Log.d(TAG, String.format("Scroll dis:%.1f delta:%.1f vel:%.1f", displacement, delta, velocity));
				return false; // return true if you handled this event
			}
		});

		return gestureDetector;
	}

	private void onLogin() {
		// open system voice recognizer
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What is your patient ID or your name?");
		this.startActivityForResult(intent, 100);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Entered onActivityResult");
		if (requestCode == 100) {
			if(resultCode == Activity.RESULT_OK) {
				// get results from the voice recognizer
				List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				showMessageAndSpeak(textView,"Hello " + results.get(0) + "!");
				sqlHelper.addUser(results.get(0), null, null,null);
				putSharedData(name, results.get(0));
				Log.d(TAG, "Successfully log in the database");
			} else {
				Toast.makeText(this, "Cannot get your name!", Toast.LENGTH_SHORT).show();

				// play a system sound
				AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audio.playSoundEffect(Sounds.ERROR);

				Log.wtf(TAG, "Activity might got canceled?");
			}
		} else if (requestCode == 200) {
			if(resultCode == Activity.RESULT_OK) {
				String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
				processPictureWhenReady(picturePath); // the file might not be ready right away
				putSharedData(pic, picturePath);

				Toast.makeText(this, "Loading the picture!", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(this, "Failed to take a picture!", Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	public void putSharedData(String key, String value){
		Editor editor = sharedpreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	private void showMessageAndSpeak(TextView text,String message) {
		text.setText(message);
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

	}

	private void processPictureWhenReady(final String picturePath) {
		final File pictureFile = new File(picturePath);

		if (pictureFile.exists()) {
			// The picture is ready; process it.
			RelativeLayout layout = (RelativeLayout)findViewById(R.id.backgroundLayout);
			layout.setBackgroundDrawable(Drawable.createFromPath(picturePath));
		} else {
			// The file does not exist yet. Before starting the file observer, you
			// can update your UI to let the user know that the application is
			// waiting for the picture (for example, by displaying the thumbnail
			// image and a progress indicator).

			final File parentDirectory = pictureFile.getParentFile();
			FileObserver observer = new FileObserver(parentDirectory.getPath()) {
				// Protect against additional pending events after CLOSE_WRITE is handled.
				private boolean isFileWritten;

				@Override
				public void onEvent(int event, String path) {
					if (!isFileWritten) {
						// For safety, make sure that the file that was created in
						// the directory is actually the one that we're expecting.
						File affectedFile = new File(parentDirectory, path);
						isFileWritten = (event == FileObserver.CLOSE_WRITE
								&& affectedFile.equals(pictureFile));

						if (isFileWritten) {
							stopWatching();

							// Now that the file is ready, recursively call
							// processPictureWhenReady again (on the UI thread).
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									processPictureWhenReady(picturePath);
								}
							});
						}
					}
				}
			};
			observer.startWatching();
		}
	}

}
