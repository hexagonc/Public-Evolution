package com.evolved.automata.parser.math;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import com.evolved.automata.filetools.StandardTools;
import com.evolved.automata.parser.CFGParser;
import com.evolved.automata.parser.StringDistribution;
import com.evolved.automata.parser.math.ExpressionFactory.TokenResult;

public class WordNumberExpressionPreProcessor extends ReferenceExpressionPreProcessor
{
	CFGParser expressionParser;
	public WordNumberExpressionPreProcessor() throws IOException
	{
		BufferedReader greader;
		greader = StandardTools.getReaderFromPackageResource("/com/evolved/automata/parser/math/number_pattern.txt");
		initialize(new CFGParser(greader));
	}
	
	public WordNumberExpressionPreProcessor(CFGParser parser) 
	{
		initialize(parser);
	}
	
	private void initialize(CFGParser parser)
	{
		expressionParser = parser;
		canonicalOperatorMap = new HashMap<String, String>();
		// Define operator groups
		infixOperators = new HashSet<String>();
		infixOperators.add("+");
		infixOperators.add("plus ");
		infixOperators.add("minus ");
		infixOperators.add("-");
		
		infixOperators.add("/");
		infixOperators.add("of ");
		infixOperators.add("times ");
		infixOperators.add("*");
		infixOperators.add("over ");
		infixOperators.add("divided by ");
		
		canonicalOperatorMap.put("+", "+");
		canonicalOperatorMap.put("-", "-");
		canonicalOperatorMap.put("*", "*");
		canonicalOperatorMap.put("/", "/");
		
		
		canonicalOperatorMap.put("plus ", "+");
		canonicalOperatorMap.put("minus ", "-");
		canonicalOperatorMap.put("of ", "*");
		canonicalOperatorMap.put("over ", "/");
		canonicalOperatorMap.put("times ", "*");
		canonicalOperatorMap.put("divided by ", "/");
		
		
		prefixOperators = new HashSet<String>();
		
		postfixOperators = new HashSet<String>();
		
		postfixOperators.add("percent");
		canonicalOperatorMap.put("percent ", "percent");
		canonicalOperatorMap.put("percent", "percent");
		
		// Define operator precedence
		operators = new Vector<HashSet<String>>();
		HashSet<String> ops = new HashSet<String>();
		
		ops = new HashSet<String>();
		ops.add("+");
		ops.add("-");
		ops.add("plus ");
		ops.add("minus ");
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("*");
		ops.add("/");
		ops.add("times ");
		ops.add("over ");
		ops.add("divided by ");
		ops.add("of ");
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("percent");
		
		operators.add(ops);
		
		ops = new HashSet<String>();
		ops.add("(");
		ops.add(")");
		canonicalOperatorMap.put(")", ")");
		canonicalOperatorMap.put("(", "(");
		operators.add(ops);
		
		totalOperators = new HashSet<String>();
		totalOperators.addAll(ops);
		totalOperators.addAll(infixOperators);
		totalOperators.addAll(prefixOperators);
		
		_operatorMatcher = new StringDistribution();  
		
		for (String op:totalOperators)
		{
			_operatorMatcher.addString(op);
		}
		_operatorMatcher.addString("percent ");
		
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
				tokens.add(new TokenResult(false, canonicalOperatorMap.get(expression.substring(i, ends[0]))));
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
		return processAlternativeTokenizations(tokens.toArray(new TokenResult[0]));
	}
	
	private TokenResult[] processAlternativeTokenizations(TokenResult[] preTokens)
	{
		LinkedList<TokenResult> out = new LinkedList<TokenResult>();
		String compositeToken;
		TokenResult prevResult;
		Argument parsedOperand;
		for (int i=0;i<preTokens.length;i++)
		{
			if (preTokens[i].OPERANDP)
				out.add(preTokens[i]);
			else
			{
				prevResult = (out.size()>0)?out.getLast():null;
				if (prevResult!=null && i<preTokens.length-1)
				{
					if (prevResult.OPERANDP && preTokens[i+1].OPERANDP)
					{
						compositeToken = prevResult.value + preTokens[i].value + preTokens[i+1].value;
						parsedOperand = parseOperand(compositeToken);
						if (parsedOperand!=null)
						{
							prevResult.replaceContents(parsedOperand, true, compositeToken);
							i+=1;
						}
						else
						{
							out.add(preTokens[i]);
							
						}
					}
					else if (!prevResult.OPERANDP && preTokens[i+1].OPERANDP)
					{
						compositeToken = preTokens[i].value + preTokens[i+1].value;
						parsedOperand = parseOperand(compositeToken);
						if (parsedOperand!=null)
						{
							out.add(new TokenResult(parsedOperand, true, compositeToken));
							i+=1;
						}
						else
						{
							out.add(preTokens[i]);
							
						}
						
					}
					else
					{
						out.add(preTokens[i]);
						
					}
				}
				else
				{
					out.add(preTokens[i]);
					
				}
			}
			
		}
		
		return out.toArray(new TokenResult[0]);
	}
	
