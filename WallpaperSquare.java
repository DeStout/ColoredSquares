package com.nerdiful.wallpaper.coloredsquares;

public class WallpaperSquare
{
	//Constants determining square location/importance
	final static int CORNER = 0;
	final static int EDGE = 1;
	final static int CENTER = 2;
	
	//Constants determining color range to adhere to
	private final static int ALL = 0;
	private final static int HOT = 1;
	private final static int COLD = 2;
	
	//Default values for alpha, sat, and value
	final static int ALPHA = 255;
	private static final float S = 0.75f;
	private static final float V = 1.0f;
	
	//values for location, color, gradient weight, and color range/update direction
	private int x, y;
	private float[] color = new float[3];
	private int xPos, yPos, pos;
	private int squaresW, squaresH;
	private float weight;
	private int colorRange = 0;
	//Switches between pos and neg. Mult by change to hue to keep color within color_range.
	private int direction = 1;
	
	//Sets screen loc on init
	public WallpaperSquare(int x, int y)
	{
		this.x = x; this.y = y;
	}
	
	//Sets color weight based on screen loc, array loc, and array size.
	public void calcWeights(int xPos, int yPos, int squaresW, int squaresH)
	{
		//Square array loc and array size grabbed
		this.xPos = xPos;
		this.yPos = yPos;
		this.squaresW = squaresW;
		this.squaresH = squaresH;
		
		//Corners updated first
		//Gradient weight is always 1.  Random hue assigned.
		if((xPos == 0 && yPos == 0) || (xPos == squaresW - 1 && yPos == 0) || (xPos == 0 && yPos == squaresH - 1) || (xPos == squaresW - 1 && yPos == squaresH - 1))
		{
			pos = CORNER;
			color[0] = 360.0f * (float) Math.random();
			color[1] = S; color[2] = V;
		}
		//Top/bottom rows updated second
		//Gradient weight determined by distance along row
		else if(yPos == 0 || yPos == squaresH - 1)
		{
			pos = EDGE;
			weight = ((float) xPos + 1) / (float) squaresW;
			color[1] = S; color[2] = V;
		}
		//All center squares updated last
		//Gradient weight determined by distance along column
		else
		{
			pos = CENTER;
			weight = ((float) yPos + 1) / (float) squaresH;
			color[1] = S; color[2] = V;
		}
	}
	
	//Updates the squares hue attribute
	public void update(WallpaperSquare[][] squares)
	{
		//Corners updated first
		//Random value added or subtracted based on direction
		//Direction value assigned based on color_range pref
		if(pos == CORNER)
		{
			color[0] += direction * (Math.random() * 1.0f - 0.25f);
			
			if(colorRange == ALL)
			{
				if(color[0] > 360 || color[0] < 0)
					direction = -direction;
			}
			else if(colorRange == HOT)
			{
				if(color[0] > 75 || color[0] < 0)
					direction = -direction;
			}
			else if(colorRange == COLD)
			{
				if(color[0] > 260 || color[0] < 90)
					direction = -direction;
			}
		}
		//Top/bottom rows updated second
		//Corner squares hues mult by weight and added together to get square color
		else if(pos == EDGE)
		{
			WallpaperSquare lt = squares[0][yPos];
			WallpaperSquare rt = squares[squaresW - 1][yPos];
			
			if(lt != null && rt != null)
				color[0] = (lt.getColor()[0] * (1 - weight) + rt.getColor()[0] * weight);
		}
		//All center squares updated last
		//Top/Borrom squares hues mult by weight and added together to get square color
		else if(pos == CENTER)
		{
			WallpaperSquare top = squares[xPos][0];
			WallpaperSquare bot = squares[xPos][squaresH - 1];
			
			if(top != null && bot != null)
				color[0] = (top.getColor()[0] * (1 - weight) + bot.getColor()[0] * weight);
		}
	}
	
	//Updates square color/range based on color_range pref
	public void updateColorRange(int range)
	{
		colorRange = range;
		if(colorRange == HOT)
			color[0] = 75.0f * (float) Math.random();
		else if(colorRange == COLD)
			color[0] = 170.0f * (float) Math.random() + 90.0f;
	}
	
	//Updates all squares sat/value based on sat pref
	public void updateSaturation(float[] values)
	{
		color[1] = values[0];
		color[2] = values[1];
	}
	
	//Returns squares color[h,s,v]
	public float[] getColor()
	{
		return color;
	}
	
	//Returns squares loc in square[][]
	public int getPos()
	{
		return pos;
	}
	
	//Returns squares screen x loc
	public int getX()
	{
		return x;
	}
	
	
	//Returns squares screen y loc
	public int getY()
	{
		return y;
	}
}
