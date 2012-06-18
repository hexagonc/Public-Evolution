package com.evolved.automata.parser;
import java.io.IOException;
import java.util.*;

import com.evolved.automata.*;
import com.evolved.automata.filetools.SimpleLogger;

public abstract class UnitParser 
{
	public static boolean debug=true;
	
	public Hashtable<String, Integer> nonTerminalMatches;
	
	public static final String wildcard = "~";
	public static final String whitespace = "$";
	public static final String numeric = "#";
	public static final String letter = "@";
	public static final String commentChar=";";
	public static final String endString="^";
	
	
	public static final String matchOne="?";
	public static final String kleene="*";
	public static final String onePlus="+";
	public static final String negation = "`";
	
	public String grammarComponent;
	public String inputString;
	public int startIndex;
	public int endIndex;
	
	
	CFGParser.GlobalState global;
	
	public final String groupName = "group";
	
	protected Hashtable<String,UnitParser> compiledComponents;
	protected Hashtable<String,String> namedComponents;
	protected int matchIndex;
	protected int maxSubMatches;
	protected String[] subGrammars;
	protected UnitParser[] subStates;
	protected UnitParser parent;
	protected UnitParser nonDeterministicAncestor;
	protected HashSet<String> captureGroupNames = null;
	protected Hashtable<String, LinkedList<String>> capturedMaps;
	protected String capturedValue=null;
	protected LinkedList<Integer> endIndices;
	protected UnitParser previous = null;
	protected int indexInParent;
	protected HashSet<String> componentsForSubgrammars;
	protected boolean isQuantifier=false;
	protected boolean isAlternation = false;
	public boolean isNonterminal=false;
	boolean priorSuccess=false;
	
	
	public static final int t_TERMINAL=0;
	public static final int t_NONTERMINAL=1;
	public static final int t_GROUP=2;
	public static final int t_CONJUNCTION=3;
	public static final int t_DISJUNCTION=4;
	public static final int t_KLEENE=5;
	public static final int t_WILDCARD=6;
	public static final int t_QUANTIFIER=7;
	
	public int type=0;
	
	
	public boolean finalized=false;
	Hashtable<String, StringDistribution> processedDistributions;
	
	
	public static com.evolved.automata.filetools.SimpleLogger log;
	
	public void setComponentsForSubgrammars(HashSet<String> componentsForSubgrammars)
	{
		this.componentsForSubgrammars = componentsForSubgrammars;
	}
	
	public void logState()
	{
		logState("general");
		
	}
	
	public static int logcount=0;
	public void logState(String message)
	{
		if (log==null)
			return;
		logcount++;
		String statePattern = "%1$s, %2$s,  %3$s,  %4$s, %5$s, %6$s, %7$s, %8$s, %9$s";
		String output = String.format(statePattern,
				SimpleLogger.delimitIfNeeded(message),
				SimpleLogger.delimitIfNeeded(this.toString()), 
				SimpleLogger.delimitIfNeeded((grammarComponent==null?"null":grammarComponent)),							
				startIndex,
				endIndex,
				indexInParent,
				maxSubMatches,
				SimpleLogger.delimitIfNeeded(((endIndex>startIndex)?inputString.substring(startIndex, endIndex):"")),
				SimpleLogger.delimitIfNeeded(inputString)
				);
		log.logMessage(""+ logcount+ ":: "+output);
		
	}
	
	
	public UnitParser()
	{
		processedDistributions = new Hashtable<String, StringDistribution>();
	}
	
	public UnitParser(CFGParser.GlobalState global)
	{
		this.global = global;
		processedDistributions = new Hashtable<String, StringDistribution>();
	}

	public UnitParser getNonDeterministicPrior()
	{
		return nonDeterministicAncestor;
	}
	
	
	public boolean isFinalized()
	{
		return finalized;
	}
	
	public int getEndIndex()
	{
		return endIndex;
	}
	
