package com.evolved.automata;

public class WeightedValue<V> {
	
	V j_value;
	double j_weight;
	public WeightedValue(V initialValue, double weight)
	{
		j_value=initialValue;
		j_weight=weight;
	}
	
	public WeightedValue(V initialValue)
	{
		j_value=initialValue;
		j_weight=0;
	}
	
	public V GetValue()
	{
		return j_value;
	}
	
	public double GetWeight()
	{
		return j_weight;
	}
	
	public void IncrementWeight()
	{
		j_weight++;
	}
	
	public void ZeroWeight()
	{
		j_weight=0;
	}

	public void UpdateValue(double delta)
	{
		j_weight+=delta;
	}
	
	public void SetWeight(double delta)
	{
		j_weight=delta;
	}
	
	public Object clone()
	{
		return new WeightedValue<V>(j_value,j_weight);
	}
	
}
