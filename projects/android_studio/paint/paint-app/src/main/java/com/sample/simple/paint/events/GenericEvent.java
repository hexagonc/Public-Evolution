package com.sample.simple.paint.events;

public class GenericEvent implements Event
{

	@Override
	public String getName() {
		
		return this.getClass().getSimpleName();
	}
	
}