	public LinkedList<String> getCapturedList(String name)
	{
		
		if ((capturedMaps!=null)&&(capturedMaps.containsKey(name)))
		{
			return capturedMaps.get(name);
		}
		else
			return null;
	}
	
	
	public String getFirstCapturedValue(String name)
	{
		LinkedList<String> v = getCapturedList(name);
		if ((v!=null)&&(v.size()>0))
			return v.getFirst();
		else
			return null;
			
	}
	
	
	public Hashtable<String, LinkedList<String>> getCaptureSet()
	{
		return capturedMaps;
		
	}
	
	public static String getValueFromMatchSpecifier(String specifier)
	{
		int lastIndex = specifier.lastIndexOf(':');
		if (lastIndex==-1)
			return specifier;
		String value = specifier.substring(0, lastIndex);
		return value;
	}
	
	public static String getGrammarFromMatchSpecifier(String specifier)
	{
		int lastIndex = specifier.lastIndexOf(':');
		String grammar = specifier.substring(lastIndex+1);
		return grammar;
	}
	
	public static String[] disjuctionofLiterals(String grammarComponent)
	{
		String[] parts = splitPattern(grammarComponent, '|', true);
		
		if ((parts!=null)&&(parts.length>1))
		{
			for (int i=0;i<parts.length;i++)
			{
				if ((parts[i].charAt(0)=='"')&&(parts[i].charAt(parts[i].length()-1) == '"'))
				{
					parts[i] = parts[i].substring(1,parts[i].length()-1 );
				}
				else
					return null;
			}
			return parts;
		}
		else
		{
			int L = grammarComponent.length();
			if (grammarComponent.startsWith("\"")&&grammarComponent.endsWith("\"")&&grammarComponent.substring(1,L-1).indexOf("\"")==-1)
				return new String[]{grammarComponent.substring(1,grammarComponent.length()-1 )};
			else
				return null;
		}
			
	}
	
	public static String[] disjuctionofLiteralsSimplified(String grammarComponent)
	{
		String[] parts = splitPattern(grammarComponent, '|', true);
		
		if ((parts!=null)&&(parts.length>1))
		{
			for (int i=0;i<parts.length;i++)
			{
				if ((parts[i].charAt(0)=='\'')&&(parts[i].charAt(parts[i].length()-1) == '\''))
				{
					parts[i] = parts[i].substring(1,parts[i].length()-1 );
				}
				else
					return null;
			}
			return parts;
		}
		else
		{
			int L = grammarComponent.length();
			if (grammarComponent.startsWith("'")&&grammarComponent.endsWith("'")&&grammarComponent.substring(1,L-1).indexOf("'")==-1)
				return new String[]{grammarComponent.substring(1,grammarComponent.length()-1 )};
			else
				return null;
		}
			
	}
	
	public static UnitParser parse(CFGParser.GlobalState global,String component, Hashtable<String,String> namedComponents)
	{
		return parse(global, component, namedComponents, true);
	}
	
	public static UnitParser parse(CFGParser.GlobalState global,  String component, Hashtable<String,String> namedComponents, boolean defaultQuantifierGreedyP)
	{
		
		component = component.trim();
		String terminalSymbol = isTerminal(component);
		if (terminalSymbol!=null)
		{
			return new TerminalMatcherState(global, terminalSymbol);
		}
		
		
		String[] parts;
		UnitParser[] subStates;
		StringDistribution distribution; 
		parts = disjuctionofLiterals(component);
		UnitParser ostate;
		if (parts!=null)
		{
			distribution= new StringDistribution();
			for (String s:parts)
			{
				distribution.addString(s);
			}
			ostate = new OptimizedAlternation(global, distribution);
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate.grammarComponent=component;
			}
			return ostate;
		}
		
		parts = isConjunction(component);
		if (parts!=null)
		{
			subStates = new UnitParser[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=UnitParser.parse(global, parts[i], namedComponents,defaultQuantifierGreedyP);
			ostate = new ConjunctionMatcherState(global, subStates);
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate.grammarComponent=component;
			}
			return ostate;
		}
		String mappedGrammar = isLabel(component, namedComponents);
		
