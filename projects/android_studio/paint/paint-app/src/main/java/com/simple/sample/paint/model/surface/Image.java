package com.simple.sample.paint.model.surface;

import java.util.ArrayList;
import java.util.HashMap;

public class Image 
{
	ArrayList<String> _layerNames;
	HashMap<String, PaintingLayer> _paintLayers;
	public static final String DEFAULT_LAYER_NAME = "default";
	
	
	/**
	 * Call this constructor when the Image is created from serialization
	 */
	public Image()
	{
		this(false);
	}
	
	/**
	 * Call this constructor with true when creating a brand new Image that will be filled
	 * incrementally
	 * @param createDefaultLayer
	 */
	public Image(boolean createDefaultLayer)
	{
		_paintLayers = new HashMap<String, PaintingLayer>();
		_layerNames = new ArrayList<String>();
		if (createDefaultLayer)
		{
			addNewLayer(DEFAULT_LAYER_NAME);
			
		}
	}
	
	public PaintingLayer addNewLayer(String name)
	{
		PaintingLayer layer = new PaintingLayer(name);
		_paintLayers.put(name, layer);
		_layerNames.add(name);
		return layer;
	}
	
	public ArrayList<BrushPoint> deleteBrushPoints(int top, int left, int bottom, int right, ArrayList<String> layers)
	{
		PaintingLayer layer;
		ArrayList<BrushPoint> out = new ArrayList<BrushPoint>();
		for (String layerName: layers)
		{
			layer = _paintLayers.get(layerName);
			out.addAll(layer.deleteBrushPoints(top, left, bottom, right));
		}
		return out;
	}
	
	public ArrayList<String> getAllLayerNames()
	{
		return _layerNames;
	}
	
	public void deleteLayer(String name)
	{
		if (!DEFAULT_LAYER_NAME.equals(name))
		{
			_layerNames.remove(name);
			_paintLayers.remove(name);
		}
	}
	
	public ArrayList<BrushPoint> getBrushPoints(int top, int left, int bottom, int right, ArrayList<String> layers)
	{
		PaintingLayer layer;
		ArrayList<BrushPoint> out = new ArrayList<BrushPoint>();
		for (String layerName: layers)
		{
			layer = _paintLayers.get(layerName);
			out.addAll(layer.getBrushPoints(top, left, bottom, right));
		}
		return out;
	}
	
	public ArrayList<BrushPoint> getBrushPoints(ArrayList<String> layers)
	{
		PaintingLayer layer;
		ArrayList<BrushPoint> out = new ArrayList<BrushPoint>();
		for (String layerName: layers)
		{
			layer = _paintLayers.get(layerName);
			out.addAll(layer.getAllPoints());
		}
		return out;
	}
	
	public PaintingLayer getLayer(String layerName)
	{
		return _paintLayers.get(layerName);
	}
}
