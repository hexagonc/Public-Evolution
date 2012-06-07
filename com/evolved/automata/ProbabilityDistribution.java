package com.evolved.automata;

import java.util.List;
import java.util.*;

public class ProbabilityDistribution<T> {

	private String a_KeyName;
	private List<WeightedValue<T>> a_valueDistribution;
	

	
	public ProbabilityDistribution(String keyName, List<WeightedValue<T>> values)
	{
		a_KeyName=keyName;
		a_valueDistribution =values;
		
	}
	
	public T GenerateValue()
	{
		WeightedValue<T> sample = AITools.ChooseWeightedRandomFair(a_valueDistribution);
		if (sample!=null)
			return sample.GetValue();
		else
			return null;
	}
	
	public String GetKey()
	{
		return a_KeyName;
	}
	
	public List<T> GetAllValues()
	{
		LinkedList<T> values = new LinkedList<T>();
		for (WeightedValue<T> v:a_valueDistribution)
		{
			values.add(v.GetValue());
		}
		return values;
	}
	
}
