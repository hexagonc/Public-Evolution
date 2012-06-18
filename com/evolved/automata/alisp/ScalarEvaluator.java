package com.evolved.automata.alisp;

public abstract class ScalarEvaluator extends Evaluator{
	public static boolean continuation(Argument arg)
	{
		return arg!=null&&arg.isContinuation();
	}
	
	protected void resetState()
	{
		output = null;
		argumentIndex=0;
	}
	
	
	Object[] output;
	
	public ScalarEvaluator(Environment env)
	{
		this.env = env;
	}
	
	public ScalarEvaluator()
	{
		
	}

	
	protected <T extends ScalarEvaluator> T cloneMe() throws InstantiationException, IllegalAccessException
	{
		Class<T> c = (Class<T>)this.getClass();
		T base = (T)c.newInstance();
		base.env = this.env;
		base.setArgs(sargs);
		return base;
	}
	
	public CompiledEvaluator clone()
	{
		try
		{
			return cloneMe();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.toString());
		}
	}
	
	public abstract Object eval(Object[] oargs);
	
	public Argument eval(Argument[] args)
	{
		try
		{
			if (args!=null)
			{
				if (argumentIndex==0)
					output = new Object[args.length];
				while (argumentIndex<args.length)
				{
					output[argumentIndex] = getDataFromArgument(args[argumentIndex]);
					argumentIndex++;
				}
					
			}
		}
		catch (ContinuationSignal ce)
		{
			return returnContinuation(ce.getArgument());
		}
		Object computedResult = eval(output);
		return resetReturn(Environment.makeAtom(computedResult));
	}
	
	public Object getDataFromArgument(Argument arg) throws ContinuationSignal
	{
		Argument targument;
		targument = getFinalValue(resume, arg);
		if (targument!=null&&targument.isContinuation())
			throw new ContinuationSignal(targument);
		if (Environment.isNull(targument))
			return null;
		return targument.oValue;
	}
	
	
	
}
