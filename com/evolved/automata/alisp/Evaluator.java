package com.evolved.automata.alisp;
import java.util.*;


public abstract class Evaluator implements CompiledEvaluator {

	boolean firstRun=true;
	public abstract CompiledEvaluator clone();

	protected Environment env;
	protected Argument[] sargs;
	protected boolean resume=false;
	
	protected int argumentIndex=0;
	
	protected abstract void  resetState();
	
	
	protected Argument returnContinuation(Argument arg)
	{
		resume=true;
		return new Argument(arg);
	}
	
	protected Argument resetReturn(Argument data)
	{
		resetState();
		return data;
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
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	protected HashSet<String> getStringSet(String[] s)
	{
		if (s==null)
			return null;
		HashSet<String> oSet = new HashSet<String>();
		for (String k:s)
			oSet.add(k);
		return oSet;	
	}
	
	protected Argument[] getRemaining(Argument[] args)
	{
		if (args==null||args.length<=2)
			return null;
		Argument[] remain = new Argument[args.length-2];
		for (int i=0;i<args.length-2;i++)
			remain[i]=args[i+2];
		return remain;
	}
	
	protected Argument[] getRemaining(Argument[] args, int minimum)
	{
		if (args==null||args.length<=minimum)
			return null;
		Argument[] remain = new Argument[args.length-minimum];
		for (int i=0;i<args.length-minimum;i++)
			remain[i]=args[i+minimum];
		return remain;
	}
	
	/**
	 * Checks whether the required number of arguments are present.
	 * @param args
	 * @param numArgs - this can be either the exact number of arguments required or the minimum number required
	 * @param greater - when true, numArgs represents the minimum number of arguments that must be present.  Otherwise
	 * is the exact number
	 * @return true means that the arguments are invalid
	 */
	protected boolean invalidArgs(Argument[] args, int numArgs, boolean greater)
	{
		if (greater)
			return args==null||args.length<numArgs;
		else
			return args==null||args.length!=numArgs;
	}
	
	protected boolean invalidArgs(Argument[] args, int numArgs)
	{
		return invalidArgs(args, numArgs, false);
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
						c= c.clone();
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
