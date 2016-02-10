package com.sample.simple.paint.events;

public class BrushTipSelectedEvent extends GenericEvent
{
	
	
	
	private BrushTipSelectedEvent()
	{
		
	}
	
	public static BrushTipSelectedEvent get()
	{
		return new BrushTipSelectedEvent();
	}

}
