package com.evolved.automata.parser.math;


import java.util.HashMap;
import java.util.LinkedList;




public class Expression 
{

	
	Argument[] _argStack;
	HashMap<String, Argument> _variables;

	protected Expression()
	{
		
	}
	
	protected Expression(Argument[] argstack, HashMap<String, Argument> vars)
	{
		_argStack = argstack;
		_variables = vars;
	}
	
	public SimpleDoubleArgument getDoubleArgument()
	{
		LinkedList<Argument> evaluationStack = new LinkedList<Argument>();
		for (Argument a:_argStack)
		{
			a.evaluate(evaluationStack);
		}
		if (evaluationStack.size()==1)
		{
			SimpleDoubleArgument o = (SimpleDoubleArgument)evaluationStack.getFirst();
			return o;
		}
		return null;
	}
	
	public Argument getEvaluate()
	{
		LinkedList<Argument> evaluationStack = new LinkedList<Argument>();
		for (Argument a:_argStack)
		{
			a.evaluate(evaluationStack);
		}
		if (evaluationStack.size()==1)
		{
			return evaluationStack.getFirst();
		}
		return null;
	}
	
	public double getDoubleValue()
	{
		SimpleDoubleArgument o = getDoubleArgument();
		return o.getValue();
	}
	
	
}
