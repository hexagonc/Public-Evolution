package com.evolved.automata.parser;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.*;


public class CFGParser {
	/**
	 * CFGParser is the main class for parsing and matching strings against a context-free grammar.  The syntax for the <br/>
	 * context-free grammar uses an EBNF-like syntax.  See http://phase-summary.blogspot.com/2012/05/context-free-grammar-parser-and-pattern.html<br/>
	 * CFGParser actually supports a syntax that is more expressive than context-free grammars, which is why I use the <br/>
	 * term, EBNF-like syntax.  The actual syntax is a combination of regex notation and Extended Backus-Naur form.
	 * @author Evolved8
	 *
	 */
	
	public static class GlobalState
	{
		private Hashtable<Integer, Hashtable<String, Integer>> failureCache;
		private Hashtable<String, Integer> maxAttemptMap;
		public static final int STARTED=0;
		public static final int FAILED=1;
		public static final int RECURSIVE=2;
		private int maxVisits=23;
		
		public GlobalState()
		{
			failureCache = new Hashtable<Integer, Hashtable<String, Integer>>();
			maxAttemptMap = new Hashtable<String, Integer>();
					
		}
		
		public GlobalState clone()
		{
			GlobalState nState = new GlobalState();
			nState.maxAttemptMap = this.maxAttemptMap;
			nState.maxVisits=this.maxVisits;
			return nState;
		}
		
		public void incrementCount(String pattern)
		{
			Integer oldCount;
			if (maxAttemptMap.containsKey(pattern))
				oldCount = maxAttemptMap.get(pattern);
			else
				oldCount=new Integer(0);
			maxAttemptMap.put(pattern, new Integer(oldCount.intValue()+1));
			
			if (oldCount.intValue()>=maxVisits)
				maxVisits=oldCount.intValue()+1;
		}
		
		public int shouldSkipComponent(String pattern, int start)
		{
//			if (pattern == null || !maxAttemptMap.containsKey(pattern))
				return STARTED;
			
//			Integer key = new Integer(start);
//			Hashtable<String, Integer> statusMap;
//			if (!failureCache.containsKey(key))
//			{
//				statusMap = new Hashtable<String, Integer>();
//				failureCache.put(key, statusMap);
//				
//			}
//			else
//				statusMap = failureCache.get(key);
//			
//			if (!statusMap.containsKey(pattern))
//			{
//				statusMap.put(pattern, 1);
//				return STARTED;
//			}
//			else
//			{
//				int oldValue =statusMap.get(pattern).intValue();
//				if ((oldValue == -1) || oldValue>=maxVisits)
//					return FAILED;
//				else
//				{
//					statusMap.put(pattern, new Integer(oldValue+1));
//					return STARTED;
//				}
//				
//			}
			
		}
		
		public void setFailureStatus(String pattern, int start)
		{
			if (pattern == null || !maxAttemptMap.containsKey(pattern))
				return;
			Integer key = new Integer(start);
			Hashtable<String, Integer> statusMap;
			if (!failureCache.containsKey(key))
			{
				statusMap = new Hashtable<String, Integer>();
				failureCache.put(key, statusMap);
				
			}
			else
				statusMap = failureCache.get(key);
			
			statusMap.put(pattern, -1);
		}
	}
	
	public static com.evolved.automata.filetools.SimpleLogger log;
	
	private Hashtable<String,String> namedComponents;
	protected Hashtable<String,Matcher> compiledComponents;
	protected Hashtable<String,Matcher> cachedCompiled;
	GlobalState baseState;
	
	private LinkedList<Matcher> endStates;
	private String inputFileFullName;
	Hashtable<String, StringDistribution> processedDistributions;
	
	HashSet<String> componentsForSubgrammars;
	
	
	
