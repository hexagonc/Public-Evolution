package com.evolved.automata.parser;

import java.util.LinkedList;

public class LiteralString extends UnitParser{
	
	public LiteralString(String value, int endIndex)
	{
		super();
		grammarComponent=value;
		this.endIndex = endIndex;
	}

	@Override
	public LinkedList<UnitParser> matchCompiled() {
		
		return null;
	}

	@Override
	public UnitParser clone() {
		
		return new LiteralString(grammarComponent, endIndex);
	}
}
