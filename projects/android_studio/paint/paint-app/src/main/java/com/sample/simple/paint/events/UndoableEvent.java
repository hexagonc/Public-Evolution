package com.sample.simple.paint.events;

public interface UndoableEvent extends Event {
	public void undo();

}
