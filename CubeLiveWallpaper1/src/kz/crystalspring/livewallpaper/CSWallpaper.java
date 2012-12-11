/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kz.crystalspring.livewallpaper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.boomerang.drawobjects.AppPoint;
import com.boomerang.drawobjects.CreatedPoint;
import com.boomerang.drawobjects.Frosty;
import com.boomerang.drawobjects.InteractiveObject;
import com.boomerang.drawobjects.MyPoint;
import com.boomerang.drawobjects.Santa;
import com.boomerang.drawobjects.TouchablePoint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/*
 * This animated wallpaper draws a rotating wireframe shape. It is similar to
 * example #1, but has a choice of 2 shapes, which are user selectable and
 * defined in resources instead of in code.
 */

public class CSWallpaper extends WallpaperService
{
	public static final String SHARED_PREFS_NAME = "cswallpaper_settings";

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine()
	{
		SnowEngine eng = new SnowEngine();
		return eng;
	}

	class SnowEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener
	{

		private final Handler mHandler = new Handler();

		private final Paint mPaint = new Paint();
		private float mOffset;
		private float mTouchX = -1;
		private float mTouchY = -1;
		private long mStartTime;
		private float mCenterX;
		private float mCenterY;

		private Bitmap currBackround;
		private InteractiveObject interObject;

		private final Runnable mDrawCube = new Runnable()
		{
			public void run()
			{
				drawFrame();
			}
		};
		private boolean mVisible;
		private SharedPreferences mPrefs;

		List<MyPoint> points;
		List<MyPoint> points_to_add;
		List<TouchablePoint> t_points;
		int POINT_COUNT = 30;
		GravityManager gm;

		SnowEngine()
		{
			final Paint paint = mPaint;
			paint.setColor(0xffffffff);
			paint.setAntiAlias(true);
			paint.setStrokeWidth(2);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStyle(Paint.Style.STROKE);

			mStartTime = SystemClock.elapsedRealtime();
			interObject = new Frosty(mPaint, getBaseContext());
			gm = new GravityManager(getBaseContext());
			gm.beginRegister();
			// mPrefs =
			mPrefs = CSWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
		{
			String snowFall = prefs.getString("snow_fall_kind", "smooth");
			if (snowFall.equals("smooth"))
				MyPoint.windy = MyPoint.SMOOTH;
			else if (snowFall.equals("windy"))
				MyPoint.windy = MyPoint.WINDY;
			else if (snowFall.equals("no_gravity"))
				MyPoint.windy = MyPoint.NO_GRAVITY;

			String interactive_mode = prefs.getString("interactive_mode", "active");
			if (interactive_mode.equals("active"))
				InteractiveObject.MODE = InteractiveObject.ACTIVE;
			else if (interactive_mode.equals("inactive"))
				InteractiveObject.MODE = InteractiveObject.INACTIVE;
			else if (interactive_mode.equals("invisible"))
				InteractiveObject.MODE = InteractiveObject.INVISIBLE;

			String wallpaper_id = prefs.getString("wallpaper_bitmap", "wallp_2");
			if (wallpaper_id.equals("wallp_2"))
				big_back_id = R.drawable.wallp_2;
			else if (wallpaper_id.equals("wallp_3"))
				big_back_id = R.drawable.wallp_3;
			else if (wallpaper_id.equals("wallp_4"))
				big_back_id = R.drawable.wallp_4;
			else if (wallpaper_id.equals("wallp_5"))
				big_back_id = R.drawable.wallp_5;
			else if (wallpaper_id.equals("wallp_6"))
				big_back_id = R.drawable.wallp_6;
			else if (wallpaper_id.equals("wallp_7"))
				big_back_id = R.drawable.wallp_7;
			else if (wallpaper_id.equals("wallp_8"))
				big_back_id = R.drawable.wallp_8;
			if (mWidth > 0 && mHeight > 0)
			{
				createRealBackground(mWidth, mHeight);
				createInterObject();
			}
		}

		private void createInterObject()
		{
			if (big_back_id == R.drawable.wallp_4)
			{
				interObject = new Frosty(mPaint, getBaseContext());
				interObject.publSurfaceChanged(mWidth, mHeight);
			} else if (big_back_id == R.drawable.wallp_2)
			{
				interObject = new Santa(mPaint, getBaseContext());
				interObject.publSurfaceChanged(mWidth, mHeight);
			} else
				interObject = null;
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
			MyPoint.density = getResources().getDisplayMetrics().density;
		}

		@Override
		public void onDestroy()
		{
			super.onDestroy();
			mHandler.removeCallbacks(mDrawCube);
			gm.pauseRegister();
		}

		@Override
		public void onVisibilityChanged(boolean visible)
		{
			mVisible = visible;
			if (visible)
			{
				drawFrame();
			} else
			{
				mHandler.removeCallbacks(mDrawCube);
			}
		}

		int mHeight;
		int mWidth;

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			super.onSurfaceChanged(holder, format, width, height);
			mWidth = width;
			mHeight = height;

			POINT_COUNT = (int) Math.round(Math.sqrt(width * height) / MyPoint.density * 0.5);

			MyPoint.max_y = height;
			MyPoint.max_x = width;

			mCenterX = width / 2.0f;
			mCenterY = height / 2.0f;

			points = new ArrayList();
			t_points = new ArrayList();
			points_to_add = new ArrayList();
			createRealBackground(mWidth, mHeight);
			createInterObject();
			drawFrame();
		}

