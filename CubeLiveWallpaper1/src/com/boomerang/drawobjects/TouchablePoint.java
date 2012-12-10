package com.boomerang.drawobjects;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;

public class TouchablePoint extends CreatedPoint
{
	int i;
	Context context;
	
	public TouchablePoint(float x, float y, Paint paint, Context context)
	{
		super(x, y, paint, context);
		this.context=context;
	}

	public boolean isHitted(float _x, float _y)
	{
		double range = Math.sqrt(Math.pow((drawable_x - _x), 2) + Math.pow((drawable_y - _y), 2));
		return range < size;
	}

	public void touch()
	{
		destroy();
	}


}
