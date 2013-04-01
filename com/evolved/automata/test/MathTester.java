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
		
		CFGParser parser = null;
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
			
			
			
			Expression exp = ExpressionFactory.parse("6*(87+123+sin(2))+50*100", null);
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
	
	
}
