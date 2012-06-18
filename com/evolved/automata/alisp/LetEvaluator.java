package com.evolved.automata.alisp;

public class LetEvaluator implements CompiledEvaluator, RovingLexicalEvaluator  {
	boolean resume=false;
	boolean firstRun=true;
	public Argument returnContinuation(Argument arg)
	{
		resume=true;
		return new Argument(arg);
	}
	
	private void resetState()
	{
		bindingList = null;
		bindingArg = null;
		argumentIndex=0;
		output = null;
		bindingIndex=0;
		numBindingArguments=0;
		pair=null;
		mappingPair=false;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	Argument bindingArg = null;
	Argument[] bindingList = null;
	public int argumentIndex=0;
	public int bindingIndex=0;
	public int numBindingArguments;
	Argument name;
	Argument pair=null;
	Argument[] output = null;
	boolean mappingPair=false;
	
	// Continuation Evaluator state
	int numberArguments;
	
	Environment env;
	public LetEvaluator(Environment env)
	{
		this.env = env;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	// First argument is a binding list which is ((name1 value1) (name2 value2))
	// Remaining arguments are arguments to execute
	@Override
	public Argument eval(Argument[] args) {
		if ((args == null)||(args.length==0))
			return null;
		
		Argument out=null;
		Argument value;
		while (true)
		{
			if (argumentIndex==0)
			{
				bindingArg = getFinalValue(resume, args[argumentIndex]);
				if (bindingArg!=null&&bindingArg.isContinuation())
					return returnContinuation(bindingArg);
				argumentIndex++;
				mappingPair=false;
			}
			else
			{
				if (bindingArg.isCons())
				{
					bindingList = bindingArg.innerList;
					numBindingArguments = bindingList.length;
					while (bindingIndex<numBindingArguments)
					{
						if (!mappingPair)
						{
							pair = getFinalValue(resume, bindingList[bindingIndex]);
							if (pair!=null&&pair.isContinuation())
								return returnContinuation(pair);
						}
						mappingPair=true;
						if (pair.isCons()&&pair.innerList.length==2)
						{
							name = pair.innerList[0];
							value = getFinalValue(resume, pair.innerList[1]);
							if (value!=null&&value.isContinuation())
							{
								return returnContinuation(value);
							}
							
							env.mapValue((String)name.oValue, value);
						}
						mappingPair=false;
						bindingIndex++;
					}
					
					while (argumentIndex<args.length)
					{
						out=getFinalValue(resume, args[argumentIndex]);
						if (out!=null&&out.isContinuation())
						{
							return returnContinuation(out);
						}
						if (!Environment.isNull(out)&&out.isBreak())
						{
							out.setBreak(false);
							return resetReturn(out);
						}
						argumentIndex++;
					}
				}
				return resetReturn(out);
			}
		}
		
		
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		LetEvaluator ceval = new LetEvaluator(env);
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
