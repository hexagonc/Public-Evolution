package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class OptimizedAlternation extends UnitParser {

	Hashtable<Integer, Hashtable<String, Integer>> matchSet;
	StringDistribution matchedDistributions;
	Integer[] viableEndIndices;

	public OptimizedAlternation(CFGParser.GlobalState global, StringDistribution processedDistributions)
	{
		super(global);
		matchedDistributions = processedDistributions;
		matchIndex = 0;
	}
	
	public UnitParser clone()
	{
		return new OptimizedAlternation(global, matchedDistributions);
	}
	
	public int setExecutionParameters(CFGParser.GlobalState global, int indexInParent, int start, String inputString, Hashtable<String, LinkedList<String>> capturedComponents, HashSet<String> nonTerminalsToCapture, UnitParser matchParent, UnitParser mismatchParent, Hashtable<String,UnitParser> compiledComponents)
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
	
	
	
	
	public LinkedList<UnitParser> matchCompiled()
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		
		if (viableEndIndices==null)
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
		}
		else
		{
			UnitParser subChild = new LiteralString(inputString.substring(startIndex,viableEndIndices[matchIndex]), viableEndIndices[matchIndex]);
		
			if (parent!=null)
			{
				return parent.compiledSuccessUpdate(subChild, indexInParent+1, capturedMaps, this);
			}
			else
			{
				
				subChild.finalize();
				grammarList.add(subChild);
				
				return grammarList;
			}
		}
	}

	
	protected LinkedList<UnitParser> compiledFailureUpdate(UnitParser subChild, int nextIndex)
	{
		
		matchIndex++;
		if (matchIndex>maxSubMatches)
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
		}
		else
		{
			return matchCompiled();
		}
	}
}
