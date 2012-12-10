package com.boomerang.drawobjects;

import java.util.Currency;

import kz.crystalspring.livewallpaper.CSWallpaper;
import kz.crystalspring.livewallpaper.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Santa extends InteractiveObject
{
	Bitmap santa;
	Bitmap santa2;
	Context context;
	Paint paint;
	int pictureNumber;
	int[] pictures = { R.drawable.santa_1, R.drawable.santa_2, R.drawable.santa_3, R.drawable.santa_4, R.drawable.santa_5,
			R.drawable.santa_6, R.drawable.santa_7 };
	SharedPreferences prefs;
	private static final String PREF_NAME = "CURR_PIC_NUMBER";

	public Santa(Paint paint, Context context)
	{
		this.paint = paint;
		this.context = context;
		pictureNumber = 0;
		prefs = context.getSharedPreferences(CSWallpaper.SHARED_PREFS_NAME + "1", 0);
		int sPictureNumber = prefs.getInt(PREF_NAME, 0);
		pictureNumber = sPictureNumber;
	}

	@Override
	protected boolean isHitted(float _x, float _y)
	{
		_x -= glWidth / 2;
		_y -= glHeight / 2;
		if (santa != null)
		{
			Log.d("Frosty_head", "Touch test");
			if ((_x >= getX() && _x <= getX() + santa.getWidth() / 1.5) && (_y >= getY() && _y <= getY() + santa.getHeight()))
				return true;
			else
				return false;
		} else
			return false;
	}

	private float getX()
	{
		return x;
	}

	private float getY()
	{
		return y;
	}

	boolean touchable = true;

	@Override
	protected void touch()
	{
		if (touchable)
		{
			Log.d("santa", "touch()");
			touchable = false;
			if (santa2 != null)
			{
				pictureNumber = getNextNumber();
				stepBitmap();
			}
			Log.d("Santa", "picture_number=" + pictureNumber + " next_number=" + getNextNumber());
			Handler handler = new Handler();
			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					touchable = true;
				}
			}, 2);
		}
	}

	private int getNextNumber()
	{
		int currNumber = pictureNumber + 1;
		if (currNumber >= pictures.length)
			currNumber = 0;
		return currNumber;
	}

	private Bitmap getBitmap(int number)
	{
		if (number >= pictures.length)
			number = pictures.length - 1;
		Bitmap bigSanta = BitmapFactory.decodeResource(context.getResources(), pictures[number]);
		int btmHeight = bigSanta.getHeight();
		int btmWidth = bigSanta.getWidth();
		float k = (float) ((float) btmHeight / (glHeight / 2.5));
		Bitmap smallsanta = Bitmap.createScaledBitmap(bigSanta, Math.round(btmWidth / k), Math.round(btmHeight / k), true);
		bigSanta.recycle();
		return smallsanta;
	}

	int x;
	int y;

	@Override
	protected void draw(Canvas c, float localOffset)
	{
		x = (int) (-glWidth / 2 - (localOffset * glWidth));
		y = (int) (glHeight / 2 - santa.getHeight() * 1.1);
		c.drawBitmap(santa, x, y, paint);
	}

	int glWidth;
	int glHeight;

	@Override
	protected void surfaceChanged(int width, int height)
	{
		Log.d("santa", "surfaceChanged");
		glWidth = width;
		glHeight = height;
		refreshBitmap();
	}

	private void refreshBitmap()
	{
		Log.d("santa", "refresh");
		if (santa != null)
			santa.recycle();
		if (santa2 != null)
			santa2.recycle();
		santa = getBitmap(pictureNumber);
		santa2 = getBitmap(getNextNumber());
	}

	private void stepBitmap()
	{
		Log.d("santa", "stepBitmap");
		if (santa != null)
			santa.recycle();
		santa = santa2;
		santa2 = null;
		AsyncTask task = new AsyncTask()
		{

			@Override
			protected Object doInBackground(Object... params)
			{
				Log.d("santa", "stepBitmap_Async");
				Bitmap btm = getBitmap(getNextNumber());
				Editor editor = prefs.edit();
				editor.putInt(PREF_NAME, pictureNumber);
				editor.commit();
				santa2 = btm;
				return null;
			}
		};
		task.execute();
	}

}
