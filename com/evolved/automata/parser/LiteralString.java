package com.evolved.automata.parser;

import java.util.LinkedList;

class LiteralString extends Matcher{
	
	public LiteralString(String value, int endIndex)
	{
		super();
		grammarComponent=value;
		this.endIndex = endIndex;
	}

	@Override
	public LinkedList<Matcher> match() {
		
		return null;
	}

	@Override
	public Matcher clone() {
		
		return new LiteralString(grammarComponent, endIndex);
	}
}
