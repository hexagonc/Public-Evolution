package com.evolved.automata.filetools;
import java.io.*;
import java.util.*;
public class StandardTools {

	public static String newLine()
	{
		return System.getProperty("line.separator");
	}
	
	public static BufferedReader  getReaderFromPackageResource(String resource)
	{
		InputStream istream = StandardTools.class.getResourceAsStream(resource);
		if (istream==null)
			return null;
		InputStreamReader reader = new InputStreamReader(istream);
		return new BufferedReader(reader);
	}
	
	public static BufferedReader[]  getReaderFromPackageResource(String[] resources)
	{
		BufferedReader[] array = new BufferedReader[resources.length];
		for (int i=0;i<resources.length;i++)
			array[i] = getReaderFromPackageResource(resources[i]);
		return array;
	}
	
	public static void writeToFile(String logFileFullName, String text)
	{
		writeToFile(logFileFullName,text,false);
	}

	
	public static void writeToFile(String logFileFullName, String text,boolean throwException)
	{
		BufferedWriter writer=null;
		try
		{
			writer= new BufferedWriter(new FileWriter(logFileFullName,true));
			writer.write(text);
			writer.newLine();
		}
		catch (Exception e)
		{
			if (throwException)
			{
				throw new RuntimeException(getDetailedExceptionText(e));
			}
		}
		finally
		{
			if (writer!=null)
			{
				try
				{
					writer.close();
				}
				catch (Exception e)
				{
					
				}
			}
		}
	}
	
	
	
