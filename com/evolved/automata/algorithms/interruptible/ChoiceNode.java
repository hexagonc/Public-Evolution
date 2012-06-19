package com.evolved.automata.algorithms.interruptible;

public interface ChoiceNode {
	public ChoicePointer NextChoice();
	public void ResetNode();
	public boolean GoalReachedP();
}
