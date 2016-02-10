package com.sample.simple.paint.events;

import java.util.LinkedList;

import android.util.Log;
import de.greenrobot.event.EventBus;

public class EventManager 
{
	private static EventManager _manager = null;
	
	LinkedList<UndoableEvent> _undoList = new LinkedList<UndoableEvent>();
	
	
	EventBus _bus;
	private EventManager()
	{
		_bus = EventBus.getDefault();
		_bus.register(this);
	}
	
	public static EventManager get()
	{
		if (_manager == null)
			_manager = new EventManager();
		return _manager;
	}
	
	public void register(Object obj)
	{
		_bus.register(obj);
	}
	
	public void unregiser(Object obj)
	{
		_bus.unregister(obj);
	}
	
	public void postEvent(UndoableEvent event)
	{
		Log.d("..<>..<>..EventManager..<>..<>..", "Posting undoable event: " + event.getName());
		_bus.post(event);
	}
	
	public void postEventSticky(UndoableEvent event)
	{
		Log.d("..<>..<>..EventManager..<>..<>..", "Posting undoable sticky event: " + event.getName());
		_bus.postSticky(event);
	}
	

	public void postEvent(Event event)
	{
		Log.d("..<>..<>..EventManager..<>..<>..", "Posting event: " + event.getName());
		_bus.post(event);
	}
	
	public void postEventSticky(Event event)
	{
		Log.d("..<>..<>..EventManager..<>..<>..", "Posting sticky event: " + event.getName());
		_bus.postSticky(event);
	}
	
	public boolean cancelEvent(Event event)
	{
		
		boolean removed = _bus.removeStickyEvent(event);
		Log.d("..<>..<>..EventManager..<>..<>..", "Successfully removed sticky event [" + event.getName() + "] " + removed);
		return removed;
	}
	
	
	
	public void onEvent(UndoEvent event)
	{
		UndoableEvent undo = null;
		if (_undoList.size()>0)
		{
			try
			{
				undo = _undoList.removeLast();
				undo.undo();
				Log.e("..<>..<>..EventManager..<>..<>..", "Successfully undid: " + undo.getName());
			}
			catch (Exception e)
			{
				if (undo != null)
					Log.e("..<>..<>..EventManager..<>..<>..", "Error undoing: " + undo.getName());
				else
					Log.e("..<>..<>..EventManager..<>..<>..", e.toString());
			}
		}
	}
}
