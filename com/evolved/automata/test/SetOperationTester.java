package com.evolved.automata.test;
import java.util.LinkedList;


import com.evolved.automata.sets.*;
import java.util.*;

public class SetOperationTester {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ArrayList<String> baseList = new ArrayList<String>();
		ArrayList<String> compList = new ArrayList<String>();
		
		
		String[] bSet = new String[]{"x1", "x2", "x3", "x4", "x5", "x6"};
		String[] cSet = new String[]{"x1", "x4", "x6", "x10", "x12"};
		//String[] bSet = new String[]{};
		//String[] cSet = new String[]{};
		
		StringSetCompare setcomparer = null;
		try
		{
			for (String s:bSet)
				baseList.add(s);
			
			for (String s:cSet)
				compList.add(s);
			setcomparer = new StringSetCompare();
			//setcomparer.setDifferencesFastAlgorithm(baseList, compList);
			setcomparer.setDifferences(baseList, compList);
			boolean first=true;
			System.out.print("Added strings: ");
			for (String s:setcomparer.getAdded())
			{
				if (first)
				{
					first=false;
					System.out.print(s);
				}
				else
					System.out.print(", " + s);
			}
			System.out.println();
			first=true;
			System.out.print("Removed strings: ");
			for (String s:setcomparer.getRemoved())
			{
				if (first)
				{
					first=false;
					System.out.print(s);
				}
				else
					System.out.print(", " + s);
			}
			System.out.println();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
