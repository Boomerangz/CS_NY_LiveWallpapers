package com.boomerang.drawobjects;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CreatedPoint extends MyPoint
{

	public CreatedPoint(float x, float y, Paint paint, Context context)
	{
		super(x, y, paint, context);
	}

	boolean upped = false;
	boolean dwned = false;

	@Override
	public void move()
	{
		super.move();
		if (drawable_y > max_y || drawable_y < -50)
			destroy();
		if (drawable_y < 3)
		{
			if (upped&&dwned)
				destroy();
			else
				upped = true;
		}
		else
		{
			dwned=true;
		}

	}
}
