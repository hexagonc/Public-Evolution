package com.evolved.automata.alisp;

public class TraceBreakEvaluator extends GenericEvaluator {

	public TraceBreakEvaluator(Environment ev)
	{
		super(ev);
	}
	
	public TraceBreakEvaluator()
	{
		
	}
	
	@Override
	public Argument processArgs(Argument[] args) {
		String key = (String)args[0].oValue;
		
		return Environment.makeAtom(key);
	}

}
