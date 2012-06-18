package com.evolved.automata.filetools;
import java.io.*;

public class SimpleLogger {

	String logFilefullname;
	boolean first=true;
	public SimpleLogger(String logFilefullname)
	{
		this.logFilefullname=logFilefullname;
	}
	
	public static String delimitIfNeeded(String data)
	{
		int comma = data.indexOf(',');
		if (comma>=0)
			return "\"" + data.replace("\"", "\"\"") + "\"";
		else
			return data;
	}
	
	public void logMessage(String message)
	{
		FileWriter fWriter = null;
		try
		{
			fWriter = new FileWriter(logFilefullname,!first);
			first=false;
			fWriter.write(message);
			fWriter.write(System.getProperty("line.separator"));
		}
		catch (Exception e)
		{
			
		}
		finally
		{
			if (fWriter!=null)
			{
				try
				{
					fWriter.close();
				}
				catch (Exception e2)
				{
					
				}
			}
		}
	}
}
