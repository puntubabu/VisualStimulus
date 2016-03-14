/*
 * Copyright (c) 2012 Jason Polites
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.polites.android;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import com.polites.android.MoveAnimationListener;
import com.polites.android.MoveAnimation;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class GestureImageView extends ImageView  {

	public static final String GLOBAL_NS = "http://schemas.android.com/apk/res/android";
	public static final String LOCAL_NS = "http://schemas.polites.com/android";
	private static final int ANIMATION_DURATION_MS = 100;

	private MoveAnimation moveAnimation;
	
	private final Semaphore drawLock = new Semaphore(0);
	private Animator animator;
	
	private float nextX;
	private float nextY;
	
	private String imageName;
	
	private Drawable drawable;

	private float x = 0, y = 0;

	private float[] rotateAroundXY = new float[2];
	
	private Rect imageRect;
	private RectF imageRectF;
	
	private boolean layout = false;

	private float scaleAdjust = 1.0f;
	private float startingScale = -1.0f;

	private float scale = 1.0f;
	private float maxScale = 5.0f;
	private float minScale = 0.75f;
	private float fitScaleHorizontal = 1.0f;
	private float fitScaleVertical = 1.0f;
	private float rotation = 0.0f;

	private float centerX;
	private float centerY;
	
	private Float startX, startY;

	private int hWidth;
	private int hHeight;

	private int resId = -1;
	private boolean recycle = false;
	private boolean strict = false;

	private int displayHeight;
	private int displayWidth;

	private int alpha = 255;
	private ColorFilter colorFilter;

	private int deviceOrientation = -1;
	private int imageOrientation;

	private GestureImageViewListener gestureImageViewListener;


	public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
	}

	public GestureImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		String scaleType = attrs.getAttributeValue(GLOBAL_NS, "scaleType");
		
		if(scaleType == null || scaleType.trim().length() == 0) {
			setScaleType(ScaleType.CENTER_INSIDE);
		}
		
		String strStartX = attrs.getAttributeValue(LOCAL_NS, "start-x");
		String strStartY = attrs.getAttributeValue(LOCAL_NS, "start-y");
		
		if(strStartX != null && strStartX.trim().length() > 0) {
			startX = Float.parseFloat(strStartX);
		}
		
		if(strStartY != null && strStartY.trim().length() > 0) {
			startY = Float.parseFloat(strStartY);
		}
		
		setStartingScale(attrs.getAttributeFloatValue(LOCAL_NS, "start-scale", startingScale));
		setMinScale(attrs.getAttributeFloatValue(LOCAL_NS, "min-scale", minScale));
		setMaxScale(attrs.getAttributeFloatValue(LOCAL_NS, "max-scale", maxScale));
		setStrict(attrs.getAttributeBooleanValue(LOCAL_NS, "strict", strict));
		setRecycle(attrs.getAttributeBooleanValue(LOCAL_NS, "recycle", recycle));
		
		initImage();
	}
	
	public void setImageRectF(int division){
		this.imageRectF.set(
				(float) this.x
						- (this.getImageWidth() / division),
				(float) this.y
						- (this.getImageHeight() / division),
				(float) this.x
						+ (this.getImageWidth() / division),
				(float) this.y
						+ (this.getImageHeight() / division));
	}
	
	public void setNextX(float x){
		this.nextX = x;
	}
	
	public void setNextY(float y){
		this.nextY = y;
	}
	
	public float getNextX(){
		return this.nextX;
	}
	
	public float getNextY(){
		return this.nextY;
	}
	
	public MoveAnimation getMoveAnim(){
		return this.moveAnimation;
	}
	
	public void setMoveAnimation(){
		moveAnimation = new MoveAnimation();
		moveAnimation.setAnimationTimeMS(ANIMATION_DURATION_MS);
		moveAnimation.setMoveAnimationListener(new MoveAnimationListener() {
			@Override
			public void onMove(final float x, final float y) {
					setPosition(moveAnimation.getTargetX(),
							moveAnimation.getTargetY());
					redraw();
			}
		});
	}
	
	public void setImageRectFRotation(){
		float[] values = new float[9];
		Matrix m = new Matrix();
		// point is the point about which to rotate.
		m.setRotate(this.rotation, rotateAroundXY[0], rotateAroundXY[1]);
		m.mapRect(this.imageRectF);
		m.getValues(values);
		for (float i : values){
			Log.e("GestureImageView", "Values: "+i);
		}
	}
	
	public void matchImageRectFWithImageView(){
		this.imageRectF.set((float)this.getLeft(), (float)this.getTop(), 
				(float)this.getRight(), (float)this.getBottom());
	}
	
	public RectF getImageRectF(){
		return imageRectF;
	}
	
	public Rect getImageRect(){
		return this.imageRect;
	}
	
	public GestureImageView(Context context) {
		super(context);
		setScaleType(ScaleType.CENTER_INSIDE);
		initImage();
	}
	
	public int getDisplayHeight() {
		return displayHeight;
	}

	public int getDisplayWidth() {
		return displayWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if(drawable != null) {
			int orientation = getResources().getConfiguration().orientation;
			if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
				displayHeight = MeasureSpec.getSize(heightMeasureSpec);

				if(getLayoutParams().width == LayoutParams.WRAP_CONTENT) {
					float ratio = (float) getImageWidth() / (float) getImageHeight();
					displayWidth = Math.round( (float) displayHeight * ratio) ;
				}
				else {
					displayWidth = MeasureSpec.getSize(widthMeasureSpec);
				}
			}
			else {
				displayWidth = MeasureSpec.getSize(widthMeasureSpec);
				if(getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
					float ratio = (float) getImageHeight() / (float) getImageWidth();
					displayHeight = Math.round( (float) displayWidth * ratio) ;
				}
				else {
					displayHeight = MeasureSpec.getSize(heightMeasureSpec);
				}				
			}
		}
		else {
			displayHeight = MeasureSpec.getSize(heightMeasureSpec);
			displayWidth = MeasureSpec.getSize(widthMeasureSpec);
		}

		setMeasuredDimension(displayWidth, displayHeight);
	}

	public float getImageViewRotation(){
		return this.rotation;
	}
	
	public String getImageName(){
		return this.imageName;
	}
	
	public void setImageName(String name){
		this.imageName = new String(name);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(changed || !layout) {
			setupCanvas(displayWidth, displayHeight, getResources().getConfiguration().orientation);
		}
	}

	public void setRotateAroundXY(float x, float y){
		rotateAroundXY[0] = x;
		rotateAroundXY[1] = y;
	}
	
	protected void setupCanvas(int measuredWidth, int measuredHeight, int orientation) {

		if(deviceOrientation != orientation) {
			layout = false;
			deviceOrientation = orientation;
		}

		if(drawable != null && !layout) {
			int imageWidth = getImageWidth();
			int imageHeight = getImageHeight();

			hWidth = Math.round(((float)imageWidth / 2.0f));
			hHeight = Math.round(((float)imageHeight / 2.0f));
			
			measuredWidth -= (getPaddingLeft() + getPaddingRight());
			measuredHeight -= (getPaddingTop() + getPaddingBottom());


			this.centerX = (float) measuredWidth / 2.0f;
			this.centerY = (float) measuredHeight / 2.0f;


			drawable.setBounds(-hWidth,-hHeight,hWidth,hHeight);


			layout = true;
		}
	}
	
	protected void computeCropScale(int imageWidth, int imageHeight, int measuredWidth, int measuredHeight) {
		fitScaleHorizontal = (float) measuredWidth / (float) imageWidth;
		fitScaleVertical = (float) measuredHeight / (float) imageHeight;
	}
	
	protected void computeStartingScale(int imageWidth, int imageHeight, int measuredWidth, int measuredHeight) {
		switch(getScaleType()) {
			case CENTER:
				// Center the image in the view, but perform no scaling.
				startingScale = 1.0f;
				break;
				
			case CENTER_CROP:
				// Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions
				// (width and height) of the image will be equal to or larger than the corresponding dimension of the view (minus padding).
				startingScale = Math.max((float) measuredHeight / (float) imageHeight, (float) measuredWidth/ (float) imageWidth);
				break;
				
			case CENTER_INSIDE:

				// Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions
				// (width and height) of the image will be equal to or less than the corresponding dimension of the view (minus padding).
				float wRatio = (float) imageWidth / (float) measuredWidth;
				float hRatio = (float) imageHeight / (float) measuredHeight;

				if(wRatio > hRatio) {
					startingScale = fitScaleHorizontal;
				}
				else {
					startingScale = fitScaleVertical;
				}

				break;
		}
	}

	protected boolean isRecycled() {
		if(drawable != null && drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			if(bitmap != null) {
				return bitmap.isRecycled();
			}
		}
		return false;
	}

	protected void recycle() {
		if(recycle && drawable != null && drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			if(bitmap != null) {
				bitmap.recycle();
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(layout) {
			if(drawable != null && !isRecycled()) {
				canvas.save();
				
				float adjustedScale = scale * scaleAdjust;

				canvas.translate(x, y);
				if(rotation != 0.0f) {
					canvas.rotate(rotation, 0, 105f);
				}

				if(adjustedScale != 1.0f) {
					canvas.scale(adjustedScale, adjustedScale);
				}

				drawable.draw(canvas);

				canvas.restore();
			}

			if(drawLock.availablePermits() <= 0) {
				drawLock.release();
			}
		}
	}

	/**
	 * Waits for a draw
	 * @param max time to wait for draw (ms)
	 * @throws InterruptedException
	 */
	public boolean waitForDraw(long timeout) throws InterruptedException {
		return drawLock.tryAcquire(timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void onAttachedToWindow() {
		animator = new Animator(this, "GestureImageViewAnimator");
		animator.start();

		if(resId >= 0 && drawable == null) {
			setImageResource(resId);
		}

		super.onAttachedToWindow();
	}

	public void animationStart(Animation animation) {
		if(animator != null) {
			animator.play(animation);
		}
	}

	public void animationStop() {
		if(animator != null) {
			animator.cancel();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if(animator != null) {
			animator.finish();
		}
		if(recycle && drawable != null && !isRecycled()) {
			recycle();
			drawable = null;
		}
		super.onDetachedFromWindow();
	}

	protected void initImage() {
		imageRect = new Rect();
		imageRectF = new RectF();

		if(this.drawable != null) {
			this.drawable.setFilterBitmap(true);
			if(colorFilter != null) {
				this.drawable.setColorFilter(colorFilter);
			}
		}
		
		if(!layout) {
			requestLayout();
			redraw();
		}
	}

	public void rotateTowardsObject(float targetX, float targetY, float crosshairX, float crosshairY){
		float rotDegrees;
		double arctan;
		// Need to account for Quadrants

		// If getImageY() returns 0, then tangent will be undefined,
		// so set the rotation to be either 90 or 270
		if ((targetY == 0)) {
			if ((targetX > crosshairX)) {
				rotDegrees = 90f;
			} else {
				rotDegrees = 270f;
			}
		} else {

			// Get arctan value (in radians)
			// Account for the fact that the arrow is rotating around point
			// (320,180)
			// but origin is (0,0)
			arctan = (Math.atan((double) (targetX - 320)
					/ (double) (targetY - 180)));

			// Convert to degrees
			rotDegrees = (float) Math.toDegrees(arctan);

			// Q1
			if ((targetX > crosshairX)
					&& (targetY < crosshairY)) {
				rotDegrees *= -1f;
			}
			// Q2
			else if ((targetX < crosshairX)
					&& (targetY < crosshairY)) {
				rotDegrees *= -1f;
			}
			// Q3
			else if ((targetX < crosshairX)
					&& (targetY > crosshairY)) {
				rotDegrees += 180f;
				rotDegrees *= -1;
			}
			// Q4
			else if ((targetX > crosshairX)
					&& (targetY > crosshairY)) {
				rotDegrees = 180f - rotDegrees;
			} else {
				// error
			}
		}
		this.setRotation(rotDegrees);

	}
	
	public void setGIVAlpha(int a){
		this.setImageAlpha(a);
		this.alpha = a;
	}
	
	public void resetGIVAlpha(){
		this.setImageAlpha(255);
		this.alpha = 255;
	}
	
	public int getGIVAlpha(){
		return this.alpha;
	}
	
	public void setImageBitmap(Bitmap image) {
		this.drawable = new BitmapDrawable(getResources(), image);
		initImage();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		this.drawable = drawable;
		initImage();
	}

	public void setImageResource(int id) {
		if(this.drawable != null) {
			this.recycle();
		}
		if(id >= 0) {
			this.resId = id;
			setImageDrawable(getContext().getResources().getDrawable(id));
		}
	}

	public int getScaledWidth() {
		return Math.round(getImageWidth() * getScale());
	}
	
	public int getScaledHeight() {
		return Math.round(getImageHeight() * getScale());
	}
	
	public int getImageWidth() {
		if(drawable != null) {
			return drawable.getIntrinsicWidth();
		}
		return 0;
	}

	public int getImageHeight() {
		if(drawable != null) {
			return drawable.getIntrinsicHeight();
		}
		return 0;
	}

	public void moveBy(float x, float y) {
		this.x += x;
		this.y += y;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void redraw() {
		postInvalidate();
	}
	
	public void setMinScale(float min) {
		this.minScale = min;

	}

	public void setMaxScale(float max) {
		this.maxScale = max;

	}

	public void setScale(float scale) {
		scaleAdjust = scale;
	}

	public float getScale() {
		return scaleAdjust;
	}

	public float getImageX() {
		return x;
	}

	public float getImageY() {
		return y;
	}
	
	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public boolean isRecycle() {
		return recycle;
	}

	public void setRecycle(boolean recycle) {
		this.recycle = recycle;
	}

	public void reset() {
		x = centerX;
		y = centerY;
		scaleAdjust = startingScale;

		redraw();
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public float getGIVRotation(){
		return this.rotation;
	}

	public void setGestureImageViewListener(GestureImageViewListener pinchImageViewListener) {
		this.gestureImageViewListener = pinchImageViewListener;
	}

	public GestureImageViewListener getGestureImageViewListener() {
		return gestureImageViewListener;
	}

	@Override
	public Drawable getDrawable() {
		return drawable;
	}

	@Override
	public void setAlpha(int alpha) {
		this.alpha = alpha;
		if(drawable != null) {
			drawable.setAlpha(alpha);
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		this.colorFilter = cf;
		if(drawable != null) {
			drawable.setColorFilter(cf);
		}
	}



	@Override
	public Matrix getImageMatrix() {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}		
		return super.getImageMatrix();
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		if(scaleType == ScaleType.MATRIX || 
			scaleType == ScaleType.CENTER ||
			scaleType == ScaleType.CENTER_CROP ||
			scaleType == ScaleType.CENTER_INSIDE
			) {
			
			super.setScaleType(scaleType);
		}
		else if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
	}

	@Override
	public void invalidateDrawable(Drawable dr) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
		super.invalidateDrawable(dr);
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
		return super.onCreateDrawableState(extraSpace);
	}

	@Override
	public void setAdjustViewBounds(boolean adjustViewBounds) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
		super.setAdjustViewBounds(adjustViewBounds);
	}

	@Override
	public void setImageLevel(int level) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
		super.setImageLevel(level);
	}

//	@Override
//	public void setImageMatrix(Matrix matrix) {
//		if(strict) {
//			throw new UnsupportedOperationException("Not supported");
//		}
//	}

	@Override
	public void setImageState(int[] state, boolean merge) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
	}

	@Override
	public void setSelected(boolean selected) {
		if(strict) {
			throw new UnsupportedOperationException("Not supported");
		}
		super.setSelected(selected);
	}
	
	public float getCenterX() {
		return centerX;
	}
	
	public float getCenterY() {
		return centerY;
	}
	
	public boolean isLandscape() {
		return getImageWidth() >= getImageHeight();
	}
	
	public boolean isPortrait() {
		return getImageWidth() <= getImageHeight();
	}
	
	public void setStartingScale(float startingScale) {
		this.startingScale = startingScale;
	}
	
	public void setStartingPosition(float x, float y) {
		this.startX = x;
		this.startY = y;
	}


	/**
	 * Returns true if the image dimensions are aligned with the orientation of the device.
	 * @return
	 */
	public boolean isOrientationAligned() {
		if(deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			return isLandscape();
		}
		else if(deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
			return isPortrait();
		}
		return true;
	}
	
	public int getDeviceOrientation() {
		return deviceOrientation;
	}
}
