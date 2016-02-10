package com.sample.simple.paint.events;

public class SetBrushColorEvent extends GenericEvent {
	
	final int _brushColor;
	
	private SetBrushColorEvent(int size)
	{
		_brushColor  = size;
	}
	
	public static SetBrushColorEvent get(int brushColor)
	{
		return new SetBrushColorEvent(brushColor);
	}

	public int getBrushColor()
	{
		return _brushColor;
	}
}
