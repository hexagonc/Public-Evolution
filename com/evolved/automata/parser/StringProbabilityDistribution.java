package com.evolved.automata.parser;
import com.evolved.automata.*;
import java.util.*;

public class StringProbabilityDistribution {
	Hashtable<String, Integer> distrib = null;
	
	public StringProbabilityDistribution()
	{
		distrib = new Hashtable<String, Integer>();
	}
	
	public StringProbabilityDistribution(Hashtable<String, Integer> weightDistrib)
	{
		if (weightDistrib!=null)
			distrib = weightDistrib;
	}
	
	public boolean isInitialized(String name)
	{
		if (distrib.containsKey(name))
		{
			Integer num = distrib.get(name);
			return num.intValue() != 0;
		}
		else
			return false;
	}
	
	public String drawString()
	{
		return AITools.ChooseWeightedRandomString(distrib, true);
	}
	
	public String drawString(String pattern, Integer weightOveride)
	{
		Integer oldWeight=null;
		if (distrib.containsKey(pattern))
			oldWeight=distrib.get(pattern);
		distrib.put(pattern, weightOveride);
		String tsample = AITools.ChooseWeightedRandomString(distrib, true);
		if (oldWeight==null)
			distrib.remove(pattern);
		else
			distrib.put(pattern, oldWeight);
		return tsample;
	}
	
	public void mergeDistribution(Hashtable<String, Integer> external)
	{
		
	}
	
	public String[] getStrings()
	{
		return distrib.keySet().toArray(new String[0]);
	}
	
	public String drawString(String[] temporaryOveride, int newValue)
	{
		LinkedList<WeightedValue<String>> oldWeights = new LinkedList<WeightedValue<String>>();
		
		for (String pattern:temporaryOveride)
		{
			if (distrib.containsKey(pattern))
			{
				oldWeights.add(new WeightedValue(pattern, new Double(distrib.get(pattern).intValue())));
				distrib.put(pattern, new Integer(newValue));
			}
		}
		
		String tsample = AITools.ChooseWeightedRandomString(distrib, true);
		if (oldWeights.size()>0)
		{
			for (WeightedValue<String> old:oldWeights)
			{
				distrib.put(old.GetValue(), new Integer((int)old.GetWeight()));
			}
		}
		
		return tsample;
	}
	
	public void addString(String value, int weightIncrement)
	{
		if (distrib.containsKey(value))
			distrib.put(value, weightIncrement + distrib.get(value));
		else
			distrib.put(value, weightIncrement);
	}
	
	public void reset()
	{
		distrib = new Hashtable<String, Integer>();
	}
	
	public Object clone()
	{
		return new StringProbabilityDistribution((Hashtable<String, Integer>)distrib.clone());
	}
}
