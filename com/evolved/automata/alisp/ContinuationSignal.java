package com.evolved.automata.alisp;

public class ContinuationSignal extends Exception{
	Argument carg=null;
	public ContinuationSignal(Argument arg)
	{
		carg=arg;
	}
	
	public Argument getArgument()
	{
		return carg;
	}

}
