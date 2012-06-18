package com.evolved.automata.alisp;

public abstract class GenericEvaluator extends Evaluator{
	
	
	public GenericEvaluator()
	{
		
	}
	
	
	public GenericEvaluator(Environment ev)
	{
		this.env=ev;
	}
	
	protected void  resetState()
	{
		argumentIndex=0;
		resume=false;
		prcessedArgs=null;
	}
	
	Argument[] prcessedArgs=null;

	@Override
	public Argument eval(Argument[] args) {
		Argument pArg = null;
		try
		{
			
			while (args!=null&&argumentIndex<args.length)
			{
				if (argumentIndex==0)
					prcessedArgs = new Argument[args.length];
				pArg = getFinalValue(resume, args[argumentIndex]);
				prcessedArgs[argumentIndex] = pArg;
				if (ScalarEvaluator.continuation(pArg))
					throw new ContinuationSignal(pArg);
				argumentIndex++;
			}
		}
		catch (ContinuationSignal cs)
		{
			return returnContinuation(cs.getArgument());
		}
		return resetReturn(processArgs(prcessedArgs));
	}

	public abstract Argument processArgs(Argument[] args);


	protected <T extends GenericEvaluator> T cloneMe() throws InstantiationException, IllegalAccessException
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
	
	
}
