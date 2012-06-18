package com.evolved.automata.alisp;

public class WhileEvaluator implements CompiledEvaluator {
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
		numArguments=0;
		iterationBodyArg=null;
		loopConditionArg=null;
		output = null;
		state=0;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	int state=0;
	
	final int EVALUATING_CONDITION=1;
	final int EVALUATING_BODY=2;
	final int DEFINING_BODY=0;
	public int numArguments=0;
	public int argumentIndex=0;
	Argument[] iterationBodyArg=null;
	Argument loopConditionArg=null;
	Argument output = null;
	Environment env;
	public WhileEvaluator(Environment env)
	{
		this.env = env;
	}

	public Environment getEnvironment()
	{
		return env;
	}
	
	// First argument is an expression to evaluate that determines if the loop continues.
	// Remaining arguments are the iteration body
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		
		while (true)
		{
			switch (state)
			{
				case DEFINING_BODY:
					iterationBodyArg = new Argument[args.length-1];
					for (int i=1;i<args.length;i++)
						iterationBodyArg[i-1]=args[i];
					numArguments = iterationBodyArg.length;
					state=EVALUATING_CONDITION;
					break;
				case EVALUATING_CONDITION:
					loopConditionArg = getFinalValue(resume, args[0]);
					if (ScalarEvaluator.continuation(loopConditionArg))
						return returnContinuation(loopConditionArg);
					if (Environment.isNull(loopConditionArg))
						return resetReturn(output);
					state=EVALUATING_BODY;
					break;
				default:
					while (argumentIndex<numArguments)
					{
						output=getFinalValue(resume, iterationBodyArg[argumentIndex]);
						if (ScalarEvaluator.continuation(output))
							return returnContinuation(output);
						if (!Environment.isNull(output)&&output.isBreak())
						{
							output.setBreak(false);
							return resetReturn(output);
						}
						argumentIndex++;
					}
					argumentIndex=0;
					state=EVALUATING_CONDITION;
			}
		}
		
		
	}
	
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		WhileEvaluator ceval = new WhileEvaluator(env);
		
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
