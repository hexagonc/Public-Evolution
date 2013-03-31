package com.evolved.automata.parser.math;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import com.evolved.automata.parser.StringDistribution;



public class Expression 
{
	public static interface ExpressionPreProcesssor
	{
		public String[] tokenize(String expression);
		public Argument[] argumentParser(String[] tokenizedAlgebraicExpression);
		public OperatorDefinition getOperatorDefinition();
		public Argument parseOperator(String opToken);
		public Argument parseOperand(String argToken);
	}
	
	public static interface OperatorDefinition
	{
		public HashSet<String> getInfixOperators();
		public HashSet<String> getPrefixOperators();
		public HashSet<String> getPostfixOperators();
		public Vector<HashSet<String>> getOperatorPrecedence();
		public HashSet<String> getTotalOperators();
	}
	
	private static class TokenResult
	{
		public boolean OPERANDP;
		public String value;
		public TokenResult(boolean operandP, String value)
		{
			this.value = value;
			OPERANDP = operandP;
		}
	}
	
	Argument[] _argStack;
	HashMap<String, Argument> _variables;
	static StringDistribution _operatorMatcher;
	
	public static Vector<HashSet<String>> operators;
	public static HashSet<String> totalOperators;
	public static HashSet<String> infixOperators;
	public static HashSet<String> prefixOperators;
	public static HashSet<String> postfixOperators;
	static
	{
		// Define operator groups
		infixOperators = new HashSet<String>();
		infixOperators.add("+");
		infixOperators.add("-");
		infixOperators.add("*");
		infixOperators.add("/");
		infixOperators.add("^");
		infixOperators.add(",");
		
		prefixOperators = new HashSet<String>();
		prefixOperators.add("sq");
		prefixOperators.add("sin");
		prefixOperators.add("cos");
		
		postfixOperators = new HashSet<String>();
		
		// Define operator precedence
		operators = new Vector<HashSet<String>>();
		HashSet<String> ops = new HashSet<String>();
		// Add lowest precedent
		ops.add(",");
		operators.add(ops);
		ops = new HashSet<String>();
		// Add lowest precedent
		ops.add("+");
		ops.add("-");
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("*");
		ops.add("/");
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("^");
		ops.add("sq");
		ops.add("sin");
		ops.add("cos");
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("(");
		ops.add(")");
		operators.add(ops);
		
		totalOperators = new HashSet<String>();
		totalOperators.addAll(ops);
		totalOperators.addAll(infixOperators);
		totalOperators.addAll(prefixOperators);
		totalOperators.addAll(postfixOperators);
		_operatorMatcher = new StringDistribution();  
			
		for (String op:totalOperators)
		{
			_operatorMatcher.addString(op);
		}
	}
	
	private Expression(Argument[] argstack, HashMap<String, Argument> vars)
	{
		_argStack = argstack;
		_variables = vars;
	}
	
	public SimpleDoubleArgument getDoubleArgument()
	{
		LinkedList<Argument> evaluationStack = new LinkedList<Argument>();
		for (Argument a:_argStack)
		{
			a.evaluate(evaluationStack);
		}
		if (evaluationStack.size()==1)
		{
			SimpleDoubleArgument o = (SimpleDoubleArgument)evaluationStack.getFirst();
			return o;
		}
		return null;
	}
	
	public double getDoubleValue()
	{
		SimpleDoubleArgument o = getDoubleArgument();
		return o.getValue();
	}
	
