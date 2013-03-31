package com.evolved.automata.parser.math;
import java.util.*;


public class BinaryOperator implements Argument 
{
	static HashMap<String, SimpleDoubleEvaluator> _eMap;
	
	String _name;
	
	// Predefined Convenience Static Evaluators
	public static final SimpleDoubleEvaluator _ADD = new SimpleDoubleEvaluator()
	{
		public double evaluate(double arg1, double arg2)
		{
			return arg1 + arg2;
		}
	};
	
	public static final SimpleDoubleEvaluator _SUBTRACT = new SimpleDoubleEvaluator()
	{
		public double evaluate(double arg1, double arg2)
		{
			return arg1 - arg2;
		}
	};
	
	public static final SimpleDoubleEvaluator _MULTIPLY = new SimpleDoubleEvaluator()
	{
		public double evaluate(double arg1, double arg2)
		{
			return arg1 * arg2;
		}
	};
	
	public static final SimpleDoubleEvaluator _DIVIDE = new SimpleDoubleEvaluator()
	{
		public double evaluate(double arg1, double arg2)
		{
			return arg1 / arg2;
		}
	};
	
	static
	{
		_eMap = new HashMap<String, BinaryOperator.SimpleDoubleEvaluator>();
		_eMap.put("+", _ADD);
		_eMap.put("-", _SUBTRACT);
		_eMap.put("*", _MULTIPLY);
		_eMap.put("/", _DIVIDE);
	}
	
	
	public static void addOperator(String name, SimpleDoubleEvaluator definition)
	{
		_eMap.put(name, definition);
	}
	
	public BinaryOperator(String name)
	{
		_name = name;
		
	}
	
	@Override
	public LinkedList<Argument> evaluate(LinkedList<Argument> evaluationStack) {
		Argument arg1 = evaluationStack.pop();
		Argument arg2 = evaluationStack.pop();
		Argument out = arg1.evaluateBinary(this, arg1, arg2);
		evaluationStack.push(out);
		return evaluationStack;
	}
	
	
	
	public static interface SimpleDoubleEvaluator
	{
		public double evaluate(double arg1, double arg2);
	}
	
	public double evaluateSimpleDouble(double v1, double v2)
	{
		return _eMap.get(_name).evaluate(v1, v2); 
	}

	// Unused.  This is called by Operator Arguments on Operand Arguments
	@Override
	public Argument evaluateBinary(BinaryOperator operator, Argument me, Argument next) {
		
		return null;
	}

	// Unused.  This is called by Function Arguments on Operand Arguments 
	@Override
	public Argument evaluateFunction(Function function, Argument... remaining) {
		
		return null;
	}
}