		if (mappedGrammar!=null)
		{	
			if (global!=null)
				global.incrementCount(component);
			return new NonTerminalMatcherState(global, component);
		}
		
		parts = isBackReference(component);
		
		if (parts!=null&&namedComponents.containsKey(parts[0]))
		{
			ostate = new NonTerminalMatcherState(global, parts[0], Integer.parseInt(parts[1]));
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component+":" + parts[1]);
				
			}
			return ostate;
		}
		
		mappedGrammar = isGroup(component);
		if (mappedGrammar!=null)
		{
			parts = segmentGroup(mappedGrammar);
			subStates = new UnitParser[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=UnitParser.parse(global, parts[i], namedComponents, defaultQuantifierGreedyP);
			ostate = new ConjunctionMatcherState(global, subStates);;
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate.grammarComponent=component;
			}
			return ostate;
		}
			
		parts = isAlternation(component.trim());
		if (parts!=null)
		{
			subStates = new UnitParser[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=UnitParser.parse(global, parts[i], namedComponents, defaultQuantifierGreedyP);
			ostate = new AlternationMatcherState(global, subStates, parts);
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate.grammarComponent=component;
			}
			
			return ostate;
		}
		QuantifierInfo qInfo;
		qInfo = isQuantifier(component);
		if (qInfo!=null)
		{
			ostate = new QuantifierMatcherState(global, defaultQuantifierGreedyP,qInfo.miniMatches, qInfo.maxMatches, parse(global, qInfo.grammar,namedComponents,defaultQuantifierGreedyP));
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate.grammarComponent=component;
			}
			
			return ostate;
		}
		
		return null;
	}
	
	
	public int setExecutionParameters(CFGParser.GlobalState global, int indexInParent, int start, String inputString, Hashtable<String, LinkedList<String>> capturedComponents, HashSet<String> nonTerminalsToCapture, UnitParser matchParent, UnitParser mismatchParent, Hashtable<String,UnitParser> compiledComponents)
	{
		this.indexInParent=indexInParent;
		this.startIndex = start;
		this.inputString=inputString;
		this.capturedMaps = capturedComponents;
		this.captureGroupNames = nonTerminalsToCapture;
		this.parent = matchParent;
		this.nonDeterministicAncestor=mismatchParent;
		this.endIndex=this.startIndex;
		this.compiledComponents=compiledComponents;
		this.global=global;
		
		return global.shouldSkipComponent(grammarComponent, startIndex);
	}
	
	protected void finalize()
	{
		
		finalized=true;

	}
	
	protected void mergeMaps(Hashtable<String, LinkedList<String>> sourceMap, Hashtable<String, LinkedList<String>> targetMap)
	{
		LinkedList<String> list = null;
		for (String key:sourceMap.keySet())
		{
			list = sourceMap.get(key);
			if (targetMap.containsKey(key))
			{
				targetMap.get(key).addAll(list);
			}
			else
			{
				targetMap.put(key, new LinkedList<String>());
				targetMap.get(key).addAll(list);
			}
		}
	}
	
	
	
	
	
	
	protected  LinkedList<UnitParser> compiledSuccessUpdate(UnitParser subChild, int nextIndex, Hashtable<String, LinkedList<String>> nextCapturedMap, UnitParser nonDeterministicPrior )
	{
		return null;
	}
	
	protected  LinkedList<UnitParser> compiledFailureUpdate(UnitParser subChild, int nextIndex)
	{
		
		
		if (nonDeterministicAncestor==null)
			return null;
		else
			return nonDeterministicAncestor.compiledFailureUpdate(this, -1);
	}
	
	protected void updateFailureCache(UnitParser nonDeterministricPrior)
	{
		
		if (!priorSuccess&&nonDeterministicAncestor==nonDeterministricPrior)
		{
			global.setFailureStatus(grammarComponent, startIndex);
			if (parent!=null)
				parent.updateFailureCache(nonDeterministricPrior);
		}
	}
	
	public abstract LinkedList<UnitParser> matchCompiled();

	public abstract UnitParser clone();
	
	public static String[] segmentGroup(String groupString)
	{
		return splitPatternByCommas(groupString);
	}
	
	private static String isBackReference(String component, Hashtable<String, LinkedList<String>> capturedComponents)
	{
		if (capturedComponents==null)
			return null;
		String[] parts = splitLast(component, ':');
		if (parts==null)
			return null;
		
		String nonTerminal = parts[0];
		if (!capturedComponents.containsKey(nonTerminal))
		{
			return null;
		}
		int matchCount = Integer.parseInt(parts[1]);
		LinkedList<String> matches = capturedComponents.get(nonTerminal);
		if (matches.size()<matchCount)
			return null;
		return matches.get(matchCount-1);
	}
	
	
	
	public static String[] isBackReference(String component)
	{
		
		String[] parts = splitLast(component, ':');
		
		if (parts!=null&&parts.length>1)
		{
			if (Character.isDigit(parts[1].charAt(0)))
			{
				boolean dMode=true;
				for (int i=1;i<parts[1].length();i++)
				{
					if (dMode)
					{
						if (Character.isDigit(parts[1].charAt(i)))
							continue;
						else if (Character.isWhitespace(parts[1].charAt(i)))
						{
							dMode=false;
						}
						else
							return null;
					}
					else
						if (Character.isWhitespace(parts[1].charAt(i)))
						{
							continue;
						}
						else
							return null;
						
				}
				
				return parts;
			}
			
			
			
			
		}
		return null;
	}
	
	private static String[] splitFirst(String input, char splitchar)
	{
		if (input==null)
			return null;
		int pos = input.indexOf(splitchar);
		if (pos==-1)
			return null;
		return new String[]{input.substring(0, pos), input.substring(pos+1)}; 
	}
	
	private static String[] splitLast(String input, char splitchar)
	{
		if (input==null)
			return null;
		int pos = input.lastIndexOf(splitchar);
		if (pos==-1)
			return null;
		return new String[]{input.substring(0, pos), input.substring(pos+1)}; 
	}
	
	private static String isTerminal(String inString)
	{
		if (inString==null)
			return null;
		
		boolean len3 = inString.length() == 3;
		boolean len4 = inString.length() == 4;
		boolean firstCharDelimiter = inString.length()>0 && "'".equals(inString.substring(0, 1));
		boolean thirdCharDelimiter = inString.length()>2 && "'".equals(inString.substring(2, 3));
		boolean fourthCharDelimiter = inString.length()>3 && "'".equals(inString.substring(3, 4));
		boolean secondCharEscape = inString.length()>1 && "\\".equals(inString.substring(1, 2));
		
		if (len3 && firstCharDelimiter && thirdCharDelimiter)
			return inString.substring(1,2);
		if (len4 && firstCharDelimiter && secondCharEscape && fourthCharDelimiter)
			return inString.substring(1,3);
		return null;
		
	}
	
	public static String isLabel(String inString, Hashtable<String,String> nonTerminals)
	{
		if (nonTerminals==null)
			return null;
		if (nonTerminals.containsKey(inString.trim()))
			return nonTerminals.get(inString.trim());
		return null;
	}
	
	public static  String[] isAlternation(String inString)
	{
		String[] parts = splitPattern(inString, '|', true);
		if ((parts!=null)&&(parts.length>1))
			return parts;
		else
			return null;
	}
	
	public static  String[] isDeterministicAlternation(String inString)
	{
		String[] parts = splitPattern(inString, '!', true);
		if ((parts!=null)&&(parts.length>1))
			return parts;
		else
			return null;
	}
	
	public static  String isGroup(String inString)
	{
		char[] values = inString.toCharArray();
		StringBuffer sBuffer = null;
		
		int pcounter = 0;
		for (int i=0;i<values.length;i++)
		{
			if ((i==0)&&(values[i]=='('))
			{
				pcounter++;
				sBuffer = new StringBuffer();
			}
			else
			{
				if ((values[i]=='(')&&!quotedCharacter(values, i))
					pcounter++;
				if ((values[i]==')')&&!quotedCharacter(values, i))
					pcounter--;
				if (pcounter==0)
				{
					if ((i!=values.length-1)||(i==0))
						return null;
					else
						return sBuffer.toString();
				}
				sBuffer.append(values[i]);
			}
		}
		return null;
	}
	
	public static  QuantifierInfo isQuantifier(String inString)
	{
		int len = inString.length();
		
		if (len>1)
		{
			char currentChar;
			int step; // (/d,/d)
			final int rparen=1, rdigit=2, ldigit=4, quantif=5;
			boolean range=false;
			step=rparen;
			
			int digit=0;
			int counter=0;
			Integer max=null, min=null;
			
			for (int i=len-1;i>=0;i--)
			{
				currentChar = inString.charAt(i);
				if (!range)
				{
					switch (currentChar)
					{
						case '?':
							return new QuantifierInfo(0, 1, "?", inString.substring(0, len - 1));
						case '+':
							return new QuantifierInfo(1, null, "+", inString.substring(0, len - 1));
						case '*':
							return new QuantifierInfo(0, null, "*", inString.substring(0, len - 1));
						case '`':
							return new QuantifierInfo(0, 0, "`", inString.substring(0, len - 1));
						default:
							range=true;
					}
					
					switch (step)
					{
						case rparen:
							if (currentChar == ')')
							{
								step = rdigit;
								counter=0;
							}
							else
								return null;
							break;
						case rdigit:
							if (Character.isDigit(currentChar))
							{
								digit+= convertChar(currentChar)*Math.pow(10, counter++);
							}
							else
							{
								if (currentChar == ',')
								{
									if (counter>0)
										max = new Integer(digit);
									else
										max = null;
									step=ldigit;
									digit=0;
									counter=0;
								}
								else
								{
									if ((currentChar == '(')&&(counter>0))
									{
										max = new Integer(digit);
										min = new Integer(digit);
										
										step=quantif;
									}
									else
										return null;
								}
							}
							
							break;
						case ldigit:
							if (Character.isDigit(currentChar))
							{
								digit+= convertChar(currentChar)*Math.pow(10, counter++);
							}
							else
							{
								
								if ((currentChar == '(')&&(counter>0))
								{
									min = new Integer(digit);
									step=quantif;
								}
								else
									return null;
								
							}
							break;
						case quantif:
							if (currentChar=='*')
								return new QuantifierInfo(min, max, "*", inString.substring(0, i));
							else
								return null;
					}
					
				}
				else
				{
					switch (step)
					{
						case rparen:
							if (currentChar == ')')
							{
								step = rdigit;
								counter=0;
							}
							else
								return null;
							break;
						case rdigit:
							if (Character.isDigit(currentChar))
							{
								digit+= convertChar(currentChar)*Math.pow(10, counter++);
							}
							else
							{
								if (currentChar == ',')
								{
									if (counter>0)
										max = new Integer(digit);
									else
										max = null;
									step=ldigit;
									digit=0;
									counter=0;
								}
								else
								{
									if ((currentChar == '(')&&(counter>0))
									{
										max = new Integer(digit);
										min = new Integer(digit);
										
										step=quantif;
									}
									else
										return null;
								}
							}
							
							break;
						case ldigit:
							if (Character.isDigit(currentChar))
							{
								digit+= convertChar(currentChar)*Math.pow(10, counter++);
							}
							else
							{
								
								if ((currentChar == '(')&&(counter>0))
								{
									min = new Integer(digit);
									step=quantif;
								}
								else
									return null;
								
							}
							break;
						case quantif:
							if (currentChar=='*')
								return new QuantifierInfo(min, max, "*", inString.substring(0, i));
							else
								return null;
					}
				}
				
			}
			
			
		}
		return null;
	}
	
	private static int convertChar(char c)
	{
		switch (c)
		{
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
		}
		return 0;
	}
	
	
	public static  String[] isConjunction(String inString)
	{
		String[] parts = splitPatternByCommas(inString);
		if (parts.length>1)
			return parts;
		else
			return null;
	}
	
	
	/**
	 * Indicates whether a character at a particular position within a pattern character array <br/>
	 * is quopted.
	 * @param array Character array representing a regular expression pattern
	 * @param pos
	 * @return
	 */
	public static boolean quotedCharacter(char[] array, int pos)
	{
		return (pos >0)&&(pos < array.length-1)&&(array[pos+1]=='\'') && ((array[pos - 1]=='\'')||((pos - 2 >=0)&&(array[pos-2]=='\'')&&(array[pos - 1]=='\\')));
	}
	
	
	public static String[] splitPatternByCommas(String tokenizedString)
	{
		return splitPattern(tokenizedString,',',true);
	}
	
	
	/**
	 * Splits a string representing a regular expression pattern on a character unless that character <br/>
	 * is quoted in single quotes
	 * @param tokenizedString The pattern string
	 * @param slitChar character to split on
	 * @param trim if true, each component delimited by the splitChar is trimmed
	 * @return Returns a string array of each component delimited by the splitChar
	 */
	public static String[] splitPattern(String tokenizedString, char slitChar, boolean trim)
	{
		if (tokenizedString==null)
			return null;
		
		LinkedList<String> splitString = new LinkedList<String>();
		final int IN_PARENTHESIS=0;
		final int OUT_PARENTHESIS=1;
		final int IN_STRING=2;
		final int OUT_STRING=3;
		
		int outState=OUT_STRING;
		int level=0;
		int state = OUT_PARENTHESIS;
		char[] chars = tokenizedString.toCharArray();
		StringBuilder segment= new StringBuilder();
		
		int totalChars=chars.length;
		try
		{
			for (int i=0;i<totalChars;i++)
			{
				
				switch (state)
				{
					case IN_PARENTHESIS:
						if ((chars[i] == ')')&&!quotedCharacter(chars, i))
						{
							if (level == 1)
								state = OUT_PARENTHESIS;
							else
								level--;
						}
						else
						{
							if ((chars[i]=='(') && !quotedCharacter(chars, i))
								level++;
						}
						segment.append(chars[i]);
						break;
					case OUT_PARENTHESIS:
						if (chars[i] == '"' && !quotedCharacter(chars, i))
						{
							state=IN_STRING;
							segment.append(chars[i]);
						}
						else
						{
							if ((chars[i] == slitChar) && !quotedCharacter(chars, i))
							{
								if (trim)
									splitString.add(segment.toString().trim());
								else
									splitString.add(segment.toString());
								segment = new StringBuilder();
							}
							else
							{
								if ((chars[i] == '(') && !quotedCharacter(chars, i))
								{
									state = IN_PARENTHESIS;
									level = 1;
								}
								segment.append(chars[i]);
							}
						}
						break;
					case IN_STRING:
						if ((chars[i] == '"') && !quotedCharacter(chars, i))
						{
							state=OUT_PARENTHESIS;
						}
						segment.append(chars[i]);
						break;
					
						
				}
				
			}
		}
		catch (Exception e)
		{
			System.out.println(tokenizedString);
		}
		
		if (segment.length()>0)
		{
			if (trim)
				splitString.add(segment.toString().trim());
			else
				splitString.add(segment.toString());
		}
		return splitString.toArray(new String[0]);
		
	}
	

	
	public static String stringToTerminalSequence(String input) {
		final int INITIAL = 0;
		final int IN_QUOTE = 1;
		final int OUT_QUOTE = 2;
		final int ESCAPE_SEEN_OUT_QUOTE = 3;
		final int ESCAPE_SEEN_IN_QUOTE = 4;
		final int ESCAPE_CHAR = 0;
		final int QUOTE = 1;
		final int NEITHER = 2;
		int state = INITIAL;
		int event;
		char[] chars = input.toCharArray();
		StringBuffer currentQuoted = null;
		StringBuffer overallOutput = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			switch (chars[i]) {
			case '\"':
				event = QUOTE;
				break;
			case '\\':
				event = ESCAPE_CHAR;
				break;
			default:
				event = NEITHER;
			}
			switch (state) {
			case INITIAL:
				switch (event) {
				case NEITHER:
					overallOutput.append(chars[i]);
					state = OUT_QUOTE;
					break;
				case ESCAPE_CHAR:
					overallOutput.append(chars[i]);
					state = ESCAPE_SEEN_OUT_QUOTE;
					break;
				case QUOTE:
					currentQuoted = new StringBuffer();
					state = IN_QUOTE;
					break;
				}
				break;
			case OUT_QUOTE:
				switch (event) {
				case NEITHER:
					overallOutput.append(chars[i]);
					state = OUT_QUOTE;
					break;
				case ESCAPE_CHAR:
					overallOutput.append(chars[i]);
					state = ESCAPE_SEEN_OUT_QUOTE;
					break;
				case QUOTE:
					currentQuoted = new StringBuffer();
					state = IN_QUOTE;
					break;
				}
				break;
			case IN_QUOTE:
				switch (event) {
				case NEITHER:
					currentQuoted.append(chars[i]);
					state = IN_QUOTE;
					break;
				case ESCAPE_CHAR:
					currentQuoted.append(chars[i]);
					state = ESCAPE_SEEN_IN_QUOTE;
					break;
				case QUOTE:
					overallOutput.append(convertToTerminal(currentQuoted
							.toString()));
					state = OUT_QUOTE;
					break;
				}
				break;
			case ESCAPE_SEEN_OUT_QUOTE:
				switch (event) {
				case NEITHER:
					overallOutput.append(chars[i]);
					state = OUT_QUOTE;
					break;
				case ESCAPE_CHAR:
					overallOutput.append(chars[i]);
					state = ESCAPE_SEEN_OUT_QUOTE;
					break;
				case QUOTE:
					overallOutput.append(chars[i]);
					state = OUT_QUOTE;
					break;
				}
				break;
			case ESCAPE_SEEN_IN_QUOTE:
				switch (event) {
				case NEITHER:
					currentQuoted.append(chars[i]);
					state = IN_QUOTE;
					break;
				case ESCAPE_CHAR:
					currentQuoted.append(chars[i]);
					state = ESCAPE_SEEN_IN_QUOTE;
					break;
				case QUOTE:
					currentQuoted.append(chars[i]);
					state = IN_QUOTE;
					break;
				}
				break;
			}
		}
		// finishing
		switch (state) {
		case INITIAL:
			return "";
		case OUT_QUOTE:
		case ESCAPE_SEEN_OUT_QUOTE:
			return overallOutput.toString();
		case IN_QUOTE: // this is an invalid state
		case ESCAPE_SEEN_IN_QUOTE:
		default:
			return null;
		}
	}

	
	
	public static String convertToTerminal(String input)
	{
		if ((input==null)||(input.length()==0))
			return "";
		char[] raw = input.toCharArray();
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("'");
		sBuilder.append("\\");
		sBuilder.append(raw[0]);
		sBuilder.append("'");
		for (int i=1;i<raw.length;i++)
		{
			sBuilder.append(",");
			sBuilder.append(" ");
			sBuilder.append("'");
			sBuilder.append("\\");
			sBuilder.append(raw[i]);
			sBuilder.append("'");
		}
		return sBuilder.toString();
	}

	public static class QuantifierInfo
	{
		public Integer miniMatches;
		public Integer maxMatches;
		public String quantifier;
		public String grammar;
		
		public QuantifierInfo(Integer miniMatches, Integer maxMatches, String quantifier, String grammar)
		{
			this.miniMatches=miniMatches;
			this.maxMatches=maxMatches;
			this.quantifier=quantifier;
			this.grammar=grammar;
			
		}
		
	}
	
	
}
