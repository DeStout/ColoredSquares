package com.nerdiful.wallpaper.coloredsquares;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class WallpaperSettings extends Activity
{
	final public static String EXTRA_PREVIEW_MODE = "android.service.wallpaper.PREVIEW_MODE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new WallpaperFragment()).commit();
		PreferenceManager.setDefaultValues(this, R.xml.livewallpaper3_settings, false);
	}
	
	public static class WallpaperFragment extends PreferenceFragment implements
		SharedPreferences.OnSharedPreferenceChangeListener
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			
			getPreferenceManager().setSharedPreferencesName(LiveWallpaper3.SHARED_PREFS);
			addPreferencesFromResource(R.xml.livewallpaper3_settings);
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}
		
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {}
	}
}
