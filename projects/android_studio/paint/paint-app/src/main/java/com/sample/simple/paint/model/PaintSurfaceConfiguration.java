package com.sample.simple.paint.model;

import java.util.ArrayList;

import com.simple.sample.paint.model.surface.BrushPoint;

import android.graphics.Color;
import android.view.MotionEvent;

public class PaintSurfaceConfiguration {
	int _brushColor;
	int _brushStrokeDiameter;
	int _eraserDiameter;
	
	SurfaceInteractionMode  _interactionMode;
	
	public static final String DEFAULT_COLOR_NAME = "#000000";
	public static final int DEFAULT_BRUSH_TIP_WIDTH = 20;
	public static final int DEFAULT_ERASER_TIP_SIZE = 40;
	
	
	public PaintSurfaceConfiguration()
	{
		this(SurfaceInteractionMode.BRUSH, Color.parseColor(DEFAULT_COLOR_NAME), DEFAULT_BRUSH_TIP_WIDTH, DEFAULT_ERASER_TIP_SIZE);
		
	}
	
	public PaintSurfaceConfiguration(SurfaceInteractionMode mode, int brushColor, int brushTipWidth, int eraserTipWidth)
	{
		
		_brushStrokeDiameter = DEFAULT_BRUSH_TIP_WIDTH;
		_eraserDiameter = DEFAULT_ERASER_TIP_SIZE;
		_interactionMode = SurfaceInteractionMode.BRUSH;
		_brushColor = brushColor;
		_brushStrokeDiameter = brushTipWidth;
		_eraserDiameter = eraserTipWidth;
	}
	
	
	public int getBrushColor()
	{
		return _brushColor;
	}
	
	public void setBrushColor(int setBrushColor)
	{
		_brushColor = setBrushColor;
	}
	
	
	public int getBrushStrokDiameter()
	{
		return _brushStrokeDiameter;
	}
	
	public void setBrushStrokDiameter(int diameter)
	{
		_brushStrokeDiameter = diameter;
	}
	
	public int getEraserSize()
	{
		return _eraserDiameter;
	}
	
	public void setEraserSize(int size)
	{
		_eraserDiameter = size;
	}
	
	
	public SurfaceInteractionMode getInteractionMode()
	{
		return _interactionMode;
	}
	
	public void setInteractionMode(SurfaceInteractionMode mode)
	{
		_interactionMode = mode;
	}
	
	
	public ArrayList<SurfaceTouch> getSurfaceTouches(MotionEvent env)
	{
		// Use simple single point
		ArrayList<SurfaceTouch> touchPoints = new ArrayList<SurfaceTouch>();
		
		int x = (int)env.getX();
		int y = (int)env.getY();
		
		switch (_interactionMode)
		{
			case BRUSH:
			{
				SurfaceTouch touch = new SurfaceTouch(_interactionMode, x, y, _brushStrokeDiameter);
				touchPoints.add(touch);
				break;
			}
			case ERASER:
			{
				SurfaceTouch touch = new SurfaceTouch(_interactionMode, x, y, _eraserDiameter);
				touchPoints.add(touch);
				break;
			}
		}
		return touchPoints;
	}
}
