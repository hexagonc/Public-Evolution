package com.evolved.automata.parser;

import java.util.*;

public class NonTerminalMatcherState extends UnitParser
{
	
	String redefinitionGrammar = null;
	String selfCaptured;
	boolean captureSelf;
	boolean firstQuantifierCapture=true;
	int capturedIndex=0;
	AlternationMatcherState alternativeChild;
	boolean backReferenceP=false;
	int backIndex;
	String backReferenceBaseGrammar;
	
	
	public int setExecutionParameters(CFGParser.GlobalState global, int indexInParent, int start, String inputString, Hashtable<String, LinkedList<String>> capturedComponents, HashSet<String> nonTerminalsToCapture, UnitParser matchParent, UnitParser mismatchParent, Hashtable<String,UnitParser> comp)
	{
		int out;
		out=super.setExecutionParameters(global, indexInParent, start, inputString, capturedComponents, nonTerminalsToCapture, matchParent,mismatchParent, comp );
		captureSelf = nonTerminalsToCapture!=null&&nonTerminalsToCapture.contains(grammarComponent);
		this.endIndex=this.startIndex;
		return out;
	}
	
	
	public NonTerminalMatcherState(CFGParser.GlobalState global,String grammarComponent)
	{
		super(global);
		this.grammarComponent=grammarComponent;
		backReferenceP=false;
		alternativeChild = null;
		firstQuantifierCapture=true;
		isNonterminal=true;
	}
	
	public UnitParser clone()
	{
		if (backReferenceP)
			return new NonTerminalMatcherState(global, grammarComponent, backIndex);
		else
			return new NonTerminalMatcherState(global, grammarComponent);
	}
	
	// Back-reference NonTerminal
	public NonTerminalMatcherState(CFGParser.GlobalState global, String grammarComponent, int backIndex)
	{
		super(global);
		backReferenceBaseGrammar = grammarComponent;
		this.grammarComponent=grammarComponent;
		backReferenceP=true;
		this.backIndex=backIndex;
		capturedIndex=0;
		
	}
	
	
	
	
	public LinkedList<UnitParser> matchCompiled()
	{
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		if (backReferenceP)
		{
			
			if (capturedMaps!=null&&capturedMaps.containsKey(backReferenceBaseGrammar))
			{
				LinkedList<String> captured = capturedMaps.get(backReferenceBaseGrammar);
				if (captured.size()>=backIndex)
				{
					String value = captured.get(backIndex-1);
					if (startIndex+value.length()<=inputString.length())
					{
						if (inputString.substring(startIndex, startIndex+value.length()).equals(value))
						{
							endIndex = startIndex+value.length();
							if (parent!=null)
							{
								return parent.compiledSuccessUpdate(this, indexInParent+1,capturedMaps, nonDeterministicAncestor);
							}
							else
							{
								this.finalize();
								grammarList.add(this);
								return grammarList;
							}
						}
					}
				}
			}
			
			if (nonDeterministicAncestor!=null)
				return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
			else
			{
				return null;
			}
		}
		else
		{
			UnitParser firstState = compiledComponents.get(grammarComponent).clone();
			
			int processType;
			processType= firstState.setExecutionParameters(global, 0, startIndex, inputString, capturedMaps, captureGroupNames, this, nonDeterministicAncestor, compiledComponents);
			
			if (processType!=CFGParser.GlobalState.STARTED)
			{
				updateFailureCache(nonDeterministicAncestor);
				return compiledFailureUpdate(this, -1);
			}
			grammarList.add(firstState);
			if ((firstState!=null)&&(firstState.isAlternation))
				alternativeChild = (AlternationMatcherState)firstState;
			return grammarList;
		}
		
		
	}
	
	
	protected LinkedList<UnitParser> compiledSuccessUpdate(UnitParser subChild, int nextIndex,Hashtable<String, LinkedList<String>> nextCapturedMap, UnitParser nonDeterministicPrior )
	{
		endIndex = subChild.getEndIndex();
		priorSuccess=true;
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		matchIndex=nextIndex;
		
		if (captureSelf&&(endIndex>startIndex))
		{
			
			if ((componentsForSubgrammars!=null)&&(componentsForSubgrammars.contains(grammarComponent)))
			{
				if (alternativeChild!=null)
					selfCaptured = alternativeChild.getLastMatchGrammar();
				else
				{
					
					UnitParser mapped = compiledComponents.get(grammarComponent);
					if (mapped instanceof NonTerminalMatcherState)
					{
						selfCaptured = mapped.grammarComponent;
					}
					else
						selfCaptured = inputString.substring(startIndex, endIndex);
				}
			}
			else
				selfCaptured = inputString.substring(startIndex, endIndex);
			LinkedList<String> l = null;
			if (nextCapturedMap!=null)
			{
				if (nextCapturedMap.containsKey(grammarComponent))
				{
					l = nextCapturedMap.get(grammarComponent);
					if (!firstQuantifierCapture)
					{
						l.set(capturedIndex, selfCaptured);
						
					}
					else
					{
						firstQuantifierCapture=false;
						l.add(selfCaptured);
						capturedIndex = l.size()-1;
					}
				}
				else
				{
					l = new LinkedList<String>();
					l.add(selfCaptured);
					nextCapturedMap.put(grammarComponent, l);
					capturedIndex=0;
					firstQuantifierCapture=false;
				}
			}
			else
			{
				nextCapturedMap = new Hashtable<String, LinkedList<String>>();
				l = new LinkedList<String>();
				l.add(selfCaptured);
				nextCapturedMap.put(grammarComponent, l);
				capturedIndex=0;
				firstQuantifierCapture=false;
			}
			
		}
		
		if (parent!=null)
		{
			return parent.compiledSuccessUpdate(this, indexInParent+1,nextCapturedMap, nonDeterministicPrior);
		}
		else
		{
			
			subChild.finalize();
			subChild.capturedMaps = nextCapturedMap;
			grammarList.add(subChild);
			return grammarList;
		}
		
		
	}
	
}
