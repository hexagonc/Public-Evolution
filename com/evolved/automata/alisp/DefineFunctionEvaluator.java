package com.evolved.automata.alisp;

public class DefineFunctionEvaluator implements CompiledEvaluator{
	
	// Not implementing continuation logic in this class.  Continuations can't be 
	// passed through this Evaluator anyway
	
	
	
	Environment env;
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public DefineFunctionEvaluator(Environment env)
	{
		this.env = env;
	}

	// First argument is a String literal with the name of the function to map
	// Second argument is a list of binding variables
	// The remaining arguments are s-expressions to be evaluated in order
	@Override
	public Argument eval(Argument[] args) {
		if ((args == null)||(args.length==0))
			return null;

		if (args.length <2)
		{
			return null;
			
		}
		else
		{
			Argument lvalue = args[0];

			String functionName = (String)lvalue.oValue;
			
			Argument[] lambdaArgs = new Argument[args.length-1];
			for (int i=0;i<lambdaArgs.length;i++)
				lambdaArgs[i]=args[i+1];
			
			CompiledEvaluator lambdaEvaluator, evaluator = new MakeLambdaEvaluator(new Environment(env));
			
			Argument lambda = evaluator.eval(lambdaArgs);
			lambdaEvaluator = (CompiledEvaluator)lambda.oValue;
			env.mapEvaluator(functionName, lambdaEvaluator);
			return lambda;
		}
		
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		DefineFunctionEvaluator ceval = new DefineFunctionEvaluator(env);
		
		ceval.setArgs(sargs);
		return ceval;
	}
	public void setArgs(Argument[] args)
	{
		sargs = args;
	}
	public Argument getCompiledResult(boolean resume)
	{
		
		return eval(sargs);
	}
	
	public void setEnvironment(Environment env)
	{
		this.env=env;
	}
}
