package com.evolved.automata.test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.evolved.automata.filetools.*;
import com.evolved.automata.parser.*;
import java.io.*;
import java.util.regex.*;
import java.util.regex.Matcher;
import com.evolved.automata.parser.math.*;


public class MathTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String grammarResource = "/com/evolved/automata/test/MathPatternDef.txt";
		BufferedReader greader;
		
		CFGParser parser = null, numberParser;
		String inputString = "5 take away 7 times 12 plus five fifths of one percent of 50 plus three ";
		LinkedList<String> results;
		String[] expr = new String[]{"6", "*", "87", "*", "123", "+", "sin", "x", "+", "cos", "x", "*", "sin", "y"};
		String[] expr2 = new String[]{"sin", "(", "6", ",", "89", ")", "+", "100"};
		String[] expected = new String[]{"6", "87", "123", "*", "*", "x", "sin", "x", "cos", "+", "y", "sin", "*", "+"};
		String[] out = null;
		
		// 6*(87+123+sin(2))+50*100
		
		try
		{
			greader = StandardTools.getReaderFromPackageResource(grammarResource);
			parser = new CFGParser(greader);
			if (parser.match(inputString, "simple_expression, '^'", new String[]{"base_arg", "arg_group"}))
			{
				System.out.println(parser.getFirstCapturedList("base_arg"));
				System.out.println(parser.getFirstCapturedList("arg_group"));
			}
			
			Expression exp = null; 
//			exp = ExpressionFactory.parse("6*(87+123+sin(2))+50*100", null);
//			System.out.println(exp.getDoubleValue());
//			
//			Node list = createList(new int[]{1,2,3,4,5}, 0);
//			System.out.println(list);
//			System.out.println(reverse(list, null));
//			
			greader = StandardTools.getReaderFromPackageResource("/com/evolved/automata/parser/math/number_pattern.txt");
			numberParser = new CFGParser(greader);
//			Double o = parseNumber(prepareSentenceTextInput("7.23"), numberParser);
//			System.out.println(o);
//			
//			Hashtable<String, LinkedList<String>> map;
//			map = parseHundreds(prepareSentenceTextInput("twelve hundred"), numberParser);
//			System.out.println(map);
			
			String spokenExpression = "five hundred fifty-three plus minus 7.23 times 200 percent of 250";
			WordNumberExpressionPreProcessor wordProcessor = new WordNumberExpressionPreProcessor(numberParser);
			exp = ExpressionFactory.parse(wordProcessor, spokenExpression, null);
			System.out.println(exp.getDoubleValue());
		}
		catch (Exception e)
		{
			java.io.StringWriter traceText = new java.io.StringWriter();
			java.io.PrintWriter pWriter = new java.io.PrintWriter(traceText,true);
			e.printStackTrace(pWriter);
			pWriter.close();
			System.out.println(traceText.toString());
			
		}

	}
	public static Hashtable<String, LinkedList<String>> parseHundreds(String numString, CFGParser parser)
	{
		Hashtable<String, LinkedList<String>> matchList = parser.matchPathExtrude(
				numString, 
				"thousand_rep, '$'*, '^'", 
				new String[]{"hundred_multipler", "decade", "digit", "below_20", "0", "word_number", "number", "whole_number", "fractional_part", "fract_digit"}, 
				new String[]{"hundred_multipler", "hundred_multipler:below_20", "hundred_multipler:decade", "hundred_multipler:digit"});
		
		if (matchList!=null)
		{
			return matchList;
		}
		else
			return null;
	}
	
	public static Double parseNumber(String numString, CFGParser parser)
	{
		LinkedList<String> captureList = null;
		Hashtable<String, LinkedList<String>> matchList = parser.matchPathExtrude(
				numString, 
				"number, '$'*, '^'", 
				new String[]{"decimal_whole_number", "-", "numeric", "numeric_digit", "sign", "thousands", "hundreds", "decade", "digit", "below_20", "0", "word_number", "number", "whole_number", "fractional_part", "fract_digit"}, 
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
	
	public static double getIntegerPartFromWords(Hashtable<String, LinkedList<String>> matchList)
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
	
	public static double getFractionalPart(LinkedList<String> digits)
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
	
	public static class Node
	{
		public int value;
		public Node next = null;
		
		public Node(int v, Node next)
		{
			this.next =next;
			value = v;
		}
		public String toString()
		{
			if (next!=null)
				return "" + value + ", " + next.toString();
			else
				return "" + value ;
		}
	}
	
	public static Node createList(int[] values, int index)
	{
		if (index == values.length)
			return null;
		else
			return new Node(values[index], createList(values, index+1));
	}
	
	public static Node reverse(Node forward, Node reversed)
	{
		if (forward == null)
			return reversed;
		else
			return reverse(forward.next, new Node(forward.value, reversed));
	}
	
	public static String retokenizeText(String baseText, boolean includeEndToken)
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
	
	public static String prepareSentenceTextInput(String lineInput)
	{
		
		lineInput = lineInput.toLowerCase();
		lineInput = retokenizeText(lineInput, true);
		return lineInput;
	}
}
