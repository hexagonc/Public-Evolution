package com.evolved.automata.alisp;
import java.util.*;
public class FindAllEvaluator implements CompiledEvaluator, RovingLexicalEvaluator
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
		resultArg=null;
		predicateExpr=null;
		iterationListArg = null;
		output=null;
		matching=null;
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	public int argumentIndex=0;
	Argument consArg=null;
	// Continuation Evaluator state
	int state;
	final int EVALUATING_CONS=0;
	final int BINDING_VARIABLE=1;
	final int EVALUATING_PREDICATE=2;
	Argument[] iterationListArg = null;
	Argument resultArg=null;
	Argument predicateExpr=null;
	String vName = null;
	Argument output= null;
	LinkedList<Argument > matching;
	
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	
	public FindAllEvaluator(Environment env)
	{
		this.env = env;
	}

	// First argument is the name of a binding variable
	// Second argument is a cons or atom
	// Third argument is optional and is a predicate expression as a function of the binding variable.  
	//  When not present, any non-null values will be returned, otherwise returns any values of cons list where
	//  predicate is true
	// Returns all members of cons List where the predicateExp is true 
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		Argument bindingArg = args[0];
		Argument loopArg=null;
		while (true)
		{
			switch (state)
			{
				case EVALUATING_CONS:
					
					vName= (String)bindingArg.oValue;
					consArg = getFinalValue(resume, args[1]);
					if (consArg!=null&&consArg.isContinuation())
						return returnContinuation(consArg);
					else
					{
						matching=new LinkedList<Argument>();
						if (Environment.isNull(consArg))
							return resetReturn(consArg);
						if (consArg.isAtom())
						{
							iterationListArg = new Argument[1];
							iterationListArg[0] = consArg;
						}
						else
						{
							iterationListArg=consArg.innerList;
						}
						state = BINDING_VARIABLE;
					}
					break;
				case BINDING_VARIABLE:
					if (argumentIndex>=iterationListArg.length)
						return resetReturn(new Argument(null, null, matching.toArray(new Argument[0])));
					loopArg = getFinalValue(resume, iterationListArg[argumentIndex]);
					if (loopArg!=null&&loopArg.isContinuation())
						return returnContinuation(loopArg);
					env.mapValue(vName, loopArg);
					
					state=EVALUATING_PREDICATE;
					
					break;
				default:
					if (args.length>2)
					{
						predicateExpr = args[2];
						output = getFinalValue(resume, predicateExpr);
						if (output!=null&&output.isContinuation())
							return returnContinuation(output);
						if (!Environment.isNull(output))
							matching.add(output);
					}
					else
					{
						if (!Environment.isNull(loopArg))
							matching.add(loopArg);
					}
					state=BINDING_VARIABLE;
					argumentIndex++;
			}
		}
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		FindAllEvaluator ceval = new FindAllEvaluator(env);
		
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
