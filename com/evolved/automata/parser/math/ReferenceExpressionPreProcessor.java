package com.evolved.automata.parser.math;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import com.evolved.automata.parser.StringDistribution;
import com.evolved.automata.parser.math.ExpressionFactory.OperatorDefinition;
import com.evolved.automata.parser.math.ExpressionFactory.TokenResult;

public class ReferenceExpressionPreProcessor implements ExpressionFactory.ExpressionPreProcesssor
{
	StringDistribution _operatorMatcher;
	public Vector<HashSet<String>> operators;
	public HashSet<String> totalOperators;
	public HashSet<String> infixOperators;
	public HashSet<String> prefixOperators;
	public HashSet<String> postfixOperators;
	HashMap<String, HashSet<String>> operatorTypes; 
	
	public ReferenceExpressionPreProcessor()
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
	
}
