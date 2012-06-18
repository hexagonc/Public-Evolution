package com.evolved.automata.alisp;

public class PopEvaluator implements CompiledEvaluator {
	boolean resume=false;
	boolean firstRun=true;
	
	public Argument returnContinuation(Argument arg)
	{
		resume=true;
		return new Argument(arg);
	}
	
	public void resetState()
	{
		resume=false;
		
		
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	Argument stackName = null;
	
	
	Environment env;
	public PopEvaluator(Environment env)
	{
		this.env = env;
	}

	public Environment getEnvironment()
	{
		return env;
	}
	
	// First argument must be a string of a variable name that is bound to a cons
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		
		Argument[] base = null, out = null;
		
		if (!args[0].isIdentifier())
			stackName = getFinalValue(resume, args[0]);
		else
			stackName = args[0];
		if (ScalarEvaluator.continuation(stackName))
			return returnContinuation(stackName);
		Argument stack=null;
		if (Environment.isNull(stackName))
			return resetReturn(null);
		else
		{
			if (stackName.isAtom())
			{
				stack = env.getVariableValue((String)stackName.oValue);
				if (stack==null||!stack.isCons())
					return resetReturn(null);
				base = stack.innerList;
				if (base.length==0)
					return resetReturn(null);
				out = new Argument[base.length-1];
				for (int i=1;i<base.length;i++)
					out[i-1]=base[i];
				stack.innerList = out;
				return resetReturn(base[0]);
			}
			else
				return resetReturn(null);
		}
		
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		PopEvaluator ceval = new PopEvaluator(env);
		
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