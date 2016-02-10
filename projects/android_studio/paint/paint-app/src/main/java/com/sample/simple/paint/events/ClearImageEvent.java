package com.sample.simple.paint.events;

public class ClearImageEvent extends GenericEvent
{
	private ClearImageEvent()
	{
		
	}
	
	public static ClearImageEvent get()
	{
		return new ClearImageEvent();
	}

	
}
