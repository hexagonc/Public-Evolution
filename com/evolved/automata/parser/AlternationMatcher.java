package com.evolved.automata.parser;

import java.util.*;
 
class AlternationMatcher extends Matcher{
	
	protected int[] endIndiceArray;
		
	public AlternationMatcher(String grammarComponent, CFGParser.GlobalState global, Matcher[] subStates, String[] subgrammars)
	{
		super(global);
		this.subStates = subStates;
		maxSubMatches = subStates.length-1;
		matchIndex = 0;
		isAlternation = true;
		this.subGrammars = subgrammars;
		this.grammarComponent=grammarComponent;
	}

	
	
	
	
	public Matcher clone()
	{
		AlternationMatcher out = new AlternationMatcher(grammarComponent, global, subStates,subGrammars);
		
		return out;
	}
	
	public String getLastMatchGrammar()
	{
		return subGrammars[matchIndex];
	}
	
	
	
	
	public LinkedList<Matcher> match()
	{
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		Matcher firstState = null;
		firstState = subStates[0].clone();
		int processType;
		processType=firstState.setExecutionParameters(global, 0, startIndex, inputString, (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():null, captureGroupNames, this, this, parsedComponents);
		if (processType!=CFGParser.GlobalState.STARTED)
		{
			return updateFromParseFailure(this, -1);
		}
		
		grammarList.add(firstState);
		return grammarList;
	}
	

	protected LinkedList<Matcher> updateFromParseSuccess(Matcher subChild, int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, Matcher nonDeterministicPrior)
	{
		
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		if (parent!=null)
		{ 
			return parent.updateFromParseSuccess(subChild, indexInParent+1, nextCapturedMap, nonDeterministicPrior);
		}
		else
		{
			subChild.finalize();
			grammarList.add(subChild);
			subChild.capturedMaps = nextCapturedMap;
			return grammarList;
		}
		
		
	}
	
	protected LinkedList<Matcher> updateFromParseFailure(Matcher subChild, int nextIndex)
	{
		
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		
		if (nextIndex==-1) // this only happens when the overall pattern has completed
			matchIndex++;
		else
			matchIndex = nextIndex;
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
			
			Matcher nextState = subStates[matchIndex].clone();
			int processType;
			processType = nextState.setExecutionParameters(global, matchIndex, startIndex, inputString, (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():null, captureGroupNames, this, this, parsedComponents);
			if (processType!=CFGParser.GlobalState.STARTED)
			{
				return updateFromParseFailure(this, -1);
			}
			
			grammarList.add(nextState);
			return grammarList;
			
		}
	}
}
