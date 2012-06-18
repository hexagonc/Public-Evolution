package com.evolved.automata.alisp;

public class MakeLambdaEvaluator implements CompiledEvaluator {
	private void resetState()
	{
		
	}
	private Argument resetReturn(Argument data)
	{
		resetState();
		return data;
	}
	
	
	// Continuation Evaluator state
	
	Environment env;
	public MakeLambdaEvaluator(Environment env)
	{
		this.env = env;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	// First argument is optional and is the variable binding list.  Use null if not used
	// Remaining arguments are the body of the function definition
	@Override
	public Argument eval(Argument[] args) {
		if (args==null||args.length<2)
			return null;

		Argument[] remaining = new Argument[args.length-1];
		for (int i=1;i<args.length;i++)
			remaining[i-1]=args[i];
		LambdaFunctionEvaluator evaluator = new LambdaFunctionEvaluator(env, args[0], remaining);
		Argument f = new Argument(null,evaluator, null);
		f.setLambda(true);
		return f;
	}
	
	Argument[] sargs;
	
	
	public CompiledEvaluator clone()
	{
		MakeLambdaEvaluator ceval = new MakeLambdaEvaluator(env);
		
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
