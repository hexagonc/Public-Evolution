package com.evolved.automata.parser;
import java.util.*;



class Parser 
{
	/**
	 * This class contains methods for building a parse-tree from a string representation
	 * of an EBNF-like pattern.  In fact, this parser can parse more general patterns than
	 * standard regular expressions, since it supports back references.  See
	 * http://phase-summary.blogspot.com/2012/05/context-free-grammar-parser-and-pattern.html for 
	 * detailed pattern rules.  There are also numerous static methods for aiding in the parse
	 * process. 
	 * 
	 */
	
	public static boolean debug=true;
	
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
	
	
	
	
	public static final int t_TERMINAL=0;
	public static final int t_NONTERMINAL=1;
	public static final int t_GROUP=2;
	public static final int t_CONJUNCTION=3;
	public static final int t_DISJUNCTION=4;
	public static final int t_KLEENE=5;
	public static final int t_WILDCARD=6;
	public static final int t_QUANTIFIER=7;
	
	
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
	
	
	/**
	 * Recursively builds a parse tree from a pattern string. This is the main method.
	 * 
	 * @param global This represents global state for all pattern matchers.  Intended to be used <br/>
	 * for caching match failures and/or successes to reduce matching the same subtrees.  Not currently <br/>
	 * used. 
	 * @param component This is the the pattern string the parse tree is being built from
	 * @param namedComponents This contains a mapping of nonterminal names to their defining pattern rules
	 * @return
	 */
	public static Matcher parse(CFGParser.GlobalState global,String component, Hashtable<String,String> namedComponents)
	{
		return parse(global, component, namedComponents, true);
	}
	
	/**
	 * Recursively builds a parse tree from a pattern string. This is the main method.  
	 * 
	 * @param global This represents global state for all pattern matchers.  Intended to be used <br/>
	 * for caching match failures and/or successes to reduce matching the same subtrees.  Not currently <br/>
	 * used. 
	 * @param component This is the the pattern string the parse tree is being built from
	 * @param namedComponents This contains a mapping of nonterminal names to their defining pattern rules
	 * @param defaultQuantifierGreedyP Indicates whether quantifiers are greedy or not by default
	 * @return root of parse tree for component or null if the pattern string is inconsistent with <br/>
	 * pattern syntax.
	 */
	public static Matcher parse(CFGParser.GlobalState global,  String component, Hashtable<String,String> namedComponents, boolean defaultQuantifierGreedyP)
	{
		
		component = component.trim();
		String terminalSymbol = isTerminal(component);
		if (terminalSymbol!=null)
		{
			return new TerminalMatcher(global, terminalSymbol);
		}
		
		
		String[] parts;
		Matcher[] subStates;
		StringDistribution distribution; 
		parts = disjuctionofLiterals(component);
		Matcher ostate;
		if (parts!=null)
		{
			distribution= new StringDistribution();
			for (String s:parts)
			{
				distribution.addString(s);
			}
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate = new OptimizedAlternationMatcher(component, global, distribution);
			}
			else
				ostate = new OptimizedAlternationMatcher(null, global, distribution);
			return ostate;
		}
		
		parts = isConjunction(component);
		if (parts!=null)
		{
			subStates = new Matcher[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=Parser.parse(global, parts[i], namedComponents,defaultQuantifierGreedyP);
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate = new ConjunctionMatcher(component,global, subStates);
			}
			else
				ostate = new ConjunctionMatcher(null,global, subStates);
			return ostate;
		}
		
		String mappedGrammar = isNonterminal(component, namedComponents);
		
		if (mappedGrammar!=null)
		{	
			if (global!=null)
				global.incrementCount(component);
			return new NonTerminalMatcher(component, global);
		}
		
		parts = isBackReference(component);
		
		if (parts!=null&&namedComponents.containsKey(parts[0]))
		{
			ostate = new NonTerminalMatcher(parts[0], global,  Integer.parseInt(parts[1]));
			
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
			subStates = new Matcher[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=Parser.parse(global, parts[i], namedComponents, defaultQuantifierGreedyP);
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate = new ConjunctionMatcher(component, global, subStates);;
			}
			else
				ostate = new ConjunctionMatcher(null, global, subStates);;
			return ostate;
		}
			
		parts = isAlternation(component.trim());
		if (parts!=null)
		{
			subStates = new Matcher[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=Parser.parse(global, parts[i], namedComponents, defaultQuantifierGreedyP);
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate = new AlternationMatcher(component, global, subStates, parts);
				
			}
			else
				ostate = new AlternationMatcher(null, global, subStates, parts);
			
			return ostate;
		}
		QuantifierInfo qInfo;
		qInfo = isQuantifier(component);
		if (qInfo!=null)
		{
			
			if (debug)
			{
				if (global!=null)
					global.incrementCount(component);
				ostate = new QuantifierMatcher(component, global, defaultQuantifierGreedyP,qInfo.miniMatches, qInfo.maxMatches, parse(global, qInfo.grammar,namedComponents,defaultQuantifierGreedyP));
			}
			else
				ostate = new QuantifierMatcher(null, global, defaultQuantifierGreedyP,qInfo.miniMatches, qInfo.maxMatches, parse(global, qInfo.grammar,namedComponents,defaultQuantifierGreedyP));
			
			return ostate;
		}
		
