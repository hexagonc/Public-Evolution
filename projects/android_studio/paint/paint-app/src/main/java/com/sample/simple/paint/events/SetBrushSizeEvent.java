package com.sample.simple.paint.events;

public class SetBrushSizeEvent extends GenericEvent {
	final int _brushSize;
	
	private SetBrushSizeEvent(int size)
	{
		_brushSize  = size;
	}
	
	public static SetBrushSizeEvent get(int brushSizePx)
	{
		return new SetBrushSizeEvent(brushSizePx);
	}

	public int getBrushTipSize()
	{
		return _brushSize;
	}
}
