package com.sample.simple.paint.events;

public class UndoEvent extends GenericEvent {
	private UndoEvent()
	{
		
	}
	
	public static UndoEvent get()
	{
		return new UndoEvent();
	}
}
