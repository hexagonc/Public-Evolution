package com.sample.simple.paint.util;

import java.util.ArrayList;
import java.util.LinkedList;


public class KDTree <T extends GeneralizedPoint> {
	
	KDNode topNode;
	
	private class KDNode
	{
		LinkedList<InnerValue> valueList;
		
		double[] coordinateKey;
		int dIndex;
		int partitionIndex;
		KDNode left;
		KDNode right;
		
		KDNode parent;
		
		public KDNode(int index, T value, KDNode parent)
		{
			valueList = new LinkedList<InnerValue>();
			valueList.add(new InnerValue(value));
			dIndex = index;
			coordinateKey = value.getCoordinates();
			partitionIndex = dIndex % coordinateKey.length;
			
		}
		
		private boolean equalKeys(double[] coord)
		{
			for (int i = 0;i<coord.length;i++)
			{
				if (coord[i] != coordinateKey[i])
					return false;
			}
			return true;
		}
		
		public void addValue(T value)
		{
			double[] coord = value.getCoordinates();
			if (equalKeys(coord))
			{
				if (!valueList.contains(value))
					valueList.add(new InnerValue(value));
				return;
			}
			
			if (coord[partitionIndex] <= coordinateKey[partitionIndex])
			{
				if (left != null)
					left.addValue(value);
				else
					left = new KDNode(dIndex + 1, value, this);
			}
			else
			{
				
				if (right != null)
					right.addValue( value);
				else
					right = new KDNode(dIndex + 1, value, this);
			}
		}
		
		public ArrayList<T> getRange( double[] maxCoordValues, double[] minCoordValues)
		{
			ArrayList<T> prior = new ArrayList<T>();
			
			if (inRange(maxCoordValues, minCoordValues))
			{
				for (InnerValue v:valueList)
				{
					if (!v.deleted)
						prior.add(v.rawValue);
				}
			}
			
			if (minCoordValues[partitionIndex] > coordinateKey[partitionIndex] && right != null)
			{
				prior.addAll(right.getRange(maxCoordValues, minCoordValues));
			}
			else if (maxCoordValues[partitionIndex] <= coordinateKey[partitionIndex] && left != null)
			{
				prior.addAll( left.getRange( maxCoordValues, minCoordValues));
			}
			else
			{
				
				if (left != null && minCoordValues[partitionIndex] <= coordinateKey[partitionIndex])
					prior.addAll(left.getRange( maxCoordValues, minCoordValues));
				if (right != null && maxCoordValues[partitionIndex] > coordinateKey[partitionIndex])
					prior.addAll(right.getRange( maxCoordValues, minCoordValues));
				
				
			}
			return prior;
		}
		
		public ArrayList<T>  deleteRange(double[] maxCoordValues, double[] minCoordValues)
		{
			ArrayList<T> out = new ArrayList<T>();
			if (inRange(maxCoordValues, minCoordValues))
			{
				for (InnerValue v:valueList)
				{
					if (!v.deleted)
					{
						v.deleted = true;
						out.add(v.rawValue);
					}
				}
			}
			if (minCoordValues[partitionIndex] > coordinateKey[partitionIndex] && right != null)
			{
				out.addAll(right.deleteRange( maxCoordValues, minCoordValues));
			}
			else if (maxCoordValues[partitionIndex] <= coordinateKey[partitionIndex] && left != null)
			{
				out.addAll(left.deleteRange( maxCoordValues, minCoordValues));
			}
			else
			{
				
				if (right != null && maxCoordValues[partitionIndex] > coordinateKey[partitionIndex])
					out.addAll(right.deleteRange(maxCoordValues, minCoordValues));
				
				if (left != null && minCoordValues[partitionIndex] <= coordinateKey[partitionIndex])
				{
					out.addAll(left.deleteRange( maxCoordValues, minCoordValues));
				}
			}
			return out;
		}
		
		private boolean inRange(double[] maxCoordValues, double[] minCoordValues)
		{
			for (int i = 0;i<maxCoordValues.length;i++)
			{
				if (coordinateKey[i]>maxCoordValues[i] || coordinateKey[i]< minCoordValues[i])
					return false;
			}
			
			return true;
		}
		
