package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class QuantifierMatcherState extends UnitParser
{
	Integer minimumRequiredMatches;
	Integer maximimRequiredMatches;
	boolean matchingQuantifier;
	boolean matchingNegative;
	boolean greedyQuantifierP=true;
	LinkedList<UnitParser> backList;
	LinkedList<UnitParser> priors;
	LinkedList<Hashtable<String, LinkedList<String>>> capturesetList;
	
	
	public QuantifierMatcherState(CFGParser.GlobalState global, boolean greedy,Integer minimumMatches, Integer maximumMatches, UnitParser subState)
	{
		super(global);
		subStates = new UnitParser[1];
		subStates[0]=subState;
		minimumRequiredMatches = minimumMatches;
		maximimRequiredMatches = maximumMatches;
		
		matchingNegative = (maximimRequiredMatches!=null)&&(maximimRequiredMatches.intValue()==0);
		
		matchingQuantifier = false;
		
		matchIndex = 0;
		endIndex=startIndex;
		isQuantifier = true;
		greedyQuantifierP=greedy;
		if (greedyQuantifierP)
		{
			backList= new LinkedList<UnitParser>();
			priors = new LinkedList<UnitParser>();
			capturesetList = new LinkedList<Hashtable<String,LinkedList<String>>>();
		}
		else
		{
			backList = null;
			priors = null;
			capturesetList=null;
		}
	}
	
	public UnitParser clone()
	{
		QuantifierMatcherState comp = new QuantifierMatcherState(global, greedyQuantifierP,minimumRequiredMatches, maximimRequiredMatches, subStates[0]); 
		comp.grammarComponent=grammarComponent;
		
		return comp;
	}
	
	
	
	public LinkedList<UnitParser> matchCompiled()
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		// Match negated quantifier
		previous = subStates[0].clone();
		int processType;
		// TODO: Consider consolidating this conditional logic.  Even though it is
		// more verbose, looks clearer as is
		if (matchingNegative)
		{
			processType = previous.setExecutionParameters(global, matchIndex, startIndex, inputString, (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():null, captureGroupNames, this, this, compiledComponents);
			if (processType != CFGParser.GlobalState.STARTED)
			{
				return compiledFailureUpdate(this, -1);
			}
			grammarList.add(previous);
			return grammarList;
		}
		else
		{
			if (greedyQuantifierP)
			{
				
				if (maximimRequiredMatches==null||matchIndex<=maximimRequiredMatches.intValue())
				{
					matchingQuantifier=true;
					processType = previous.setExecutionParameters(global, matchIndex, startIndex, inputString, (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():new Hashtable<String, LinkedList<String>>(), captureGroupNames, this, this, compiledComponents);
					if (processType != CFGParser.GlobalState.STARTED)
					{
						return compiledFailureUpdate(this, -1);
					}
					grammarList.add(previous);
					return grammarList;
				}
				else
				{
					updateFailureCache(nonDeterministicAncestor);
					if (nonDeterministicAncestor==null)
						return null;
					else
						return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
				}
			}
			else
			{
				if ((matchIndex < minimumRequiredMatches.intValue())||(parent==null))
				{
					
					processType = previous.setExecutionParameters(global, matchIndex, startIndex, inputString, (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():null, captureGroupNames, this, this, compiledComponents);
					if (processType != CFGParser.GlobalState.STARTED)
					{
						return compiledFailureUpdate(this, -1);
					}
					grammarList.add(previous);
					return grammarList;
				}
				else
				{
					
					return parent.compiledSuccessUpdate(this, indexInParent+1,capturedMaps, this );
				}
			}
			
			
			
		}
		
	}
	
	protected LinkedList<UnitParser> compiledFailureUpdate(UnitParser subChild, int nextIndex)
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		UnitParser priorChild=null, childPrior=null;
		int processType;
		if (matchingNegative)
		{
			matchingQuantifier=false;
			return parent.compiledSuccessUpdate(this, indexInParent+1,capturedMaps, nonDeterministicAncestor );
		}
		else
		{
			if (greedyQuantifierP)
			{
				Hashtable<String, LinkedList<String>> newCaptureTable =null;
				
				if (!matchingQuantifier)
				{ // This means that the rest of the string can not be matched with the starting point
				  // defined by the prior match
					matchIndex--;
					if (backList.size()>0)
					{
						priorChild = backList.removeLast();
						childPrior = priors.removeLast();
						endIndex=priorChild.startIndex;
						capturesetList.removeLast();
					}
					
					if (capturesetList.size()>0)
					{
						newCaptureTable=capturesetList.getLast();
					}
					else
					{
						newCaptureTable = (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():new Hashtable<String, LinkedList<String>>();
					}
					
					// Check if the prior match can be processed differently
					if (priorChild!=null&&childPrior!=this)
					{
						// Try to find another solution
						matchingQuantifier=true;
						return childPrior.compiledFailureUpdate(this, -1);
					}
					else
					{// Can't use prior match at all, must completely discard
					 // Check if enough prior matches exist to move forward
						
						if (minimumRequiredMatches==null || matchIndex>=minimumRequiredMatches.intValue())
						{
							if (parent!=null)
							{
								matchingQuantifier=false;
								return parent.compiledSuccessUpdate(this, indexInParent+1,  newCaptureTable, this);
							}
							else
							{								
								subChild.finalize();
								grammarList.add(subChild);
								return grammarList;
							}
						}
						else
						{// Minimum number of matches not satisfied by this failure, must fail entirely
							updateFailureCache(nonDeterministicAncestor);
							if (nonDeterministicAncestor==null)
								return null;
							else
								return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
						}
						
					}
					
				}
				else
				{
					if (capturesetList.size()>0)
					{
						newCaptureTable=capturesetList.getLast();
					}
					else
					{
						newCaptureTable = (capturedMaps!=null)?(Hashtable<String, LinkedList<String>> )capturedMaps.clone():new Hashtable<String, LinkedList<String>>();
					}
					
					// Failed to match the quantifier.  Check if minimum number of matches satisfied
					if (minimumRequiredMatches==null || backList.size()>=minimumRequiredMatches.intValue())
					{// Since you can't match the quantifier, try to match the rest of the pattern
						if (parent!=null)
						{
							matchingQuantifier=false;
							return parent.compiledSuccessUpdate(this, indexInParent+1, newCaptureTable, this);
						}
						else
						{// no rest of the pattern, so you're done								
							subChild.finalize();
							grammarList.add(subChild);
							return grammarList;
						}
					}
					else
					{// fail the entire quantifier
						updateFailureCache(nonDeterministicAncestor);
						if (nonDeterministicAncestor==null)
							return null;
						else
							return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
					}
					
				}
			
			}
			
			
			if (matchingQuantifier || (matchIndex < minimumRequiredMatches.intValue()))
			{
				updateFailureCache(nonDeterministicAncestor);
				if (nonDeterministicAncestor==null)
					return null;
				else
					return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
			}
			else
			{
				// try matching the quantifier
				
				if ((maximimRequiredMatches == null)||(maximimRequiredMatches.intValue()>matchIndex))
				{
					matchingQuantifier=true;
					if (endIndex>=inputString.length())
					{
						if (parent!=null)
						{
							return parent.compiledSuccessUpdate(this, indexInParent+1, capturedMaps, nonDeterministicAncestor);
						}
						else
						{
							if (nonDeterministicAncestor!=null)
							{
								LinkedList<UnitParser> nextPoints = nonDeterministicAncestor.compiledFailureUpdate(subChild, -1);
								if (nextPoints!=null)
									grammarList.addAll(nextPoints);
							}
							subChild.finalize();
							grammarList.add(subChild);
							return grammarList;
						}
					}
					previous = previous.clone();
					processType = previous.setExecutionParameters(global, matchIndex, endIndex, inputString, capturedMaps, captureGroupNames, this, this, compiledComponents);
					if (processType != CFGParser.GlobalState.STARTED)
						return compiledFailureUpdate(this, -1);
					
					grammarList.add(previous);
					return grammarList;
				}
				else
				{
					if (nonDeterministicAncestor!=null)
						return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
					else
					{
						return null;
					}
				}
				
			}
		}
	}
	
	protected LinkedList<UnitParser> compiledSuccessUpdate(UnitParser subChild,  int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, UnitParser nonDeterministicPrior)
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		endIndex = subChild.getEndIndex();
		
		matchIndex =nextIndex;
		UnitParser nextState = null;
		int processType;
		
		if (matchingNegative)
		{
			
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
		}
		
		if (greedyQuantifierP)
		{
			Hashtable<String, LinkedList<String>> newCaptureTable =null;
			
			if (nextCapturedMap==null)
				newCaptureTable = new Hashtable<String, LinkedList<String>>();
			else
				newCaptureTable=nextCapturedMap;
			
			backList.add(subChild);
			priors.add(nonDeterministicPrior);
			capturesetList.add(newCaptureTable);
			
			if (maximimRequiredMatches==null || matchIndex<maximimRequiredMatches)
			{
				matchingQuantifier=true;
				nextState = subStates[0].clone();
				processType = nextState.setExecutionParameters(global, matchIndex, endIndex, inputString, newCaptureTable, captureGroupNames, this, this,compiledComponents);
				if (processType != CFGParser.GlobalState.STARTED)
					compiledFailureUpdate(this, -1);
				
				grammarList.add(nextState);
				return grammarList;
			}
			else
			{
				// Need to match the thing that comes next, if there is such a thing
				if (parent!=null)
				{
					matchingQuantifier=false;
					return parent.compiledSuccessUpdate(this, indexInParent+1, newCaptureTable, this);
				}
				else
				{
					
					subChild.finalize();
					grammarList.add(subChild);
					return grammarList;
				}
			}
		}
		else
		{
			capturedMaps = nextCapturedMap;
			if (matchIndex<minimumRequiredMatches.intValue())
			{
				nextState = subStates[0].clone();
				processType = nextState.setExecutionParameters(global, matchIndex, endIndex, inputString, nextCapturedMap, captureGroupNames, this, nonDeterministicPrior,compiledComponents);
				if (processType != CFGParser.GlobalState.STARTED)
				{
					if (nonDeterministicPrior!=nonDeterministicAncestor)
					{
						return nonDeterministicPrior.compiledFailureUpdate(this, -1);
					}
					else
						return compiledFailureUpdate(this, -1);
				}
				
				grammarList.add(nextState);
				return grammarList;
			}
			else
			{
				
				if (parent!=null)
				{
					matchingQuantifier=false;
					return parent.compiledSuccessUpdate(this, indexInParent+1,nextCapturedMap, nonDeterministicPrior );
				}
				else
				{
					if (endIndex>=inputString.length())
					{
						if (parent!=null)
						{
							matchingQuantifier=false;
							return parent.compiledSuccessUpdate(this, indexInParent+1, nextCapturedMap, nonDeterministicPrior);
						}
						else
						{
							if (nonDeterministicPrior!=null)
							{
								LinkedList<UnitParser> nextPoints = nonDeterministicPrior.compiledFailureUpdate(subChild, -1);
								if (nextPoints!=null)
									grammarList.addAll(nextPoints);
							}
							subChild.finalize();
							grammarList.add(subChild);
							return grammarList;
						}
					}
					
					
					matchingQuantifier=true;
					nextState = subStates[0].clone();
					processType = nextState.setExecutionParameters(global, matchIndex, endIndex, inputString, nextCapturedMap, captureGroupNames, this, nonDeterministicPrior, compiledComponents);
					if (processType != CFGParser.GlobalState.STARTED)
					{
						if (nonDeterministicPrior!=nonDeterministicAncestor)
						{
							return nonDeterministicPrior.compiledFailureUpdate(this, -1);
						}
						else
							return compiledFailureUpdate(this, -1);
					}
					grammarList.add(nextState);
					return grammarList;
				}
			}
			
		}
		
		
		
	}
}
