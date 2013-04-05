package com.evolved.automata.alisp;

/**
 * Fixed issue with unnecessary cloning of function arguments that was causing problems with continuations
 * @author Evolved8
 *
 */

public class LambdaFunctionEvaluator implements CompiledEvaluator 
{
	boolean totalFirst=true;
	boolean resume=true;
	public Argument returnContinuation(Argument arg)
	{
		resume=true;
		return new Argument(arg);
	}
	
	int subargumentIndex=0;
	public void resetState()
	{
		subargumentIndex=0;
		argumentIndex=0;
		resume=false;
	}
	
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	private Argument returnArg(Argument ret)
	{
		if (ret!=null&&ret.isContinuation())
			return returnContinuation(ret);
		else
			return resetReturn(ret);
	}
	
	public int argumentIndex=0;
	
	// Continuation Evaluator state
	int numberArguments;
	
	Environment executionEnv=null;
	
	Environment env;
	String[] argList;
	Argument[] body;
	
	public void setExecutionEnvironment(Environment exEv)
	{
		executionEnv=exEv;
	}
	
	public LambdaFunctionEvaluator(Environment env, Argument args,Argument[] body)
	{
		this.env = env;
		executionEnv=env;
		argList = Environment.getStringArrayFromListFast(args);
		this.body = body;
	}

	public LambdaFunctionEvaluator(Environment env, String[] bindingNames,Argument[] body)
	{
		this.env = env;
		executionEnv=env;
		argList = bindingNames;
		this.body = body;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	// Argument types should depend on the argument usage, however, there should be no fewer
	// arguments than the ones in the lambda definition
	@Override
	public Argument eval(Argument[] args) 
	{
		Argument avalue=null;
		if (args!=null&&argList!=null)
		{
			while (argumentIndex<Math.min(args.length, argList.length))
			{
				
				avalue = getArgumentFinalValue(resume, args[argumentIndex]);
				if (avalue!=null&&avalue.isContinuation())
					return returnContinuation(avalue);
				env.mapValue(argList[argumentIndex], Environment.copyArgument(avalue));
				
				argumentIndex++;
			}
			
		}
			
		Argument out=null;
		
		while (subargumentIndex<body.length)
		{
			out = getBodyFinalValue(resume, body[subargumentIndex]);
			if (out!=null&&out.isContinuation())
				return returnContinuation(out);
			if (out!=null&&out.isBreak())
			{
				out.setBreak(false);
				return resetReturn(out);
			}
			subargumentIndex++;
		}
			
		return resetReturn(out);
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		LambdaFunctionEvaluator ceval = new LambdaFunctionEvaluator(env, argList,body.clone());
		ceval.executionEnv=executionEnv;
		if (sargs!=null)
			ceval.setArgs((Argument[])sargs.clone());
		
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
		
		if (totalFirst)
		{
			totalFirst=false;
			resolveArgumentEvaluators();
			resolveBodyArgumentEvaluators();
		}
			
		return eval(sargs);
	}
	
	public void setEnvironment(Environment env)
	{
		this.env=env;
	}
	
	
	
	protected Argument getArgumentFinalValue(boolean r, Argument in)
	{
		if (in!=null&&in.isEvaluator())
			return evaluateArgument(in);
		else
			return getArgumentVariableValue(in, true);
	}
	
	protected Argument getBodyFinalValue(boolean r, Argument in)
	{
		if (in!=null&&in.isEvaluator())
			return evaluateArgument(in);
		else
			return getBodyVariableValue(in, true);
	}
	
	protected Argument getBodyVariableValue(Argument nValue, boolean resolveVariables)
	{
		if (nValue!=null&&resolveVariables&&nValue.isIdentifier())
			return env.getVariableValue((String)nValue.oValue);
		else
			return nValue;
	}
	
	protected Argument getArgumentVariableValue(Argument nValue, boolean resolveVariables)
	{
		if (nValue!=null&&resolveVariables&&nValue.isIdentifier())
			return executionEnv.getVariableValue((String)nValue.oValue);
		else
			return nValue;
	}
	
	protected void resolveArgumentEvaluators()
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
					lamb.setExecutionEnvironment(executionEnv);
					nValue = new Argument(lamb);
					
				}
				else
				{
					if (com instanceof RovingLexicalEvaluator)
					{
						
						com.setEnvironment(new Environment(executionEnv));
					}
					else
						com.setEnvironment(executionEnv);
					nValue = new Argument(com);
				}
				
			}
			else
			{
				if (nValue!=null&&nValue.isCons()&&nValue.innerList.length>0&&nValue.innerList[0].isIdentifier())
				{
					
					c = executionEnv.getFunction((String)nValue.innerList[0].oValue);
					if (c!=null)
					{
						c=c.clone();
						if (c instanceof LambdaFunctionEvaluator)
						{
							lamb = (LambdaFunctionEvaluator)c.clone();
							lamb.setExecutionEnvironment(executionEnv);
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
	
	protected void resolveBodyArgumentEvaluators()
	{
		if (body==null)
			return;
		Argument[] newArgs = new Argument[body.length];
		Argument nValue;
		CompiledEvaluator com;
		LambdaFunctionEvaluator lamb=null;
		Argument[] nArgs;
		CompiledEvaluator c;
		
		for (int j=0;j<body.length;j++)
		{
			nValue  = body[j];
			if (nValue!=null&&nValue.isEvaluator())
			{
				
				com = nValue.getEvaluator().clone();
				// TODO: Figure out a better way of determining if it is a lambda function
				if (com instanceof LambdaFunctionEvaluator)
				{
					lamb = (LambdaFunctionEvaluator)com;
					lamb.setExecutionEnvironment(env);
					//lamb.setEnvironment(env);
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
		body = newArgs;
	}
	
	protected Argument evaluateArgument(Argument arg)
	{
		CompiledEvaluator com = arg.getEvaluator();
		return com.getCompiledResult(resume);
		
	}
}
