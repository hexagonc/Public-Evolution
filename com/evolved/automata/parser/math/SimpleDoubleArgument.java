package com.evolved.automata.parser.math;
import java.util.*;

public class SimpleDoubleArgument implements Argument
{
	double _value;
	public SimpleDoubleArgument(double value)
	{
		_value = value;
	}
	
	@Override
	public LinkedList<Argument> evaluate(LinkedList<Argument> evaluationStack) {
		evaluationStack.push(this);
		return evaluationStack;
	}

	public double getValue()
	{
		return _value;
	}

	@Override
	public Argument evaluateBinary(BinaryOperator operator, Argument me, Argument next) {
		SimpleDoubleArgument actual = (SimpleDoubleArgument)next;
		double value = operator.evaluateSimpleDouble(_value, actual.getValue());
		
		return new SimpleDoubleArgument(value);
	}

	@Override
	public Argument evaluateFunction(Function function, Argument... remaining) {
		double[] args = new double[remaining.length];
		SimpleDoubleArgument actual;
		for (int i=0;i<remaining.length;i++)
		{
			actual = (SimpleDoubleArgument)remaining[i];
			args[i] = actual.getValue();
		}
		double value = function.evaluateSimpleDouble(args);
		return new SimpleDoubleArgument(value);
	}
}
