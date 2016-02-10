package com.sample.simple.paint.events;

public class EraserTipSelectedEvent extends GenericEvent 
{
	private EraserTipSelectedEvent()
	{
		
	}
	
	public static EraserTipSelectedEvent get()
	{
		return new EraserTipSelectedEvent();
	}

}
