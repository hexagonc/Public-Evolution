package com.sample.simple.paint.model;

import com.simple.sample.paint.model.surface.BrushPoint;

public class SurfaceTouch {
	
	SurfaceInteractionMode _interactionMode;
	int _size;
	int _x;
	int _y;
	
	
	public SurfaceTouch(SurfaceInteractionMode mode, int x, int y, int size)
	{
		
		_x = x;
		_y = y;
		_size = size;
		_interactionMode = mode;
	}
	
	
	
	public SurfaceInteractionMode getInteractionMode()
	{
		return _interactionMode;
	}
	
	public int x()
	{
		return _x;
	}
	
	public int y()
	{
		return _y;
	}
	
	public int size()
	{
		return _size;
	}
}
