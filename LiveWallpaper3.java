package com.nerdiful.wallpaper.coloredsquares;

import android.view.SurfaceHolder;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class LiveWallpaper3 extends AnimationWallpaper
{
	protected static final String SHARED_PREFS= "coloredsquares_settings";
	
	@Override
	public Engine onCreateEngine()
	{
		return new WallpaperEngine();
	}
	
	class WallpaperEngine extends AnimationEngine implements
		SharedPreferences.OnSharedPreferenceChangeListener
	{
		//Preferences
		private static final String KEY_PREF_SIDES = "square_size";
		private static final String KEY_PREF_SPEED = "update_speed";
		private static final String KEY_PREF_COLOR = "color_range";
		private static final String KEY_PREF_SAT = "saturation";
		
		private Paint paint = new Paint();
		
		private int squareSide = 64;						//Default square size and gap in between.  Updated later based on square_size preference
		private int squareGap = 10;
		
		private int width = 720, height = 1080;				//Default size of device screen
		
		private int numSquaresW, numSquaresH;				//Ints holding the number of squares to be draw and size of vertical/horizontal margins
		private int marginW, marginH;
		
		private WallpaperSquare[][] squares;				//Array holding the squares
		
		
		protected SharedPreferences preferences;			//Holds the preferences set by the user
		
		WallpaperEngine()
		{
			//Makes sure preview wallpaper is using preferences, not default values
			preferences = LiveWallpaper3.this.getSharedPreferences(SHARED_PREFS, 0);
            preferences.registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onCreate(SurfaceHolder holder)
		{
			super.onCreate(holder);
		}
		
		@Override
		//Assign new width and height and calc new squares assigning preference settings
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			this.width = width;
			this.height = height;
			
			calcSquares();
			initSquares();
			
			updateSquares(preferences.getString(KEY_PREF_SIDES, "64"), KEY_PREF_SIDES);
			updateSquares(preferences.getString(KEY_PREF_SPEED, "2"), KEY_PREF_SPEED);
			
			super.onSurfaceChanged(holder, format, width, height);
		}
		
		@Override
		//Set string = to the preference value that was changed
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
		{
			String pref = null;
			
			if(key.equals(KEY_PREF_SIDES))
				pref = prefs.getString(key, "64");
			else if(key.equals(KEY_PREF_SPEED))
				pref = prefs.getString(key, "2");
			else if(key.equals(KEY_PREF_COLOR))
				pref = prefs.getString(key, "0");
			else if(key.equals(KEY_PREF_SAT))
				pref = prefs.getString(key, "75 100");
			
			if(pref != null)
				updateSquares(pref, key);
		}
		
		//Update squares based on what preference was changed
		private void updateSquares(String pref, String key)
		{
			//Recalculate squares when size pref is changed
			//Color and Saturation prefs remain the same but must be reassigned
			if(key.equals(KEY_PREF_SIDES))
			{
				squareSide = Integer.valueOf(pref);
				squareGap = (int) ((10.0 / 64.0) * squareSide);
				
				calcSquares();
				initSquares();
				
				//Get color pref and assign to recalculated squares
				key = KEY_PREF_COLOR;
				pref = preferences.getString(key, "0");
				updateColorAndSat(pref, key);
				
				//Get sat pref and assign to recalculated squares
				key = KEY_PREF_SAT;
				pref = preferences.getString(key, "75 100");
				updateColorAndSat(pref, key);
			}
			
			//Update run() call back delay through super class method
			else if(key.equals(KEY_PREF_SPEED))
				super.updateSpeed(Integer.valueOf(pref));
			
			//Color and Sat updated in one method for modularity with updating square_sides pref
			else if(key.equals(KEY_PREF_COLOR) || key.equals(KEY_PREF_SAT))
				updateColorAndSat(pref, key);
		}
		
		//Update the square Color ranges and Sat
		private void updateColorAndSat(String pref, String key)
		{
			//Only updates the four corner squares color range
			//Extracts int (0,1,2) from pref which is passed to each square
			if(key.equals(KEY_PREF_COLOR))
			{
				squares[0][0].updateColorRange(Integer.valueOf(pref));
				squares[numSquaresW - 1][0].updateColorRange(Integer.valueOf(pref));
				squares[0][numSquaresH - 1].updateColorRange(Integer.valueOf(pref));
				squares[numSquaresW - 1][numSquaresH - 1].updateColorRange(Integer.valueOf(pref));
			}
			
			//Updates all squares saturation/value
			//Extracts int[sat,value] from pref which is passed to each square
			else if(key.equals(KEY_PREF_SAT))
			{				
				String[] saturation = pref.split(" ");
				float[] values = new float[saturation.length];
				for(int i=0; i<values.length; i++)
					values[i] = Float.parseFloat(saturation[i]);
				
				synchronized(squares)
				{
					for(int i=0; i<numSquaresW; i++)
					{
						for(int j=0; j<numSquaresH; j++)
							squares[i][j].updateSaturation(values);
					}
				}
			}
		}
		
		//Calcs how many squares to draw on screen and initializes squares[][] accordingly
		private void calcSquares()
		{
			//Determines how many squares will fit w/o margins or gaps
			numSquaresW = width/squareSide;
			numSquaresH = height/squareSide;
			
			//Decrements square count to account for gaps and margins as gap sized minimum
			while(width % (numSquaresW * squareSide) < (numSquaresW + 1) * squareGap)
				numSquaresW--;
			while(height % (numSquaresH * squareSide) < (numSquaresH + 1) * squareGap)
				numSquaresH--;
			
			//Divides extra space left over after squares and gaps by to two to calc margin size
			marginW = (width - (numSquaresW * squareSide) - ((numSquaresW - 1) * squareGap)) / 2;
			marginH = (height - (numSquaresH * squareSide) - ((numSquaresH - 1) * squareGap)) / 2;
			
			//initializes squares[][] based on calcs
			squares = new WallpaperSquare[numSquaresW][numSquaresH];
		}
		
		//Initializes squares based on calcSquares()
		private void initSquares()
		{
			synchronized(squares)
			{
				for(int i=0; i<numSquaresW; i++)
				{
					for(int j=0; j<numSquaresH; j++)
					{
						//Screen location determined
						int xLoc = marginW + (squareSide + squareGap) * i;
						int yLoc = marginH + (squareSide + squareGap) * j;
						
						//Squares initialized, passed screen location.
						squares[i][j] = new WallpaperSquare(xLoc, yLoc);
						//Square hue weight calc.  Passed square[][] location and square[][] w/h
						squares[i][j].calcWeights(i, j, numSquaresW, numSquaresH);
					}
				}
			}
		}
		
		//Returns a specific square from squares
		public WallpaperSquare getSquare(int w, int h)
		{
			return squares[w][h];
		}
		
		@Override
		//Grabs and locks canvas which is passed to paint(). Canvas is unlocked
		protected void draw()
		{
			SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try
			{
				c = holder.lockCanvas();
				if(c != null)
					paint(c);
			}
			finally
			{
				if(c != null && holder != null)
					holder.unlockCanvasAndPost(c);
			}
		}
		
		@Override
		//Updates the squares in order of importance
		protected void iter()
		{
			synchronized(squares)
			{
				//Update Corners first
				squares[0][0].update(squares);
				squares[numSquaresW - 1][0].update(squares);
				squares[0][numSquaresH - 1].update(squares);
				squares[numSquaresW - 1][numSquaresH - 1].update(squares);
				
				//Update top and bottom rows
				for(int j=0; j<numSquaresH; j+=(numSquaresH-1))
				{
					for(int i=0; i<numSquaresW; i++)
					{
						squares[i][j].update(squares);
					}
				}
				
				//Update the center
				for(int i=0; i<numSquaresW; i++)
				{
					for(int j=1; j<numSquaresH-1; j++)
					{
						squares[i][j].update(squares);
					}
				}
			}
			
			super.iter();
		}
		
		//Draws the background and all of the squares based on the color given
		void paint(Canvas c)
		{
			c.save();
			//Background black is drawn
			c.drawColor(0xff000000);
			
			synchronized(squares)
			{
				for(int i=0; i<numSquaresW; i++)
				{
					for(int j=0; j<numSquaresH; j++)
					{
						//HSV color format converted to color int
						WallpaperSquare square = squares[i][j];
						int color = Color.HSVToColor(square.getColor());
						//square screen loc retrieved
						float x = square.getX(); float y = square.getY();
						
						//Square color set and drawn based on screen loc and square_size pref
						paint.setAntiAlias(true);
						paint.setColor(Color.argb(WallpaperSquare.ALPHA, Color.red(color), Color.green(color), Color.blue(color)));
						paint.setStyle(Paint.Style.FILL_AND_STROKE);
						c.drawRect(x, y, x + squareSide, y + squareSide, paint);
						
						//Square outline drawn with modified color
						paint.setColor(Color.argb(WallpaperSquare.ALPHA, 63 + 3 * Color.red(color) / 4, 63 + 3 * Color.green(color) / 4, 63 + 3 * Color.blue(color) / 4));
						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth((int) ((squareSide / 64.0) * 3.0f));
						c.drawRect(x, y, x + squareSide, y + squareSide, paint);
					}
				}
			}
			
			c.restore();
		}
	}
}
