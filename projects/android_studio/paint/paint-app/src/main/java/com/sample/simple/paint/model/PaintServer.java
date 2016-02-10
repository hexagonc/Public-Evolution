package com.sample.simple.paint.model;

import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;

import com.sample.simple.paint.events.ClearImageEvent;
import com.sample.simple.paint.events.EventManager;
import com.simple.sample.paint.model.surface.BrushPoint;
import com.simple.sample.paint.model.surface.Image;
import com.simple.sample.paint.model.surface.PaintingLayer;

import de.greenrobot.event.EventBus;

public class PaintServer implements ImageChangedListener
{
	Thread _processThread;
	ImageSettings _currentImageSettings;
	ArrayList<SurfaceTouch> _touchQueue = null;
	Object _synch = new Object();
	long _processSleepMilli = 50;
	LinkedList<Stroke> _strokeList;
	PaintSurfaceConfiguration _strokeConfiguration;
	BrushPointsUpdatedListener _listener;
	
	public PaintServer()
	{
		_strokeList = new LinkedList<Stroke>();
		_touchQueue = new ArrayList<SurfaceTouch>();
		setup();
	}
	
	public PaintServer(ImageSettings settings)
	{
		_strokeList = new LinkedList<Stroke>();
		_touchQueue = new ArrayList<SurfaceTouch>();
		_currentImageSettings = settings;
		setup();
	}
	
	private void setup()
	{
		EventManager.get().register(this);
	}
	
	
	
	
	public void setImageSettings(ImageSettings settings)
	{
		synchronized (_synch)
		{
			_currentImageSettings = settings;
			_touchQueue.clear();
		}
		_strokeList.clear();
	}
	
	public void setBrushPointUpdatedListener(BrushPointsUpdatedListener listener)
	{
		_listener = listener;
	}
	
	public void beginStroke(PaintSurfaceConfiguration surfaceConfiguration)
	{
		BrushPoint.resetBrushIndex();
		if (_currentImageSettings == null)
			throw new IllegalStateException("Cannot begin stroke with null ImageSettings");
		Stroke stroke;
		_strokeConfiguration = surfaceConfiguration;
		SurfaceInteractionMode mode = _strokeConfiguration.getInteractionMode();
		switch (mode)
		{
			case ERASER:
				stroke = new EraserStroke(_currentImageSettings);
				_strokeList.add(stroke);
				break;
			case BRUSH:
				stroke = new BrushStroke(_currentImageSettings);
				_strokeList.add(stroke);
				break;
		}
	}
	
	public void endStroke()
	{
		// Doing cleanup.  May handle this differently
		// TODO: add undo capability
	}
	
	public void addSurfaceTouches(ArrayList<SurfaceTouch> touchList)
	{
		synchronized (_synch)
		{
			_touchQueue.addAll(touchList);
		}
	}
	
	public ArrayList<BrushPoint> getPoints(int top, int left, int bottom, int right)
	{
		ArrayList<String> visibleLayers = _currentImageSettings.getVisibleLayers();
		Image image = _currentImageSettings.getImage();
		return image.getBrushPoints(top, left, bottom, right, visibleLayers);
	}
	
	public ArrayList<BrushPoint> getPoints()
	{
		ArrayList<String> visibleLayers = _currentImageSettings.getVisibleLayers();
		Image image = _currentImageSettings.getImage();
		return image.getBrushPoints(visibleLayers);
	}

	@Override
	public void onImageChanged(ImageSettings settings) {
		setImageSettings(settings);
		
	}
	
	public void processTouchQueue()
	{
		ArrayList<BrushPoint> brushPoints = new ArrayList<BrushPoint>();
		ArrayList<BrushPoint> deletedPoints = new ArrayList<BrushPoint>();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
		synchronized (_synch)
		{
			if (_touchQueue.size() == 0)
				return;
			
			Stroke currentStroke = _strokeList.getLast();
			String activeLayerName;
			ArrayList<String> names;
			int left, right, bottom, top;
			
			
			for (SurfaceTouch touchPoint:_touchQueue)
			{
				if (touchPoint.getInteractionMode() == SurfaceInteractionMode.BRUSH)
				{
					minX = Math.min(minX, touchPoint.x());
					maxX = Math.max(maxX, touchPoint.x());
					minY = Math.min(minY, touchPoint.y());
					maxY = Math.max(maxY, touchPoint.y());
					brushPoints.add(new BrushPoint(touchPoint.x(), touchPoint.y(), touchPoint.size(), _strokeConfiguration.getBrushColor()));
				}
				else
				{
					activeLayerName = _currentImageSettings.getActiveLayerName();
					names = new ArrayList<String>();
					names.add(activeLayerName);
					left = touchPoint.x() - (int)(touchPoint.size()/2);
					right = touchPoint.x() + (int)(touchPoint.size()/2) - 1;
					
					top = touchPoint.y() - (int)(touchPoint.size()/2);
					bottom = touchPoint.y() + (int)(touchPoint.size()/2) - 1;
					
					minX = Math.min(minX, left);
					maxX = Math.max(maxX, right);
					minY = Math.min(minY, top);
					maxY = Math.max(maxY, bottom);
					
					deletedPoints.addAll(_currentImageSettings.getImage().deleteBrushPoints(top, left, bottom, right, names));
					
				}
			}
			// Only one of these lists will have points in it at a time
			assert !(brushPoints.size()>0 && deletedPoints.size()>0);
			
			currentStroke.addBrushPoints(brushPoints, _currentImageSettings.getActiveLayerName());
			currentStroke.addBrushPoints(deletedPoints, _currentImageSettings.getActiveLayerName());
			
			if (brushPoints.size()>0)
			{
				activeLayerName = _currentImageSettings.getActiveLayerName();
				names = new ArrayList<String>();
				names.add(activeLayerName);
				
				PaintingLayer layer = _currentImageSettings.getImage().getLayer(activeLayerName);
				long start = System.currentTimeMillis();
				layer.addAllPoints(brushPoints);
				Log.d("ADD_POINTS", "Time to add " + brushPoints.size() + " points: " + (System.currentTimeMillis() - start) + "ms");
			}
			_touchQueue.clear();
		}
		if (brushPoints.size()>0 || deletedPoints.size()>0)
		{
			_listener.onImageUpdated(minY, minX, maxY, maxX);
		}
		
	}
	
	public void undo()
	{
		if (_strokeList.size()>0)
		{
			Stroke last = _strokeList.removeLast();
			last.undo();
			if (_listener != null)
				_listener.onImageUpdated();
		}
	}
	
	public void stop()
	{
		if (_processThread != null && _processThread.isAlive())
			_processThread.interrupt();
	}
	
	public void start()
	{
		stop();
		_processThread = new Thread()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						Thread.sleep(_processSleepMilli);
						processTouchQueue();
						if (Thread.currentThread().isInterrupted())
							break;
					}
				}
				catch (InterruptedException ie)
				{
					
				}
				catch (Exception e)
				{
					Log.e("...<>..<>..", e.toString());
				}
				
			}
		};
		_processThread.start();
	}
	
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.
	//							Event Handlers
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.

	public void onEvent(ClearImageEvent clear)
	{
		if (_currentImageSettings != null)
		{
			Image image = _currentImageSettings.getImage();
			ArrayList<String> layerNames = image.getAllLayerNames();
			for (String name:layerNames)
			{
				PaintingLayer layer = image.getLayer(name);
				layer.clear();
			}
			if (_listener != null)
				_listener.onImageUpdated();
		}
	}
}
