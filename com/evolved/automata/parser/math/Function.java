package com.evolved.automata.parser.math;

import java.util.HashMap;
import java.util.LinkedList;




public class Function implements Argument
{
	static HashMap<String, SimpleDoubleFunction> _eMap;
	static HashMap<String, Integer> _argumentCountMap;
	
	String _name;
	
	public static interface SimpleDoubleFunction
	{
		public double evaluate(double ... args);
	}
	
	// Predefined Convenience Static Evaluators
	public static final SimpleDoubleFunction _NEGATE = new SimpleDoubleFunction()
	{
		public double evaluate(double ... args)
		{
			return -1*args[0];
		}
	};
	
	public static final SimpleDoubleFunction _SQUARE = new SimpleDoubleFunction()
	{
		public double evaluate(double ... args)
		{
			return args[0]*args[0];
		}
	};
	
	public static final SimpleDoubleFunction _COS = new SimpleDoubleFunction()
	{
		public double evaluate(double ... args)
		{
			return Math.cos(args[0]);
		}
	};
	
	public static final SimpleDoubleFunction _SIN = new SimpleDoubleFunction()
	{
		public double evaluate(double ... args)
		{
			return Math.sin(args[0]);
		}
	};
	
	public static final SimpleDoubleFunction _PERCENT = new SimpleDoubleFunction()
	{
		public double evaluate(double ... args)
		{
			return args[0]/100;
		}
	};
	
	static
	{
		_eMap = new HashMap<String, Function.SimpleDoubleFunction>();
		_eMap.put("-", _NEGATE);
		_eMap.put("sq", _SQUARE);
		_eMap.put("sin", _SIN);
		_eMap.put("cos", _COS);
		_eMap.put("percent", _PERCENT);
		
		_argumentCountMap = new HashMap<String, Integer>();
		_argumentCountMap.put("-",1);
		_argumentCountMap.put("sq",1);
		_argumentCountMap.put("sin", 1);
		_argumentCountMap.put("cos", 1);
		_argumentCountMap.put("percent", 1);
		
	
	}
	
	public Function(String token)
	{
		_name = token;
	}
	
	
	public static void addFunction(String name, int argCount, SimpleDoubleFunction definition)
	{
		_eMap.put(name, definition);
		_argumentCountMap.put(name, argCount);
	}
	
	public double evaluateSimpleDouble(double ... args)
	{
		return _eMap.get(_name).evaluate(args); 
	}


	@Override
	public LinkedList<Argument> evaluate(LinkedList<Argument> evaluationStack) {
		int acount = _argumentCountMap.get(_name);
		Argument[] args = new Argument[acount];
		for (int i=0;i<acount;i++)
		{
			args[i] = evaluationStack.pop();
		}
		Argument result = args[0].evaluateFunction(this, args);
		evaluationStack.push(result);
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
