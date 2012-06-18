package com.evolved.automata.alisp;
import java.util.LinkedList;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.security.cert.*;
import javax.net.ssl.*;



public class StaticFunctionEvaluator extends GenericEvaluator {

	public StaticFunctionEvaluator()
	{
		
	}
	
	public StaticFunctionEvaluator(Environment ev) {
		super(ev);
		
	}

	/**
	 * This is a multi-purpose Evaluator Class for combining multiple functions into one<br/>
	 * The first argument is the name of the function to call.
	 * The second argument is the 
	 */
	@Override
	public Argument processArgs(Argument[] args) {
		if (invalidArgs(args, 1, true))
			return null;
		String functionName = Environment.getStringFromArg(args[0]);
		
		try
		{
			
			Argument[] rem = getRemaining(args, 1);
			if (functionName.equals("abs"))
				return abs(rem);
			else if (functionName.equals("random"))
				return random(rem);
			else if (functionName.equals("time"))
				return currentTime(rem);
			else if (functionName.equals("cos"))
				return cosine(rem);
			else if (functionName.equals("string"))
				return castString(rem);
			else if (functionName.equals("int"))
				return castInteger(rem);
			else if (functionName.equals("double"))
				return castDouble(rem);
			else if (functionName.equals("+"))
				return addition(rem);
			else if (functionName.equals("*"))
				return multiplication(rem);
			else if (functionName.equals("/"))
				return division(rem);
			else if (functionName.equals("-"))
				return minus(rem);
			else if (functionName.equals("<="))
				return lessthanequal(rem);
			else if (functionName.equals(">="))
				return greaterthanequal(rem);
			else if (functionName.equals("<"))
				return lessthan(rem);
			else if (functionName.equals(">"))
				return greaterthan(rem);
			else if (functionName.equals("list"))
				return list(rem);
			else if (functionName.equals("equals"))
				return simpleEquals(rem);
			else if (functionName.equals("format"))
				return format(rem);
			else if (functionName.equals("not"))
				return not(rem);
			else if (functionName.equals("len"))
				return len(rem);
			else if (functionName.equals("first"))
				return first(rem);
			else if (functionName.equals("second"))
				return second(rem);
			else if (functionName.equals("nth"))
				return nth(rem);
			else if (functionName.equals("last"))
				return last(rem);
			else if (functionName.equals("pow"))
				return pow(rem);
			else if (functionName.equals("id"))
				return createId(rem);
			else if (functionName.equals("split"))
				return splitString(rem);
			else if (functionName.equals("resolve"))
				return resolveId(rem);
			else if (functionName.equals("mod"))
				return mod(rem);
			else if (functionName.equals("make-array"))
					return make_array(rem);
			else if (functionName.equals("starts-with"))
				return starts_with(rem);
			else if (functionName.equals("ends-with"))
				return ends_with(rem);
			else if (functionName.equals("read-url"))
				return read_url(rem);
			else if (functionName.equals("read-surl"))
				return read_url_secure(rem);
			else if (functionName.equals("#'"))
				return get_function(rem);
			
		}
		catch (Exception e)
		{
			throw new RuntimeException(functionName + " " + e.getMessage());
		}
		throw new RuntimeException(functionName + " is not a valid static function");
	}
	
