package com.simple.sample.paint.model.surface;

import com.sample.simple.paint.util.GeneralizedPoint;

public class BrushPoint implements GeneralizedPoint {
	private int _x;
	private int _y;
	private int _diameter;
	private int _color;
	
	double[] _coord;
	
	static int _pointIndex = 0;
	
	final int _orderIndex;
	public BrushPoint(int x, int y, int diameter, int color)
	{
		_x = x;
		_y = y;
		_diameter = diameter;
		_color = color;
		_coord = new double[]{_x, _y};
		_orderIndex = _pointIndex++;
	}
	
	public static void resetBrushIndex()
	{
		_pointIndex+=2;
	}
	
	public int getOrderIndex()
	{
		return _orderIndex;
	}
	
	public int x()
	{
		return _x;
	}
	
	public int y()
	{
		return _y;
	}
	
	public int diameter()
	{
		return _diameter;
	}
	
	public int color()
	{
		return _color;
	}

	@Override
	public double[] getCoordinates() {
		return _coord;
	}
	
	
	
}
