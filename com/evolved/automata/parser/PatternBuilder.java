package com.evolved.automata.parser;
import com.evolved.automata.*;
import com.evolved.automata.filetools.*;


import java.io.*;
import java.util.*;

public class PatternBuilder {
	CFGParser parser = null;
	CFGParser conformanceparser = null;
	Hashtable<String, PatternGenerator> distribution;
	Hashtable<String, String> components;
	PatternBuilderInfo builderInfo = null;
	HashSet<String> patternsBuilt;
	
	public PatternBuilder(String[] grammarLines) throws IOException
	{
		BufferedReader bReader = StandardTools.getReaderFromPackageResource("/com/evolved/automata/parser/CFG_grammar.txt");
		conformanceparser = new CFGParser(bReader);
		distribution = new Hashtable<String, PatternGenerator>();
		components = new Hashtable<String, String>();
		
		String name;
		
		String[] defParts;
		String matchComponents=null;
		
		for (String matchLine:grammarLines)
		{
			defParts = Parser.splitPattern(matchLine, '=', true);
			name = defParts[0].trim();
			matchComponents = defParts[1].trim();
			if (containsInvalidGrammar(matchComponents))
				matchComponents="\"\"";
			matchComponents=matchComponents.replace(", '^'", "");
			components.put(name, matchComponents);
				
		}
		parser = new CFGParser(grammarLines);
		addPatternInfo();
	}
	
	public PatternBuilder(String inputFile) throws IOException
	{
		this(convertFileLines(inputFile));
	}
	
	public PatternBuilder(BufferedReader inputFile) throws IOException
	{
		this(convertFileLines(inputFile));
	}
	
	public String[] getSubComponents(String nonTerminal)
	{
		if (components.containsKey(nonTerminal))
		{
			String def = components.get(nonTerminal);
			String[] subParts = Parser.isAlternation(def);
			
			
			if (subParts!=null)
			{
				
				for (String s:subParts)
				{
					if (!components.containsKey(s))
					{
						return null;
					}
				}
			}
			return subParts;
		}
		return null;
	}
	
	private static String[] convertFileLines(String inputFile) throws IOException
	{
		LinkedList<String> lines = new LinkedList<String>();
		String[] behaviorPatternLines = StandardTools.getDataFileLines(inputFile);
		for (String lineinput:behaviorPatternLines)
		{
			lineinput=lineinput.trim();
			if (lineinput.length()>0&&lineinput.charAt(0)!=';')
				lines.add(lineinput);
		}
		
		return lines.toArray(new String[0]);
	}
	
	private static String[] convertFileLines(BufferedReader inputFile) throws IOException
	{
		LinkedList<String> lines = new LinkedList<String>();
		String[] behaviorPatternLines = StandardTools.getDataFileLines(inputFile);
		for (String lineinput:behaviorPatternLines)
		{
			lineinput=lineinput.trim();
			if (lineinput.length()>0&&lineinput.charAt(0)!=';')
				lines.add(lineinput);
		}
		
		return lines.toArray(new String[0]);
	}
	
	private boolean containsInvalidGrammar(String line)
	{
		return line.indexOf("' '")>=0 || line.indexOf("'@'")>=0 || line.indexOf("'#'")>=0 || line.indexOf("'$'")>=0|| line.indexOf("'~'")>=0;
	}
	
	public String[] getNonterminalNames()
	{
		return components.keySet().toArray(new String[0]);
	}
	
	public String generateSimplePattern(String pattern, Hashtable<String, String> inputParameters)
	{
		if (builderInfo==null)
			addPatternInfo();
		if (inputParameters!=null)
		{
			builderInfo.createParameterSet();
			for (String key:inputParameters.keySet())
			{
				builderInfo.addParameter(key, inputParameters.get(key));
			}
		}
		return builderInfo.generatePattern(pattern, null);
	}
	
	public String[] extrudePattern(String pattern, Hashtable<String, String> inputParameters)
	{
		if (builderInfo==null)
			addPatternInfo();
		if (inputParameters!=null)
		{
			builderInfo.createParameterSet();
			for (String key:inputParameters.keySet())
			{
				builderInfo.addParameter(key, inputParameters.get(key));
			}
		}
		return builderInfo.extrudePattern(pattern, null);
	}
	
