package com.evolved.automata.alisp;

/**
 * Fixed issue with unnecessary cloning of function arguments that was causing problems with continuations
 * @author Evolved8
 *
 */
public class IfEvaluator implements CompiledEvaluator {
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
		
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	
	Environment env;
	
	
	private Argument returnArg(Argument ret)
	{
		if (ret!=null&&ret.isContinuation())
			return returnContinuation(ret);
		else
			return resetReturn(ret);
	}
	
	
	
	// Continuation Evaluator state
	int numberArguments;
	
	Argument conditionValue=null;
	
	public IfEvaluator(Environment env)
	{
		this.env = env;
	}
	public Environment getEnvironment()
	{
		return env;
	}
	
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		
		numberArguments = args.length;
		
		while (true)
		{
			switch (argumentIndex)
			{
				case 0:
					Argument conditionValue=getFinalValue(resume, args[0]);
					if (conditionValue!=null&&conditionValue.isContinuation())
						return returnContinuation(conditionValue);
					else
					{
						if (!Environment.isNull(conditionValue))
							argumentIndex=1;
						else
							if (numberArguments>2)
								argumentIndex=2;
							else
								return resetReturn(conditionValue);
					}
					break;
				case 1:
					return returnArg(getFinalValue(resume, args[1]));
					
				default:
					return returnArg(getFinalValue(resume, args[2]));
					
			}
		}
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		IfEvaluator ceval = new IfEvaluator(env);
		
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
			firstRun=false;
			resolveEvaluators();
		}
		return eval(sargs);
	}
	
	
	
	public void setEnvironment(Environment env)
	{
		this.env=env;
	}
	
	protected Argument evaluateArgument(Argument arg)
	{
		CompiledEvaluator com = arg.getEvaluator();
		return com.getCompiledResult(resume);
		
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
}
