package com.evolved.automata.algorithms.interruptible;

public interface ChoicePointer {
	public ChoiceNode ResolveNode();
	public void Undo();
	
}
