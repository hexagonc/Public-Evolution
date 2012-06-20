package com.evolved.automata.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

class TerminalMatcher extends Matcher 
{
	
	
	public TerminalMatcher(CFGParser.GlobalState global, String grammarComponent)
	{
		super(global);
		
		this.grammarComponent=grammarComponent;
		
	}
	
	public Matcher clone()
	{
		return new TerminalMatcher(global, grammarComponent);
	}
	
	
	
	public LinkedList<Matcher> match()
	{
		boolean tMatch=false;
		LinkedList<Matcher> grammarList = new LinkedList<Matcher>();
		if ((inputString==null) || (!(tMatch=matchEndString(startIndex)) && inputString.length()<=startIndex) ||(inputString.length()==0))
		{
			updateFailureCache(nonDeterministicAncestor);
			if (nonDeterministicAncestor==null)
				return null;
			else
				return nonDeterministicAncestor.updateFromParseFailure(this, -1);
		}
		else
		{
			if (tMatch || matchTerminal(grammarComponent, inputString.charAt(startIndex)))
			{
				endIndex=(tMatch)?startIndex:startIndex+1;
				if (parent!=null)
				{
					
					return parent.updateFromParseSuccess(this, indexInParent+1,capturedMaps,nonDeterministicAncestor);
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
					return nonDeterministicAncestor.updateFromParseFailure(this, -1);
			}
		}
	}
	
	private boolean matchEndString(int index)
	{
		return grammarComponent.equals(Parser.endString)&&index==inputString.length();
			
	}
	
	private boolean matchTerminal(String terminal, char raw)
	{
		if (terminal.length()>1)
			return terminal.charAt(1)==raw;
		else
		{
			
			if (terminal.substring(0,1).equals(Parser.wildcard))
				return true;
			else if (terminal.substring(0,1).equals(Parser.whitespace))
				return Character.isWhitespace(raw);
			else if (terminal.substring(0,1).equals(Parser.numeric))
				return Character.isDigit(raw);
			else if (terminal.substring(0,1).equals(Parser.letter))
				return Character.isLetter(raw);
			else
				return terminal.charAt(0) == raw;
		}
	}
	
}
