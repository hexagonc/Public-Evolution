package com.evolved.automata.alisp;
import java.util.Hashtable;
import java.util.LinkedList;



public class ProjectEvaluator implements CompiledEvaluator
{
	
	int state=0;
	
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
		state=RESOLVING_MAP;
		project = null;
		input = null;
		projectionName = null;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	

	final int RESOLVING_MAP=0;
	final int RESOLVING_KEY=1;
	final int PROCESSING_DATA=2;
	
	
	Argument project = null;
	Argument input = null;
	String projectionName = null;
	
	Environment env;
	public ProjectEvaluator(Environment env)
	{
		this.env=env;
	}

	public Environment getEnvironment()
	{
		return env;
	}
	
	
	
	// First argument is either:
	//    (1) An atom
	//				(1) Hashtable<String, String> 
	//				(2) Hashtable<String, LinkedList<String>>
	// 	  (2) A cons
	//		        (1) <Hashtable<String,String>
	//			    (2) <Hashtable<String, LinkedList<String>>
	// Second argument is a string
	
	// Return is cons String 
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
			
			
			while (true)
			{
				switch (state)
				{
					case RESOLVING_MAP:
						input = getFinalValue(resume, args[0]);
						if (ScalarEvaluator.continuation(input))
							return returnContinuation(input);
						state = RESOLVING_KEY;
						break;
					case RESOLVING_KEY:
						project = getFinalValue(resume, args[1]);
						if (ScalarEvaluator.continuation(project))
							return returnContinuation(project);
						if (Environment.isNull(project)||Environment.isNull(input))
							return resetReturn(null);
						projectionName = (String)project.oValue;
						state = PROCESSING_DATA;
						break;
					default:
						LinkedList<String> output = null;
						output = extractProjection(input, projectionName);
						if (output!=null)
						{
							Argument[] out = new Argument[output.size()];
							int i=0;
							for (String oString:output)
							{
								out[i++] = new Argument(null, oString, null);
							}
							return resetReturn(new Argument(null, null, out));
						}
						else
							return resetReturn(null);
				}
			}
			
		}
		
		
	}
	
	private LinkedList<String> extractProjection(Argument a, String name)
	{
		Object value;
		LinkedList<String> output = null, subList;
		
		if (Environment.isNull(a))
			return null;
		if (a.isAtom())
		{
			if (a.oValue instanceof Hashtable)
			{
				Hashtable<String,?> result = (Hashtable<String,?>)a.oValue;
				if (result.containsKey(name))
				{
					value = result.get(name);
					if (value instanceof String)
					{
						output = new LinkedList<String>();
						output.add((String)value);
					}
					else
					{
						if (value instanceof LinkedList)
						{
							output = (LinkedList<String>)value;
						}
					}
					return output;
				}
			}
			return null;
		}
		else
		{
			if (a.isCons())
			{
				output = new LinkedList<String>();
				for (Argument subArg:a.innerList)
				{
					subList = extractProjection(subArg, name);
					if (subList!=null)
						output.addAll(subList);
				}
				
			}
		}
		if ((output!=null)&&(output.size()>0))
			return output;
		else
			return null;
	}
	
	Argument[] sargs;
	
	public CompiledEvaluator clone()
	{
		ProjectEvaluator ceval = new ProjectEvaluator(env);
		
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
