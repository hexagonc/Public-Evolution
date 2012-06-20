package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

class ConjunctionMatcher extends Matcher 
{
	
	public ConjunctionMatcher(String grammarComponent, CFGParser.GlobalState global, Matcher[] subStates)
	{
		super(global);
		this.subStates = subStates;
		maxSubMatches = subStates.length-1;
		matchIndex = 0;
		this.grammarComponent=grammarComponent;
	}
	
	public Matcher clone()
	{
		ConjunctionMatcher n = new ConjunctionMatcher(grammarComponent, global, subStates);
		return n; 
	}
	
	
	public LinkedList<Matcher> match()
	{
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		Matcher firstState = null;
		firstState = subStates[0].clone();
		int processType;
		processType=firstState.setExecutionParameters(global, 0, startIndex, inputString, capturedMaps, captureGroupNames, this, nonDeterministicAncestor, parsedComponents);
		if (processType!=CFGParser.GlobalState.STARTED)
		{
			updateFailureCache(nonDeterministicAncestor);
			return updateFromParseFailure(this, -1);
		}
		
		grammarList.add(firstState);
		return grammarList;
	}
	
	protected LinkedList<Matcher> updateFromParseSuccess(Matcher subChild, int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, Matcher nonDeterministicPrior )
	{
		endIndex = subChild.getEndIndex();
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		matchIndex=nextIndex;
		logState("success update");
		if (matchIndex>maxSubMatches)
		{
			finalize();
			if (parent!=null)
			{
				grammarList = parent.updateFromParseSuccess(this, indexInParent+1, nextCapturedMap, nonDeterministicPrior);
				return grammarList;
			}
			else
			{
				subChild.finalize();
				grammarList.add(subChild);
				subChild.capturedMaps = nextCapturedMap;
				return grammarList;
			}
		}
		else
		{
			Matcher nextState = subStates[matchIndex].clone();
			int processType;
			processType=nextState.setExecutionParameters(global, matchIndex, endIndex, inputString, nextCapturedMap, captureGroupNames, this, nonDeterministicPrior, parsedComponents);
			if (processType!=CFGParser.GlobalState.STARTED)
			{
				if (nonDeterministicPrior==nonDeterministicAncestor)
				{
					updateFailureCache(nonDeterministicPrior);
				}
				return nonDeterministicPrior.updateFromParseFailure(this, -1);
			}
			
			grammarList.add(nextState);
			return grammarList;
		}
		
		
	}
}
