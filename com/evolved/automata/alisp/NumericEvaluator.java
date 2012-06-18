package com.evolved.automata.alisp;

public abstract class NumericEvaluator extends Evaluator
{
	public void resetState()
	{
		argumentIndex=0;
		resume=false;
		dArgs = null;
	}
	
	
	double[] dArgs;
	
	public NumericEvaluator()
	{
		resume=false;
	}
	
	protected double[] getDoubleArgs(Argument[] baseArgs, Double defaultIfNull) throws ContinuationSignal
	{
		if (baseArgs==null)
			return null;
		 
		Argument nArg;
		while (argumentIndex<baseArgs.length)
		{
			if (argumentIndex==0)
				dArgs = new double[baseArgs.length];
			nArg = getFinalValue(resume, baseArgs[argumentIndex]);
			if (nArg!=null&&nArg.isContinuation())
				throw new ContinuationSignal(returnContinuation(nArg));
			if (!Environment.isNull(nArg))
			{
				dArgs[argumentIndex] = ((Number)nArg.oValue).doubleValue();
			}
			else
				if (defaultIfNull==null)
					return null;
				else
					dArgs[argumentIndex]=defaultIfNull.doubleValue();
			argumentIndex++;
			
		}
		
		return dArgs;
	}
	
	protected double[] getDoubleArgs(Argument[] baseArgs) throws ContinuationSignal
	{
		return getDoubleArgs( baseArgs, null);
	}
	
	protected Argument makeReturnValue(double d)
	{
		return resetReturn(Environment.makeAtom(new Double(d)));
	}
}
