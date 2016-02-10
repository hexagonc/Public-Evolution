package com.simple.sample.paint.model.surface;

import java.util.ArrayList;

import com.sample.simple.paint.util.KDTree;

public class PaintingLayer 
{
	KDTree<BrushPoint> _pointTree;
	String _name;
	public PaintingLayer(String name)
	{
		_name = name;
		_pointTree = new KDTree<BrushPoint>();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void addPoint(BrushPoint point)
	{
		_pointTree.addValue(point);
	}
	
	public void addAllPoints(ArrayList<BrushPoint> pointList)
	{
		_pointTree.addAllPoints(pointList);
	}
	
	public void clear()
	{
		_pointTree.deleteAllPoints();
	}
	
	public ArrayList<BrushPoint>  deleteBrushPoints(int top, int left, int bottom, int right)
	{
		double[] maxCoords = new double[]{right, bottom}, minCoords = new double[]{left, top};
		return _pointTree.deleteRange(maxCoords, minCoords);
		
	}
	
	public void deleteBrushPoints(ArrayList<BrushPoint> points)
	{
		for (BrushPoint point:points)
		{
			_pointTree.deletePoint(point.getCoordinates());
		}
	}
	
	public ArrayList<BrushPoint> getAllPoints()
	{
		return _pointTree.getAllPoints();
	}
	
	public ArrayList<BrushPoint> getBrushPoints(int top, int left, int bottom, int right)
	{
		double[] maxCoords = new double[]{right, bottom}, minCoords = new double[]{left, top};
		return _pointTree.getRange(maxCoords, minCoords);
	}
	
	
}
