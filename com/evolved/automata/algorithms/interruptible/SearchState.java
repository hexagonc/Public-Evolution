package com.evolved.automata.algorithms.interruptible;

public interface SearchState {
	public SearchState GetNextState(EventType etype, Object eventData);
}
