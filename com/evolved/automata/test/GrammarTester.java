package com.evolved.automata.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.evolved.automata.filetools.*;
import com.evolved.automata.parser.*;
import java.io.*;

public class GrammarTester {
	
	
	
	public static void main(String[] args) {
		
		
		TestClass[] t = null;
		int total, matchcount=0;
		
		boolean match;
		
		
		try
		{
			t = makeTestClasses();
			total = t.length;
			for (int i=0;i<total;i++)
			{
				System.out.print("parse test");
				match = t[i].runCompiledTest();
				
				if (match)
					matchcount++;
				else
					System.out.println("miss");

			}
			
			System.out.println("" + matchcount + " out of " + total);
			
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
	
	

	public static TestClass[] makeTestClasses() throws IOException
	{
		String grammarResourceFileFullName = "/com/evolved/automata/parser/meta_commands.txt";
		BufferedReader reader = StandardTools.getReaderFromPackageResource(grammarResourceFileFullName);
		CFGParser matcher = new CFGParser(reader);
		TestClass[] t = new TestClass[]
		{
			new TestClass(matcher, "basic_number","1",true, new String[]{"basic_number"}),
			new TestClass(matcher, "term","1 + 10",true, new String[]{"term"}),
			new TestClass(matcher, "term","-12",true, new String[]{"term"}),
		    new TestClass(matcher, "term","(1+(50*3+x^2)+6)/10",true, new String[]{"term"}),
			new TestClass(matcher, "term","(58 +78)",false, new String[]{"term"}),		
		   new TestClass(matcher, "('a' | 'b' | 'c')`, 'd', 'e'","de",true, null),
		 new TestClass(matcher, "('a' | 'b' | 'c')`, 'd', 'e'","ade",false, null),
		 new TestClass(matcher, "('a' | 'b' | 'c')`, '@'+","de",true, null),
		 new TestClass(matcher, "(('a' | 'b' | 'c')`, '@')+","aeaa",false, null),
		 new TestClass(matcher, "'@'+","hjeil",true, null),
		
		 new TestClass(matcher, "('a', 'b')+, 'c', 'd'","abababcd",true, null),
		 new TestClass(matcher, "('a', 'b')*, 'c', 'd'", "ab", false, null),
		 new TestClass(matcher, "('a', 'b')?, 'a', 'b', 'c', 'd'","abcd",true, null),
		 new TestClass(matcher, "('a', 'b')+, 'c', 'd'","abababcd",true, null),
//		
		 new TestClass(matcher, "(('a' | 'b' | 'c')`, '@')+","hjeil",true, null),
		 new TestClass(matcher, "command_string","enable",true, null),
		 new TestClass(matcher, "command_string","in able",true, null),
		 new TestClass(matcher, "command_string","in sable",false, null),
		 new TestClass(matcher, "command_string","disable",true, null),
		 new TestClass(matcher, "command_string","disable moving robot left",true, null),
		 new TestClass(matcher, "command_string","disable moving robot left",true, null),
		 new TestClass(matcher, "'a', 'b'?","a",true, null),
		new TestClass(matcher, "command","random, random last",false, new String[]{"middle", "last"}),
		new TestClass(matcher, "command","rafinal",true, new String[]{"middle", "last"}),
		new TestClass(matcher, "prefix, ' ', command_entered","set enable",false, new String[]{"command_entered","specific_enable","general_enable"}),
		new TestClass(matcher, "parameters","se e",true, new String[]{"parameters"}),
		new TestClass(matcher, "parameters","se en",true, new String[]{"parameters"}),
		new TestClass(matcher, "prefix, ' ', command_entered","set enable backward and forward",true, new String[]{"command_entered","specific_enable","general_enable"}),
		new TestClass(matcher, "'#'*(4), '@'","123gf",false, null),
		new TestClass(matcher, "'#'*(4), '@'","1234f",true, null),
		new TestClass(matcher, "(('b', 'u', 'i', 'l', 'd')`, word)","truly",true, new String[]{"word"}), 
		new TestClass(matcher, "(('b', 'u', 'i', 'l', 'd')`, word), (' ', (('b', 'u', 'i', 'l', 'd')`, word))*","truly amazing software free grammar",true, new String[]{"word"}),
		new TestClass(matcher, "(('b', 'u', 'i', 'l', 'd')`, word), (' ', (('b', 'u', 'i', 'l', 'd')`, word))*","build amazing build free grammar",false, new String[]{"word"}),
		new TestClass(matcher, "word, ' ', word:1","none none",true, new String[]{"word"}),
		new TestClass(matcher, "word, ' ', word:1","none one",false, new String[]{"word"}),
		new TestClass(matcher, "word, (' ', ((word:1)`, word))+, '^'","none other than the first",true, new String[]{"word"}),
		new TestClass(matcher, "word, (' ', ((word:1)`, word))+, '^'","none other than none first",false, new String[]{"word"}),
		new TestClass(matcher, "word, ' ', ((word:1)`, word), ' ', ((word:1 | word:2)`, word), ' ', ((word:1 | word:2 | word:3)`, word), ' ', ((word:1 | word:2 | word:3 | word:4)`, word)","none other than none first",false, new String[]{"word"}),
		new TestClass(matcher, "word, ' ', ((word:1)`, word), ' ', (word:1 | word:2)`, word","none other than",true, new String[]{"word"}),
		new TestClass(matcher, "product, ' '*, ('*' | '/'), ' '*, product","(e^(2*pi/std)+8/10)/(sin(x)*cos(x))",true, new String[]{"product"})
		
		};
		return t;
	}
	
	
	
	public static class TestClass
	{
		String grammar;
		String input;
		boolean expected;
		CFGParser cfg;
		String[] capturevalues;
		
		
		public TestClass(CFGParser matcher, String topGrammar, String inputString, boolean expectedMatch, String[] cValues)
		{
			expected= expectedMatch;
			//input=inputString.trim()+" |";
			input=inputString;
			//grammar = topGrammar+", ' '*, '|'";
			grammar = topGrammar;
			cfg = matcher;
			capturevalues = cValues;
		}
		
		public boolean runCompiledTest()
		{
			String reportString = "Grammar: %1$s - input: %2$s - expected value: %3$s - Actual value: %4$s - Time: %5$s";
			boolean output;
			long start = System.currentTimeMillis();
			output = cfg.match(input, grammar, capturevalues,true);
			long end = System.currentTimeMillis();
			boolean match = output == expected;
			System.out.println(String.format(reportString, grammar, input, expected, output, end-start));
			if (output)
			{
				String outputDescriptionPattern = "Match: %1$s -- [ %2$s -> %3$s ] ";
				String oString=null;
				String captured;
				LinkedList<Matcher> gState = cfg.getOutputSet();
				Matcher s;
				if (capturevalues!=null)
				{
					for (int i=0;i<gState.size();i++)
					{
						s= gState.get(i);
						for (String matchValue:capturevalues)
						{
								
							if ((captured=s.getFirstCapturedValue(matchValue))!=null)
							{
								oString = String.format(outputDescriptionPattern, i,matchValue,captured );
								System.out.println(oString);
							}
						}
						
						
						
					}
				}
				
				
			}
			return match;
		}
		
		
	}
}
