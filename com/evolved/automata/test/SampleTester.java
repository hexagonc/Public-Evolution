package com.evolved.automata.test;
import com.evolved.automata.*;

import java.util.*;

public class SampleTester {
	
	/**
	 * Sample program for testing sample process for weighted Strings. The
	 * larger totalIterations gets, the closer the proportion of
	 * samples per value should approach the proportion of the value over
	 * the sum of all values. This proves that each value is sampled uniformly.
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		LinkedList<WeightedValue<String>> weightedStrings= null;
		int totalIterations=2000;
		try
		{
			
			weightedStrings = new LinkedList<WeightedValue<String>>();
			weightedStrings.add(new WeightedValue("left", 25));
			weightedStrings.add(new WeightedValue("right", 25));
			weightedStrings.add(new WeightedValue("forward", 25));
			weightedStrings.add(new WeightedValue("backward", 25));
			LongitudinalRandomTest(weightedStrings, totalIterations);
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This is a test function used to prove that ChooseWeightedRandomFair is truly a
	 * uniform, unbiased sample from a set of weighted options
	 * 
	 * @param test
	 * @param totalInterations
	 */
	public static <V> void LongitudinalRandomTest(LinkedList<WeightedValue<V>> test, int totalInterations)
	{
		StringBuilder report = new StringBuilder();
		WeightedValue<V> chosenValue;
		Integer count=null;
		Hashtable<V,Integer> map = new Hashtable<V,Integer>();
		int totalCount=0;
		for (int i=0;i<totalInterations;i++)
		{
			chosenValue=AITools.ChooseWeightedRandomFair(test);
			if (chosenValue!=null&&map.containsKey(chosenValue.GetValue()))
			{
				count=map.get(chosenValue.GetValue());
				map.put(chosenValue.GetValue(), new Integer(count.intValue()+1));
			}
			else
			{
				if (chosenValue!=null)
				{
					map.put(chosenValue.GetValue(), new Integer(1));
				}
				
			}
			
			totalCount++;
		}
		double mappedTotal=0;
		for (WeightedValue<V> result:test)
		{
			if (map.containsKey(result.GetValue()))
			{
				
				mappedTotal=map.get(result.GetValue()).intValue();
			}
			else
				mappedTotal=0;
			report.append("The follow results apply:\n");
			report.append(String.format("Value %1s with weight %2s occurred a total of %3s times which is %4s percent\n", result.GetValue(),result.GetWeight(),mappedTotal,(int)(100.0*mappedTotal/totalCount)));
		}
		System.out.println(report);
		
	}
}