		private void createRealBackground(int width, int height)
		{
			if (currBackround != null)
				currBackround.recycle();
			Bitmap bigBack = getBigBackground(width, height);
			int btmHeight = bigBack.getHeight();
			int btmWidth = bigBack.getWidth();
			float k = (float) btmHeight / (float) height;
			Bitmap smallback = Bitmap.createScaledBitmap(bigBack, Math.round(btmWidth / k), height, true);
			bigBack.recycle();
			currBackround = smallback;
		}

		int big_back_id = R.drawable.wallp_2;

		private Bitmap getBigBackground(int width, int height)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(getBaseContext().getResources(), big_back_id, options);
			int btmHeight = options.outHeight;
			int btmWidth = options.outWidth;
			float k = (float) height / (float) btmHeight;
			float k2 = (float) width / (float) btmWidth;
			k = (k > k2) ? k : k2;
			// Log.d("Decoding_Image", "k=" + k);
			options = new BitmapFactory.Options();
			options.inSampleSize = Math.round(1 / k);
			options.inPreferredConfig = Config.RGB_565;
			Log.d("Decoding_Image", "1/k=" + options.inSampleSize);
			Bitmap bigBack = BitmapFactory.decodeResource(getBaseContext().getResources(), big_back_id, options);
			return bigBack;
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder)
		{
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder)
		{
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mDrawCube);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels)
		{
			mOffset = xOffset;
			drawFrame();
		}

		boolean create = false;

		@Override
		public void onTouchEvent(final MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
			{
				if (create || event.getAction() == MotionEvent.ACTION_DOWN)
				{
					AsyncTask task = new AsyncTask()
					{

						@Override
						protected Object doInBackground(Object... params)
						{
							try
							{
								MyPoint point = new CreatedPoint((int) event.getX(), (int) event.getY(), mPaint, getBaseContext());
								addPoint(point);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
							return null;
						}
					};
					task.execute();
				}
				create = !create;
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					if (interObject != null && interObject.publIsHitted(event.getX(), event.getY()))
						interObject.publTouch();
					else
						for (TouchablePoint point : t_points)
						{
							if (point.isHitted(event.getX(), event.getY()))
							{
								point.touch();
								break;
							}

						}
				}
			} else
			{
				mTouchX = -1;
				mTouchY = -1;
			}
			super.onTouchEvent(event);
		}

		private void addPoint(MyPoint point)
		{
			if (point != null)
			{
				points_to_add.add(point);
			}
		}

		MoveTask moveTask;
		// RotateTask rotateTask_1;
		// RotateTask rotateTask_2;
		final int fps = 30;
		final int frame_delay = 1000 / fps;

		void drawFrame()
		{
			final SurfaceHolder holder = getSurfaceHolder();
			final Rect frame = holder.getSurfaceFrame();
			final int width = frame.width();
			final int height = frame.height();
			if (moveTask == null || moveTask.getStatus() != Status.RUNNING)
			{
				moveTask = new MoveTask();
				moveTask.execute();
			}
			// if (MyPoint.need_to_rotate)
			// {
			// if (rotateTask_1 == null || rotateTask_1.getStatus() !=
			// Status.RUNNING)
			// {
			// rotateTask_1 = new RotateTask();
			// rotateTask_1.execute(Integer.valueOf(0));
			// }
			// if (rotateTask_2 == null || rotateTask_2.getStatus() !=
			// Status.RUNNING)
			// {
			// rotateTask_2 = new RotateTask();
			// rotateTask_2.execute(Integer.valueOf(1));
			// }
			// }

			long now = new Date().getTime();
			Canvas c = null;
			try
			{
				c = holder.lockCanvas();

				if (c != null)
				{
					// c.setBitmap(currBackround);
					drawBackground(c);
					List<MyPoint> destroyed = new ArrayList<MyPoint>(points.size());
					synchronized (points)
					{
						for (MyPoint point : points)
						{
							point.draw(c);
							if (point.isDestroyed())
								destroyed.add(point);
						}
					}
					points.removeAll(destroyed);
					t_points.removeAll(destroyed);

					double angle = GravityManager.mAngle;

					Thread addTask = new Thread()
					{
						TouchablePoint tp;
						MyPoint mp;

						@Override
						public void run()
						{
							float touch = (float) (Math.random() * 100);
							double rand_x;
							double rand_y;
							if (touch < 0.005)
							{
								rand_y = 0;
								rand_x = (Math.random() * width);
								tp = new AppPoint((float) rand_x, (float) rand_y, mPaint, getBaseContext());
								addPoint(tp);
							}
							// Log.d("Gravity Angle", "" +
							// GravityManager.mAngle);
							if (touch < 4 && points.size() < POINT_COUNT * 0.5)
							{
								rand_y = 0;
								rand_x = (Math.random() * width);
								mp = new TouchablePoint((float) rand_x, (float) rand_y, mPaint, getBaseContext());
								addPoint(mp);
							}
						}
					};
					addTask.start();
				}
			} finally
			{
				if (c != null)
					holder.unlockCanvasAndPost(c);
				addNewPoints();
				mHandler.removeCallbacks(mDrawCube);
				if (mVisible)
				{
					long after = new Date().getTime();
					long delay = (after - now);
					delay = (delay < frame_delay) ? frame_delay - delay : 10;
					mHandler.postDelayed(mDrawCube, delay);
				}
			}
		}

		private synchronized void addNewPoints()
		{

			for (int i = 0; i < points_to_add.size(); i++)
			{
				MyPoint point = points_to_add.get(i);
				if (point != null)
				{
					points.add(point);
					if (TouchablePoint.class.isInstance(point))
						t_points.add((TouchablePoint) point);
				}
				if (points.size() > POINT_COUNT * 2)
					points.get(0).destroy();
			}
			points_to_add.clear();
			Collections.sort(points, new Comparator<MyPoint>()
			{
				@Override
				public int compare(MyPoint lhs, MyPoint rhs)
				{
					if (lhs != null && rhs != null)
						return lhs.size.compareTo(rhs.size);
					else
						return 0;
				}
			});
		}

		class MoveTask extends AsyncTask
		{
			@Override
			protected Object doInBackground(Object... arg0)
			{
				while (!isCancelled())
				{
					List<MyPoint> pnts = new ArrayList<MyPoint>();
					synchronized (points)
					{
						pnts.addAll(points);
					}
					Date dt_begin = new Date();
					for (MyPoint point : pnts)
					{
						if (point != null)
							point.move();
					}
					Date dt_end = new Date();
					if (dt_end.getTime() - dt_begin.getTime() < 1000 / (fps * 1.5))
					{
						try
						{
							Thread.sleep(1000 / (int) (fps * 1.5) - (dt_end.getTime() - dt_begin.getTime()));
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				return null;
			}
		};

		void drawBackground(Canvas c)
		{
			c.save();
			c.translate(mCenterX, mCenterY);

			RectF dst = new RectF(0, 0, mWidth, mHeight);
			float localOffset = mOffset;
			if (mWidth > mHeight)
				localOffset = 0;
			c.drawBitmap(currBackround, -mWidth / 2 - (localOffset * mWidth), -mHeight / 2, mPaint);
			if (interObject != null)
				interObject.publDraw(c, localOffset);
			long now = SystemClock.elapsedRealtime();
			c.restore();
		}

		void drawTouchPoint(Canvas c)
		{
			if (mTouchX >= 0 && mTouchY >= 0)
			{
				c.drawCircle(mTouchX, mTouchY, 80, mPaint);
				c.drawText(Float.toString(mTouchX) + ", " + Float.toString(mTouchY), mTouchX + 20, mTouchY + 20, mPaint);
			}

		}
	}
}
