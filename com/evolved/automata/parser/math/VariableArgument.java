package com.evolved.automata.parser.math;

import java.util.HashMap;
import java.util.LinkedList;

public class VariableArgument implements Argument{

	String _value;
	HashMap<String, Argument> _vars;
	
	public VariableArgument(String name, HashMap<String, Argument> vars)
	{
		_value = name;
		_vars = vars;
	}
	@Override
	public LinkedList<Argument> evaluate(LinkedList<Argument> evaluationStack) 
	{
		evaluationStack.push(_vars.get(_value));
		return evaluationStack;
	}

	@Override
	public Argument evaluateBinary(BinaryOperator operator, Argument me,
			Argument next) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Argument evaluateFunction(Function function, Argument... remaining) {
		// TODO Auto-generated method stub
		return null;
	}

}