	public CFGParser()
	{
		namedComponents = new Hashtable<String, String>();
		compiledComponents = new Hashtable<String, Matcher>();
		
	}
	
	
	public CFGParser(String[] definitionComponent) 
	{
		String[] defParts;
		namedComponents = new Hashtable<String, String>();
		
		for (String definition:definitionComponent)
		{
			if (definition.length()>0)
			{
				if (!definition.substring(0, 1).equals(Parser.commentChar))
				{
					defParts = Parser.splitPattern(definition, '=', true);
					namedComponents.put(defParts[0], defParts[1]);
				}
			}
		}
		
		endStates = null;
		
		processedDistributions = new Hashtable<String, StringDistribution>();
		
		String value;
		String[]  subParts;
		StringDistribution sDistribution;
		for (String sName:namedComponents.keySet())
		{
			value = namedComponents.get(sName);
			subParts = Parser.disjuctionofLiterals(value);
			if (subParts!=null)
			{
				sDistribution = new StringDistribution();
				for (String s:subParts)
					sDistribution.addString(s);
				processedDistributions.put(sName, sDistribution);
			}
		}
		buildNonTerminalParseMap();
	}
	
	public CFGParser(LinkedList<String> definitionComponent) throws IOException
	{
		
		String[] defParts;
		namedComponents = new Hashtable<String, String>();
		
		for (String definition:definitionComponent)
		{
			if (definition.length()>0)
			{
				if (!definition.substring(0, 1).equals(Parser.commentChar))
				{
					defParts = Parser.splitPattern(definition, '=', true);
					//namedComponents.put(defParts[0], GrammarState.stringToTerminalSequence(defParts[1]));
					namedComponents.put(defParts[0], defParts[1]);
				}
			}
		}
		
		endStates = null;
		
		processedDistributions = new Hashtable<String, StringDistribution>();
		
		String value;
		String[]  subParts;
		StringDistribution sDistribution;
		for (String sName:namedComponents.keySet())
		{
			value = namedComponents.get(sName);
			subParts = Parser.disjuctionofLiterals(value);
			if (subParts!=null)
			{
				sDistribution = new StringDistribution();
				for (String s:subParts)
					sDistribution.addString(s);
				processedDistributions.put(sName, sDistribution);
			}
		}
		buildNonTerminalParseMap();
	}
	
	
	public CFGParser(String grammarFileFullName) throws IOException
	{
		
		String pattern = "<\\s*(\\S+)>";
		Pattern p = Pattern.compile(pattern);
		
		String includeResource;
		String[] defParts;
		namedComponents = new Hashtable<String, String>();
		String[] definitionComponent = com.evolved.automata.filetools.StandardTools.getDataFileLines(grammarFileFullName);
		for (String definition:definitionComponent)
		{
			if (definition.length()>0)
			{
				if (!definition.substring(0, 1).equals(Parser.commentChar))
				{
					includeResource = includeReference(p, definition);
					if (includeResource==null)
					{
						defParts = Parser.splitPattern(definition, '=', true);
						
						if (defParts.length>2)
							throw new RuntimeException("Invalid grammar");
						namedComponents.put(defParts[0], defParts[1]);
					}
					else
					{
						addGrammarFromResource(p, includeResource, namedComponents);
					}
				}
			}
		}
		
		endStates = null;
		
		processedDistributions = new Hashtable<String, StringDistribution>();
		
		String value;
		String[]  subParts;
		StringDistribution sDistribution;
		for (String sName:namedComponents.keySet())
		{
			value = namedComponents.get(sName);
			subParts = Parser.disjuctionofLiterals(value);
			if (subParts!=null)
			{
				sDistribution = new StringDistribution();
				for (String s:subParts)
					sDistribution.addString(s);
				processedDistributions.put(sName, sDistribution);
			}
		}
		buildNonTerminalParseMap();
	}
	
