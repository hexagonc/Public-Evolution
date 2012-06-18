package com.evolved.automata.parser;
import java.util.*;

public class StringDistribution 
{
	private LinkedList<Integer> stringLengths;
	private HashSet<String> possibleStrings;
	
	public StringDistribution()
	{
		stringLengths = new LinkedList<Integer>();
		possibleStrings = new HashSet<String>();
	}
	
	public void addString(String value)
	{
		if (!possibleStrings.contains(value))
		{
			possibleStrings.add(value);
			int size = value.length();
			for (Integer length:stringLengths)
			{
				if (length.intValue()==size)
					return;
			}
			stringLengths.add(new Integer(size));
		}
	}
	
	public Integer[] matchString(int startIndex, String inputString)
	{
		LinkedList<Integer> o=null;
		String subString;
		for (Integer i:stringLengths)
		{
			if (startIndex + i>inputString.length())
			{
				continue;
			}
			else
			{
				subString = inputString.substring(startIndex, i+startIndex);
				if (possibleStrings.contains(subString))
				{
					if (o == null)
						o = new LinkedList<Integer>();
					o.add(i+startIndex);
				}
			}
		}
		if (o==null)
			return null;
		else
			return o.toArray(new Integer[0]);
	}
}
