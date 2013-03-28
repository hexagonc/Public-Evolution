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


public class MathTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String grammarResource = "/com/evolved/automata/test/MathPatternDef.txt";
		BufferedReader greader;
		
		CFGParser parser = null;
		String inputString = "5 take away 7 times 12 plus five fifths of one percent of 50 plus three ";
		LinkedList<String> results;
		String[] expr = new String[]{"6", "*", "87", "*", "123", "+", "sin", "x", "+", "cos", "x", "*", "sin", "y"};
		String[] expected = new String[]{"6", "87", "123", "*", "*", "x", "sin", "x", "cos", "+", "y", "sin", "*", "+"};
		String[] out = null;
		
		
		try
		{
			greader = StandardTools.getReaderFromPackageResource(grammarResource);
			parser = new CFGParser(greader);
			if (parser.match(inputString, "simple_expression, '^'", new String[]{"base_arg", "arg_group"}))
			{
				System.out.println(parser.getFirstCapturedList("base_arg"));
				System.out.println(parser.getFirstCapturedList("arg_group"));
			}
			
			out = convertToRPL(expr);
			for (String s:out)
			{
				System.out.print(" ");
				System.out.print(s);
			}
			System.out.println();
			
				
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
	
	public static final String[][] operators = new String[][]{new String[]{"+", "-"}, new String[]{"*", "/"}, new String[]{"^", "sin", "cos"}};
	
	
	public static String[] convertToRPL(String[] algebraic_expr)
	{
		return convertToRPL(0, 0, algebraic_expr.length-1, algebraic_expr);
	}
	
	public static final HashSet<String> infixOperators;
	public static final HashSet<String> prefixOperators;
	//public static final HashSet<String> postfixOperators;
	static
	{
		infixOperators = new HashSet<String>();
		infixOperators.add("+");
		infixOperators.add("-");
		infixOperators.add("*");
		infixOperators.add("/");
		infixOperators.add("^");
		
		prefixOperators = new HashSet<String>();
		prefixOperators.add("sin");
		prefixOperators.add("cos");
	}
	
	public static String[] convertToRPL(final int opPrec, final int start, final int end, final String[] algebraic_expr)
	{
		if (opPrec == operators.length)
		{
			return new String[]{algebraic_expr[start]};
		}
		String[] left;
		String[] right;
		for (int i=start;i<=end;i++)
		{
			for (int j=0;j<operators[opPrec].length;j++)
			{
				String operator = operators[opPrec][j];
				if (algebraic_expr[i].equals(operator))
				{
					if (infixOperators.contains(operator))
					{
						
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						return add(add(left, right), operator);
					}
					else if (prefixOperators.contains(operator))
					{
						right = convertToRPL(opPrec, i+1, end, algebraic_expr);
						return add(right, operator);
					}
					else 
					{
						left = convertToRPL(opPrec, start, i-1, algebraic_expr);
						return add(left, operator);
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
