package com.evolved.automata.alisp;

public class ConcatEvaluator implements CompiledEvaluator {
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
		sBuilder=null;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	
	// Continuation Evaluator state
	
	
	StringBuilder sBuilder=null;
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public ConcatEvaluator(Environment env)
	{
		this.env = env;
	}

	// First argument must be an atom
	// remaining arguments can be an atom
	// Returns concatentated string
	// There must be at least one argument to this function.  Null arguments are treated as empty strings
	@Override
	public Argument eval(Argument[] args) 
	{
		if (args == null)
			return null;
		 
		
		Argument result=null;
		while(argumentIndex<args.length)
		{
			if (argumentIndex==0)
				sBuilder = new StringBuilder();
			
			result = getFinalValue(resume, args[argumentIndex]);
			if (result!=null&&result.isContinuation())
				return resetReturn(result);
			
			if (!Environment.isNull(result)&&result.isAtom())
				sBuilder.append((String)result.oValue.toString());
			argumentIndex++;
		}
		
		if (sBuilder!=null)
			return resetReturn(new Argument(null, sBuilder.toString(), null));
		else
			return resetReturn(new Argument(null, "", null));
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		ConcatEvaluator ceval = new ConcatEvaluator(env);
		
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
			resetState();
		if (firstRun)
		{
			firstRun=false;
			resolveEvaluators();
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
		Argument[] newArgs = new Argument[sargs.length];
		Argument nValue;
		CompiledEvaluator com;
		LambdaFunctionEvaluator lamb=null;
		Argument[] nArgs;
		CompiledEvaluator c;
		
		for (int j=0;j<sargs.length;j++)
		{
			nValue  = sargs[j];
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
							lamb = (LambdaFunctionEvaluator)c;
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
		sargs = newArgs;
	}
	
	protected Argument evaluateArgument(Argument arg)
	{
		CompiledEvaluator com = arg.getEvaluator();
		return com.getCompiledResult(resume);
		
	}
}
