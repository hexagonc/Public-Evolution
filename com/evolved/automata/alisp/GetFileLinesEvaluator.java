package com.evolved.automata.alisp;
import com.evolved.automata.filetools.*;


public class GetFileLinesEvaluator extends GenericEvaluator {

	public GetFileLinesEvaluator()
	{
		
	}
	
	public GetFileLinesEvaluator(Environment env)
	{
		super(env);
		
	}
	
	
	
	// First argument is the full filename string
	// Second argument is optional.  When true, input all data as a single line, otherwise input as a string list.  When not present, import as a string[]
	// Returns a single string or string cons depending on the second argument.  Return null if file is empty, doesn't exist, or there is any error
	public Argument processArgs(Argument[] args)
	{
		if(args==null||args.length==0)
			return null;
		
		String inputFileName = (String)args[0].oValue;
		String[] output = null;
		Argument[] aString= null;
		try
		{
			if ((args.length==2&&Environment.isNull(args[1])) ||  args.length==1)
			{
				output = StandardTools.getDataFileLines(inputFileName);
				if (output == null||output.length==0)
					return null;
				aString = new Argument[output.length];
				for (int i=0;i<output.length;i++)
					aString[i] = new Argument(null, output[i], null);
				return new Argument(null, null, aString);
				
			}
			else
			{
				String data = StandardTools.getDataFileLine(inputFileName);
				if (data == null)
					return null;
				else
					return new Argument(null, data, null);
			}
			
			
		}
		catch (Exception e)
		{
			return null;
		}
		
	}
	
	
	
}
