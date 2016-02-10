package com.sample.simple.paint.model;

import java.util.HashMap;

import com.sample.simple.paint.events.BrushTipSelectedEvent;
import com.sample.simple.paint.events.EraserTipSelectedEvent;
import com.sample.simple.paint.events.EventManager;
import com.sample.simple.paint.events.SetBrushColorEvent;
import com.sample.simple.paint.events.SetBrushSizeEvent;
import com.simple.sample.paint.model.surface.Image;

public class PaintApp {

	
	HashMap<String, ImageSettings> _imageMap;
	ImageSettings _settings;
	String _currentImageName;
	PaintSurfaceConfiguration _paintConfiguration;
	ImageChangedListener _listener;
	
	
	public PaintApp()
	{
		_imageMap = new HashMap<String, ImageSettings>();
		_paintConfiguration = new PaintSurfaceConfiguration();
		configure();
	}
	
	
	public PaintApp(ImageSettings settings, SurfaceInteractionMode mode, int brushColor, int brushTipWidth, int eraserTipWidth)
	{
		_imageMap = new HashMap<String, ImageSettings>();
		_paintConfiguration = new PaintSurfaceConfiguration(mode, brushColor, brushTipWidth, eraserTipWidth );
		if (settings != null)
		{
			_imageMap.put(settings.getFileName(), settings);
		}
		configure();
	}
	
	private void configure()
	{
		EventManager.get().register(this);
	}
	
	
	public void setImageChangedListener(ImageChangedListener listener)
	{
		_listener = listener;
	}
	
	public void createNewImage(String name)
	{
		// TODO Redo this so that it works correctly when loading an Image from a file 
		Image image = new Image(true);
		
		// TODO: Use factory methods instead of new
		_settings = new ImageSettings(image);
		_settings.setActiveLayerName(Image.DEFAULT_LAYER_NAME);
		
		_settings.setFileName(name);
		
		_imageMap.put(name, _settings);
	}
	
	
	public void setCurrentImage(String name)
	{
		if (!_imageMap.containsKey(name))
			throw new IllegalArgumentException("No image name exists called '" + name + "'" );
		if (!name.equals(_currentImageName) && _listener != null)
		{
			_listener.onImageChanged(_imageMap.get(name));
		}
		_currentImageName = name;
	}
	
	public void deleteImage(String name)
	{
		if (name.equals(_currentImageName))
			_currentImageName = null;
		_imageMap.remove(name);
	}
	
	public ImageSettings getCurrentImageSettings()
	{
		return _imageMap.get(_currentImageName);
	}
	
	public PaintSurfaceConfiguration getPaintConfiguration()
	{
		return _paintConfiguration;
	}
	
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.
	//							Event Handlers
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.
	
	public void onEvent(BrushTipSelectedEvent brushEvent)
	{
		_paintConfiguration.setInteractionMode(SurfaceInteractionMode.BRUSH);
	}
	
	public void onEvent(EraserTipSelectedEvent eraserEvent)
	{
		_paintConfiguration.setInteractionMode(SurfaceInteractionMode.ERASER);
	}
	
	public void onEvent(SetBrushColorEvent brushColorEvent)
	{
		_paintConfiguration.setBrushColor(brushColorEvent.getBrushColor());
	}
	
	public void onEvent(SetBrushSizeEvent sizeEvent)
	{
		_paintConfiguration.setBrushStrokDiameter(sizeEvent.getBrushTipSize());
	}
	
}
