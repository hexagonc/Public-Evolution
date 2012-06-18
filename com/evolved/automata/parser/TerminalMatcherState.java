package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

public class TerminalMatcherState extends UnitParser 
{
	
	
//	public TerminalMatcherState(CFGParser.GlobalState global, int indexInParent, String grammarComponent, int initialIndex, String inputString, String matchGrammar, Hashtable<String, LinkedList<String>> nextCapturedMap, UnitParser parent, UnitParser nonDeterministicAncestor)
//	{
//		super(global);
//		this.indexInParent=indexInParent;
//		this.grammarComponent=matchGrammar;
//		this.parent = parent;
//		this.nonDeterministicAncestor=nonDeterministicAncestor;
//		this.inputString = inputString;
//		
//		startIndex = initialIndex;
//		capturedMaps = nextCapturedMap;
//	}
	
	public TerminalMatcherState(CFGParser.GlobalState global, String grammarComponent)
	{
		super(global);
		
		this.grammarComponent=grammarComponent;
		
	}
	
	public UnitParser clone()
	{
		return new TerminalMatcherState(global, grammarComponent);
	}
	
	
	
	public LinkedList<UnitParser> matchCompiled()
	{
		boolean tMatch=false;
		LinkedList<UnitParser> grammarList = new LinkedList<UnitParser>();
		if ((inputString==null) || (!(tMatch=matchEndString(startIndex)) && inputString.length()<=startIndex) ||(inputString.length()==0))
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
		}
		else
		{
			if (tMatch || matchTerminal(grammarComponent, inputString.charAt(startIndex)))
			{
				endIndex=(tMatch)?startIndex:startIndex+1;
				if (parent!=null)
				{
					
					return parent.compiledSuccessUpdate(this, indexInParent+1,capturedMaps,nonDeterministicAncestor);
				}
				else
				{
					
					grammarList.add(this);
					return grammarList;
				}
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
	}
	
	private boolean matchEndString(int index)
	{
		return grammarComponent.equals(UnitParser.endString)&&index==inputString.length();
			
	}
	
	private boolean matchTerminal(String terminal, char raw)
	{
		if (terminal.length()>1)
			return terminal.charAt(1)==raw;
		else
		{
			
			if (terminal.substring(0,1).equals(wildcard))
				return true;
			else if (terminal.substring(0,1).equals(whitespace))
				return Character.isWhitespace(raw);
			else if (terminal.substring(0,1).equals(numeric))
				return Character.isDigit(raw);
			else if (terminal.substring(0,1).equals(letter))
				return Character.isLetter(raw);
			else
				return terminal.charAt(0) == raw;
		}
	}
	
}