	@Override
	public Argument parseOperator(String opToken) {
		
		String mappedToken = canonicalOperatorMap.get(parseToken(opToken));
		
		if (infixOperators.contains(mappedToken))
		{
			return new BinaryOperator(mappedToken);
		}
		else if (prefixOperators.contains(mappedToken))
		{
			return new Function(mappedToken);
		}
		else if (postfixOperators.contains(mappedToken))
		{
			return new Function(mappedToken);
		}
		return null;
	}
	
	

	@Override
	public Argument parseOperand(String argToken) 
	{
		Double dValue = parseNumber(prepareSentenceTextInput(argToken.trim()), expressionParser);
		if (dValue==null)
			return null;
		return new SimpleDoubleArgument(dValue.doubleValue());
	}
	
	private String parseToken(String token)
	{
		Hashtable<String, LinkedList<String>> map = expressionParser.matchPathExtrude(
				token, 
				"operators, '^'", 
				new String[]{"operators"},
				new String[]{"operators"});
		if (map!=null)
			return map.get("operators").getFirst().substring(1);
		else
			return null;
	}
	
	private Double parseNumber(String numString, CFGParser parser)
	{
		LinkedList<String> captureList = null;
		Hashtable<String, LinkedList<String>> matchList = parser.matchPathExtrude(
				numString, 
				"number, '$'*, '^'", 
				new String[]{"-", "numeric", "decimal_whole_number", "numeric_digit", "sign", "thousands", "hundreds", "decade", "digit", "below_20", "0", "word_number", "number", "whole_number", "fractional_part", "fract_digit"}, 
				new String[]{"number", "thousands", "hundreds", "decade", "fract_digit", "digit", "sign", "below_20"});
		if (matchList!=null)
		{
			boolean containsFractionP = matchList.containsKey("fractional_part");
			boolean containsIntegerP = matchList.containsKey("whole_number");
			double fractionalPart = 0;
			double integerPart = 0;
			if (containsFractionP)
			{
				if (matchList.containsKey("fract_digit"))
				{
					captureList = matchList.get("fract_digit");
				}
				else
					captureList = matchList.get("numeric_digit");
				fractionalPart = getFractionalPart(captureList);
			}
			
			if (containsIntegerP)
			{
				if (matchList.containsKey("decimal_whole_number"))
				{
					integerPart = Integer.parseInt(matchList.get("numeric").getFirst());
				}
				else
				{
					integerPart = getIntegerPartFromWords(matchList);
				}
			}
			double total = integerPart + fractionalPart;
			if (matchList.containsKey("-"))
				total*= -1;
			return total;	
		}
		return null;
	}
	
	private double getIntegerPartFromWords(Hashtable<String, LinkedList<String>> matchList)
	{
		int thousands = 0, hundreds = 0, decades = 0, below_20 = 0;
		if (matchList.containsKey("thousands"))
		{
			thousands = Integer.parseInt(matchList.get("thousands").getFirst());
		}
		
		if (matchList.containsKey("hundreds"))
		{
			hundreds = Integer.parseInt(matchList.get("hundreds").getFirst());
		}
		
		if (matchList.containsKey("decade"))
		{
			decades = Integer.parseInt(matchList.get("decade").getFirst());
			if (matchList.containsKey("digit"))
				decades+=Integer.parseInt(matchList.get("digit").getFirst());
		}
		else
		{
			if (matchList.containsKey("below_20"))
				below_20 = Integer.parseInt(matchList.get("below_20").getFirst());
		}
		return thousands + hundreds + decades + below_20;
	}
	
	private double getFractionalPart(LinkedList<String> digits)
	{
		double total = 0;
		
		double multiplier = 1;
		for (String digit:digits)
		{
			if (total == 0)
				total = Integer.parseInt(digit);
			else
				total = (10*total) + Integer.parseInt(digit);
			multiplier*=10;
		}
		return total/multiplier;
	}
	
	private  String retokenizeText(String baseText, boolean includeEndToken)
	{
		
		String[] split =  baseText.split(" ");
		StringBuilder sBuilder = new StringBuilder();
		String part;
		for (int i=0;i<split.length;i++)
		{
			part = split[i];
			if (part.length()>0)
			{
				if (i==0)
				{
					sBuilder.append(part);
				}
				else
				{
					sBuilder.append(" ");
					sBuilder.append(part);
				}
			}
		}
		return sBuilder.toString() + ((includeEndToken)?" ":"");
	}
	
	private  String prepareSentenceTextInput(String lineInput)
	{
		
		lineInput = lineInput.toLowerCase();
		lineInput = retokenizeText(lineInput, true);
		return lineInput;
	}
}