	public Argument get_function(Argument[] args)
	{
		if (invalidArgs(args, 1, true))
			return null;
		
		try
		{
			String fname = (String)args[0].oValue;
			CompiledEvaluator com = env.getFunction(fname);
			return new Argument(com.clone());
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * 
	 * Reads the contents of a raw http url as a string.  Input parameters, if present will be url-encoded if request method
	 * is GET.
	 * @param  args[0] should be the base url string, minus any keyword parameters
	 * @param  arg[1] represents the request method and can be either "GET" or "POST", ignoring case. <br/>
	 * default value is "GET"
	   @param args[2] is a string key-value list or null.  The values will be automatically url-encoded
	 * @param  arg[3] is the raw data to send as a string
	 * @param  arg[4] is a string key-value list that needs to be populated if args[3] is non-null.<br/>
	 * You should specify the content-type of args[3] and any other parameters.  You do not need to <br/>
	 * specify the content-length
	 
	 * 
	 * @return returns a list.  The first argument is the server return code.  The second value is <br/>
	 * the data returned by the server, if any
	 * @throws MalformedURLException 
	 */
	public Argument read_url(Argument[] args) throws MalformedURLException, IOException
	{
		if (invalidArgs(args, 2, true))
			return null;
		
		StringBuilder kvPairs=new StringBuilder();
		boolean keysPresent = args.length>2 && !Environment.isNull(args[2]), first=true, uploadP=false,  posting=false;
		String requestMethod = (String)args[1].oValue;
		String key, value;
		if (keysPresent)
		{
			kvPairs = new StringBuilder();
			
			for (Argument pair:args[2].innerList)
			{
				key = (String)pair.innerList[0].oValue;
				value = (String)pair.innerList[1].oValue;
				if (!first)
					kvPairs.append("&");
				first=false;
				kvPairs.append(key);
				kvPairs.append("=");
				kvPairs.append(URLEncoder.encode(value, "UTF-8"));
				
			}
			
		}
		String url = null;
		
		URL u = null;
		HttpURLConnection httpConn = null;
		
		try
		{
			if ("GET".equals(requestMethod.toUpperCase()))
			{
				url = (String)args[0].oValue + kvPairs.toString();
				u = new URL(url);
				httpConn = (HttpURLConnection)u.openConnection();
				httpConn.setRequestMethod("GET");
			}
			else
			{
				url = (String)args[0].oValue;
				u = new URL(url);
				httpConn = (HttpURLConnection)u.openConnection();
				if (posting = "POST".equals(requestMethod.toUpperCase()))
				{
					httpConn.setRequestMethod("POST");
					httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				}
				else
					httpConn.setRequestMethod(requestMethod.toUpperCase());
			}
			
			uploadP = args.length>3 && !Environment.isNull(args[3]);
			String data = null;
			if (uploadP || posting)
			{
				
				httpConn.setDoOutput(true);
				if (posting)
				{
					
					data = kvPairs.toString();
				}
				else
				{
					data = (String)args[3].oValue;
					Argument[] requestProperties = args[4].innerList;
					for (Argument pair:requestProperties)
					{
						key= (String)pair.innerList[0].oValue;
						value= (String)pair.innerList[1].oValue;
						httpConn.setRequestProperty(key, value);
					}
				}
				httpConn.setRequestProperty("Content-Length", ""+data.length());
				
			}
			
			int responseCode;
			httpConn.connect();
			responseCode = httpConn.getResponseCode();
			InputStream istream = null;
			OutputStream ostream = null;
			StringBuilder responseBuilder = new StringBuilder();
			if (responseCode==200)
			{
				if (uploadP)
				{
					byte[] buffer = data.getBytes(Charset.forName("UTF-8"));
					ostream = httpConn.getOutputStream();
					ostream.write(buffer);
					ostream.close();
				}
				istream = httpConn.getInputStream();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(istream));
				String lineInput;
				
				while ((lineInput = bReader.readLine())!=null)
				{
					responseBuilder.append(lineInput);
				}
				bReader.close();
				
			}
			Object[] output = new Object[2];
			output[0] = new Integer(responseCode);
			output[1] = responseBuilder.toString();
			return Environment.makeCons(output);
		}
		finally
		{
			if (httpConn!=null)
			{
				httpConn.disconnect();
				
			}
		}
		
	}
	
	
	/**
	 * 
	 * Reads the contents of a raw http url as a string.  Input parameters, if present will be url-encoded if request method
	 * is GET.
	 * @param  args[0] should be the base url string, minus any keyword parameters
	 * @param  arg[1] represents the request method and can be either "GET" or "POST", ignoring case. <br/>
	 * default value is "GET"
	   @param args[2] is a string key-value list or null.  The values will be automatically url-encoded
	 * @param  arg[3] is the raw data to send as a string
	 * @param  arg[4] is a string key-value list that needs to be populated if args[3] is non-null.<br/>
	 * You should specify the content-type of args[3] and any other parameters.  You do not need to <br/>
	 * specify the content-length
	 
	 * 
	 * @return returns a list.  The first argument is the server return code.  The second value is <br/>
	 * the data returned by the server, if any
	 * @throws MalformedURLException 
	 */
	public Argument read_url_secure(Argument[] args) throws MalformedURLException, IOException
	{
		if (invalidArgs(args, 2, true))
			return null;
		
		StringBuilder kvPairs=new StringBuilder();
		boolean keysPresent = args.length>2 && !Environment.isNull(args[2]), first=true, uploadP=false,  posting=false;
		String requestMethod = (String)args[1].oValue;
		String key, value;
		if (keysPresent)
		{
			kvPairs = new StringBuilder();
			
			for (Argument pair:args[2].innerList)
			{
				key = (String)pair.innerList[0].oValue;
				value = (String)pair.innerList[1].oValue;
				if (!first)
					kvPairs.append("&");
				first=false;
				kvPairs.append(key);
				kvPairs.append("=");
				kvPairs.append(URLEncoder.encode(value, "UTF-8"));
				
			}
			
		}
		String url = null;
		
		URL u = null;
		HttpsURLConnection httpConn = null;
		
		try
		{
			if ("GET".equals(requestMethod.toUpperCase()))
			{
				url = (String)args[0].oValue + kvPairs.toString();
				u = new URL(url);
				httpConn = (HttpsURLConnection)u.openConnection();
				httpConn.setRequestMethod("GET");
			}
			else
			{
				url = (String)args[0].oValue;
				u = new URL(url);
				httpConn = (HttpsURLConnection)u.openConnection();
				if (posting = "POST".equals(requestMethod.toUpperCase()))
				{
					httpConn.setRequestMethod("POST");
					httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				}
				else
					httpConn.setRequestMethod(requestMethod.toUpperCase());
			}
			
			uploadP = args.length>3 && !Environment.isNull(args[3]);
			String data = null;
			if (uploadP || posting)
			{
				
				httpConn.setDoOutput(true);
				if (posting)
				{
					
					data = kvPairs.toString();
				}
				else
				{
					data = (String)args[3].oValue;
					Argument[] requestProperties = args[4].innerList;
					for (Argument pair:requestProperties)
					{
						key= (String)pair.innerList[0].oValue;
						value= (String)pair.innerList[1].oValue;
						httpConn.setRequestProperty(key, value);
					}
				}
				httpConn.setRequestProperty("Content-Length", ""+data.length());
				
			}
			
			int responseCode;
			httpConn.connect();
			responseCode = httpConn.getResponseCode();
			InputStream istream = null;
			OutputStream ostream = null;
			StringBuilder responseBuilder = new StringBuilder();
			if (responseCode==200)
			{
				if (uploadP)
				{
					byte[] buffer = data.getBytes(Charset.forName("UTF-8"));
					ostream = httpConn.getOutputStream();
					ostream.write(buffer);
					ostream.close();
				}
				istream = httpConn.getInputStream();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(istream));
				String lineInput;
				
				while ((lineInput = bReader.readLine())!=null)
				{
					responseBuilder.append(lineInput);
				}
				bReader.close();
				
			}
			Object[] output = new Object[2];
			output[0] = new Integer(responseCode);
			output[1] = responseBuilder.toString();
			return Environment.makeCons(output);
		}
		finally
		{
			if (httpConn!=null)
			{
				httpConn.disconnect();
				
			}
		}
		
	}
	
