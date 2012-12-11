package kz.crystalspring.livewallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class CSSetWallpaperActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent i = new Intent();
		if (Build.VERSION.SDK_INT > 15)
		{
			i.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
			String p = CSWallpaper.class.getPackage().getName();
			String c = CSWallpaper.class.getCanonicalName();
			i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(p, c));
		} else
		{
			String invite=getResources().getString(R.string.invite);
			invite=invite.replace("1234", "\'"+getResources().getString(R.string.wallpapers)+"\'");
			Toast.makeText(getBaseContext(), invite, Toast.LENGTH_LONG).show();
			i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		}
		startActivityForResult(i, 0);
		finish();
	}
}
