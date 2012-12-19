package com.boomerang.drawobjects;

import kz.crystalspring.livewallpaper.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;

public class AppPoint extends TouchablePoint
{
	static int[] apps_icons = { R.drawable.logo_app, R.drawable.logo_pp, R.drawable.logo_jam, R.drawable.logo_grimble, R.drawable.logo_ps,
			R.drawable.logo_pm, R.drawable.logo_azbuka };
	static String[] apps_names = { "kz.crystalspring.appstan", "kz.crystalspring.pointplus", "kz.com.pack.jam", "kz.crystalspring.nine",
			"kz.crystalspring.pit_stop_kz","kz.pm.informer","http://www.azbuka.kz" };

	public AppPoint(float x, float y, Paint paint, Context context)
	{
		super(x, y, paint, context);
		size = (int) (60 * density);
		imageBitmap = getRandomPick(context);
		speed *= 0.5;
		sinus_multi = 0;
		this.paint = paint;
	}

	@Override
	protected void editWithSize(double real_size)
	{
		// super.editWithSize(real_size);
	}

	private Bitmap getRandomPick(Context context)
	{
		i = (int) Math.round((Math.random() * (apps_icons.length - 1)));
		Bitmap btm = BitmapFactory.decodeResource(context.getResources(), apps_icons[i]);
		Bitmap bt = Bitmap.createScaledBitmap(btm, size, size, true);
		btm.recycle();
		return bt;
	}

	@Override
	public void touch()
	{
		openMarket();
	}

	private void openMarket()
	{
		Uri uri;
		if (i <= 5)
			uri = Uri.parse("market://details?id=" + apps_names[i]);
		else
			uri = Uri.parse(apps_names[i]);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
