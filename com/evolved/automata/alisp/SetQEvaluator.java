package com.evolved.automata.alisp;

public class SetQEvaluator implements CompiledEvaluator{
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
		state=PREEVALUATE_BINDING_VARIABLE;
		lvalue=null;
		rvalue = null;
		vName=null;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	private Argument lvalue=null;
	private Argument rvalue = null;
	public int state=0;
	public static final int PREEVALUATE_BINDING_VARIABLE=0;
	public static final int EVALUATE_VALUE=1;
	String vName=null;
	
	Environment env;
	public SetQEvaluator(Environment env)
	{
		this.env = env;
	}
	public Environment getEnvironment()
	{
		return env;
	}
	
	@Override
	public Argument eval(Argument[] args) {
		if ((args == null)||(args.length!=2))
			return null;
		
		
		while (true)
		{
			switch (state)
			{
				case PREEVALUATE_BINDING_VARIABLE:
					if (args[0].isIdentifier())
						lvalue = args[0];
					else
						lvalue = getFinalValue(resume, args[0]);
					if (Environment.isNull(lvalue) || !lvalue.isIdentifier())
						return resetReturn(null);
					vName=(String)lvalue.oValue;
					state = EVALUATE_VALUE;
					break;
				default:
					rvalue = args[1];
					rvalue = getFinalValue(resume, rvalue);
					
		
					if (ScalarEvaluator.continuation(rvalue))
						return returnContinuation(rvalue);
					Argument nArg;
					if (rvalue!=null&&rvalue.isConstantP())
					{
						nArg = Environment.makeAtom(rvalue.oValue);
						
					}
					else if (rvalue!=null&&rvalue.isCons()&&rvalue.innerList.length==0)
					{
						nArg = new Argument(null, null, new Argument[0]);
					}
					else
						nArg=rvalue;
					env.mapValue(vName, nArg);
					return resetReturn(rvalue);
				
					
			}
		
		}
	}
	
	Argument[] sargs;
	
	public CompiledEvaluator clone()
	{
		SetQEvaluator ceval = new SetQEvaluator(env);
		
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
