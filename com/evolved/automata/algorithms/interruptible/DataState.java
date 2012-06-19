package com.evolved.automata.algorithms.interruptible;

public interface DataState extends SearchState{
	public SearchState GetNextState(EventType etype, Object eventData);
	public Object GetData();
}
