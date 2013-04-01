package com.evolved.automata.parser.math;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import com.evolved.automata.parser.StringDistribution;


public class ExpressionFactory extends Expression
{
	public static interface ExpressionPreProcesssor
	{
		public TokenResult[] tokenize(String expression);
		public Argument[] argumentParser(TokenResult[] tokenizedAlgebraicExpression, HashMap<String, Argument> vars);
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
	
	public static class TokenResult
	{
		public boolean OPERANDP;
		public String value;
		public TokenResult(boolean operandP, String value)
		{
			this.value = value;
			OPERANDP = operandP;
		}
		
		public String toString()
		{
			return value;
		}
	}
	
	public static final ExpressionPreProcesssor _baseExpressionPreprocessor = new ExpressionPreProcesssor()
	{
		StringDistribution _operatorMatcher;
		public Vector<HashSet<String>> operators;
		public HashSet<String> totalOperators;
		public HashSet<String> infixOperators;
		public HashSet<String> prefixOperators;
		public HashSet<String> postfixOperators;
		HashMap<String, HashSet<String>> operatorTypes; 
		
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
			
			operatorTypes = new HashMap<String, HashSet<String>>();
			operatorTypes.put("infixOperators", infixOperators);
			operatorTypes.put("prefixOperators", prefixOperators);
			operatorTypes.put("postfixOperators", postfixOperators);
		}
		
		@Override
		public TokenResult[] tokenize(String expression) {
			expression = expression.trim();
			int i = 0;
			StringBuilder token = new StringBuilder();
			LinkedList<TokenResult> tokens = new LinkedList<TokenResult>();
			Integer[] ends;
			while (i<expression.length())
			{
				ends = _operatorMatcher.matchString(i, expression);
				if (ends!=null)
				{
					if (token.length()>0)
						tokens.add(new TokenResult(true, token.toString()));
					tokens.add(new TokenResult(false, expression.substring(i, ends[0])));
					i = ends[0];
					token = new StringBuilder();
				}
				else
				{
					token.append(expression.charAt(i));
					i++;
				}
			}
			if (token.length()>0)
				tokens.add(new TokenResult(true, token.toString()));
			return tokens.toArray(new TokenResult[0]);
		}

		@Override
		public Argument[] argumentParser(TokenResult[] tokenizedAlgebraicExpression, HashMap<String, Argument> vars) 
		{
			Argument[] args = new Argument[tokenizedAlgebraicExpression.length];
			
			for (int i=0;i<args.length;i++)
			{
				if (vars!=null && vars.containsKey(tokenizedAlgebraicExpression[i].value))
				{
					args[i] = new VariableArgument(tokenizedAlgebraicExpression[i].value, vars);
				}
				else if (tokenizedAlgebraicExpression[i].OPERANDP)
					args[i] = parseOperand(tokenizedAlgebraicExpression[i].value);
				else
					args[i] = parseOperator(tokenizedAlgebraicExpression[i].value);
			}
			return args;
		}

		@Override
		public OperatorDefinition getOperatorDefinition() {
			return new OperatorDefinition()
			{

				@Override
				public HashSet<String> getInfixOperators() {
					
					return infixOperators;
				}

				@Override
				public HashSet<String> getPrefixOperators() {
					
					return prefixOperators;
				}

				@Override
				public HashSet<String> getPostfixOperators() {
					
					return postfixOperators;
				}

				@Override
				public Vector<HashSet<String>> getOperatorPrecedence() {
					
					return operators;
				}

				@Override
				public HashSet<String> getTotalOperators() {
					return totalOperators;
				}
				
			};
		}

		@Override
		public Argument parseOperator(String opToken) {
			if (infixOperators.contains(opToken))
			{
				return new BinaryOperator(opToken);
			}
			else if (prefixOperators.contains(opToken))
			{
				return new Function(opToken);
			}
			return null;
		}

		@Override
		public Argument parseOperand(String argToken) {
			return new SimpleDoubleArgument(Double.parseDouble(argToken));
		}
		
	};
	
	public static Expression parse(String expression)
	{
		return parse(expression, null);
	}
	
	public static Expression parse(String expression, HashMap<String, Argument> vars)
	{
		
		TokenResult[] algebraicForm = _baseExpressionPreprocessor.tokenize(expression);
		HashMap<String, HashSet<String>> operatorTypes = getOperatorTypes(_baseExpressionPreprocessor);
		OperatorDefinition def = _baseExpressionPreprocessor.getOperatorDefinition();
		Vector<HashSet<String>> prec = def.getOperatorPrecedence();
		TokenResult[] tokenStack = convertToRPL(operatorTypes, prec, algebraicForm);
		Argument[] argStack =  _baseExpressionPreprocessor.argumentParser(tokenStack, vars);
		return new Expression(argStack, vars);
	}
	
