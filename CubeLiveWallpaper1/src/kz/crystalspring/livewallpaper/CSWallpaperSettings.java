/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package kz.crystalspring.livewallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class CSWallpaperSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(CSWallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.cube2_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		getListView().setBackgroundColor(getResources().getColor(R.color.background));
		// addContentView(getCrystalButton(), getCrystalParams());
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		Log.d("Ont Item Click", "" + preference.getKey());
		if (preference.getKey().equals("share"))
		{
			share();
			return true;
		} else if (preference.getKey().equals("crystal"))
		{
			return true;
		} else
			return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void openCrystalPage(View v)
	{
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.crystalspring.kz"));
		startActivity(browserIntent);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Log.d("Ont Item Click", "" + position);
	}

	private void share()
	{
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		// Add data to the intent, the receiving app will decide what to do with
		// it.
		intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_header));
		intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text));
		startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_invite)));
	}

	private LayoutParams getCrystalParams()
	{
		FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT);
		ll.topMargin = (int) (getWallpaperDesiredMinimumHeight() / 1.5);
		return ll;
	}

	private View getCrystalButton()
	{
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.crystal_button, null);
		View button = view.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//openCrystalPage();
			}
		});
		return view;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
	}
}
