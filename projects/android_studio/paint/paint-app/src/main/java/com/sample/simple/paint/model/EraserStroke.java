package com.sample.simple.paint.model;

import java.util.ArrayList;

import com.simple.sample.paint.model.surface.BrushPoint;
import com.simple.sample.paint.model.surface.Image;
import com.simple.sample.paint.model.surface.PaintingLayer;

public class EraserStroke implements Stroke {
	ImageSettings _settings;
	ArrayList<BrushPoint> _points;
	String _strokeLayerName;
	
	public EraserStroke(ImageSettings settings)
	{
		_settings = settings;
		_points = new ArrayList<BrushPoint>();
	}
	
	public void addBrushPoints(ArrayList<BrushPoint> points, String layer)
	{
		_points.addAll(points);
		_strokeLayerName = layer;
	}
	
	public void undo()
	{
		Image image = _settings.getImage();
		PaintingLayer layer = image.getLayer(_strokeLayerName);
		if (layer != null)
		{
			
			layer.addAllPoints(_points);
		}
	}

}
