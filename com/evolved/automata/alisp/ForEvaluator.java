package com.evolved.automata.alisp;

public class ForEvaluator  extends Evaluator implements RovingLexicalEvaluator {

	protected  void  resetState()
	{
		state=EVALUATING_CONS;
		argumentIndex=0;
		numArguments=0;
		iterationListArg = null;
		output= null;
		consArg=null;
		iterationBodyArg=null;
		resume=false;
		bodyIndex=0;
		numBodyArguments=0;
	}
	
	int state = 0;
	final int EVALUATING_CONS=0;
	final int BINDING_VARIABLE=1;
	final int EVALUATING_BODY=2;
	final int EVALUATING_RETURNVALUE=3;
	
	int argumentIndex=0;
	int numArguments=0;
	int numBodyArguments=0;
	int bodyIndex=0;
	Argument[] iterationListArg = null;
	
	String vName = null;
	Argument output= null;
	Argument consArg=null;
	Argument[] iterationBodyArg=null;

	// First argument is the loop variable name
	// Second argument is the cons to iterate over (can evaluate to a cons)
	// Third argument is the thing to return when the loop exits
	// remaining arguments are the loop body
	@Override
	public Argument eval(Argument[] args) {
		if (args == null)
			return null;
		Argument bindingArg = args[0];
		Argument loopArg=null;
		Argument breakSignal=null;
		try
		{
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
							
							iterationBodyArg = new Argument[args.length-3];
							numArguments = iterationListArg.length;
							numBodyArguments = iterationBodyArg.length;
							for (int i=0;i<numBodyArguments;i++)
								iterationBodyArg[i] = args[i+3];
							state = BINDING_VARIABLE;
						}
						break;
					case BINDING_VARIABLE:
						if (argumentIndex>=iterationListArg.length)
						{
							state=EVALUATING_RETURNVALUE;
						}
						else
						{
							
							loopArg = getFinalValue(resume, iterationListArg[argumentIndex]);
							if (loopArg!=null&&loopArg.isContinuation())
								throw new ContinuationSignal(loopArg);
							env.mapValue(vName, loopArg);
							state=EVALUATING_BODY;
						}
						break;
					case EVALUATING_BODY:
						breakSignal=evaluateBody(iterationBodyArg);
						if (breakSignal!=null&&breakSignal.isBreak())
						{
							state = EVALUATING_RETURNVALUE;
						}
						else
							state = BINDING_VARIABLE;
						break;
					default:
						output = args[2];
						output = getFinalValue(resume, output);
						if (output!=null&&output.isContinuation())
							throw new ContinuationSignal(output);
						return resetReturn(output);
						
				}
			}
		}
		catch (ContinuationSignal cs)
		{
			return returnContinuation(cs.getArgument());
		}
	}
	
	public Argument evaluateBody(Argument[] body) throws ContinuationSignal
	{
		Argument output=null;
		while (bodyIndex<numBodyArguments)
		{
			output=getFinalValue(resume, body[bodyIndex]);
			if (output!=null&&output.isContinuation())
				throw new ContinuationSignal(output);
			bodyIndex++;
		}
		argumentIndex++;
		bodyIndex=0;
		return output;
	}
	
	public CompiledEvaluator clone()
	{
		ForEvaluator ceval = new ForEvaluator(env);
		ceval.setArgs(sargs);
		return ceval;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public ForEvaluator(Environment env)
	{
		this.env = env;
	}
	
	
}
