package com.boomerang.drawobjects;

import java.io.IOException;
import java.util.List;

import kz.crystalspring.livewallpaper.GravityManager;
import kz.crystalspring.livewallpaper.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.FloatMath;
import android.util.Log;

public class MyPoint
{
	public float offset_y;

	public float y;
	public float x;

	public float drawable_x;
	public float drawable_y;

	public double speed;
	public Bitmap imageBitmap;
	public Paint paint;
	public double sinus_multi = 1;
	public Integer size = 0;

	private boolean destroyed;
	public static float max_y = -1;
	public static float max_x = -1;

	public static float density = 1;

	public static float y_gravity;
	public static float x_gravity;

	float bitmap_rotating = 1;
	static final double min_bitmap_size = 10.0;
	static final double max_bitmap_size = 50.0;

	public static final int SMOOTH = 0;
	public static final int WINDY = 1;
	public static final int NO_GRAVITY = 2;

	public static int windy = SMOOTH;

	double range = max_bitmap_size - min_bitmap_size;

	public MyPoint(float x, float y, Paint paint, Context context)
	{
		this.x = x;
		this.y = y;

		this.drawable_x = x;
		this.drawable_y = y;

		this.speed = 0.7 * density;// (Math.random() * 5 + 2) * density;
		this.paint = paint;
		this.offset_y = 0;// (float) (Math.random() * 100);

		int number = (int) (Math.random() * 49) + 1;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ALPHA_8;
		try
		{
			String filename = "snow_" + intToString(number, 2) + ".png";
			// Log.d("LiveWallpaper_File", filename);
			this.imageBitmap = BitmapFactory.decodeStream(context.getAssets().open(filename), null, options);
		} catch (IOException e)
		{
			e.printStackTrace();
			this.imageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowflake, options);
		}
		this.sinus_multi = Math.random() * 0.8;

		double real_size = (Math.random() * range + min_bitmap_size);

		this.size = (int) (real_size * density);
		this.destroyed = false;

		editWithSize(real_size);