		return null;
	}
	

	/**
	 * Determines whether a string EBNF-like pattern can be parsed into a Alternation of literal <br/>
	 * strings.  Literal strings are delimited by double-quotes and can not contain the pipe <br/>
	 * character itself.  If the string literal contains the pipe character than consider using the <br/>
	 * method convertToTerminalSequencePattern to convert the literal to a raw terminal sequence.  <br/>
	 * Also matches a single string literal.
	 * @param grammarComponent EBNF-like pattern 
	 * @return String array representing each subcomponent of the alternation or null <br/>
	 * if the string can not be interpreted as a top-level alternation.  Does not include <br/>
	 * the double-quote delimiters.
	 */
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
	
	/**
	 * Tests whether grammarComponent specifies a alternation of string literals although, in <br/>
	 * contrast to the method, disjuctionofLiterals, string literals can be delimited by single or <br/>
	 * double quotes.
	 * @param grammarComponent string representing an EBNF-like pattern
	 * @return String array representing each subcomponent of the alternation or null <br/>
	 * if the string can not be interpreted as a top-level alternation.  Does not include <br/>
	 * the double-quote delimiters.
	 */
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
	
	
	/**
	 * Determines if an EBNF-like pattern string can be parsed into a conjunction of subpatterns
	 * @param groupString
	 * @return
	 */
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
	
	
	/**
	 * Determines if an EBNF-like string pattern can be interpreted as a back-reference to a <br/>
	 * previously captured non-terminal
	 * @param component
	 * @return string array where the first component is the base nonterminal name and the second <br/>
	 * component is the string back-reference index.  Back-reference indices are 1-based
	 */
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
	
	/**
	 * Determines whether a string representing an EBNF-like pattern can be interpreted <br/>
	 * as a terminal symbol
	 * @param inString
	 * @return returns the raw character represented by the terminal or null if <br/>
	 * the pattern can not be considered a terminal symbol
	 */
	public static String isTerminal(String inString)
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
	
	/**
	 * Determines if the string representation of an EBNF-like pattern can be interpreted as a <br/>
	 * non-terminal symbol.
	 * @param inString pattern string
	 * @param nonTerminals A hashtable mapping non-terminal names to their pattern definition
	 * @return Returns the definition of the non-terminal string if it is a non-terminal, else null
	 */
	public static String isNonterminal(String inString, Hashtable<String,String> nonTerminals)
	{
		if (nonTerminals==null)
			return null;
		if (nonTerminals.containsKey(inString.trim()))
			return nonTerminals.get(inString.trim());
		return null;
	}
	
	/**
	 * Determines if an EBNF-like pattern string can be interpreted as an alternation of
	 * smaller patterns
	 * @param inString pattern
	 * @return Returns an array of each smaller pattern or null if the other overall pattern <br/>
	 * isn't an alternation.
	 */
	public static  String[] isAlternation(String inString)
	{
		String[] parts = splitPattern(inString, '|', true);
		if ((parts!=null)&&(parts.length>1))
			return parts;
		else
			return null;
	}
	
	
	/**
	 * Same as isAlternation
	 * @param inString
	 * @return
	 */
	public static  String[] isDeterministicAlternation(String inString)
	{
		String[] parts = splitPattern(inString, '!', true);
		if ((parts!=null)&&(parts.length>1))
			return parts;
		else
			return null;
	}
	
	/**
	 * Determines whether an EBNF-like pattern can be interpreted as a smaller pattern contained <br/>
	 * in parenthesis
	 * @param inString
	 * @return The parenthesized pattern or null if the string is not a parenthesized pattern
	 */
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
	
	/**
	 * Determines whether an EBNF-like pattern can be interpreted as another pattern <br/>
	 * quantified.  
	 * @param inString
	 * @return Returns a QuantifierInfo object containing quantifier properties or null if <br/>
	 * the pattern is not a quantifier
	 */
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
	
	/**
	 * Determines whether an EBNF-like pattern can be interpreted as a conjunction of smaller <br/>
	 * sub-patterns.  If sub-patterns are string literals then they can not contain commas.
	 * @param inString pattern string
	 * @return Array of each sub-pattern or null if inString is not a conjunction
	 */
	
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
	
	/**
	 * Splits a string representing a regular expression pattern by comma unless that comma <br/>
	 * is quoted in single quotes
	 * @param tokenizedString The pattern string
	 * @return Returns a string array of each trimmed component delimited by commas
	 */
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
	
	
	/**
	 * Converts a string into a pattern representing a sequence of terminal characters
	 * @param input string 
	 * @return pattern representing a sequence of terminal symbols that will match input
	 */
	public static String convertToTerminalSequencePattern(String input)
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