		public boolean deletePoint(double[] coord)
		{
			boolean deletedRoot = false;
			if (equalKeys(coord))
			{
				if (parent == null)
				{
					if (left == null && right == null)
						deletedRoot = true;
				}
				else
				{
					if (left == null && right == null)
					{
						if (parent.left == this)
							parent.left = null;
						if (parent.right == this)
							parent.right = null;
					}
				}
				
				for (InnerValue v:valueList)
				{
					v.deleted = true;
				}
			}
			else
			{
				if (coord[partitionIndex] <= coordinateKey[partitionIndex] && left != null)
				{
					left.deletePoint(coord);
				}
				else if (coord[partitionIndex] > coordinateKey[partitionIndex] && right != null)
				{
					right.deletePoint(coord);
				}
			}
			return deletedRoot;
		}
		
		public ArrayList<T> getAllPoints()
		{
			ArrayList<T> out = new ArrayList<T>();
			
			for (InnerValue v:valueList)
			{
				if (!v.deleted)
					out.add(v.rawValue);
			}
			
			if (left != null)
				out.addAll(left.getAllPoints());
			if (right != null)
				out.addAll(right.getAllPoints());
			
			return out;
		}
		
		
	}
	
	public void addValue(T value)
	{
		if (value == null)
			throw new IllegalArgumentException("Cannot add null value to Tree");
		if (topNode != null)
		{
			topNode.addValue(value);
		}
		else
			topNode = new KDNode(0, value, null);
	}
	
	public ArrayList<T> getRange(double[] maxCoordValues, double[] minCoordValues)
	{
		
		if (topNode != null)
		{
			return topNode.getRange(maxCoordValues, minCoordValues);
		}
		return new ArrayList<T>();
	}
	
	public ArrayList<T> deleteRange(double[] maxCoordValues, double[] minCoordValues)
	{
		
		if (topNode != null)
		{
			return topNode.deleteRange(maxCoordValues, minCoordValues);
		}
		else
			return new ArrayList<T>();
	}
	
	public void deletePoint(double[] coord)
	{
		
		if (topNode != null)
		{
			if (topNode.deletePoint(coord))
				topNode = null;
		}
	}
	
	public ArrayList<T> getAllPoints()
	{
		if (topNode != null)
		{
			return topNode.getAllPoints();
		}
		else
			return new ArrayList<T>();
	}
	
	private int randomIndex(int min, int max)
	{
		int delta = (int)((max + 1 - min) * Math.random());
		
		return Math.min(max, min + delta);
	}
	
	public void addAllPoints(ArrayList<T> values)
	{
		int i = 0, randIndex = 0;
		int length = values.size();
		int maxIndex = length - 1;
		
		while (i <= maxIndex)
		{
			if (i == maxIndex)
			{
				if (topNode != null)
					topNode.addValue(values.get(i));
				else
					topNode = new KDNode(0, values.get(i), null);
			}
			else
			{
				randIndex = randomIndex(i, maxIndex);
				if (topNode != null)
					topNode.addValue(values.get(randIndex));
				else
					topNode = new KDNode(0, values.get(randIndex), null);
				
				if (randIndex != i)
				{
					values.set(randIndex, values.get(i));
				}
			}
			i++;
		}
		
	}
	
	public void cleanup()
	{
		if (topNode != null)
		{
			ArrayList<T> values = getAllPoints();
			topNode = null;
			
			addAllPoints(values);
		}
	}
	
	public void deleteAllPoints()
	{
		topNode = null;
		
	}
	
	private class InnerValue
	{
		T rawValue;
		boolean deleted = false;
		
		public InnerValue(T v)
		{
			rawValue = v;
		}
		
		@Override
		public boolean equals(Object v)
		{
			if (v instanceof KDTree.InnerValue)
			{
				InnerValue other = (KDTree<T>.InnerValue)v;
				return (deleted == other.deleted) && rawValue.equals(other.rawValue);
			}
			else
				return false;
		}
	}
	
}