	public CFGParser(BufferedReader grammarResourceName) throws IOException
	{
		
		String pattern = "<\\s*(\\S+)>";
		Pattern p = Pattern.compile(pattern);
		
		String includeResource;
		String[] defParts;
		namedComponents = new Hashtable<String, String>();
		String definition;
		
		try
		{
			while ((definition = grammarResourceName.readLine())!=null)
			{
				if (definition.length()>0)
				{
					if (!definition.substring(0, 1).equals(Parser.commentChar))
					{
						includeResource = includeReference(p, definition);
						if (includeResource==null)
						{
							defParts = Parser.splitPattern(definition, '=', true);
							//namedComponents.put(defParts[0], GrammarState.stringToTerminalSequence(defParts[1]));
							namedComponents.put(defParts[0], defParts[1]);
						}
						else
						{
							addGrammarFromResource(p, includeResource, namedComponents);
						}
					}
				}
			}
		}
		finally
		{
			if (grammarResourceName!=null)
			{
				grammarResourceName.close();
			}
		}
		endStates = null;
		
		processedDistributions = new Hashtable<String, StringDistribution>();
		
		String value;
		String[]  subParts;
		StringDistribution sDistribution;
		for (String sName:namedComponents.keySet())
		{
			value = namedComponents.get(sName);
			subParts = Parser.disjuctionofLiterals(value);
			if (subParts!=null)
			{
				sDistribution = new StringDistribution();
				for (String s:subParts)
					sDistribution.addString(s);
				processedDistributions.put(sName, sDistribution);
			}
		}
		buildNonTerminalParseMap();
	}
	
	public void resetGrammar()
	{
		baseState = new GlobalState();
		namedComponents = new Hashtable<String, String>();
		compiledComponents = new Hashtable<String, Matcher>();
	}
	
	
	public String[] getNonterminalNames()
	{
		return namedComponents.keySet().toArray(new String[0]);
	}
	
	
	public void addStringAlternative(String nonTerminal, String sValue)
	{
		String[] parts = null;
		String originalGrammar;
		String simpleAppendPattern = "%1$s | \"%2$s\"";
		String groupAppendPattern = "(%1$s) | \"%2$s\"";
		String defaultPattern="\"%1$s\"";
		String finalOutput=null;
		
		if (namedComponents.containsKey(nonTerminal))
		{
			originalGrammar = namedComponents.get(nonTerminal);
			parts = Parser.isConjunction(originalGrammar);
			if (parts!=null)
			{
				finalOutput = String.format(groupAppendPattern, originalGrammar, sValue);
				
			}
			else
				finalOutput = String.format(simpleAppendPattern, originalGrammar, sValue);
			
		}
		else
			finalOutput = String.format(defaultPattern, sValue);
		
		namedComponents.put(nonTerminal, finalOutput);
		
	}
	
	public void addNonterminalAlternative(String nonTerminal, String sValue)
	{
		String[] parts = null;
		String originalGrammar;
		String simpleAppendPattern = "%1$s | %2$s";
		String groupAppendPattern = "(%1$s) | %2$s";
		String defaultPattern="%1$s";
		String finalOutput=null;
		
		if (namedComponents.containsKey(nonTerminal))
		{
			originalGrammar = namedComponents.get(nonTerminal);
			parts = Parser.isConjunction(originalGrammar);
			if (parts!=null)
			{
				finalOutput = String.format(groupAppendPattern, originalGrammar, sValue);
				
			}
			else
				finalOutput = String.format(simpleAppendPattern, originalGrammar, sValue);
			
		}
		else
			finalOutput = String.format(defaultPattern, sValue);
		
		namedComponents.put(nonTerminal, finalOutput);
		
	}
	
	public void addStringConjunction(String nonTerminal, String sValue)
	{
		String[] parts = null;
		String originalGrammar;
		String simpleAppendPattern = "%1$s, \"%2$s\"";
		String groupAppendPattern = "(%1$s), \"%2$s\"";
		String defaultPattern="\"%1$s\"";
		String finalOutput=null;
		
		if (namedComponents.containsKey(nonTerminal))
		{
			originalGrammar = namedComponents.get(nonTerminal);
			parts = Parser.isAlternation(originalGrammar);
			if (parts!=null)
			{
				finalOutput = String.format(groupAppendPattern, originalGrammar, sValue);
			}
			else
				finalOutput = String.format(simpleAppendPattern, originalGrammar, sValue);
			
		}
		else
			finalOutput = String.format(defaultPattern, sValue);
		
		namedComponents.put(nonTerminal, finalOutput);
		
	}
	