	public void clearPatternsBuilt()
	{
		patternsBuilt = new HashSet<String>();
	}
	
	public void addPatternToBuilt(String patternName)
	{
		patternsBuilt.add(patternName);
	}
	
	public String[] getPatternsBuilt()
	{
		if (patternsBuilt!=null)
			return patternsBuilt.toArray(new String[0]);
		else
			return null;
	}
	
	public boolean matchPattern(String input, String pattern)
	{
		return parser.match(input, pattern, null);
	}
	
	private void compilePattern()
	{
		String g=null;
		try
		{
			for (String gg:components.keySet())
			{
				g=gg;
				distribution.put(gg, PatternGenerator.compile(this, components.get(gg)));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Pattern builder error near: "+g);
		}
	}
	
	private PatternBuilderInfo addPatternInfo()
	{
		builderInfo = new PatternBuilderInfo(this);
		compilePattern();
		return  builderInfo;
	}
	
	public PatternBuilderInfo getCurrentBuilderInfo()
	{
		return builderInfo;
	}
	
	
	public StringProbabilityDistribution getStringDistribution(String name)
	{
		return builderInfo.getDistribution(name);
	}
	
	
	public static class PatternBuilderInfo
	{
		Hashtable<String, Hashtable<String, Integer>> generateDistribuion;
		Hashtable<String, Hashtable<String, Integer>> generation_log;
		
		Hashtable<String, LinkedList<String>> precaptured;
		
		PatternBuilder parent;
		public PatternBuilderInfo(PatternBuilder parent)
		{
			this.parent=parent;
			generateDistribuion = new Hashtable<String, Hashtable<String, Integer>>();
			
			
		}
		
		public void resetTotalDistribution()
		{
			generateDistribuion = new Hashtable<String, Hashtable<String, Integer>>();
		}
		
		public void setParent(PatternBuilder p)
		{
			parent = p;
		}
		
		public String generatePattern(String pattern, String[] captureList)
		{
			parent.clearPatternsBuilt();
			PatternGenerator topGenerator = PatternGenerator.compile(parent, pattern);
			if (topGenerator==null)
				return null;
			HashSet<String> captureSet = null;
			if (captureList!=null)
			{
				captureSet = new HashSet<String>();
				for (String c:captureList)
				{
					captureSet.add(c);
				}
			}
			
			
			return topGenerator.sample(generation_log, precaptured, captureSet);
		}
		
		public String[] extrudePattern(String pattern, String[] captureList)
		{
			parent.clearPatternsBuilt();
			PatternGenerator topGenerator = PatternGenerator.compile(parent, pattern);
			if (topGenerator==null)
				return null;
			HashSet<String> captureSet = null;
			if (captureList!=null)
			{
				captureSet = new HashSet<String>();
				for (String c:captureList)
				{
					captureSet.add(c);
				}
			}
			
			
			return topGenerator.extrude(generation_log, precaptured, captureSet);
		}
		
		
		public void createParameterSet()
		{
			precaptured = new Hashtable<String, LinkedList<String>>();
		}
		
		public String getGeneratedValue(String key, int index)
		{
			LinkedList<String> values = null;
			if (precaptured!=null&&precaptured.containsKey(key))
			{
				values = precaptured.get(key);
				if (values.size()>=index)
					return values.get(index-1);
			}
			return null;
				 
		}
		
		public String getGeneratedValue(String key)
		{
			return getGeneratedValue(key, 1);
				 
		}
		
		public void newLog()
		{
			generation_log = new Hashtable<String, Hashtable<String, Integer>>();
		}
		
		public void mergeLog()
		{
			mergeLog(1);
		}
		
		public void mergeLog(int weight)
		{
			if (generation_log !=null)
			{
				Hashtable<String, Integer> subGrammars;
				for (String highGrammar:generation_log.keySet())
				{
					subGrammars = generation_log.get(highGrammar);
					for (String lowGrammar:subGrammars.keySet())
					{
						addMergeKey(generateDistribuion, highGrammar, lowGrammar, subGrammars.get(lowGrammar), weight);
					}
				}
			}
		}
		
		public void setLog(Hashtable<String, Hashtable<String, Integer>> newLog)
		{
			generation_log = newLog;
		}
		
		public void setInputParameters(Hashtable<String, LinkedList<String>> input)
		{
			precaptured = input;
		}
		
		public void addParameter(String key, String value)
		{
			if (precaptured == null)
				precaptured = new Hashtable<String, LinkedList<String>>();
			addToMap(precaptured, key, value);
					
		}
		
		public void removeParameter(String highGrammar)
		{
			if (precaptured!=null&&precaptured.containsKey(highGrammar))
				precaptured.remove(highGrammar);
		}
		
		public StringProbabilityDistribution getDistribution(String name)
		{
			if (generateDistribuion.containsKey(name))
				return new StringProbabilityDistribution(generateDistribuion.get(name));
			else
			{
				Hashtable<String, Integer> newDistrib = new Hashtable<String, Integer>();
				generateDistribuion.put(name, newDistrib);
				return new StringProbabilityDistribution(newDistrib);
			}
		}
		
		public Hashtable<String, Hashtable<String, Integer>> getDistribution()
		{
			return generateDistribuion;
		}
		
		public Hashtable<String, Hashtable<String, Integer>> getBuildLog()
		{
			return generation_log;
		}
	}
	
	public static void addToMap(Hashtable<String, LinkedList<String>> capturedMap, String key, String value)
	{
		LinkedList<String> values = null;
		if (capturedMap.containsKey(key))
			values = capturedMap.get(key);
		else
		{
			values = new LinkedList<String>();
			capturedMap.put(key, values);
		}
		values.add(value);
	}
	
	public static void addMergeKey(Hashtable<String, Hashtable<String, Integer>> target, String highGrammar, String lowGrammar, Integer value, int weight)
	{
		int increment = value.intValue()*weight;
		Integer oldValue = new Integer(0);
		Hashtable<String, Integer> subGrammars = null;
		if (target.containsKey(highGrammar))
		{
			subGrammars = target.get(highGrammar);
		}
		else
		{
			subGrammars = new Hashtable<String, Integer>();
		}
		
		if (subGrammars.containsKey(lowGrammar))
		{
			oldValue = subGrammars.get(lowGrammar);
		}
		
		Integer newValue = new Integer(oldValue.intValue()+increment);
		subGrammars.put(lowGrammar, newValue);
	}
	
	
	public Hashtable<String, PatternGenerator> getCompiledMap()
	{
		return distribution;
	}
	
	public Hashtable<String, String> getNonterminalMap()
	{
		return components;
	}
	
	
	public PatternBuilder(CFGParser parser) 
	{
		this.parser=parser;
	}
	
	public static Hashtable<String, PatternGenerator> copyDeep(Hashtable<String, PatternGenerator> input)
	{
		if (input == null)
			return null;
		
		PatternGenerator gen = null;
		Hashtable<String, PatternGenerator> output = new Hashtable<String, PatternGenerator>();
		for (String key:input.keySet())
		{
			gen = input.get(key).clone();
			output.put(key, gen);
		}
		return output;
	}
		


	public String[] negativePattern(String pattern)
	{
		pattern=pattern.trim();
		return null;
		// TODO: Figure out a better way of doing this
//		
//		if (pattern.indexOf('`')>-1)
//		{
//			if (pattern.charAt(0)!='('||pattern.charAt(pattern.length()-1)!=')')
//				return null;
//			pattern = pattern.substring(1,pattern.length()-1);
//			String[] parts = pattern.split("`, ");
//			if (parts.length==2)
//			{
//				
//				Hashtable<String, LinkedList<String>> match = conformanceparser.matchPathExtrude(parts[0], "compact_component, '^'", null);
//				if (match==null)
//					return null;
//				match = conformanceparser.matchPathExtrude(parts[1], "compact_component, '^'", null);
//				if (match==null)
//					return null;
//				String negativePattern = parts[0];
//				String positivePattern = parts[1];
//				return new String[]{negativePattern, positivePattern};
//			}
//			
//		}
		
		
		
	}
	
}
