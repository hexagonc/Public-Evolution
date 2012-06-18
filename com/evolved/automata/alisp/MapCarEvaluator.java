package com.evolved.automata.alisp;

public class MapCarEvaluator implements CompiledEvaluator, RovingLexicalEvaluator
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
		argumentIndex=0;
		state=EVALUATING_CONS;
		expression = null;
		iterationListArg = null;
		output=null;
		loopArg=null;
		consArg=null;
		numArguments=0;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	public int numArguments=0;
	Argument consArg=null;
	// Continuation Evaluator state
	int state;
	final int EVALUATING_CONS=0;
	final int BINDING_VARIABLE=1;
	final int EVALUATING_EXPRESSION=2;
	
	Argument[] iterationListArg = null;
	Argument[] output = null;
	Argument loopArg=null;
	Argument expression = null;
	
	
	Environment env;
	public MapCarEvaluator(Environment env)
	{
		this.env = env;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}

	// First argument is a variable name that each element of argument two is bound to
	// Second argument is a cons list to iterate over
	// Third argument is an expression, usually a function of the bound variable that will be
	// evaluated 
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		String loopVariable = (String)args[0].oValue;
		
		while (true)
		{
			switch (state)
			{
				case EVALUATING_CONS:
					consArg = getFinalValue(resume, args[1]);
					if (ScalarEvaluator.continuation(consArg))
						return returnContinuation(consArg);
					if (Environment.isNull(consArg))
						return consArg;
					state = BINDING_VARIABLE;
					
					if (consArg.isAtom())
					{
						env.mapValue(loopVariable, consArg);
						numArguments=1;
						output = new Argument[1];
						state = EVALUATING_EXPRESSION;
					}
					else
					{
						iterationListArg = consArg.innerList;
						numArguments=consArg.innerList.length;
						output = new Argument[numArguments];
						state = BINDING_VARIABLE;
					}
					
					break;
				case BINDING_VARIABLE:
					if (argumentIndex>=numArguments)
						return resetReturn(new Argument(null, null, output));
					loopArg = getFinalValue(resume, iterationListArg[argumentIndex]);
					if (ScalarEvaluator.continuation(loopArg))
						return returnContinuation(loopArg);
					env.mapValue(loopVariable, loopArg);
					state = EVALUATING_EXPRESSION;
					break;
				default:
					expression = getFinalValue(resume, args[2]);
					if (ScalarEvaluator.continuation(expression))
						return returnContinuation(expression);
					output[argumentIndex] = expression;
					state = BINDING_VARIABLE;
					argumentIndex++;
			}
		}
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		MapCarEvaluator ceval = new MapCarEvaluator(env);
		
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
