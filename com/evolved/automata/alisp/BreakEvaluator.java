package com.evolved.automata.alisp;

public class BreakEvaluator extends GenericEvaluator
{
	
	public BreakEvaluator(Environment env)
	{
		this.env = env;
	}
	
	public BreakEvaluator()
	{
		
	}
	
	// Uses any number of arguments.  Last argument in list is attached the break property
	@Override
	public Argument processArgs(Argument[] args) {
		Argument output=null;
		if (args!=null&&args.length>0)
		{
			output= args[args.length-1];
			
			if (output==null)
				output = new Argument(null, "T", null);
			output.setBreak(true);
		}
		else
		{
			output = new Argument(null, "T", null);
			output.setBreak(true);
		}
		return output;
	}
	
	
}
