package com.nerdiful.wallpaper.coloredsquares;

import android.content.SharedPreferences;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/*
 * Background Engine and Wallpaper service.
 * Structural credit to Google
 */
public abstract class AnimationWallpaper extends WallpaperService
{
	protected abstract class AnimationEngine extends Engine
		implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		private boolean visible;
		private Handler mHandler = new Handler();
		
		//Affects the delay until run() is called again.
		//User changes through preferences.
		private int updateSpeed = 2;
		
		private Runnable mIter = new Runnable()
		{
			public void run()
			{
				iter();
				draw();
			}
		};
		
		@Override
		public void onDestroy()
		{
			super.onDestroy();
			mHandler.removeCallbacks(mIter);
		}

		@Override
		//Continue drawing if visible, else remove all call backs.
		public void onVisibilityChanged(boolean vis)
		{
			visible = vis;
			if(visible)
			{
				iter();
				draw();
			}
			else
				mHandler.removeCallbacks(mIter);
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int w, int h)
		{
			iter();
			draw();
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder)
		{
			super.onSurfaceDestroyed(holder);
			visible = false;
			mHandler.removeCallbacks(mIter);
		}
		
		//Called when user changes the update_speed preference.
		//Adjusts the rate at which run() is called.
		void updateSpeed(int speed)
		{
			updateSpeed = speed;
		}
		
		protected abstract void draw();
		
		//Call the next update based on the update_speed assigned by the user.
		protected void iter()
		{
			mHandler.removeCallbacks(mIter);
			if(visible)
				mHandler.postDelayed(mIter, (int) (1000 / updateSpeed));
		}
	}
}
