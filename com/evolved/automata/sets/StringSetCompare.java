package com.evolved.automata.sets;
import java.util.*;
public class StringSetCompare 
{
	ArrayList<String> added;
	ArrayList<String> removed;
	
	public class Node
	{
	  
	    String key;
	    int parent;
	    Node leftChild;
	    Node rightChild;
	    
	    public Node()
	    {
	    	
	    }
	    
	    public Node(String k, int id)
	    {
	    	key=k;
	    	parent = id;
	    }
	    
	    
	    public void add(String newKey, int parentId)
	    {
	    	  if (key == null)
	    	  {
	    	  	  key = newKey;
	    	  	  parent = parentId;
	    	  }
	    	  else
	    	      if (key.equals(newKey))
	    	      {
	    	      	  if (parentId!=parent)
	    	      		  parent = 0;
	    	      }
	    	      else if (key.compareTo(newKey)<0)
	    	      {
	    	      	  if (rightChild == null)
	    	      		  rightChild = new Node(newKey, parentId);
	    	      	  else
	    	      		  rightChild.add(newKey, parentId);
	    	      	  
	    	      }
	    	      else if (key.compareTo(newKey)>0)
	    	      {
	    	      	  if (leftChild == null)
	    	      		  leftChild = new Node(newKey, parentId);
	    	      	  else
	    	      		  leftChild.add(newKey, parentId);
	    	      }
	    	         
	    }
	    
	    public void buildLists()
	    {
	    	  if (leftChild!=null)
	    		  leftChild.buildLists();
	    	  
	    	    
	    	  if (parent == -1 && key!=null)
	    		  removed.add(key);
	    	  else if (parent == 1 && key!=null)
	    	  {
	    	  	  added.add(key);
	    	  }
	    	  
	    	  if (rightChild!=null)
	    	  	 rightChild.buildLists();
	    }
	}
	
	public StringSetCompare()
	{
		added = new ArrayList<String>();
		removed = new ArrayList<String>();
	}
	
	public void setDifferences(ArrayList<String> base, ArrayList<String> comp)
	{
		added = new ArrayList<String>();
		removed = new ArrayList<String>();
		
		Node treeRoot = new Node();
		for (String b:base)
		{
			treeRoot.add(b, -1);
		}
		
		for (String c:comp)
		{
			treeRoot.add(c, 1);
		}
		
		treeRoot.buildLists();
	}

	public void setDifferencesPresorted(ArrayList<String> base, ArrayList<String> comp)
	{
		added = new ArrayList<String>();
		removed = new ArrayList<String>();
		int i=0, j=0, maxi=base.size(), maxj= comp.size();

		while (i!=maxi || j!=maxj)
		{
		    if (i!=maxi && j!=maxj && comp.get(j).equals(base.get(i)))
		    {
		        i = Math.min(i+1, maxi);
		        j = Math.min(j+1, maxj);
		    }
		    else if (j==maxj || base.get(i).compareTo(comp.get(j))<0)
	        {
	            removed.add(base.get(i));
	            i = Math.min(i+1, maxi);
	        }
	        else if (i == maxi || comp.get(j).compareTo(base.get(i))<0)
	        {
	            added.add(comp.get(j));
	            j = Math.min(j+1, maxj);
	        }
		    
		}
	}
	
	public void setDifferencesFastAlgorithm(ArrayList<String> base, ArrayList<String> comp)
	{
		added = new ArrayList<String>();
		removed = new ArrayList<String>();
		String[] array_baseStrings = base.toArray(new String[0]);
		String[] array_compStrings = comp.toArray(new String[0]);
		
		Comparator<String> comparator = 
		new Comparator<String>(){
			public int compare(String s1, String s2)
			{
				return s1.compareTo(s2);
			}
			
			public boolean equals(Object o)
			{
				return true;
			}
		};
		
		Arrays.sort(array_baseStrings, comparator);
		Arrays.sort(array_compStrings, comparator);
		
		base.clear();
		comp.clear();
		for (String sb:array_baseStrings)
			base.add(sb);
				
		for (String sc:array_compStrings)
			comp.add(sc);
				
		int i=0, j=0, maxi=base.size(), maxj= comp.size();

		while (i!=maxi || j!=maxj)
		{
		    if (i!=maxi && j!=maxj && comp.get(j).equals(base.get(i)))
		    {
		        i = Math.min(i+1, maxi);
		        j = Math.min(j+1, maxj);
		    }
		    else if (j==maxj || base.get(i).compareTo(comp.get(j))<0)
	        {
	            removed.add(base.get(i));
	            i = Math.min(i+1, maxi);
	        }
	        else if (i == maxi || comp.get(j).compareTo(base.get(i))<0)
	        {
	            added.add(comp.get(j));
	            j = Math.min(j+1, maxj);
	        }
		    
		}
	}
	
	
	public String[] getAdded()
	{
		return added.toArray(new String[0]);
	}
	
	public String[] getRemoved()
	{
		return removed.toArray(new String[0]);
	}
}
