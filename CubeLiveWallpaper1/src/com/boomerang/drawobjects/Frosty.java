package com.boomerang.drawobjects;

import kz.crystalspring.livewallpaper.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Frosty extends InteractiveObject
{
	Context context;
	Paint paint;
	Bitmap frosty;
	Bitmap frostyHead;
	Integer touch_number = 0;
	Matrix matrix;

	public Frosty(Paint paint, Context context)
	{
		this.context = context;
		this.paint = paint;
	}

	int glWidth;
	int glHeight;
	int size;

	protected void surfaceChanged(int width, int height)
	{
		glWidth = width;
		glHeight = height;
		if (frosty != null)
			frosty.recycle();
		Bitmap bigFrosty = BitmapFactory.decodeResource(context.getResources(), R.drawable.frosty);
		Bitmap bigFrostyHead = BitmapFactory.decodeResource(context.getResources(), R.drawable.frosty_head);
		int btmHeight = bigFrosty.getHeight();
		int btmWidth = bigFrosty.getWidth();
		int btmHeight_head = bigFrostyHead.getHeight();
		int btmWidth_head = bigFrostyHead.getWidth();
		size = btmWidth_head;
		float k = (float) 1080 / (float) height;
		Bitmap smallfrosty = Bitmap.createScaledBitmap(bigFrosty, Math.round(btmWidth / k), Math.round(btmHeight / k), true);
		Bitmap smallfrostyhead = Bitmap.createScaledBitmap(bigFrostyHead, Math.round(btmWidth_head / k), Math.round(btmHeight_head / k),
				true);
		bigFrosty.recycle();
		bigFrostyHead.recycle();
		frosty = smallfrosty;
		frostyHead = smallfrostyhead;
	}

	int x_head;
	int y_head;
	float xxx;
	private float y_offset = 0;
	protected void draw(Canvas c, float localOffset)
	{
		if (frosty != null && !frosty.isRecycled())
		{
			int x = (int) (-glWidth / 2 - (localOffset * glWidth));
			int y = (int) (glHeight / 2 - frosty.getHeight() * 1.7);
			c.drawBitmap(frosty, x, y, paint);
			x_head = (int) (-glWidth / 2) + frosty.getWidth() / 2 - frostyHead.getWidth() / 2;
			y_head = (int) (y - frostyHead.getHeight() * 0.8);
			if (matrix == null)
			{
				matrix = new Matrix();
				matrix.setTranslate(this.x_head, this.y_head);
			}
			float[] values = new float[9];
			matrix.getValues(values);
			xxx = values[2];
			float yyy = values[5];
			values[2] = values[2] - (localOffset * glWidth);
			values[5] = values[5] - y_offset;
			// Log.d("Frosty_head", "x=" + values[2] + " y=" + values[5]);
			matrix.setValues(values);
			c.drawBitmap(frostyHead, matrix, paint);
			values[2] = xxx;
			values[5] = yyy;
			xxx = values[2] - (localOffset * glWidth);
			matrix.setValues(values);
			if (Math.abs(localOffset * glWidth) > frosty.getWidth() * 1.5)
				refreshFrosty();
		}
	}

	private void refreshFrosty()
	{
		y_offset = 0;
		angle = 0;
		matrix = null;
		falling = false;
	}

	@Override
	protected boolean isHitted(float _x, float _y)
	{
		_x -= glWidth / 2;
		_y -= glHeight / 2;
		if (frostyHead != null)
		{
			Log.d("Frosty_head", "Touch test");
			double range = Math.sqrt(Math.pow((getHeadX() + frostyHead.getWidth() / 2 - _x), 2)
					+ Math.pow((getHeadY() + frostyHead.getHeight() / 3 - _y), 2));
			boolean hitted = range < size;
			Log.d("Frosty_head", "x_head=" + x_head + " y_head=" + y_head + " x_=" + _x + " y_=" + _y);
			if (hitted)
				Log.d("Frosty_head", "Touch test passed, Range=" + range);
			return hitted;
		} else
			return false;
	}

	private float getHeadY()
	{
		if (matrix != null)
		{
			float[] values = new float[9];
			matrix.getValues(values);
			return values[5] + frostyHead.getHeight();
		} else
			return -100;
	}

	private float getHeadX()
	{
		if (matrix != null)
		{
			float[] values = new float[9];
			matrix.getValues(values);
			return xxx - size / 2;
		} else
			return -100;
	}

	protected void touch()
	{
		Log.d("Frosty_head", "Touch action");
		if (touch_number == 0)
		{
			handler.removeCallbacks(firstTouch);
			handler.postDelayed(firstTouch, 100);
		}
	}

	Handler handler = new Handler();
	Runnable firstTouch = new Runnable()
	{
		@Override
		public void run()
		{
			firstTouch();
		}
	};
	int angle;

	protected void firstTouch()
	{
		touch_number = -1;
		angle += 27;
		if (angle < 80)
			rotateImage(frostyHead, angle);
		else
			fallImage();
		touch_number = 0;
	}

	private boolean falling = false;
	private Handler fall_handler = new Handler();
	private Runnable fall_runnable = new Runnable()
	{
		@Override
		public void run()
		{
			fall_more();
		}
	};

	private void fallImage()
	{
		if (!falling)
		{
			falling = true;
			fall_more();
		}
	}

	private void fall_more()
	{
		if (Math.abs(y_offset) < frosty.getHeight() * 0.8)
		{
			y_offset -= frosty.getHeight() * 0.8 / (600 / 30);
			handler.removeCallbacks(fall_runnable);
			handler.postDelayed(fall_runnable, 30);
		}
	}

	public void rotateImage(Bitmap inImage, float angle)
	{
		if (matrix != null)
		{
			this.matrix.reset();
			this.matrix.setTranslate(this.x_head, this.y_head);
			this.matrix.postRotate(angle, this.x_head + inImage.getWidth() / 2, this.y_head + inImage.getHeight() - 10);
		}
	}

}

class FrostyHead
{

}