	public Argument castInteger(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		Number num = (Number)args[0].oValue;
		return Environment.makeAtom(new Integer(num.intValue()));
	}
	
	public Argument ends_with(Argument[] args)
	{
		if (invalidArgs(args, 2))
			return null;
		String baseword = (String)args[0].oValue;
		String endPat = (String)args[1].oValue;
		if (baseword.endsWith(endPat))
			return args[0];
		else
			return null;
	}
	
	public Argument starts_with(Argument[] args)
	{
		if (invalidArgs(args, 2))
			return null;
		String baseword = (String)args[0].oValue;
		String startPat = (String)args[1].oValue;
		if (baseword.startsWith(startPat))
			return args[0];
		else
			return null;
	}
	
	
	public Argument make_array(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		int size = Environment.getIntFromArg(args[0]);
		Argument[] array = new Argument[size];
		for (int i=0;i<size;i++)
			array[i]=new Argument();
		return new Argument(null, null, array);
	}
	
	
	public Argument mod(Argument[] args)
	{
		if (invalidArgs(args, 2))
			return null;
		int[] iargs = getIntegerArgs(args);
		int base = iargs[0];
		int modulus = iargs[1];
		int mod = base % modulus;
		return Environment.makeAtom(new Integer(mod));
	}
	/**
	 * First argument is a string 
	 * Second argument is character to split on
	 * Returns a string list
	 * @param args
	 * @return
	 */
	public Argument splitString(Argument[] args)
	{
		String base = (String)args[0].oValue;
		String split = (String)args[1].oValue;
		
		LinkedList<String> words = new LinkedList<String>();
		StringBuilder word = new StringBuilder();
		char c;
		for (int i=0;i<base.length();i++)
		{
			if ((c=base.charAt(i))==split.charAt(0))
			{
				words.add(word.toString());
				word = new StringBuilder();
			}
			else
				word.append(c);
		}
		
		if (word.length()>0)
			words.add(word.toString());
		return Environment.makeCons(words.toArray(new String[0]));
		
	}
	