	public static String getDetailedExceptionText(Exception e)
	{
		java.io.StringWriter traceText = new java.io.StringWriter();
		java.io.PrintWriter pWriter = new java.io.PrintWriter(traceText,true);
		e.printStackTrace(pWriter);
		pWriter.close();
		return traceText.toString();
	}
    
	
	public static void WriteTraceLog(String tracefilefullname,String message)
	{
		GregorianCalendar now = new GregorianCalendar();
		String logMessage=String.format(
				"[%1$s/%2$s/%3$s]%4$s:%5$s:%6$s  %7$s\r\n",
				now.get(Calendar.MONTH)+1,
				now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.YEAR),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND),
				message);
		AppendAllText(tracefilefullname,logMessage);
	}
	public static void AppendAllText(String inputFileFullName, String text) throws RuntimeException
	{
		//input.spl
		BufferedWriter bWriter=null;
		
		try
		{
			bWriter = new BufferedWriter(new FileWriter(inputFileFullName,true));
			bWriter.write(text);
			
		}
		catch (Exception e)
		{
			
		}
		finally
		{
			if (bWriter!=null)
				try {
					bWriter.close();
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
		}
		
	}
	
	public static Boolean FileExists(String fileFullName)
	{
		File f = new File(fileFullName);
		return f.exists();
	}
	
	public static void DeleteFile(String fileFullName) throws RuntimeException
	{
		File f = new File(fileFullName);
		try
		{
			f.delete();
		}
		catch (Exception e)
		{
			java.io.StringWriter traceText = new StringWriter();
			java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
			e.printStackTrace(pWriter);
			pWriter.close();
			throw new RuntimeException(traceText.toString());
		}
	}
	
	public static void ImportCSVFile(String csvFileFullName, CSVRowImporter importer, boolean includeCellPositionP)
	{
		String inputFileFullName=csvFileFullName;
		BufferedReader bfReader=null;
		String lineInput="";
		String[] lineSplit;
		int i=0;
		try
		{
			bfReader = new BufferedReader(new FileReader(inputFileFullName));
			while ((lineInput=bfReader.readLine())!=null)
			{
				lineSplit=lineInput.split(",");
				
				for (int j=0;j<lineSplit.length;j++)
				{
					if (includeCellPositionP)
						importer.ImportCellData(lineSplit[j].trim(),i,j);
					else
						importer.ImportCellData(lineSplit[j].trim());
				}
				i++;
			}
		}
		catch (Exception e)
		{
			java.io.StringWriter traceText = new StringWriter();
			java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
			e.printStackTrace(pWriter);
			pWriter.close();
			throw new RuntimeException(traceText.toString());
		}
	}
	
	public static void ProcessFile(String inputFileFullName, LineInputProcessor lineProcessor) 
	{
		
		BufferedReader bfReader=null;
		String lineInput="";
		
		try
		{
			bfReader = new BufferedReader(new FileReader(inputFileFullName));
			while ((lineInput=bfReader.readLine())!=null)
			{
				lineProcessor.ProcessLine(lineInput);
			}
		}
		catch (Exception e)
		{
			java.io.StringWriter traceText = new StringWriter();
			java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
			e.printStackTrace(pWriter);
			pWriter.close();
			throw new RuntimeException(traceText.toString());
		}
	}
	
	/**
	 * Reads a file 
	 * @param inputFileFulName
	 * @return returns an array of strings for each line in the file
	 * @throws IOException
	 */
	public static String[] getDataFileLines(String inputFileFulName) throws IOException
	{
		LinkedList<String> outLine = new LinkedList<String>();
		String lineInput="";
		File f = new File(inputFileFulName);
		BufferedReader reader=null;
		try
		{
			if (f.exists())
			{
				reader = new BufferedReader(new FileReader(f));
				while ((lineInput=reader.readLine())!=null)
				{
					if (lineInput.trim().length()>0)
						outLine.add(lineInput.trim());
				}
				return outLine.toArray(new String[0]);
			}
			else
				return null;
		}
		finally
		{
			if (reader!=null)
			{
				
				try {
					reader.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	/**
	 * Reads a file 
	 * @param inputFileFulName
	 * @return returns an array of strings for each line in the file
	 * @throws IOException
	 */
	public static String[] getDataFileLines(BufferedReader reader) throws IOException
	{
		LinkedList<String> outLine = new LinkedList<String>();
		String lineInput;
		try
		{
			if (reader!=null)
			{
				
				while ((lineInput=reader.readLine())!=null)
				{
					if (lineInput.trim().length()>0)
						outLine.add(lineInput.trim());
				}
				return outLine.toArray(new String[0]);
			}
			else
				return null;
		}
		finally
		{
			if (reader!=null)
			{
				
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Reads a text file into a single String line
	 * @param inputFileFulName
	 * @return File contents as a single string
	 * @throws IOException
	 */
	public static String getDataFileLine(String inputFileFulName) throws IOException
	{
		
		String lineInput="";
		StringBuffer sBuffer = new StringBuffer();
		File f = new File(inputFileFulName);
		BufferedReader reader=null;
		try
		{
			if (f.exists())
			{
				reader = new BufferedReader(new FileReader(f));
				while ((lineInput=reader.readLine())!=null)
				{
					sBuffer.append(lineInput);
				}
				return sBuffer.toString();
			}
			else
				return null;
		}
		finally
		{
			if (reader!=null)
			{
				
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Reads a text package resource into a single String line
	 * @param fully qualified package resource name
	 * @return File contents as a single string or null if there was an error
	 */
	public static String getPackageDataFileLine(String resourceName)
	{
		
		String lineInput="";
		StringBuffer sBuffer = new StringBuffer();
		
		BufferedReader reader=null;
		try
		{
			reader = getReaderFromPackageResource(resourceName);
			if (reader!=null)
			{
				
				while ((lineInput=reader.readLine())!=null)
				{
					sBuffer.append(lineInput);
				}
				return sBuffer.toString();
			}
			else
				return null;
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			if (reader!=null)
			{
				
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Reads a file 
	 * @param inputFileFulName
	 * @return returns an array of strings for each line in the file
	 * @throws IOException
	 */
	public static String[] getPackageDataFileLines(String resourceName)
	{
		LinkedList<String> outLine = new LinkedList<String>();
		String lineInput="";
		
		BufferedReader reader=null;
		try
		{
			reader = getReaderFromPackageResource(resourceName);
			if (reader!=null)
			{
				
				while ((lineInput=reader.readLine())!=null)
				{
					if (lineInput.trim().length()>0)
						outLine.add(lineInput.trim());
				}
				return outLine.toArray(new String[0]);
			}
			else
				return null;
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			if (reader!=null)
			{
				
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
