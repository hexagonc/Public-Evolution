package com.evolved.automata.alisp;

public class CondEvaluator implements CompiledEvaluator {
	boolean resume=false;
	boolean firstRun=true;
	public Argument returnContinuation(Argument arg)
	{
		resume=true;
		return new Argument(arg);
	}
	
	private void resetState()
	{
		resume=false;
		argumentIndex=0;
		result=null;
		evaluatingConditionP=true;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	Argument consArg=null;
	// Continuation Evaluator state
	
	Argument result=null;
	
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public CondEvaluator(Environment env)
	{
		this.env = env;
	}
	
	boolean evaluatingConditionP=true;
	
	/**
	  Returns the first of a list of conditions that is true
	*/
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		Argument condition, conclusion;
		while (argumentIndex<args.length)
		{
			if (evaluatingConditionP)
			{
				condition = args[argumentIndex].innerList[0];
				result = getFinalValue(resume, condition);
				if (result!=null&&result.isContinuation())
					return returnContinuation(result);
				if (Environment.isNull(result))
				{
					argumentIndex++;
					continue;
				}
				
				evaluatingConditionP=false;
			}
			if (args[argumentIndex].innerList.length>1)
			{
				if (!evaluatingConditionP)
				{
					conclusion = args[argumentIndex].innerList[1];
					result = getFinalValue(resume, conclusion);
					if (result!=null&&result.isContinuation())
						return returnContinuation(result);
					
				}
			}
			return resetReturn(result);
			
		}
		
		return resetReturn(null);
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		CondEvaluator ceval = new CondEvaluator(env);
		
		ceval.setArgs(sargs);
		return ceval;
	}
	public void setArgs(Argument[] args)
	{
		sargs = args;
	}
	public Argument getCompiledResult(boolean resume)
	{
		if (!resume)
		{
			resetState();
		}
		if (firstRun)
		{
			resolveEvaluators();
			firstRun=false;
		}
		return eval(sargs);
	}
	
	public void setEnvironment(Environment env)
	{
		this.env=env;
	}
	
	protected Argument getFinalValue(boolean r, Argument in)
	{
		if (in!=null&&in.isEvaluator())
			return evaluateArgument(in);
		else
			return getVariableValue(in, true);
	}
	
	protected Argument getVariableValue(Argument nValue, boolean resolveVariables)
	{
		if (nValue!=null&&resolveVariables&&nValue.isIdentifier())
			return env.getVariableValue((String)nValue.oValue);
		else
			return nValue;
	}
	
	protected void resolveEvaluators()
	{
		if (sargs==null)
			return;
		
		for (int j=0;j<sargs.length;j++)
		{
			sargs[j] = resolveEvaluators(sargs[j].innerList);
			
		}
		
	}
	
	protected Argument resolveEvaluators(Argument[] subArgs)
	{
		
		Argument[] newArgs = new Argument[subArgs.length];
		Argument nValue;
		CompiledEvaluator com;
		LambdaFunctionEvaluator lamb=null;
		Argument[] nArgs;
		CompiledEvaluator c;
		
		for (int j=0;j<subArgs.length;j++)
		{
			nValue  = subArgs[j];
			if (nValue!=null&&nValue.isEvaluator())
			{
				
				com = nValue.getEvaluator().clone();
				// TODO: Figure out a better way of determining if it is a lambda function
				if (com instanceof LambdaFunctionEvaluator)
				{
					lamb = (LambdaFunctionEvaluator)com;
					lamb.setExecutionEnvironment(env);
					nValue = new Argument(lamb);
					
				}
				else
				{
					if (com instanceof RovingLexicalEvaluator)
					{
						
						com.setEnvironment(new Environment(env));
					}
					else
						com.setEnvironment(env);
					nValue = new Argument(com);
				}
				
			}
			else
			{
				if (nValue!=null&&nValue.isCons()&&nValue.innerList.length>0&&nValue.innerList[0].isIdentifier())
				{
					
					c = env.getFunction((String)nValue.innerList[0].oValue);
					if (c!=null)
					{
						c=c.clone();
						if (c instanceof LambdaFunctionEvaluator)
						{
							lamb = (LambdaFunctionEvaluator)c.clone();
							lamb.setExecutionEnvironment(env);
							nArgs = new Argument[nValue.innerList.length-1];
							for (int i=1;i<nValue.innerList.length;i++)
								nArgs[i-1]=nValue.innerList[i];
							lamb.setArgs(nArgs);
							nValue=new Argument(lamb);
						}
					}
						
				}
				
			}
			newArgs[j] = nValue;
			
		}
		
		return new Argument(null, null, newArgs);
	}
	
	protected Argument evaluateArgument(Argument arg)
	{
		CompiledEvaluator com = arg.getEvaluator();
		return com.getCompiledResult(resume);
		
	}
}
