package com.evolved.automata.alisp;

import com.evolved.automata.filetools.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Environment {
	
	public static class ParserResult
	{
		public Argument argument = null;
		public int endIndex=0;
		
		public ParserResult(Argument arg, int end)
		{
			argument=arg;
			endIndex=end;
		}
	}
	
	
	
	Hashtable<String, CompiledEvaluator> functionsNames;
	
	Hashtable<String, Argument> valueNames;
	
	HashSet<String> lexical;
	
	public static final Argument nullValue = new Argument(); 
	
	public static final char commentChar = ';';
	
	private Environment prevEnvironment;
	
	public static final Argument True = new Argument(null, new Object(),null);
	
	Hashtable<String, CompiledEvaluator> staticFunctions;
	
	public Environment()
	{
		
		lexical= new HashSet<String>();
		addStandardFunctions();
		addStaticFunctions();
		
	}
	
	private void addStandardFunctions()
	{
		valueNames = new Hashtable<String, Argument>();
		valueNames.put("F", new Argument());
		
		functionsNames = new Hashtable<String, CompiledEvaluator>();
		functionsNames = new Hashtable<String, CompiledEvaluator>();
		
		functionsNames.put("if", new IfEvaluator(this));
		functionsNames.put("or", new OrEvaluator(this));
		functionsNames.put("and", new AndEvaluator(this));

		functionsNames.put("setf", new SetFEvaluator(this));
		functionsNames.put("for", new ForEvaluator(this));
		functionsNames.put("while", new WhileEvaluator(this));
		functionsNames.put("break", new BreakEvaluator(this)); 
		functionsNames.put("progn", new ProgEvaluator(this));  
		functionsNames.put("some", new SomeEvaluator(this)); 
		functionsNames.put("find", new FindAllEvaluator(this));  
		functionsNames.put("switch", new SwitchEvaluator(this)); 
		functionsNames.put("all", new AllEvaluator(this));  
		functionsNames.put("trace-label", new TraceBreakEvaluator(this)); 
		
		functionsNames.put("mapcar", new MapCarEvaluator(this));  
		  
		functionsNames.put("defun", new DefineFunctionEvaluator(this));  
		functionsNames.put("setq", new SetQEvaluator(this));  
		functionsNames.put("read-file", new GetFileLinesEvaluator(this));  
		
		functionsNames.put("lambda", new MakeLambdaEvaluator(this));
		functionsNames.put("funcall", new FuncallEvaluator(this));
		functionsNames.put("append", new AppendEvaluator(this));
		functionsNames.put("concat", new ConcatEvaluator(this));
		
		functionsNames.put("let", new LetEvaluator(this));
		functionsNames.put("pop", new PopEvaluator(this));
		functionsNames.put("eval", new EvaluateEvaluator(this));
		functionsNames.put("bind-function", new BindFunctionEvaluator(this));
		
		
		functionsNames.put("=", new NumericEqualEvaluator(this));
		functionsNames.put("project", new ProjectEvaluator(this));
		functionsNames.put("when", new WhenEvaluator(this));
		functionsNames.put("unless", new UnlessEvaluator(this));
		functionsNames.put("cond", new CondEvaluator(this));
		this.addLexicalEvaluators("let");
		this.addLexicalEvaluators("lambda");
		this.addLexicalEvaluators("mapcar");
		this.addLexicalEvaluators("for");
		this.addLexicalEvaluators("all");
		this.addLexicalEvaluators("some");
		this.addLexicalEvaluators("find");
	}
	
	public void addLexicalEvaluators(String name)
	{
		lexical.add(name);
	}
	
	private void addStaticFunctions()
	{
		staticFunctions = new Hashtable<String, CompiledEvaluator>();
		
		StaticFunctionEvaluator sEvaluator = new StaticFunctionEvaluator(this);
		staticFunctions.put("abs", sEvaluator);
		staticFunctions.put("random",sEvaluator);
		staticFunctions.put("time",sEvaluator);
		staticFunctions.put("cos", sEvaluator);
		staticFunctions.put("string",sEvaluator);
		staticFunctions.put("double",sEvaluator);
		staticFunctions.put("int",sEvaluator);
		staticFunctions.put("+",sEvaluator);
		staticFunctions.put("-",sEvaluator);
		staticFunctions.put("*",sEvaluator);
		staticFunctions.put("/",sEvaluator);
		staticFunctions.put(">=",sEvaluator);
		staticFunctions.put("<=",sEvaluator);
		staticFunctions.put(">",sEvaluator);
		staticFunctions.put("<",sEvaluator);
		staticFunctions.put("list",sEvaluator);
		staticFunctions.put("equals",sEvaluator);
		staticFunctions.put("format",sEvaluator);
		staticFunctions.put("pattern-format",sEvaluator);
		staticFunctions.put("resolve", sEvaluator);
		staticFunctions.put("split",sEvaluator);
		staticFunctions.put("id",sEvaluator);
		staticFunctions.put("not",sEvaluator);
		staticFunctions.put("first",sEvaluator);
		staticFunctions.put("second",sEvaluator);
		staticFunctions.put("nth",sEvaluator);
		staticFunctions.put("len",sEvaluator);
		staticFunctions.put("last",sEvaluator);
		staticFunctions.put("mod",sEvaluator);
		staticFunctions.put("make-array",sEvaluator);
		staticFunctions.put("starts-with",sEvaluator);
		staticFunctions.put("ends-with",sEvaluator);
		staticFunctions.put("#'",sEvaluator);
		staticFunctions.put("read-url",sEvaluator);
		staticFunctions.put("read-surl",sEvaluator);
	}
	
	
	public Environment(Environment prev)
	{
		prevEnvironment = prev;
		
		lexical = prev.lexical;
		addStaticFunctions();
		
	}
	
	public CompiledEvaluator parserIntoEvaluator(String s_expression)
	{
		ParserResult result = parse(s_expression);
		if (result==null)
			throw new RuntimeException("Fast compilation error: Invalid lisp expression");
		Argument out = result.argument;
		if (out.isEvaluator())
		{
			
			return out.getEvaluator();
		}
		return null;
	}
	
	
	public Argument getParsedResult(String input)
	{
		return getParsedResult(false,input);
	}
	
	public Argument getParsedResult(boolean resume,String s_expression)
	{
		ParserResult result = parse(s_expression);
		if (result==null)
			throw new RuntimeException("Fast compilation error: Invalid lisp expression");
		Argument out = result.argument;
		if (out.isEvaluator())
		{
			CompiledEvaluator eval = out.getEvaluator();
			return eval.getCompiledResult(resume);
		}
		return out;
	}
	
	
	public Argument getPreParsedResult(CompiledEvaluator evaluator, boolean resume)
	{
		return evaluator.getCompiledResult(resume);
	}
	
	public ParserResult parse(String input)
	{
		return parse(null, input, 0);
	}
	
	public ParserResult parse(Environment env, String input, int start)
	{
		
		//leading whitespace
		while (Character.isWhitespace(input.charAt(start)) && start<input.length())
		{
			start++;
		}
		
		// Can't match for tokens at the end of the stirng
		if (start==input.length())
			return null;
		
		// Try numeric
		int index=start;
		char c;
		StringBuilder num = new StringBuilder();
		boolean contains_decimal=false, contains_integer=false, contains_fraction=false;
		boolean contains_negative=false;
		while (index<input.length())
		{
			c = input.charAt(index);
			if (Character.isDigit(c))
			{
				if (contains_decimal)
					contains_fraction=true;
				if (!contains_fraction)
					contains_integer=true;
				num.append(c);
				
			}
			else if (c=='-')
			{
				if (!contains_negative&&!contains_integer&&!contains_decimal&&!contains_fraction)
				{
					contains_negative=true;
				}
				else
					break;
				
				num.append(c);
				
			}
			else if (c=='.')
			{
				if (!contains_decimal)
				{
					contains_decimal=true;
					num.append(c);
				}
				else
					break;
				
			}
			else if (Character.isWhitespace(c) || c=='(' || c==')')
			{
				if (contains_fraction || (contains_integer && !contains_decimal))
				{
					if (contains_fraction)
						return new ParserResult(makeAtom(new Double(num.toString())).setConstant(), index);
					else
						return new ParserResult(makeAtom(new Integer(num.toString())).setConstant(), index);
				}
				else
					break;
				
			}
			else
			{
				break;
			}
			if (index==input.length()-1)
			{
				if (contains_fraction || (contains_integer && !contains_decimal))
				{
					if (contains_fraction)
						return new ParserResult(makeAtom(new Double(num.toString())).setConstant(), index+1);
					else
						return new ParserResult(makeAtom(new Integer(num.toString())).setConstant(), index+1);
				}
			}
			index++;
		}
		
		// Identifiers
		index=start;
		boolean startsLetter=false;
		StringBuilder id = new StringBuilder();
		while (index<input.length())
		{
			c = input.charAt(index);
			if (!Character.isWhitespace(c)&&!Character.isDigit(c) && c!='(' && c!=')' && c!='\"')
			{
				if (!startsLetter)
					startsLetter=true;
				id.append(c);
			}
			else if (c!='(' && c!=')' && !Character.isWhitespace(c)&&c!='\"')
			{
				if (startsLetter)
					id.append(c);
				else
					break;
				
			}
			else if (c=='(' || c==')' || Character.isWhitespace(c))
			{
				if (startsLetter)
				{
					Argument out = makeAtom(id.toString());
					out.setIdentifier(true);
					return new ParserResult(out, index);
				}
				
				break;
			}
			else
			{
				break;
			}
			if (index==input.length()-1)
			{
				if (startsLetter)
				{
					Argument out = makeAtom(id.toString());
					out.setIdentifier(true);
					return new ParserResult(out, index+1);
				}
			}
			index++;
		}
		
		// String
		index=start;
		boolean contains_quote=false, previous_delimiter=false;
		StringBuilder sBuilder = new StringBuilder();
		while (index<input.length())
		{
			c = input.charAt(index);
			if (c=='\"')
			{
				if (!contains_quote)
				{
					contains_quote=true;
				
				}
				else if (previous_delimiter)
				{
					previous_delimiter=false;
					sBuilder.append(c);
				}
				else
				{
					return new ParserResult(makeAtom(sBuilder.toString()).setConstant(), index+1);
				}
			}
			else if (c == '\\')
			{
				if (!contains_quote)
					break;
				previous_delimiter=true;
			}
			else
			{
				if (!contains_quote)
					break;
				if (previous_delimiter)
				{
					sBuilder.append('\\');
					previous_delimiter=false;
				}
				sBuilder.append(c);
			}
			if (index==input.length()-1)
			{
				break;
			}
			index++;
		}
		
		// s-expression
		index=start;
		LinkedList<Argument> argList = new LinkedList<Argument>();
		ParserResult result=null;
		
		CompiledEvaluator evaluator=null;
		String name;
		boolean first=true;
		Argument function;
		if (input.charAt(index)=='(')
		{
			index++;
			while (index<input.length())
			{
				if (Character.isWhitespace(input.charAt(index)))
				{
					index++;
					continue;
				}
				if (evaluator!=null)
				{
					result = parse(evaluator.getEnvironment(), input, index);
				}
				else
					if (env!=null)
						result = parse(env, input, index);
					else
						result = parse(this, input, index);
				
				if (result!=null)
				{
					function = result.argument;
					if (first)
					{
						first=false;
						if (function.isIdentifier())
						{
							name = (String)function.oValue;
							if (staticFunctions.containsKey(name))
							{
								evaluator=staticFunctions.get(name).clone();
								
								evaluator.setEnvironment(env==null?this:env);
								argList.add(function);
							}
							else
							{
								evaluator = getFunction(name);
								if (evaluator==null)
								{
									argList.add(function);
								}
								
							}
							
						}
						else
							argList.add(function);
					}
					else
						argList.add(function);
					
					index = result.endIndex;
				}
				else if (input.charAt(index) == ')')
				{
					if (evaluator!=null)
					{
						if (argList.size()>0)
							evaluator.setArgs(argList.toArray(new Argument[0]));
						else
							evaluator.setArgs(null);
						return new ParserResult(new Argument(evaluator), index+1);
					}
					else
						if (argList.size()==0)
							return new ParserResult(new Argument(null, null, new Argument[0]), index+1);
						else
							return new ParserResult(new Argument(null, null, argList.toArray(new Argument[0])), index+1);
				}
				else
					return null;
					
			}
		}
		
		return null;
	}
	
	public void addStaticFunction(String name, CompiledEvaluator staticEvaluator)
	{
		staticFunctions.put(name, staticEvaluator);
	}
	
	public void addFunctions(Hashtable<String, CompiledEvaluator> functionsNames)
	{
		this.functionsNames=functionsNames;
	}
	
	public void addSingleFunction(String name, CompiledEvaluator evaluator, boolean lexicalScopedFunctionP)
	{
		if (functionsNames==null)
			functionsNames = new Hashtable<String, CompiledEvaluator>();
		if (evaluator!=null)
		{
			functionsNames.put(name, evaluator);
			if (lexicalScopedFunctionP)
				lexical.add(name);	
		}
			
	}
	
	public void addSingleStaticFunction(String name, CompiledEvaluator evaluator)
	{
		staticFunctions.put(name,evaluator);
			
	}
	
	public Environment getChildEnvironment()
	{
		return new Environment(this);
	}
	
	public void addIdentifiers(Hashtable<String, Argument> valueNames)
	{
		this.valueNames=valueNames;
	}
	
	public void mapValue(String key, Argument value)
	{
		if (valueNames == null)
			valueNames = new Hashtable<String, Argument>();
		if (isNull(value))
		{
			value=new Argument();
		}
		valueNames.put(key, value);
			
	}
	
	public void mapEvaluator(String key, CompiledEvaluator value)
	{
		if (functionsNames == null)
			functionsNames = new Hashtable<String, CompiledEvaluator>();
		functionsNames.put(key, value);
			
	}
	
	
	public Environment(Hashtable<String, CompiledEvaluator> functionsNames, Hashtable<String, Argument> valueNames)
	{
		this.functionsNames = functionsNames;
		this.valueNames = valueNames;
		
		addStaticFunctions();
	}
	
	
	public CompiledEvaluator getFunction(String name)
	{
		Hashtable<String, CompiledEvaluator> map = functionsNames;
		boolean cont=true;
		CompiledEvaluator cEvaluator;
		boolean lexicalScopeP = lexical!=null&&lexical.contains(name);
		Environment currentEnvironment=this;
		while (cont)
		{
			if (map!=null&&map.containsKey(name))
			{
				cEvaluator = map.get(name);
				cEvaluator = (CompiledEvaluator)cEvaluator.clone();
				if (lexicalScopeP || cEvaluator instanceof RovingLexicalEvaluator|| cEvaluator instanceof LambdaFunctionEvaluator)
				{
					cEvaluator.setEnvironment(new Environment(this));
				}
				else
				{
					cEvaluator.setEnvironment(this);
				}
				
				return cEvaluator;
			}
			if (currentEnvironment.prevEnvironment!=null)
			{
				currentEnvironment = currentEnvironment.prevEnvironment;
				map = currentEnvironment.functionsNames;
			}
			else
				return null;
		}
		return null;
	}
	

	private Argument[] mapCompiledArguments(Argument[] sargs)
	{
		return mapCompiledArguments(true, sargs);
	}
	
	private Argument[] mapCompiledArguments(boolean resume, Argument[] sargs)
	{
		
		CompiledEvaluator com;
		if (sargs!=null)
		{
			Argument arg = null;
			for (int i=0;i<sargs.length;i++)
			{
				while (sargs[i].isEvaluator())
				{
					com = sargs[i].getEvaluator();
					arg = com.getCompiledResult(resume);
					sargs[i]=arg;
				}
			}
		}
		return sargs;
	}
	

	
	public Argument getVariableValue(String name)
	{
		
		Argument value = (valueNames==null)?null:valueNames.get(name);
		if (value!=null||value == nullValue)
		{
			return (value!=null)?value:null;
		}
		
		if (prevEnvironment!=null)
			return prevEnvironment.getVariableValue(name);
		else
		{
			if (staticFunctions.containsKey(name))
				return makeAtom(name);
			else
				return null;
		}
	}

	/**
	 * This is the fast version of this function for when it is already known that each value of list
	 * is a string literal
	 * @param innerList -  a list of expressions that should resolve to a string literal
	 */
	public static String[] getStringArrayFromListFast(Argument list)
	{
		LinkedList<String> output = new LinkedList<String>();
		if (list == null)
			return null;
		if (isNull(list))
			return null;
		for (Argument value:list.innerList)
		{
			output.add((String)value.oValue);
		}
		if (output.size()==0)
			return null;
		else
			return output.toArray(new String[0]);
	}

	public static String getStringFromArg(Argument input)
	{
		
		if (!isNull(input)&&input.isAtom())
			return (String)input.oValue;
		else
			return null;
	}

	
	public static Argument getConsFromStringArray(String[] arg)
	{
		if (arg==null)
			return null;
		Argument[] o = new Argument[arg.length];
		for (int i=0;i<arg.length;i++)
			o[i] = new Argument(null, arg[i], null);
		return new Argument(null, null, o);
	}
	
	public static Argument getConsFromStringHashSet(HashSet<String> hash)
	{
		if (hash==null)
			return null;
		return getConsFromStringArray(hash.toArray(new String[0]));
	}
	
	public static Hashtable<String, String> buildHashtableFromCons(Argument cons)
	{
		if (cons==null)
			return null;
		Hashtable<String, String> output = new Hashtable<String, String>();
		
		
		
		String key, value;
		if (cons.isCons())
		{
			for (Argument kvPair:cons.innerList)
			{
				
				if (kvPair.isCons())
				{
					key = (String)kvPair.innerList[0].oValue;
					value = (String)kvPair.innerList[1].oValue;
					output.put(key, value);
				}
			}
		}
		if (output.size()==0)
			return null;
		else
			return output;

	}
	
	public static boolean isNull(Argument arg)
	{
		return arg==null || arg.isNull();
	}
	
	
	public Argument[] getDataFromList(Argument list)
	{
		if (list!=null&&list.isCons())
			return list.innerList;
		else
			return null;
	}
	int counter=0;
	public void loadFromFileFast(BufferedReader fileReader) 
	{
		String data = null;
		try
		{
			String lineinput=null;
			StringBuilder sbuilder = new StringBuilder();
			while ((lineinput=fileReader.readLine())!=null)
			{
				if (lineinput.trim().length()==0 || lineinput.trim().charAt(0)==commentChar)
				{
					continue;
				}
				sbuilder.append(" " + lineinput);
			}
			
			
			data = sbuilder.toString();
			
			ParserResult p = null;
			int start=0;
			p = parse(data);
			Argument a;
			CompiledEvaluator ev=null;
			while (true)
			{
				start = p.endIndex;
				a = p.argument;
				if (a.isEvaluator())
				{
					ev=a.getEvaluator();
					getPreParsedResult(ev, false);
				}
				if (data.substring(start).trim().length()>0)
				{
					data = data.substring(start);
					p = parse(data);
				}
				else
					break;
				counter++;
			}
			
		}
		catch (IOException ie)
		{
			throw new RuntimeException(ie.toString());
		}
		catch (Exception e)
		{
			throw new RuntimeException("parse error near: "+ data.substring(0, data.length()));
		}
		finally
		{
			if (fileReader!=null)
			{
				try
				{
					fileReader.close();
				}
				catch (Exception ei)
				{
					
				}
			}
		}
	}
	
	
	public void loadFromFileFast(String filefullname) 
	{
		try
		{
			String[] dataLines = StandardTools.getDataFileLines(filefullname);
			StringBuilder sbuilder = new StringBuilder();
			for (String lineinput:dataLines)
			{
				if (lineinput.trim().length()==0 || lineinput.trim().charAt(0)==commentChar)
				{
					continue;
				}
				sbuilder.append(" " + lineinput);
			}
			
			String data = sbuilder.toString();
			
			ParserResult p = null;
			int start=0;
			p = parse(data);
			Argument a;
			CompiledEvaluator ev=null;
			while (true)
			{
				start = p.endIndex;
				a = p.argument;
				if (a.isEvaluator())
				{
					ev=a.getEvaluator();
					getPreParsedResult(ev, false);
				}
				if (data.substring(start).trim().length()>0)
				{
					data = data.substring(start);
					p = parse(data);
				}
				else
					break;
			}
			
		}
		catch (IOException ie)
		{
			throw new RuntimeException(ie.toString());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error loading grammar in input file: [" + filefullname+ "] " + e.toString());
		}
	}
	
	
	public static Argument makeAtom(Object arg)
	{
		if (arg==null)
			return nullValue;
		return new Argument(null, arg, null);
	}
	
	public static Argument makeCons(Object[] args)
	{
		if (args==null)
			return nullValue;
		Argument[] cargs = new Argument[args.length];
		for (int i=0;i<cargs.length;i++)
			cargs[i]=makeAtom(args[i]);
		return new Argument(null, null, cargs);
	}
	
	public static Argument[] makeArgArray(Object[] oargs)
	{
		Argument[] args = null;
		if (oargs!=null)
		{
			args = new Argument[oargs.length];
			for (int i=0;i<oargs.length;i++)
				args[i] = makeAtom(oargs[i]);
		}
		return args;
	}
	
	public static Object getDataFromArgument(Argument arg)
	{
		if (isNull(arg))
			return null;
		return arg.oValue;
	}
	
	public static Argument copyArgument(Argument rvalue)
	{
		Argument mValue = new Argument();
		if (Environment.isNull(rvalue))
			return rvalue;
		mValue.parent=rvalue.parent;
		mValue.oValue=rvalue.oValue;
		mValue.sValue=rvalue.sValue;
		mValue.innerList = rvalue.innerList;
		mValue.TYPE=rvalue.TYPE;
		mValue.continuationP=rvalue.continuationP;
		mValue.evaluator = rvalue.evaluator;
		mValue.identifierString=rvalue.identifierString;
		mValue.lambdaP=rvalue.lambdaP;
		return mValue;
			
	}
	public static Object[] getArrayFromArgArray(Argument[] args)
	{
		Object[] output = null;
		if (args!=null)
		{
			output = new Object[args.length];
			for (int i=0;i<args.length;i++)
				output[i] = getDataFromArgument(args[i]);
		}
		return output;
	}
	
	public static Object[] getArrayFromCons(Argument cons)
	{
		Object[] output = null;
		
		if (!isNull(cons)&&cons.isCons()&&cons.innerList!=null)
		{
			output = new Object[cons.innerList.length];
			for (int i=0;i<cons.innerList.length;i++)
				output[i] = cons.innerList[i];
		}
		return output;
	}
	
	/**
	 * Converts a cons representing a hashtable, (( key1 value1) (key2 value2) ... ) 
	 * into a hashtable <String, Object>
	 * @param cons
	 * @return
	 */
	public static Hashtable<String, Object> getHashtableFromCons(Argument cons)
	{
		Hashtable<String, Object> output = new Hashtable<String, Object>();
		if (isNull(cons)||!cons.isCons())
			return null;
		
		Argument consPair = null;
		Object[] rawPair;
		try
		{
			
			for (Object kvPair: getArrayFromCons(cons))
			{
				consPair = (Argument)kvPair;
				rawPair = getArrayFromCons(consPair);
				output.put((String)rawPair[0], rawPair[1]);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid cons for creating hashtable");
			
		}
		return output;
	}
	
	/**
	 * Converts a cons representing a hashtable, (( key1 value1) (key2 value2) ... ) 
	 * into a hashtable <String, String>
	 * @param cons
	 * @return
	 */
	public static Hashtable<String, String> getStringHashtableFromCons(Argument cons)
	{
		Hashtable<String, String> output = new Hashtable<String, String>();
		if (isNull(cons)||!cons.isCons())
			return null;
		
		Argument consPair = null;
		Object[] rawPair;
		try
		{
			
			for (Object kvPair: getArrayFromCons(cons))
			{
				consPair = (Argument)kvPair;
				rawPair = getArrayFromCons(consPair);
				output.put((String)((Argument)rawPair[0]).oValue , (String)((Argument)rawPair[1]).oValue);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid cons for creating hashtable");
			
		}
		return output;
	}
	
	public Object evaluateScalarFunction(String fName, Object ... params)
	{
		Argument[] fArgs = makeArgArray(params);
		CompiledEvaluator cEvaluator = getFunction(fName);
		Argument output = cEvaluator.eval(fArgs);
		return getDataFromArgument(output);
	}
	
	public Object[] evaluateConsFunction(String fName, Object ... params)
	{
		Argument[] fArgs = makeArgArray(params);
		CompiledEvaluator cEvaluator = getFunction(fName);
		Argument output = cEvaluator.eval(fArgs);
		return getArrayFromCons(output);
	}
	
	public static String getStringArgResult(Argument arg)
	{
		Object out = getDataFromArgument(arg);
		if (out!=null)
			return (String)out;
		else
			return null;
	}
	
	public void bindScalarValue(String variableName, Object value)
	{
		mapValue(variableName, makeAtom(value));
	}
	
	public void bindConsValue(String variableName, Object[] value)
	{
		mapValue(variableName, makeCons(value));
	}
	
	public static int getIntFromArg(Argument arg)
	{
		Number numb = (Number)arg.oValue;
		return numb.intValue();
	}
	
	public static double getDoubleFromArg(Argument arg)
	{
		Number numb = (Number)arg.oValue;
		return numb.doubleValue();
	}
	
	
}
