package com.evolved.automata.alisp;

public class SwitchEvaluator implements CompiledEvaluator {
	
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
		consArg=null;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	
	// Continuation Evaluator state
	
	Argument result=null;
	Argument consArg=null;
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public SwitchEvaluator(Environment env)
	{
		this.env = env;
	}

	@Override
	public Argument eval(Argument[] args) {
		if (args == null||args.length<2)
			return null;
		
		Argument intermediate;
		
		if (consArg == null)
		{
			while (argumentIndex<args.length)
			{
				if (argumentIndex==0)
				{
					result = getFinalValue(resume, args[argumentIndex]);
					if (result!=null&&result.isContinuation())
						return returnContinuation(result);
					argumentIndex++;
				}
				else
				{
					intermediate  = args[argumentIndex];
					if (intermediate.isCons()&&intermediate.innerList.length>1)
					{
						Argument cValue = getFinalValue(resume, intermediate.innerList[0]);
						if (cValue!=null&&cValue.isContinuation())
							return returnContinuation(cValue);
						StaticFunctionEvaluator f = new StaticFunctionEvaluator(env);
						Argument output = f.simpleEquals(new Argument[]{result, cValue});
						if (Environment.isNull(output))
						{
							argumentIndex++;
							continue;
						}
						else
						{
							consArg = intermediate.innerList[1];
							break;
						}
						
					}
					argumentIndex++;
				}
				
			}
		}
		
		if (consArg != null)
		{
			Argument finalOutput = getFinalValue(resume, consArg);
			if (finalOutput!=null&&finalOutput.isContinuation())
				return returnContinuation(finalOutput);
			else
				return resetReturn(finalOutput);
		}
		
		return resetReturn(null);
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		SwitchEvaluator ceval = new SwitchEvaluator(env);
		
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
			if (j==0)
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
			else
			{
				nValue  = sargs[j].innerList[1];
				newArgs[j]=sargs[j];
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
				
				newArgs[j].innerList[1] = nValue;
			}
			
			
			
		}
		sargs = newArgs;
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
