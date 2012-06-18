package com.evolved.automata.alisp;


public class BindFunctionEvaluator implements CompiledEvaluator 
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
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public BindFunctionEvaluator(Environment env)
	{
		this.env = env;
	}

	// First argument is a String literal with the name of the function to map
	// Second argument is an Evaluator or expression that returns an Evaluator
	@Override
	public Argument eval(Argument[] args) {
		if ((args == null)||(args.length==0))
			return null;
		if (args.length !=2)
		{
			return null;
		}
		else
		{
			
			Argument lvalue = args[0];
			Argument rvalue = args[1];
			
			if (lvalue.isAtom())
			{
				rvalue = getFinalValue(resume, rvalue);
				if (rvalue!=null&&rvalue.isContinuation())
					return returnContinuation(rvalue);
				
				env.mapEvaluator((String)lvalue.oValue, (CompiledEvaluator)rvalue.oValue);
				return resetReturn(rvalue);
			}
			
		}
		// calling resetReturn is probably unnecessary for this function
		return resetReturn(null);
	}
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		BindFunctionEvaluator ceval = new BindFunctionEvaluator(env);
		
		ceval.setArgs(sargs);
		return ceval;
	}
	public void setArgs(Argument[] args)
	{
		sargs = args;
	}
	public Argument getCompiledResult(boolean resume)
	{
		
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
		sargs = newArgs;
	}
	
	protected Argument evaluateArgument(Argument arg)
	{
		CompiledEvaluator com = arg.getEvaluator();
		return com.getCompiledResult(resume);
		
	}
}