	public Argument resolveId(Argument[] args)
	{
		if (args[0].isAtom()
				//&&args[0].isIdentifier()
				)
		{
			
			return env.getVariableValue((String)args[0].oValue);
		}
		else
			return null;
	}
	
	
	
	public Argument createId(Argument[] args)
	{
		if (args[0].isAtom())
		{
			Argument newArg = Environment.makeAtom((String)args[0].oValue);
			newArg.setIdentifier(true);
			return newArg;
		}
		else
			return null;
	}
	
	
	public Argument len(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		
		Argument consArg = args[0];
		
		if (Environment.isNull(consArg))
			return new Argument(null, new Double(0), null);
		
		if (!consArg.isCons())
		{
			if (consArg.oValue instanceof String)
			{
				return Environment.makeAtom(new Integer(((String)consArg.oValue).length()));
			}
			else
				return new Argument(null, new Double(1), null);
		}
		else
		{
			return new Argument(null, new Double(consArg.innerList.length), null);
		}
		
	}
	

	public Argument last(Argument[] args)
	{
		if (args == null)
			return null;
		
		Argument consArg = args[0];
		
		Argument[] iterationListArg = null;
		
		
		if (env.isNull(consArg)||!consArg.isCons())
		{
			iterationListArg = new Argument[1];
			iterationListArg[0] = consArg;
		}
		else
		{
			iterationListArg=consArg.innerList;
		}
		
		return resetReturn(iterationListArg[iterationListArg.length-1]);
	}
	
	public Argument first(Argument[] args)
	{
		if (args == null)
			return null;
		
		Argument consArg = args[0];
		
		Argument[] iterationListArg = null;
		
		
		if (env.isNull(consArg)||!consArg.isCons())
		{
			iterationListArg = new Argument[1];
			iterationListArg[0] = consArg;
		}
		else
		{
			iterationListArg=consArg.innerList;
		}
		
		return resetReturn(iterationListArg[0]);
	}
	
	
	public Argument second(Argument[] args)
	{
		Argument consArg = args[0];
		
		Argument[] iterationListArg = null;
		
		if (Environment.isNull(consArg)||!consArg.isCons())
			return null;
		iterationListArg=consArg.innerList;
		if (iterationListArg.length<2)
			return null;
		return iterationListArg[1];
	}
	
	
	public Argument nth(Argument[] args)
	{
		Argument consArg = args[0];
		Argument index = args[1];
		Number num = (Number)index.oValue;
		Argument[] iterationListArg = null;
		
		if (Environment.isNull(consArg)||!consArg.isCons())
			return null;
		iterationListArg=consArg.innerList;
		if (iterationListArg.length<=num.intValue())
			return null;
		return iterationListArg[num.intValue()];
	}
	
	
	public Argument not(Argument[] args)
	{
		if (args == null)
			return null;
		if (Environment.isNull(args[0]))
			return Environment.makeAtom(Environment.True);
		else
			return null;
		
	}
	
	
	public Argument and(Argument[] args)
	{
		if (args == null)
			return null;
		Argument out = null;
		for (int i=0;i<args.length;i++)
		{
			out = args[i];
			if (Environment.isNull(out))
				return out;
			
		}
		return out;
		
	}
	