	public static Expression parse(String sRep, HashMap<String, Argument> vars)
	{
		String[] algebraicForm = tokenize(sRep);
		String[] sargStack = convertToRPL(algebraicForm);
		Argument[] argStack =  parseArguments(sargStack, vars);
		return new Expression(argStack, vars);
		
	}
	
//	public static Expression parse(String sRep, HashMap<String, Argument> vars, ExpressionPreProcesssor processor)
//	{
//		String[] algebraicForm = processor.tokenize(sRep);
//		if (algebraicForm ==null)
//			algebraicForm = tokenize(sRep);
//		String[] sargStack = processor.argumentParser(algebraicForm); 
//				
//				convertToRPL(algebraicForm);
//		Argument[] argStack =  parseArguments(sargStack, vars);
//		return new Expression(argStack, vars);
//		
//	}
	
	
	public static double quickEvaluate(String sRep, HashMap<String, Double> vars)
	{
		String[] algebraicForm = tokenize(sRep);
		String[] sargStack = convertToRPL(algebraicForm);
		HashMap<String, Argument> aVars = null;
		if (vars!=null)
		{
			aVars = new HashMap<String, Argument>();
			for (String key:vars.keySet())
			{
				aVars.put(key, new SimpleDoubleArgument(vars.get(key)));
			}
		}
		Argument[] argStack =  parseArguments(sargStack, aVars);
		return (new Expression(argStack, aVars)).getDoubleValue();
		
	}
	
	//TODO: Finish this
	private static Argument[] parseArguments(String[] tokenized, HashMap<String, Argument> vars)
	{
		Argument[] args = new Argument[tokenized.length];
		String argName;
		for (int i=0;i<args.length;i++)
		{
			argName = tokenized[i].trim();
			if (vars!=null && vars.containsKey(argName))
			{
				args[i] = new VariableArgument(argName, vars);
			}
			else if (infixOperators.contains(argName))
			{
				args[i] = new BinaryOperator(argName);
			}
			else if (prefixOperators.contains(argName))
			{
				args[i] = new Function(argName);
			}
			else
			{
				// TODO: Generalize this for summing operations and units
				args[i] = parseNumber(argName);
			}
		}
		return args;
	}
	
	private static Argument parseNumber(String s)
	{
		// TODO: Make this work for units
		return new SimpleDoubleArgument(Double.parseDouble(s));
	}

	private static String[] tokenize(String input)
	{
		// TODO: make this work with units
		return fastTokenize(input);
	}
	
	private static String[] fastTokenize(String input )
	{
		int i = 0;
		StringBuilder token = new StringBuilder();
		LinkedList<String> tokens = new LinkedList<String>();
		Integer[] ends;
		while (i<input.length())
		{
			ends = _operatorMatcher.matchString(i, input);
			if (ends!=null)
			{
				if (token.length()>0)
					tokens.add(token.toString());
				tokens.add(input.substring(i, ends[0]));
				i = ends[0];
				token = new StringBuilder();
			}
			else
			{
				token.append(input.charAt(i));
				i++;
			}
		}
		if (token.length()>0)
			tokens.add(token.toString());
		return tokens.toArray(new String[0]);
	}
	
	public Argument getVariableValue(String name)
	{
		return _variables.get(name);
	}
	
	
	
	
	public static String[] convertToRPL(String[] algebraic_expr)
	{
		return convertToRPL(0, 0, algebraic_expr.length-1, algebraic_expr);
	}
	
	
	
	private static int findNextRightParens(int start, String[] input)
	{
		int b=1;
		for (int i=start; i<input.length;i++)
		{
			if (input[i].equals(")"))
			{
				b--;
				if (b==0)
					return i;
			}
			else if (input[i].equals("("))
				b++;
					
		}
		return -1;
	}
	
