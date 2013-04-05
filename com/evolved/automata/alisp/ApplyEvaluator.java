package com.evolved.automata.alisp;

public class ApplyEvaluator implements CompiledEvaluator
{
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
		resumeInner=false;
		state=GETTING_FUNCTION;
		ev = null;
		newArgs = null;
		argIndex=0;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public final int GETTING_FUNCTION=0;
	public final int EVALUATING_INPUT_ARGS=1;
	public final int EVALUATING_FUNCTION=2;
	int state=GETTING_FUNCTION;
	LambdaFunctionEvaluator ev;
	Argument[] newArgs = null;
	boolean resumeInner=false;
	int argIndex=0;
	Environment env;
	
	public ApplyEvaluator(Environment env)
	{
		this.env = env;
		
	}
	
	public Environment getEnvironment()
	{
		return env;
	}

	// First argument is the name string of the function to call
	// Second argument, if present is a cons list of the arguments values
	@Override
	public Argument eval(Argument[] args) {
		if (args==null||args.length==0)
			return null;
		String sValue;
		try
		{
			while (true)
			{
				switch (state)
				{
					case GETTING_FUNCTION:
						sValue = (String)args[0].oValue;
						Argument mapped = env.getVariableValue(sValue);
						
						if (!Environment.isNull(mapped) && mapped.isLambda())
						{
							ev = (LambdaFunctionEvaluator)mapped.oValue;
							ev.setExecutionEnvironment(env);
						}
						else
							 return null;
						newArgs = new Argument[args.length-1];
						state = EVALUATING_INPUT_ARGS;
						break;
					case EVALUATING_INPUT_ARGS:
						Argument inputArgs =null;
						if (args.length > 1)
						{
							
							
							inputArgs = getFinalValue(resume, args[argIndex+1]);
							if (inputArgs!=null&&inputArgs.isContinuation())
								throw new ContinuationSignal(inputArgs);
							newArgs[argIndex] = inputArgs;
							argIndex++;
							
							if (argIndex==newArgs.length)
							{
								ev.setArgs(newArgs);
								state = EVALUATING_FUNCTION;
							}
							
							
						}
						else
							state = EVALUATING_FUNCTION;
						break;
					default:
						Argument returnArg = ev.getCompiledResult(resumeInner);
						if (returnArg!=null&&returnArg.isContinuation())
						{
							resumeInner=true;
							throw new ContinuationSignal(returnArg);
						}
						else
							return resetReturn(returnArg);
				}
			}
		}
		catch (ContinuationSignal cs)
		{
			return returnContinuation(cs.getArgument());
		}
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		ApplyEvaluator ceval = new ApplyEvaluator(env);
		
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
