package com.sample.simple.paint.model;

import java.util.ArrayList;

import com.simple.sample.paint.model.surface.Image;

public class ImageSettings {
	String _activeLayerName;
	String _fileName;
	Image _image;
	ArrayList<String> _visibleLayerNameList;

	public ImageSettings(Image img)
	{
		_image = img;
		_visibleLayerNameList = new ArrayList<String>();
		ArrayList<String> layers = _image.getAllLayerNames();
		if (layers!= null && layers.size()>0)
		{
			_activeLayerName = layers.get(0);
			_visibleLayerNameList.add(_activeLayerName);
		}
		
	}
	
	
	public Image getImage()
	{
		return _image;
	}
	public ArrayList<String> getVisibleLayers()
	{
		return _visibleLayerNameList;
	}
	

	public void setVisibleLayerNames(ArrayList<String> names)
	{
		_visibleLayerNameList = names;
	}
	
	public String getActiveLayerName()
	{
		return _activeLayerName;
	}
	
	public void setActiveLayerName(String name)
	{
		_activeLayerName = name;
	}
	
	public String getFileName()
	{
		return _fileName;
	}
	
	public void setFileName(String name)
	{
		_fileName = name;
	}
	
	
	
}