	public static String[] convertToRPL(final int opPrec, final int start, final int end, final String[] algebraic_expr)
	{
		if (opPrec == operators.size())
		{
			return new String[]{algebraic_expr[start]};
		}
		String[] left;
		String[] right;
		String[] subexp;
		int cont;
		for (int i=start;i<=end;i++)
		{
			if (algebraic_expr[i].equals("(") && !operators.get(opPrec).contains("(")) // skip over parenthesis
			{
				i = findNextRightParens(i+1, algebraic_expr);
				continue;
			}
			
			for (String operator:operators.get(opPrec))
			{
				if (operator.contains(algebraic_expr[i]))
				{
					if (infixOperators.contains(operator))
					{
						
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						if (operator.equals(","))
							return add(left, right);
						else
							return add(add(left, right), operator);
					}
					else if (prefixOperators.contains(operator))
					{
						if (algebraic_expr[i+1].equals("("))
						{
							cont = findNextRightParens(i+2, algebraic_expr);
							subexp = convertToRPL(0, i+2, cont-1, algebraic_expr);
							if (cont+1<=end)
								right = convertToRPL(opPrec, cont+1, end, algebraic_expr);
							else
								right = new String[0];
							return add(add(subexp, operator), right);
						}
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						return add(right, operator);
					}
					else if (postfixOperators.contains(operator))
					{
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						return add(left, operator);
					}
					else
					{
						if (algebraic_expr[i].equals("(")) // <-- this should always be true at this point
						{
							cont = findNextRightParens(i+1, algebraic_expr);
							subexp = convertToRPL(0, i+1, cont-1, algebraic_expr);
							if (i-1>= start)
								left = convertToRPL(opPrec, start, i-1, algebraic_expr);
							else
								left = new String[0];
							if (cont+1<=end)
								right = convertToRPL(opPrec, cont+1, end, algebraic_expr);
							else
								right = new String[0];
							
							return add(left, add(subexp, right));
						}
					}
				}
			}
			
		}
		
		return convertToRPL(opPrec+1, start, end, algebraic_expr);
	}
	
	public static String[] convertToRPL(Vector<HashSet<String>> operatorPrecendenceSet, final int opPrec, final int start, final int end, final String[] algebraic_expr)
	{
		if (opPrec == operatorPrecendenceSet.size())
		{
			return new String[]{algebraic_expr[start]};
		}
		String[] left;
		String[] right;
		String[] subexp;
		int cont;
		for (int i=start;i<=end;i++)
		{
			if (algebraic_expr[i].equals("(") && !operatorPrecendenceSet.get(opPrec).contains("(")) // skip over parenthesis
			{
				i = findNextRightParens(i+1, algebraic_expr);
				continue;
			}
			
			for (String operator:operatorPrecendenceSet.get(opPrec))
			{
				if (operator.contains(algebraic_expr[i]))
				{
					if (infixOperators.contains(operator))
					{
						
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						if (operator.equals(","))
							return add(left, right);
						else
							return add(add(left, right), operator);
					}
					else if (prefixOperators.contains(operator))
					{
						if (algebraic_expr[i+1].equals("("))
						{
							cont = findNextRightParens(i+2, algebraic_expr);
							subexp = convertToRPL(0, i+2, cont-1, algebraic_expr);
							if (cont+1<=end)
								right = convertToRPL(opPrec, cont+1, end, algebraic_expr);
							else
								right = new String[0];
							return add(add(subexp, operator), right);
						}
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						return add(right, operator);
					}
					else if (postfixOperators.contains(operator))
					{
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						return add(left, operator);
					}
					else
					{
						if (algebraic_expr[i].equals("(")) // <-- this should always be true at this point
						{
							cont = findNextRightParens(i+1, algebraic_expr);
							subexp = convertToRPL(0, i+1, cont-1, algebraic_expr);
							if (i-1>= start)
								left = convertToRPL(opPrec, start, i-1, algebraic_expr);
							else
								left = new String[0];
							if (cont+1<=end)
								right = convertToRPL(opPrec, cont+1, end, algebraic_expr);
							else
								right = new String[0];
							
							return add(left, add(subexp, right));
						}
					}
				}
			}
			
		}
		
		return convertToRPL(opPrec+1, start, end, algebraic_expr);
	}
	
	public static String[] add(String[] list, String value)
	{
		String[] out = new String[list.length+1];
		for (int i=0;i<list.length;i++)
			out[i] = list[i];
		out[list.length] = value;
		return out;
	}
	
	public static String[] add(String[] l1, String[] l2)
	{
		int total = 0;
		String[] out = new String[total = (l1.length + l2.length)];
		int j=0;
		for (int i=0;i<l1.length;i++)
		{
			out[j] = l1[i];
			j++;
		}
		for (int i=0;i<l2.length;i++)
		{
			out[j] = l2[i];
			j++;
		}
		return out;
	}
}
