package com.evolved.automata.parser.math;
import java.util.*;
public interface Argument 
{
	public enum ARGUMENT_TYPE
	{
		SIMPLE_DOUBLE,
		BINARY_OPERATOR,
		FUNCTION
		
	}
	
	public LinkedList<Argument> evaluate(LinkedList<Argument> evaluationStack);
	
	public Argument evaluateBinary(BinaryOperator operator, Argument me, Argument next);
	public Argument evaluateFunction(Function function, Argument ... remaining);
}
