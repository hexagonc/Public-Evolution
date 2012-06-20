package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

class OptimizedAlternationMatcher extends Matcher {

	Hashtable<Integer, Hashtable<String, Integer>> matchSet;
	StringDistribution matchedDistributions;
	Integer[] viableEndIndices;

	public OptimizedAlternationMatcher(String grammarComponent, CFGParser.GlobalState global, StringDistribution processedDistributions)
	{
		super(global);
		matchedDistributions = processedDistributions;
		matchIndex = 0;
		this.grammarComponent=grammarComponent;
	}
	
	
	
	public Matcher clone()
	{
		return new OptimizedAlternationMatcher(grammarComponent, global, matchedDistributions);
	}
	
	public int setExecutionParameters(CFGParser.GlobalState global, int indexInParent, int start, String inputString, Hashtable<String, LinkedList<String>> capturedComponents, HashSet<String> nonTerminalsToCapture, Matcher matchParent, Matcher mismatchParent, Hashtable<String,Matcher> compiledComponents)
	{
		int processType;;
		processType=super.setExecutionParameters(global, indexInParent, start, inputString, capturedComponents, nonTerminalsToCapture, matchParent, mismatchParent, compiledComponents);
		viableEndIndices = matchedDistributions.matchString(start, inputString);
		if (viableEndIndices!=null)
			maxSubMatches = viableEndIndices.length-1;
		else
			maxSubMatches=0;
		return processType;
	}
	
	
	public LinkedList<Matcher> match()
	{
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		
		if (viableEndIndices==null)
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.updateFromParseFailure(this, -1);
		}
		else
		{
			Matcher subChild = new LiteralString(inputString.substring(startIndex,viableEndIndices[matchIndex]), viableEndIndices[matchIndex]);
		
			if (parent!=null)
			{
				return parent.updateFromParseSuccess(subChild, indexInParent+1, capturedMaps, this);
			}
			else
			{
				
				subChild.finalize();
				grammarList.add(subChild);
				
				return grammarList;
			}
		}
	}

	
	protected LinkedList<Matcher> updateFromParseFailure(Matcher subChild, int nextIndex)
	{
		
		matchIndex++;
		if (matchIndex>maxSubMatches)
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.updateFromParseFailure(this, -1);
		}
		else
		{
			return match();
		}
	}
}
