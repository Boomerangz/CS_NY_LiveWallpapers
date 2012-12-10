package com.boomerang.drawobjects;

import android.graphics.Canvas;
import android.util.Log;

public abstract class InteractiveObject
{
	public final static int ACTIVE=1;
	public final static int INACTIVE=2;
	public final static int INVISIBLE=3;
	public static int MODE=INVISIBLE;
	
	protected abstract boolean isHitted(float _x, float _y);
	protected abstract void touch();
	protected abstract void draw(Canvas c, float localOffset);
	protected abstract void surfaceChanged(int width, int height);
	
	public boolean publIsHitted(float _x, float _y)
	{
		if (MODE==ACTIVE)
			return isHitted(_x, _y);
		else
			return false;
	}
	
	public void publTouch()
	{
		if (MODE==ACTIVE)
			touch();
	}
	
	public void publDraw(Canvas c, float localOffset)
	{
		if (MODE!=INVISIBLE)
		{
			draw(c, localOffset);
		}
	}
	
	public void publSurfaceChanged(int width, int height)
	{
		if (MODE!=INVISIBLE)
		{
			Log.d("santa","publSurfaceChanged");
			surfaceChanged(width, height);
		}
	}
	
}