	public static double quickEvaluate(String sRep, HashMap<String, Double> vars)
	{
		HashMap<String, Argument> aVars = null;
		if (vars!=null)
		{
			aVars = new HashMap<String, Argument>();
			for (String key:vars.keySet())
			{
				aVars.put(key, new SimpleDoubleArgument(vars.get(key)));
			}
		}
		Expression e = parse(sRep, aVars);
		
		return e.getDoubleValue();
		
	}
	
	public static double quickEvaluate(String sRep)
	{
		return quickEvaluate(sRep, null);
	}
	
	public static TokenResult[] convertToRPL(HashMap<String, HashSet<String>> operatorTypes, Vector<HashSet<String>> operatorPrecendenceSet, TokenResult[] algebraic_expr)
	{
		return convertToRPL(operatorTypes, operatorPrecendenceSet, 0, 0, algebraic_expr.length-1, algebraic_expr);
	}
	
	
	private static HashMap<String, HashSet<String>> getOperatorTypes(ExpressionPreProcesssor processor)
	{
		OperatorDefinition def = processor.getOperatorDefinition();
		HashMap<String, HashSet<String>> types = new HashMap<String, HashSet<String>>();
		types.put("infixOperators", def.getInfixOperators());
		types.put("prefixOperators", def.getPrefixOperators());
		types.put("postfixOperators", def.getPostfixOperators());
		return types;
	}
	
	private static int findNextRightParens(int start, TokenResult[] input)
	{
		int b=1;
		for (int i=start; i<input.length;i++)
		{
			if (input[i].value.equals(")"))
			{
				b--;
				if (b==0)
					return i;
			}
			else if (input[i].value.equals("("))
				b++;
					
		}
		return -1;
	}
	
	private static TokenResult[] convertToRPL(HashMap<String, HashSet<String>> operatorTypes, Vector<HashSet<String>> operatorPrecendenceSet, final int opPrec, final int start, final int end, final TokenResult[] algebraic_expr)
	{
		if (opPrec == operatorPrecendenceSet.size())
		{
			return new TokenResult[]{algebraic_expr[start]};
		}
		TokenResult[] left;
		TokenResult[] right;
		TokenResult[] subexp;
		int cont;
		for (int i=start;i<=end;i++)
		{
			if (algebraic_expr[i].value.equals("(") && !operatorPrecendenceSet.get(opPrec).contains("(")) // skip over parenthesis
			{
				i = findNextRightParens(i+1, algebraic_expr);
				continue;
			}
			
			for (String operator:operatorPrecendenceSet.get(opPrec))
			{
				if (operator.equals(algebraic_expr[i].value))
				{
					if (operatorTypes.get("infixOperators").contains(operator))
					{
						
						left = convertToRPL(operatorTypes, operatorPrecendenceSet, opPrec, start, i-1, algebraic_expr);
						right = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, i+1, end, algebraic_expr);
						if (operator.equals(","))
							return add(left, right);
						else
							return add(add(left, right), algebraic_expr[i]);
					}
					else if (operatorTypes.get("prefixOperators").contains(operator))
					{
						if (algebraic_expr[i+1].value.equals("("))
						{
							cont = findNextRightParens(i+2, algebraic_expr);
							subexp = convertToRPL(operatorTypes, operatorPrecendenceSet,0, i+2, cont-1, algebraic_expr);
							if (cont+1<=end)
								right = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, cont+1, end, algebraic_expr);
							else
								right = new TokenResult[0];
							return add(add(subexp, algebraic_expr[i]), right);
						}
						right = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, i+1, end, algebraic_expr);
						return add(right, algebraic_expr[i]);
					}
					else if (operatorTypes.get("postfixOperators").contains(operator))
					{
						left = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, start, i-1, algebraic_expr);
						return add(left, algebraic_expr[i]);
					}
					else
					{
						if (algebraic_expr[i].value.equals("(")) 
						{
							cont = findNextRightParens(i+1, algebraic_expr);
							subexp = convertToRPL(operatorTypes, operatorPrecendenceSet,0, i+1, cont-1, algebraic_expr);
							if (i-1>= start)
								left = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, start, i-1, algebraic_expr);
							else
								left = new TokenResult[0];
							if (cont+1<=end)
								right = convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec, cont+1, end, algebraic_expr);
							else
								right = new TokenResult[0];
							
							return add(left, add(subexp, right));
						}
					}
				}
			}
			
		}
		
		return convertToRPL(operatorTypes, operatorPrecendenceSet,opPrec+1, start, end, algebraic_expr);
	}
	
	private static TokenResult[] add(TokenResult[] list, TokenResult value)
	{
		TokenResult[] out = new TokenResult[list.length+1];
		for (int i=0;i<list.length;i++)
			out[i] = list[i];
		out[list.length] = value;
		return out;
	}
	
	private static TokenResult[] add(TokenResult[] l1, TokenResult[] l2)
	{
		TokenResult[] out = new TokenResult[l1.length + l2.length];
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
