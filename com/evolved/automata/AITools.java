package com.evolved.automata;
import java.util.regex.*;
import java.util.*;


public class AITools {
	
	
	/**
	 * Uniformly selects a random element of generic type V from a list of values
	 * 
	 * @param itemList
	 */
	public static <V> V ChooseRandom(List<V> itemList)
	{
		if ((itemList==null)||(itemList.size()<1))
			return null;
		else
		{
			int maxIndex=itemList.size();
			int chosenIndex=(int)Math.min(Math.random()*maxIndex, maxIndex-1);
			return itemList.get(chosenIndex);
		}
	}
	
	/**
	 * Uniformly selects a random element of generic type V from an array of values.
	 * 
	 * @param itemArray
	 */
	public static <V> V ChooseRandom(V[] itemArray)
	{
		if ((itemArray==null)||(itemArray.length<1))
			return null;
		else
		{
			int maxIndex=itemArray.length;
			int chosenIndex=(int)Math.min(Math.random()*maxIndex, maxIndex-1);
			return itemArray[chosenIndex];
		}
	}
	/**
	 * This function implements a slightly altered version of simulated sampling from
	 * a Bernoulli urn filled with colored balls.  With this function, colors with
	 * more balls are weighted slightly more than it would from a completely fair
	 * uniform sampling.  This function only works properly  when the weights are non-
	 * negative. TODO: Fix this so that the maximum and minimum weights are arbitrary
	 * and introduction an error and comparison scale as is done with
	 * ChooseWeightedRandomPartition
	 * 
	 * @param itemList
	 * @param favor_high    favor_high
	 */
	public static <V> WeightedValue<V> ChooseWeightedRandom(List<WeightedValue<V>> itemList, boolean favor_high)
	{
		if ((itemList==null)||(itemList.size()<1))
			return null;
		else
		{
			
			double maxValue=0;
			double weightFactor=(favor_high)?1:-1;
			int randomPrec=10000;
			int cutoff;
			for (WeightedValue<V> wValue: itemList)
			{
				if (wValue.GetWeight()>=maxValue)
					maxValue=wValue.GetWeight();
			}
			cutoff =  (int)(maxValue*Math.random()*randomPrec);
			List<WeightedValue<V>> choiceList = new LinkedList<WeightedValue<V>>();
			for (WeightedValue<V> potential:itemList)
			{
				if (weightFactor*potential.GetWeight()*randomPrec>=weightFactor*cutoff)
					choiceList.add(potential);
			}
			return ChooseRandom(choiceList);
		}
	}
	
	
	/**
	 * This function simulates sampling from a Bernoulli urn filled with colored balls.
	 * Each element of 'itemList' represents a 'color' and the number of balls is
	 * modeled as a double precision float.  The actual thing that is selected is the
	 * generic parameter V.  This only works with non-negative double precision
	 * weights. If sum of all weights are less than minTotal then returns null.
	 * TODO: Introduce an error and comparison scale as is done with
	 * ChooseWeightedRandomPartition
	 * 
	 * @param itemList    itemList
	 */
	public static <V> WeightedValue<V> ChooseWeightedRandomFair(List<WeightedValue<V>> itemList)
	{
		if ((itemList==null)||(itemList.size()<1))
			return null;
		else
		{
			
			double cutoff;
			double totalWeight=0;
			double minTotal = 0.000001D;
			for (WeightedValue<V> wValue: itemList)
			{
				totalWeight+=wValue.GetWeight();
			}
			if (totalWeight<minTotal)
				return null;
			cutoff =  totalWeight*Math.random();
			double pastRange=0,weight;
			
			WeightedValue<V> chosen=null;
			
			for (WeightedValue<V> wValue:itemList)
			{
				
				weight=wValue.GetWeight();
				
				if ((weight+pastRange)>=cutoff)
				{
					chosen= wValue;
					break;
				}
				pastRange+=weight;
			}
			return chosen;
			
		}
	}
	
	/**
	 * This function implements a slightly altered version of simulated sampling from
	 * a Bernoulli urn filled with colored balls.  With this function, colors with
	 * more balls are weighted slightly more than it would from a completely fair
	 * uniform sampling.  This function only works properly  when the weights are non-
	 * negative. TODO: Fix this so that the maximum and minimum weights are arbitrary
	 * and introduction an error and comparison scale as is done with
	 * ChooseWeightedRandomPartition
	 * 
	 * @param itemList
	 * @param favor_high    favor_high
	 */
	public static  String ChooseWeightedRandomString(Hashtable<String, Integer> stringDistribution, boolean favor_high)
	{
		if ((stringDistribution==null)||(stringDistribution.size()<1))
			return null;
		else
		{
			
			double cutoff;
			double totalWeight=0;
			Integer value;
			for (String wValue: stringDistribution.keySet())
			{
				value = stringDistribution.get(wValue);
				totalWeight+=value.intValue();
			}
			cutoff =  totalWeight*Math.random();
			double pastRange=0,weight;
			
			for (String wValue:stringDistribution.keySet())
			{
				
				value = stringDistribution.get(wValue);
				
				if ((value.intValue()+pastRange)>=cutoff)
				{
					return wValue;
				}
				pastRange+=value.intValue();
			}
			return null;
			
		}
	}
	
	public static void shiftBackValues(Map<String,String> map, String[] baseKeyNames, String[] newBaseValues,int shiftDepth)
	{
		if (newBaseValues!=null)
		{
			for (int i=0;i<baseKeyNames.length;i++)
				shiftBackValue(map,baseKeyNames[i],newBaseValues[i],shiftDepth);
		}
		else
		{
			for (int i=0;i<baseKeyNames.length;i++)
				shiftBackValue(map,baseKeyNames[i], null,shiftDepth);
		}
		
		
	}
	
	public static void shiftBackValue(Map<String,String> map, String baseKeyName, String newBaseValue,int shiftDepth)
	{
		String moreRecentValue,moreRecentKey, currentKey;
		String separator=".";
		String keyPattern = "%1$s%2$s%3$s";
		for (int i=shiftDepth;i>=1;i--)
		{
			currentKey=String.format(keyPattern, baseKeyName,separator,i);
			if (i>1)
			{
				moreRecentKey=String.format(keyPattern, baseKeyName,separator,i-1);
			}
			else
				moreRecentKey=baseKeyName;
			if (map.containsKey(moreRecentKey))
				map.put(currentKey, map.get(moreRecentKey));
			else
				map.remove(currentKey);
		}
		if (newBaseValue!=null)
			map.put(baseKeyName, newBaseValue);
		else
			map.remove(baseKeyName);
	}

}
