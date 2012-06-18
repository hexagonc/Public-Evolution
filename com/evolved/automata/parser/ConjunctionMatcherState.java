package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class ConjunctionMatcherState extends UnitParser 
{
	
	public ConjunctionMatcherState(CFGParser.GlobalState global, UnitParser[] subStates)
	{
		super(global);
		this.subStates = subStates;
		maxSubMatches = subStates.length-1;
		matchIndex = 0;
	}
	
	public UnitParser clone()
	{
		
		// TODO: Review whether need to clone subStates, probably not
		ConjunctionMatcherState n = new ConjunctionMatcherState(global, subStates);
		n.grammarComponent=grammarComponent;
		
		return n; 
	}
	
	
	public LinkedList<UnitParser> matchCompiled()
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		UnitParser firstState = null;
		firstState = subStates[0].clone();
		int processType;
		processType=firstState.setExecutionParameters(global, 0, startIndex, inputString, capturedMaps, captureGroupNames, this, nonDeterministicAncestor, compiledComponents);
		if (processType!=CFGParser.GlobalState.STARTED)
		{
			updateFailureCache(nonDeterministicAncestor);
			return compiledFailureUpdate(this, -1);
		}
		
		grammarList.add(firstState);
		return grammarList;
	}
	
	protected LinkedList<UnitParser> compiledSuccessUpdate(UnitParser subChild, int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, UnitParser nonDeterministicPrior )
	{
		endIndex = subChild.getEndIndex();
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		matchIndex=nextIndex;
		logState("success update");
		if (matchIndex>maxSubMatches)
		{
			finalize();
			if (parent!=null)
			{
				grammarList = parent.compiledSuccessUpdate(this, indexInParent+1, nextCapturedMap, nonDeterministicPrior);
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
			UnitParser nextState = subStates[matchIndex].clone();
			int processType;
			processType=nextState.setExecutionParameters(global, matchIndex, endIndex, inputString, nextCapturedMap, captureGroupNames, this, nonDeterministicPrior, compiledComponents);
			if (processType!=CFGParser.GlobalState.STARTED)
			{
				if (nonDeterministicPrior==nonDeterministicAncestor)
				{
					updateFailureCache(nonDeterministicPrior);
				}
				return nonDeterministicPrior.compiledFailureUpdate(this, -1);
			}
			
			grammarList.add(nextState);
			return grammarList;
		}
		
		
	}
}