	public Argument or(Argument[] args)
	{
		if (args == null)
			return null;
		
		for (int i=0;i<args.length;i++)
		{
			if (!Environment.isNull(args[i]))
				return args[i];
			
		}
		return null;
		
	}
	
	
	public Argument format(Argument[] args)
	{
		String formatString=null;
		
		Argument echo = args[0];
		
		if (Environment.isNull(args[1]))
			return null;
		
		formatString = (String)args[1].oValue;
		
		String[] sargs = new String[args.length-2];
		
		for (int i=2;i<args.length;i++)
		{
			
			if (!Environment.isNull(args[i]))
			{
				sargs[i-2]=args[i].toString();
			}
			else
				sargs[i-2]="null";
		}
		
		String format = String.format(formatString, (Object[])sargs);
		if (!Environment.isNull(echo))
			System.out.print(format);
		return new Argument(null, format, null);
	}
	
	public Argument simpleEquals(Argument[] args)
	{
		Argument lvalue=null;
		Argument rvalue = null;
		if (args == null||args.length !=2)
			return null;
		lvalue = args[0];
		rvalue= args[1];
		return compare(lvalue, rvalue);
	}
	
	private Argument compare(Argument lvalue, Argument rvalue)
	{
		
		if (env.isNull(lvalue) && env.isNull(rvalue))
			return Environment.True;
		
		if (env.isNull(lvalue) || env.isNull(rvalue))
			return null;
		
		if (lvalue.isAtom()&&rvalue.isAtom())
		{
			if (lvalue.oValue.equals(rvalue.oValue))
				return lvalue;
			else
				return null;
		}
		Argument result = null;
		
		if (lvalue.isCons()&&rvalue.isCons())
		{
			if (lvalue.innerList.length!=rvalue.innerList.length)
				return null;
			else
			{
				for (int i=0;i<lvalue.innerList.length;i++)
				{
					result = compare(lvalue.innerList[i], rvalue.innerList[i]);
					if (result == null)
						return null;
				}
				return result;
			}
		}
		
		return null;
		
	}
	
	
	public Argument list(Argument[] args)
	{
		if (args == null)
			return new Argument(null, null, new Argument[0]);
		
		Argument[] output = new Argument[args.length];
		
		Argument out=null;
		for (int i=0;i<output.length;i++)
		{
			out =  args[i];
			output[i]=out;
			
		}
		return new Argument(null, null, output);
		
	}
	
	
	public Argument greaterthan(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double lvalue = out[0];
		double rvalue = out[1];
		
		if (lvalue>rvalue)
			return Environment.makeAtom(lvalue);
		else
			return null;
		
	}
	
	public Argument lessthan(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double lvalue = out[0];
		double rvalue = out[1];
		
		if (lvalue<rvalue)
			return Environment.makeAtom(lvalue);
		else
			return null;
		
	}
	
	public Argument greaterthanequal(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double lvalue = out[0];
		double rvalue = out[1];
		
		if (lvalue>=rvalue)
			return Environment.makeAtom(lvalue);
		else
			return null;
		
	}
	
