package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import com.evolved.automata.filetools.SimpleLogger;

public abstract class Matcher {
	
	public String grammarComponent;
	public String inputString;
	public int startIndex;
	public int endIndex;
	
	
	CFGParser.GlobalState global;
	
	public final String groupName = "group";
	
	protected Hashtable<String,Matcher> parsedComponents;
	protected Hashtable<String,String> namedComponents;
	protected int matchIndex;
	protected int maxSubMatches;
	protected String[] subGrammars;
	protected Matcher[] subStates;
	protected Matcher parent;
	protected Matcher nonDeterministicAncestor;
	protected HashSet<String> captureGroupNames = null;
	protected Hashtable<String, LinkedList<String>> capturedMaps;
	protected String capturedValue=null;
	protected LinkedList<Integer> endIndices;
	protected Matcher previous = null;
	protected int indexInParent;
	protected HashSet<String> componentsForSubgrammars;
	protected boolean isQuantifier=false;
	protected boolean isAlternation = false;
	public boolean isNonterminal=false;
	boolean priorSuccess=false;
	public int type=0;
	public Hashtable<String, Integer> nonTerminalMatches;
	
	public boolean finalized=false;
	Hashtable<String, StringDistribution> processedDistributions;
	
	public Matcher()
	{
		processedDistributions = new Hashtable<String, StringDistribution>();
	}
	
	public Matcher(CFGParser.GlobalState global)
	{
		this.global = global;
		processedDistributions = new Hashtable<String, StringDistribution>();
	}

	public int setExecutionParameters(CFGParser.GlobalState global, int indexInParent, int start, String inputString, Hashtable<String, LinkedList<String>> capturedComponents, HashSet<String> nonTerminalsToCapture, Matcher matchParent, Matcher mismatchParent, Hashtable<String,Matcher> compiledComponents)
	{
		this.indexInParent=indexInParent;
		this.startIndex = start;
		this.inputString=inputString;
		this.capturedMaps = capturedComponents;
		this.captureGroupNames = nonTerminalsToCapture;
		this.parent = matchParent;
		this.nonDeterministicAncestor=mismatchParent;
		this.endIndex=this.startIndex;
		this.parsedComponents=compiledComponents;
		this.global=global;
		
		return global.shouldSkipComponent(grammarComponent, startIndex);
	}
	
	public String getPatternToMatch()
	{
		return grammarComponent;
	}
	
	
	public Matcher getNonDeterministicPrior()
	{
		return nonDeterministicAncestor;
	}
	
	
	public boolean isFinalized()
	{
		return finalized;
	}
	
	public int getEndIndex()
	{
		return endIndex;
	}
	
	public LinkedList<String> getCapturedList(String name)
	{
		
		if ((capturedMaps!=null)&&(capturedMaps.containsKey(name)))
		{
			return capturedMaps.get(name);
		}
		else
			return null;
	}
	
	
	public String getFirstCapturedValue(String name)
	{
		LinkedList<String> v = getCapturedList(name);
		if ((v!=null)&&(v.size()>0))
			return v.getFirst();
		else
			return null;
			
	}
	
	
	public Hashtable<String, LinkedList<String>> getCaptureSet()
	{
		return capturedMaps;
		
	}
	
	public void setComponentsForSubgrammars(HashSet<String> componentsForSubgrammars)
	{
		this.componentsForSubgrammars = componentsForSubgrammars;
	}
	
	public void logState()
	{
		logState("general");
		
	}
	
	public static com.evolved.automata.filetools.SimpleLogger log;
	
	
	
	public static int logcount=0;
	public void logState(String message)
	{
		if (log==null)
			return;
		logcount++;
		String statePattern = "%1$s, %2$s,  %3$s,  %4$s, %5$s, %6$s, %7$s, %8$s, %9$s";
		String output = String.format(statePattern,
				SimpleLogger.delimitIfNeeded(message),
				SimpleLogger.delimitIfNeeded(this.toString()), 
				SimpleLogger.delimitIfNeeded((grammarComponent==null?"null":grammarComponent)),							
				startIndex,
				endIndex,
				indexInParent,
				maxSubMatches,
				SimpleLogger.delimitIfNeeded(((endIndex>startIndex)?inputString.substring(startIndex, endIndex):"")),
				SimpleLogger.delimitIfNeeded(inputString)
				);
		log.logMessage(""+ logcount+ ":: "+output);
		
	}
	
	
	protected void finalize()
	{
		
		finalized=true;

	}
	
	protected void mergeMaps(Hashtable<String, LinkedList<String>> sourceMap, Hashtable<String, LinkedList<String>> targetMap)
	{
		LinkedList<String> list = null;
		for (String key:sourceMap.keySet())
		{
			list = sourceMap.get(key);
			if (targetMap.containsKey(key))
			{
				targetMap.get(key).addAll(list);
			}
			else
			{
				targetMap.put(key, new LinkedList<String>());
				targetMap.get(key).addAll(list);
			}
		}
	}
	
	
	
	
	
	
	protected  LinkedList<Matcher> updateFromParseSuccess(Matcher subChild, int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, Matcher nonDeterministicPrior )
	{
		return null;
	}
	
	protected  LinkedList<Matcher> updateFromParseFailure(Matcher subChild, int nextIndex)
	{
		
		
		if (nonDeterministicAncestor==null)
			return null;
		else
			return nonDeterministicAncestor.updateFromParseFailure(this, -1);
	}
	
	protected void updateFailureCache(Matcher nonDeterministricPrior)
	{
		
		if (!priorSuccess&&nonDeterministicAncestor==nonDeterministricPrior)
		{
			global.setFailureStatus(grammarComponent, startIndex);
			if (parent!=null)
				parent.updateFailureCache(nonDeterministricPrior);
		}
	}
	
	public abstract LinkedList<Matcher> match();

	public abstract Matcher clone();
}
