package com.evolved.automata.alisp;

public class NumericEqualEvaluator extends NumericEvaluator {
	public NumericEqualEvaluator(Environment env)
	{
		this.env=env;
	}
	
	public CompiledEvaluator clone()
	{
		NumericEqualEvaluator ceval = new NumericEqualEvaluator(env);
		
		ceval.setArgs(sargs);
		return ceval;
		
	}
	
	@Override
	public Argument eval(Argument[] args) {
		
		if (args == null)
			return null;
		try
		{
			double[] dArgs = getDoubleArgs(args);
			if (dArgs==null&&dArgs.length<2)
				return resetReturn(null);
			else
				return (dArgs[0] == dArgs[1])?makeReturnValue(dArgs[0]):resetReturn(null);	
		}
		catch (ContinuationSignal e)
		{
			return e.getArgument();
		}
	}
}