	public Argument pow(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double base = out[0];
		double exp = out[1];
		double value = Math.pow(base, exp);
		return Environment.makeAtom(new Double(value));
		
	}
	
	
	public Argument lessthanequal(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double lvalue = out[0];
		double rvalue = out[1];
		
		if (lvalue<=rvalue)
			return Environment.makeAtom(lvalue);
		else
			return null;
		
	}
	
	public Argument minus(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double d = out[0];
		for (int i=1;i<out.length;i++)
			d-=out[i];
		return Environment.makeAtom(new Double(d));
	}
	
	public Argument division(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double d = out[0];
		for (int i=1;i<out.length;i++)
			d/=out[i];
		return Environment.makeAtom(new Double(d));
	}
	
	public Argument multiplication(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double d = out[0];
		for (int i=1;i<out.length;i++)
			d*=out[i];
		return Environment.makeAtom(new Double(d));
	}
	
	public Argument addition(Argument[] args)
	{
		double[] out = getDoubleArgs(args);
		double d = out[0];
		for (int i=1;i<out.length;i++)
			d+=out[i];
		return Environment.makeAtom(new Double(d));
	}
	
	
	
	private Argument castString(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		if (args[0].isAtom())
			return env.makeAtom(args[0].oValue.toString());
		else
			return env.makeAtom(args[0].toString());
	}
	
	private Argument castDouble(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		try
		{
			return env.makeAtom(new Double(Double.parseDouble(args[0].oValue.toString())));
		}
		catch (Exception e)
		{
			
		}
		return null;
	}
	
	
	private Argument cosine(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		double inputArg = Environment.getDoubleFromArg(args[0]);
		
		return makeDoubleArgument(Math.cos(inputArg));
	}
	
	public Argument makeDoubleArgument(double input)
	{
		return env.makeAtom(new Double(input));
	}
	private Argument currentTime(Argument[] args)
	{
		double time = (double)System.currentTimeMillis();
		return env.makeAtom(new Double(time));
	}
	
	private Argument abs(Argument[] args)
	{
		if (invalidArgs(args, 1))
			return null;
		double a = env.getDoubleFromArg(args[0]);
		if (a<0)
			return env.makeAtom(new Double(-a));
		else
			return env.makeAtom(new Double(a));
	}
	
	private Argument random(Argument[] args)
	{
		if (invalidArgs(args, 2))
			return null;
		double start = env.getDoubleFromArg(args[0]);
		double end = env.getDoubleFromArg(args[1]);
		if (start==0.0&&end==1.0)
			return env.makeAtom(new Double(Math.random()));
		else
			return env.makeAtom(new Double(Math.random()*(end-start)+start));
	}

	
	
	protected double[] getDoubleArgs(Argument[] baseArgs, Double defaultIfNull)
	{
		if (baseArgs==null)
			return null;
		 
		Argument nArg;
		double[] dArgs = new double[baseArgs.length];
		for  (int i=0;i<baseArgs.length;i++)
		{
			
			nArg = baseArgs[i];
			
			if (!env.isNull(nArg))
			{
				dArgs[i] = ((Number)nArg.oValue).doubleValue();
			}
			else
				if (defaultIfNull==null)
					return null;
				else
					dArgs[i]=defaultIfNull.doubleValue();
			
		}
		
		return dArgs;
	}
	
	protected double[] getDoubleArgs(Argument[] baseArgs) 
	{
		return getDoubleArgs( baseArgs, null);
	}
	
	protected int[] getIntegerArgs(Argument[] baseArgs) 
	{
		return getIntegerArgs( baseArgs, null);
	}
	
	protected int[] getIntegerArgs(Argument[] args, Integer defaultIfNull)
	{
		if (args==null)
			return null;
		 
		Argument nArg;
		int[] iArgs = new int[args.length];
		for  (int i=0;i<args.length;i++)
		{
			
			nArg = args[i];
			
			if (!env.isNull(nArg))
			{
				iArgs[i] = ((Number)nArg.oValue).intValue();
			}
			else
				if (defaultIfNull==null)
					return null;
				else
					iArgs[i]=defaultIfNull.intValue();
			
		}
		
		return iArgs;
	}
	
}