	public void addNonterminalConjunction(String nonTerminal, String sValue)
	{
		String[] parts = null;
		String originalGrammar;
		String simpleAppendPattern = "%1$s, %2$s";
		String groupAppendPattern = "(%1$s), %2$s";
		String defaultPattern="%1$s";
		String finalOutput=null;
		
		if (namedComponents.containsKey(nonTerminal))
		{
			originalGrammar = namedComponents.get(nonTerminal);
			parts = Parser.isAlternation(originalGrammar);
			if (parts!=null)
			{
				finalOutput = String.format(groupAppendPattern, originalGrammar, sValue);
			}
			else
				finalOutput = String.format(simpleAppendPattern, originalGrammar, sValue);
			
		}
		else
			finalOutput = String.format(defaultPattern, sValue);
		
		namedComponents.put(nonTerminal, finalOutput);
		
	}
	
	public Matcher parse(String grammar)
	{
		return parse(grammar, true);
	}
	
	public Matcher parse(String grammar, boolean defaultQuantifierGreedyP)
	{
		
		Matcher state = Parser.parse(new CFGParser.GlobalState(), grammar, namedComponents, defaultQuantifierGreedyP);
		return state;
	}
	
	private void buildNonTerminalParseMap()
	{
		baseState = new GlobalState();
		Matcher mapped=null;
		compiledComponents = new Hashtable<String, Matcher>();
		String ntName = "";
		try
		{
			for (String nonTerminalName:namedComponents.keySet())
			{
				ntName=nonTerminalName;
				mapped = Parser.parse(baseState, namedComponents.get(nonTerminalName), namedComponents);
				compiledComponents.put(nonTerminalName, mapped);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid compiled grammar exception near " + ntName);
		}
		
	}
	

	private String includeReference(Pattern p, String lineInput)
	{
		java.util.regex.Matcher m = p.matcher(lineInput);
		if (m.matches())
			return m.group(1);
		else
			return null;
	}
	
	// TODO: Retire this function or make it private since it has no effect if you use
	// the method matchPathExtrude
	public void addGrammarsForSubGrammarMatching(String[] names)
	{
		if (names==null)
		{
			componentsForSubgrammars = null;
			return;
		}
		if (componentsForSubgrammars == null)
			componentsForSubgrammars = new HashSet<String>();
		for (String grammar:names)
		{
			componentsForSubgrammars.add(grammar);
		}
	}
	
	private void addGrammarFromResource(Pattern p, String resourceFullName, Hashtable<String, String> namedComponents) throws IOException
	{
		String definition;
		String[] defParts;
		String includeResource;
		
		BufferedReader bReader = getReaderFromPackageResource(resourceFullName);
		if (bReader==null)
			return;
		while ((definition=bReader.readLine())!=null)
		{
			definition=definition.trim();
			if (definition.length()>0 && !definition.substring(0, 1).equals(Parser.commentChar))
			{
				includeResource = includeReference(p, definition);
				if (includeResource==null)
				{
					defParts = Parser.splitPattern(definition, '=', true);
					namedComponents.put(defParts[0], defParts[1]);
				}
				else
				{
					addGrammarFromResource(p, includeResource, namedComponents);
				}
			}
		}
		
	}
	
	public static BufferedReader  getReaderFromPackageResource(String resource)
	{
		InputStream istream = CFGParser.class.getResourceAsStream(resource);
		if (istream==null)
			return null;
		InputStreamReader reader = new InputStreamReader(istream);
		return new BufferedReader(reader);
	}
	
	public SearchResults findPattern(String input, String pattern)
	{
		return findPattern(input, pattern, -1, false, null);
	}
	
	public SearchResults findPattern(String input, String pattern, int matchCount)
	{
		return findPattern(input, pattern, matchCount, false, null);
	}
	
	
	public SearchResults findPattern(String input, String pattern,int matchCount, boolean continuous, String patternEndDelimiter)
	{
		if (matchCount == 0)
			return null;
		SearchResults results = new SearchResults();
		
		
		StringBuffer s = new StringBuffer();
		String subString=input;
		
		
		LinkedList<String> matches = new LinkedList<String>();
		boolean match;
		boolean second=false;
		while (subString.length()>0)
		{
			
			match = match(((second&&continuous)?patternEndDelimiter + subString:subString),pattern, new String[]{pattern},true);
			if (!match)
			{
				second=false;
				s.append(subString.charAt(0) );
				subString=subString.substring(1);
			}
			else
			{
				if (second&&continuous)
				{
					matches.add(subString.substring(0, getFirstResultEndIndex() - patternEndDelimiter.length()));
					subString=subString.substring(getFirstResultEndIndex() - patternEndDelimiter.length());
				}
				else
				{
					matches.add(subString.substring(0, getFirstResultEndIndex()));
					subString=subString.substring(getFirstResultEndIndex());
				}
				second=true;
				
				matchCount--;
				
				
				results.captureList = matches;
				
				if (matchCount==0)
				{
					results.nonMatchedInput = s.toString();
					return results;
				}
				
			}
				
		}
		if (matches.size()>0)
		{
			results.nonMatchedInput = s.toString();
			return results;
		}
		else
			return null;
		
	}
	
	
	
	public Hashtable<String, LinkedList<String>> matchPathExtrude(String inputString, String grammarComponent, String[] captureNames)
	{
		return matchPathExtrude(inputString, grammarComponent,captureNames, null);
	}
	
	// TODO: Modify this properly to deal with subpatterns
	public Hashtable<String, LinkedList<String>> matchPathExtrude(String inputString, String grammarComponent, String[] captureNames,  String[] subGrammars)
	{
		addGrammarsForSubGrammarMatching(subGrammars);
		String subGrammar;
		boolean match;
		if (captureNames==null)
		{
			match=match(inputString, grammarComponent, null);
			if (match)
				return new Hashtable<String, LinkedList<String>>();
			return null;
		}
			
		LinkedList<String> iterativeCaptureKeys = new LinkedList<String>();
		LinkedList<String> baseCaptureKeys = new LinkedList<String>();
		HashSet<String> totalKeys = new HashSet<String>();
		String[] path = null;
		for (String cName:captureNames)
		{
			path = cName.split("\\:");
			if (path.length<2)
			{
				baseCaptureKeys.add(cName);
				totalKeys.add(cName);
			}
			else
			{
				iterativeCaptureKeys.add(cName);
				totalKeys.add(path[0]);
			}
		}
		
		match = match(inputString, grammarComponent, totalKeys.toArray(new String[0]));
		if (match)
		{
			Hashtable<String, LinkedList<String>> resultSet = getFirstCaptureSet(), baseSet, outputSet;
			outputSet = new Hashtable<String, LinkedList<String>>();
			LinkedList<String> remapped= null, old;
			String input, grammar, captureComponent;
			for (String iKey:iterativeCaptureKeys)
			{
				
				path = iKey.split("\\:");
				if (!resultSet.containsKey(path[0]))
					continue;
				
				nextAttempt:for (String topValue:resultSet.get(path[0]))
				{
					baseSet = resultSet;
					captureComponent=path[0];
					input =  topValue;
					for (int i=0;i<path.length-1;i++)
					{
						grammar = path[i];
						
						captureComponent = path[i+1];
						subGrammar = String.format("%1$s", grammar);
						match = match(input, subGrammar, new String[]{captureComponent});
						if (match)
						{
							baseSet = getFirstCaptureSet();
							if (!baseSet.containsKey(captureComponent))
								continue nextAttempt;
							input = baseSet.get(captureComponent).getFirst();
						}
						else
							continue nextAttempt;
					}
					if (!outputSet.containsKey(iKey))
					{
						old = new LinkedList<String>();
						outputSet.put(iKey, old);
					}
					else
						old = outputSet.get(iKey);
					remapped = baseSet.get(captureComponent);
					old.addAll(remapped);
				}
				
			}
			for (String simpleKey:baseCaptureKeys)
			{
				if (resultSet.containsKey(simpleKey))
					outputSet.put(simpleKey, resultSet.get(simpleKey));
			}
			return outputSet;
		}
		else
			return null;
	}
	
	public boolean matchCompiled(String inputString, Matcher precompiled, String[] captureNames)
	{
		return matchCompiled(inputString, precompiled, captureNames, true, new Hashtable<String, LinkedList<String>>());
	}
	
	public boolean matchCompiled(String inputString, Matcher precompiled, String[] captureNames,  boolean defaultQuantifiersGreedyP )
	{
		return matchCompiled(inputString, precompiled, captureNames, defaultQuantifiersGreedyP, new Hashtable<String, LinkedList<String>>());
	}
	
	public boolean matchCompiled(String inputString, Matcher precompiled, String[] captureNames, boolean defaultQuantifiersGreedyP ,  Hashtable<String, LinkedList<String>> capturedComponents)
	{
		return matchPrebind(inputString, precompiled, captureNames, defaultQuantifiersGreedyP, capturedComponents);
	}
	
	
	
	public boolean match(String inputString, String grammarComponent, String[] captureNames)
	{
		return match(inputString, grammarComponent, captureNames, true, new Hashtable<String, LinkedList<String>>());
	}
	
	public boolean match(String inputString, String pattern, String[] captureNames, boolean defaultQuantifiersGreedyP)
	{
		return match(inputString, pattern, captureNames, defaultQuantifiersGreedyP, new Hashtable<String, LinkedList<String>>());
	}
	
	/**
	 * Main method for matching a string against a parse-tree of unit pattern matchers.
	 * @param inputString 
	 * @param EBNF-like pattern string to parse into a parse-tree
	 * @param captureNames names of non-terminals to capture
	 * @param defaultQuantifiersGreedyP 
	 * @param capturedComponents input parameters
	 * @return true if the inputstring can be matched by the parse tree or false otherwise
	 */
	public boolean match(String inputString, String pattern, String[] captureNames, boolean defaultQuantifiersGreedyP,  Hashtable<String, LinkedList<String>> capturedComponents)
	{
		Matcher precompiled = parse(pattern);
		return matchPrebind(inputString, precompiled, captureNames, defaultQuantifiersGreedyP, capturedComponents);
	}
	
	/**
	 * Main method for matching a string against a parse-tree of unit pattern matchers.
	 * @param inputString 
	 * @param precompiled root of parse-tree 
	 * @param captureNames names of non-terminals to capture
	 * @param defaultQuantifiersGreedyP 
	 * @param capturedComponents input parameters
	 * @return true if the inputstring can be matched by the parse tree or false otherwise
	 */
	private boolean matchPrebind(String inputString, Matcher precompiled, String[] captureNames, boolean defaultQuantifiersGreedyP, Hashtable<String, LinkedList<String>> capturedComponents)
	{
		Matcher currentState=null;
		LinkedList<Matcher> frontier=null, matchedStates=null,  nextStates=null;
		String nonTerminalName;
		HashSet<String> cNames= null;
		HashSet<Integer> map;
		Hashtable<String, HashSet<Integer>> nonTerminalCaptureDistribution = new Hashtable<String, HashSet<Integer>>(); 
		if ((captureNames!=null)&&(captureNames.length>0))
		{
			cNames = new HashSet<String>();
			for (String name: captureNames)
			{
				cNames.add(name);
			}
					
		}
		Matcher s;
		boolean circularDefinition;
		int i=0, startIndex;
		try
		{
			
			currentState=precompiled;
			currentState.setExecutionParameters(baseState.clone(), 0, 0, inputString, capturedComponents, cNames, null, null, compiledComponents);
			frontier = new LinkedList<Matcher>();
			matchedStates =new LinkedList<Matcher>();
			frontier.add(currentState);
			
			while (frontier.size()>0)
			{
				currentState = removeNextStateFromFrontier(frontier);
				currentState.setComponentsForSubgrammars(componentsForSubgrammars);
				
				nextStates = currentState.match();
				i++;
				if (nextStates!=null)
				{
					while (nextStates.size()>0)
					{
						s = nextStates.removeFirst();
						if (s.isFinalized())
						{
							matchedStates.add(s);
							frontier.clear();
							nextStates.clear();
							
						}
						else
						{
							nonTerminalName = s.grammarComponent;
							if (nonTerminalName!=null&&namedComponents.containsKey(nonTerminalName))
							{
								startIndex = s.startIndex;
								
								if (!nonTerminalCaptureDistribution.containsKey(nonTerminalName))
								{
									map = new HashSet<Integer>();
									map.add(startIndex);
									nonTerminalCaptureDistribution.put(nonTerminalName, map);
								}
								else
								{
									map = nonTerminalCaptureDistribution.get(nonTerminalName);
									if (map.contains(startIndex))
									{
										circularDefinition = findSelfInAncestors(s);
										if (circularDefinition)
										{
											addAllCompiledExtract(s, frontier);
											break;
										}
									}
									else
										map.add(startIndex);
								}
								frontier.add(s);
								
							}
							else
								frontier.add(s);
						}
					}
					
					
				}
			}
			
		}
		catch (Exception e)
		{
			boolean cont=true;
			while (cont)
			{
				if (currentState!=null)
				{
					if (currentState.isNonterminal)
						throw new RuntimeException("Invalid grammar exception near: " + currentState.grammarComponent);
					currentState=currentState.parent;
				}
				else
					break;
			}
			throw new RuntimeException("Invalid grammar exception");
		}
		
		if ((matchedStates!=null)&&(matchedStates.size()>0))
			endStates = matchedStates;
		else
			endStates = null;
		return matchedStates!=null&&matchedStates.size()>0;
	}
	
	private boolean findSelfInAncestors(Matcher state)
	{
		Matcher parent = state;
		String name = state.grammarComponent;
		int recursionCount = 0;
		int threshold = 4;
		while ((parent = parent.parent)!=null)
		{
			if (parent.grammarComponent!=null&&parent.grammarComponent.equals(name)&&parent.startIndex == state.startIndex)
				recursionCount++;
			if (recursionCount==threshold)
				return true;
		}
		return false;
	}
	
	private void addAllCompiledExtract(Matcher s, LinkedList<Matcher> nextStates)
	{
		Matcher prior = s.getNonDeterministicPrior();
		if (prior!=null)
		{
			LinkedList<Matcher> newPoints = prior.updateFromParseFailure(s, -1);
			if (newPoints!=null)
				nextStates.addAll(newPoints);
		}
	}
	
	
	Matcher removeNextStateFromFrontier(LinkedList<Matcher> frontier)
	{
		return frontier.removeLast();
	}
	
	public LinkedList<Matcher> getOutputSet()
	{
		return endStates;
	}
	
	public int getFirstResultEndIndex()
	{
		if ((endStates!=null)&&(endStates.size()>0))
			return endStates.getFirst().getEndIndex();
		else
			return -1;
	}
	
	public Hashtable<String, LinkedList<String>> getFirstCaptureSet()
	{
		if ((endStates!=null)&&(endStates.size()>0))
			return endStates.getFirst().getCaptureSet();
		else
			return null;
	}
	
	public LinkedList<String> getFirstCapturedList(String name)
	{
		
		Hashtable<String, LinkedList<String>> list = getFirstCaptureSet();
		if ((list == null)||(!list.containsKey(name)))
			return null;
		return list.get(name);
	}
	
	
	public String getFirstCapturedValue(String key)
	{
		Hashtable<String, LinkedList<String>> captured = getFirstCaptureSet();
		if ((captured!=null)&&(captured.containsKey(key)))
		{
			return captured.get(key).getFirst();
		}
		else
			return null;
	}
	
	public String[] identifyMatchParent(String patternName, String text)
	{
		LinkedList<String> output = new LinkedList<String>();
		
		String patternDefinition =  namedComponents.get("patternName");
		String[] parts = Parser.isAlternation(patternDefinition);
		for (String grammar:parts)
		{
			output.add(grammar);
		}
		return output.toArray(new String[0]);
	}
	
	public static class SearchResults
	{
		public String nonMatchedInput;
		public LinkedList<String> captureList;
	}
}
	
	