		// Log.d("LiveWallPaperMyPoint", "" + density + " " + speed);
	}

	protected void editWithSize(double real_size)
	{
		double multiply = (real_size - min_bitmap_size) / range;
		imageBitmap = bluredScaledBitmap(imageBitmap, Math.pow(multiply, 5));
		Bitmap bt = Bitmap.createScaledBitmap(imageBitmap, size, size, true);
		imageBitmap.recycle();
		imageBitmap = bt;
		speed *= 1 - (multiply * 0.5);
	}

	protected Bitmap bluredScaledBitmap(Bitmap btm, double intensity)
	{

		paint = new Paint();
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		int diapazone = 100;
		paint.setAlpha((int) ((1 - intensity) * diapazone) + 255 - diapazone);

		btm = blurBitmap(btm, intensity);
		return btm;
	}

	private Bitmap blurBitmap(Bitmap btm2, double intensity)
	{
		// int btm_w = btm.getWidth();
		// int btm_h = btm.getHeight();
		// btm = Bitmap.createScaledBitmap(btm, (int) Math.round(btm_w *
		// Math.sin((1 - intensity))),
		// (int) Math.round(btm_h * Math.sin((1 - intensity))), false);
		// btm = Bitmap.createScaledBitmap(btm, (int) (size), (int) (size),
		// true);
		return btm2;
		// return btm;
	}

	public static Bitmap applyGaussianBlur(Bitmap src)
	{
		double[][] GaussianBlurConfig = new double[][] { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(GaussianBlurConfig);
		convMatrix.Factor = 3;
		convMatrix.Offset = 0;
		return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
	}

	static String intToString(int num, int digits)
	{
		StringBuffer s = new StringBuffer(digits);
		int zeroes = digits - (int) (Math.log(num) / Math.log(10)) - 1;
		for (int i = 0; i < zeroes; i++)
		{
			s.append(0);
		}
		return s.append(num).toString();
	}

	Bitmap resizedBitmap = null;

	public void draw(Canvas c)
	{
		if (resizedBitmap == null || resizedBitmap.isRecycled())
			c.drawBitmap(imageBitmap, drawable_x, drawable_y, paint);
		else
			c.drawBitmap(resizedBitmap, drawable_x, drawable_y, paint);
	}

	// protected float getX()
	// {
	// return Math.round(axe_x + FloatMath.sin((y + offset_y) / (20 * density))
	// * sinus_multi);
	// }

	private static double angle = 180;
	private static double cos_angle = Math.cos(Math.toRadians(angle));
	private static double sin_angle = Math.sin(Math.toRadians(angle));

	public static void setAngle(double _angle)
	{
		angle = _angle;
		cos_angle = Math.cos(Math.toRadians(angle));
		sin_angle = Math.sin(Math.toRadians(angle));
	}

	public synchronized void move()
	{
		double x_speed;
		double y_speed;
		if (windy == WINDY)
		{
			x_speed = -speed * 6 * Math.sqrt(sinus_multi);
			y_speed = (-(x_speed * sin_angle) - (speed * 4 * cos_angle));
			x_speed = x_speed * cos_angle - speed * 4 * sin_angle;

		} else if (windy == SMOOTH)
		{
			x_speed = FloatMath.sin((y + offset_y) / (30 * density)) * sinus_multi;
			y_speed = (-(x_speed * sin_angle) - (speed * cos_angle));
			x_speed = x_speed * cos_angle - speed * sin_angle;
		} else
		{
			x_speed = FloatMath.sin((y + offset_y) / (30 * density)) * sinus_multi;
			y_speed = (-(x_speed * sin_angle) - (-speed * cos_angle));
			x_speed = x_speed * cos_angle - (-speed) * sin_angle;
		}
		// dxn = dx * cos fi - dy * sin fi;
		// dyn = dx * sin fi + dy * cos fi;
		drawable_x += x_speed;
		drawable_y += y_speed;
		y += speed;
		x += 0;
		if (drawable_x > max_x + 50)
			drawable_x = -10;
		else if (drawable_x < -50)
			drawable_x = max_x + 50;
		if (drawable_y < -50)
			drawable_y = max_y - 1;
//		bitmap_rotating += 0.9;
//		bitmap_rotating = bitmap_rotating % 360;
	}

//	int rotator = 1;
//	float rotateAngle = 0;
//
//	public void rotateImageTask()
//	{
//		AsyncTask task = new AsyncTask()
//		{
//			@Override
//			protected Object doInBackground(Object... arg0)
//			{
//				rotateImage();
//				return null;
//			}
//		};
//		task.execute();
//	}
//
//	static long rotations_complete = 0;
//	public static final boolean need_to_rotate=false;
//	public void rotateImage()
//	{
//		if (need_to_rotate&&size > ((max_bitmap_size - min_bitmap_size) / 3 + min_bitmap_size))
//		{
//			Bitmap bitmapOrg = imageBitmap;
//			int width = bitmapOrg.getWidth();
//			int height = bitmapOrg.getHeight();
//			Matrix matrix = new Matrix();
//			matrix.postRotate(rotateAngle, width / 2, height / 2);
//			rotateAngle += 0.5;
//			Bitmap bitmapWip = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
//			// if (resizedBitmap != null)
//			// synchronized (resizedBitmap)
//			// {
//			// resizedBitmap.recycle();
//			// resizedBitmap = bitmapWip;
//			// }
//			resizedBitmap = bitmapWip;
//			rotator = 0;
//			rotations_complete++;
//		}
//		// Log.d("Rotations_Complete", Long.toString(rotations_complete));
//	}

	public void destroy()
	{
		destroyed = true;
	}

	public boolean isDestroyed()
	{
		return destroyed;
	}
}
