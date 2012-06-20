package com.evolved.automata.parser;
import java.util.*;

import com.evolved.automata.parser.Parser.QuantifierInfo;



public abstract class PatternGenerator {
	protected String patternName;
	protected String avoidPattern = null;
	public static final int defaultQuantifierMaxReps=10;
	
	PatternBuilder parentBuilder = null;
	
	public void setAvoidPattern(String aPattern)
	{
		avoidPattern = aPattern;
	}
	
	public abstract String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames);
	
	public abstract String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames);
	
	public abstract PatternGenerator clone();
	
	public boolean passesAvoidance(String result)
	{
		if (avoidPattern==null)
			return true;
		return !parentBuilder.matchPattern(result, avoidPattern);
	}
	
	
	protected void updateCaptureList(String newValue, Hashtable<String, LinkedList<String>> captureList)
	{
		LinkedList<String> list = null;
		if (captureList!=null)
		{
			if (captureList.containsKey(patternName))
				list = captureList.get(patternName);
			else
			{
				list = new LinkedList<String>();
				captureList.put(patternName, list);
			}
			list.add(newValue);
		}
	}
	
	
	
	protected void updateLog(String sampledPattern, Hashtable<String, Hashtable<String, Integer>> sample_log)
	{
		parentBuilder.addPatternToBuilt(sampledPattern);
		Hashtable<String, Integer> samples = new Hashtable<String, Integer>();
		Integer priorCount = 0;
		if (sample_log!=null)
		{
			if (!sample_log.containsKey(patternName))
			{
				samples = new Hashtable<String, Integer>();
				sample_log.put(patternName, samples);
			}
			else
			{
				samples = sample_log.get(patternName);
			}
			if (samples.containsKey(sampledPattern))
				priorCount = samples.get(sampledPattern);
			else
				priorCount=new Integer(0);
			samples.put(sampledPattern, priorCount+1);
		}
	}
	
	
	public static class NegatedDistribution extends PatternGenerator
	{
		
		PatternGenerator neg;
		PatternGenerator pos;
		
		
		public NegatedDistribution(PatternBuilder pBuilder, String pName, PatternGenerator negative, PatternGenerator positive)
		{
			parentBuilder =pBuilder;
			patternName = pName;
			neg = negative;
			pos = positive;
		}
		
		public NegatedDistribution()
		{
			
		}
		
		public NegatedDistribution(NegatedDistribution template)
		{
			neg = template.neg;
			pos = template.pos;
			patternName = template.patternName;
			avoidPattern = template.avoidPattern;
			parentBuilder = template.parentBuilder;
					
		}
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			neg.setAvoidPattern(null);
			String negative = neg.sample(log, precaptured, captureNames);
			pos.setAvoidPattern(negative);
			String positive = pos.sample(log, precaptured, captureNames);
			if (positive!=null)
				return null;
			else
				return positive;
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			
			return new String[]{""};
		}
		
		public PatternGenerator clone()
		{
			return new NegatedDistribution(this);
		}
	}
	
	
	public static class LiteralDistribution extends PatternGenerator
	{
		StringProbabilityDistribution distrib = null;
		
		public LiteralDistribution(PatternBuilder pBuilder, String pName, String[] strings)
		{
			parentBuilder = pBuilder;
			patternName = pName;
			distrib = parentBuilder.getStringDistribution(patternName);
			
			for (String s:strings)
			{
				if (!distrib.isInitialized(s))
					distrib.addString(s, 1);
			}
					
			
		}
		
		public LiteralDistribution()
		{
			
		}
		
		public LiteralDistribution(LiteralDistribution template)
		{
			distrib = (StringProbabilityDistribution)template.distrib.clone();
			patternName = template.patternName;
			avoidPattern = template.avoidPattern;
			parentBuilder = template.parentBuilder;
					
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			
			return distrib.getStrings();
		}
		
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			String sample;
			LinkedList<String> failedStrings = new LinkedList<String>();
			for (String key:distrib.getStrings())
			{
				if (!passesAvoidance(key))
					failedStrings.add(key);
			}
			
			sample = distrib.drawString(failedStrings.toArray(new String[0]), 0);
			
			if (passesAvoidance(sample))
			{
				updateLog(sample, log);
				return sample;
			}
			else
				return null;
			
			
		}
		
		public PatternGenerator clone()
		{
			return new LiteralDistribution(this);
		}
	}
	
	public static class DisjunctiveNonterminalDistribution extends PatternGenerator
	{
		StringProbabilityDistribution distrib = null;
		Hashtable<String, PatternGenerator> subGrammars;
		boolean deterministicP=false;
		
		public DisjunctiveNonterminalDistribution(PatternBuilder pBuilder, String pName, Hashtable<String, PatternGenerator> sub)
		{
			parentBuilder = pBuilder;
			patternName = pName;
			distrib = parentBuilder.getStringDistribution(patternName);
			
			for (String s:sub.keySet())
			{
				if (!distrib.isInitialized(s))
					distrib.addString(s, 1);
			}
			
			subGrammars = sub;
		}
		
		public void setDeterministic()
		{
			deterministicP=true;
		}
		
		public DisjunctiveNonterminalDistribution()
		{
			patternName = null;
			distrib = new StringProbabilityDistribution();
			
			subGrammars=null;
		}
		
		
		public DisjunctiveNonterminalDistribution(DisjunctiveNonterminalDistribution template)
		{
			patternName = template.patternName;
			distrib = (StringProbabilityDistribution)template.distrib.clone();
			avoidPattern = template.avoidPattern;
			subGrammars= PatternBuilder.copyDeep(template.subGrammars);
			parentBuilder = template.parentBuilder;
		}
		
		public PatternGenerator clone()
		{
			DisjunctiveNonterminalDistribution d = new DisjunctiveNonterminalDistribution(this);
			d.deterministicP=deterministicP;
			return d;
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			LinkedList<String> out = new LinkedList<String>();
			String[] sout;
			for (String key:subGrammars.keySet())
			{
				sout = subGrammars.get(key).extrude(log, precaptured, captureNames);
				if (sout!=null)
				{
					for (String pat:sout)
					{
						out.add(pat);
					}
				}
			}
			
			if (out.size()==0)
				return null;
			else
				return out.toArray(new String[0]);
		}
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			PatternGenerator sGrammar = null;
			String sample, sout;
			
			if (avoidPattern==null)
			{
				String sub = distrib.drawString();
				sGrammar = subGrammars.get(sub);
				sout  = sGrammar.sample(log, precaptured, captureNames);
				if (sout!=null)
				{
					updateLog(sub, log);
					if (captureNames!=null && captureNames.contains(patternName))
					{
						updateCaptureList(sout, precaptured);
					}
					return sout;
				}
				else
					return null;
			}
			else
			{
				// Need to find all patterns that produce a result consistent with the avoidance pattern
				
				LinkedList<String> failedStrings = new LinkedList<String>();
				
				// TODO: Not going to finish this yet
				for (String subGrammar:distrib.getStrings())
				{
					sGrammar = subGrammars.get(subGrammar);
					sout = sGrammar.sample(null, precaptured, captureNames);
					if (!passesAvoidance(sout))
						failedStrings.add(subGrammar);
				}
				
				sample = distrib.drawString(failedStrings.toArray(new String[0]), 0);
				return sample;
			}
			
			
		}
	}
	
	public static class NonterminalDistribution extends PatternGenerator
	{
		
		Hashtable<String, String> component;
		Hashtable<String, PatternGenerator> compiled;
		int referenceN=0;
		boolean isBackReferenceP=false;
		String backReference = null;
		
		public NonterminalDistribution(PatternBuilder pBuilder, String component)
		{
			parentBuilder = pBuilder;
			compiled = parentBuilder.getCompiledMap();
			this.component = parentBuilder.getNonterminalMap();
			patternName = component;
		}
		
		public NonterminalDistribution(NonterminalDistribution template)
		{
			if (template.compiled!=null)
				compiled = template.compiled;
			
			if (template.component!=null)
				component = template.component;
			else
				component =null;
			
			patternName = template.patternName;
			isBackReferenceP = template.isBackReferenceP;
			referenceN = template.referenceN;
			avoidPattern = template.avoidPattern;
			parentBuilder = template.parentBuilder;
		}
		
		
		public NonterminalDistribution(PatternBuilder pBuilder, String pattern, String referenceName, int index)
		{
			parentBuilder = pBuilder;
			referenceN = index;
			patternName = pattern; 
			backReference=referenceName;
			isBackReferenceP = true;
		}
		
		public PatternGenerator clone()
		{
			return new NonterminalDistribution(this);
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			if (isBackReferenceP)
			{
				// TODO: Do something better here
				return new String[]{""};
			}
			
			PatternGenerator pat = compiled.get(patternName);
			
			return  pat.extrude(log, precaptured, captureNames);
			
			
		}
		
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			if (isBackReferenceP)
			{
				if (precaptured!=null&& precaptured.containsKey(backReference))
				{
					LinkedList<String> cList = precaptured.get(backReference);
					if (cList.size()>=referenceN)
					{
						String value = cList.get(referenceN-1);
						if (passesAvoidance(value))
							return value;
					}
					return null;
				}
				return null;
			}
			else
			{
				PatternGenerator pat = compiled.get(patternName);
				boolean alteredChild=false;
				String oldAvoid = null;
				if (pat instanceof PatternGenerator.LiteralDistribution || pat instanceof PatternGenerator.DisjunctiveNonterminalDistribution || pat instanceof PatternGenerator.AlternationDistribution)
				{
					oldAvoid = pat.avoidPattern;
					pat.setAvoidPattern(avoidPattern);
					alteredChild= true;
				}
				
				String s = pat.sample(log, precaptured, captureNames);
				if (alteredChild)
					pat.setAvoidPattern(oldAvoid);
				if (s!=null&&passesAvoidance(s))
				{
					if (captureNames!=null&&captureNames.contains(patternName))
						updateCaptureList(s, precaptured);
					else
						parentBuilder.addPatternToBuilt(patternName);
					return s;
				}
				else
					return null;
			}
			
		}
	}
	
	
	public static class ConjunctionDistribution extends PatternGenerator
	{
		
		PatternGenerator[] subGenerators = null;
		
		public ConjunctionDistribution(PatternBuilder pBuilder, String pName, PatternGenerator[] sub)
		{
			parentBuilder = pBuilder;
			subGenerators = sub;
			patternName =  pName;
		}
		
		public ConjunctionDistribution()
		{
			subGenerators = null;
		}
		
		public ConjunctionDistribution(ConjunctionDistribution template)
		{
			if (template.subGenerators!=null)
			{
				int count = 0;
				subGenerators = new PatternGenerator[count = template.subGenerators.length];
				for (int i=0;i<count;i++)
				{
					subGenerators[i] = template.subGenerators[i].clone();
				}
			}
			patternName =  template.patternName;
			avoidPattern = template.avoidPattern;
			parentBuilder = template.parentBuilder;
		}
		
		public PatternGenerator clone()
		{
			return new ConjunctionDistribution(this);
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			LinkedList<String> out = new LinkedList<String>(), mid = new LinkedList<String>();
			
			String[]  eout;
			
			for (int i=0;i<subGenerators.length;i++)
			{
				eout = subGenerators[i].extrude(log, precaptured, captureNames);
				if (eout==null)
					return null;
				mid = new LinkedList<String>();
				if (out.size()==0)
				{
					for (String sout:eout)
					{
						if (sout!=null && passesAvoidance(sout))
						{
							mid.add(sout);
						}
					}
					
				}
				else
				{
					
					for (String s:out)
					{
						for (String sout:eout)
						{
							if (sout!=null && passesAvoidance(sout))
							{
								mid.add(s+sout);
							}
						}
					}
				}
				
				if (mid.size()==0)
					return null;
				out=mid;
				
			}
			
			
			if (out.size()==0)
				return null;
			else
			{
				return out.toArray(new String[0]);
			}
		}
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			StringBuilder sBuilder = new StringBuilder();
			String pout;
			for (PatternGenerator gen:subGenerators)
			{
				pout = gen.sample(log, precaptured, captureNames);
				if (pout!=null)
					sBuilder.append(pout);
				else
					return null;
				
			}
			pout = sBuilder.toString();
			if (avoidPattern!=null&&pout.equals(avoidPattern))
				return null;
			else
				return pout;
			
		}
	}
	
	
	public static class QuantifierDistribution extends PatternGenerator
	{
		StringProbabilityDistribution distrib = null;
		
		PatternGenerator subGenerators = null;
		int min=0;
		int max = 0;
		public QuantifierDistribution(PatternBuilder pBuilder, String pName, PatternGenerator sub, int min_matches, int max_matches)
		{
			parentBuilder = pBuilder;
			patternName =pName;
			subGenerators = sub;
			min = min_matches;
			max = max_matches;
		}
		
		public QuantifierDistribution()
		{
			
		}
		
		public QuantifierDistribution(QuantifierDistribution template)
		{
			min = template.min;
			max = template.max;
			patternName =template.patternName;
			avoidPattern = template.avoidPattern;
			subGenerators = (PatternGenerator)template.subGenerators.clone();
			parentBuilder = template.parentBuilder;
		}
		
		public PatternGenerator clone()
		{
			return new QuantifierDistribution(this);
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			LinkedList<String> out =  new LinkedList<String>();
			String[] sout = subGenerators.extrude(log, precaptured, captureNames);
			if (sout==null)
				if (min==0)
					return new String[]{""};
				else
					return null;
			StringBuilder sBuilder;
			boolean emptyPresent=false;
			for (int i=min;i<=max;i++)
			{
				for (String base:sout)
				{
					if (base.length()==0)
					{
						emptyPresent=true;
						continue;
					}
					sBuilder = new StringBuilder();
					for (int j=0;j<i;j++)
					{
						sBuilder.append(base);
					}
					if (sBuilder.length()>0)
						out.add(sBuilder.toString());
				}
				
			}
			if (min==0&&emptyPresent==false)
				out.add("");
			if (out.size()>0)
				return out.toArray(new String[0]);
			else
				return null;
		}
		
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			int reps = (int)(Math.random()*(max - min+1))+min;
			StringBuilder s = new StringBuilder();
			String sout = null;
			for (int i=0;i<reps;i++)
			{
				sout = subGenerators.sample(log, precaptured, captureNames);
				if (sout!=null)
					s.append(sout);
				else
					if (min<=i)
						return s.toString();
					else
						return null;
			}
			sout = s.toString();
			if (passesAvoidance(sout))
				return sout;
			return null;
		}
	}
	
	
	public static class AlternationDistribution extends PatternGenerator
	{
		StringProbabilityDistribution distrib = null;
		
		boolean deterministicP=false;
		PatternGenerator[] subGenerators = null;
		
		public AlternationDistribution(PatternBuilder pBuilder, String pName, PatternGenerator[] sub)
		{
			parentBuilder = pBuilder;
			patternName = pName;
			subGenerators = sub;
			distrib = parentBuilder.getStringDistribution(patternName);
			
			for (PatternGenerator s:sub)
			{
				if (!distrib.isInitialized(s.patternName))
					distrib.addString(s.patternName, 1);
			}
			
		}
		
		public void setDeterministric()
		{
			deterministicP=true;
		}
		
		public AlternationDistribution()
		{
			
		}
		
		public AlternationDistribution(AlternationDistribution template)
		{
			if (template.subGenerators!=null)
			{
				int count = 0;
				subGenerators = new PatternGenerator[count = template.subGenerators.length];
				for (int i=0;i<count;i++)
				{
					subGenerators[i] = template.subGenerators[i];
				}
			}
			patternName =  template.patternName;
			avoidPattern = template.avoidPattern;
			parentBuilder = template.parentBuilder;
		}
		
		public PatternGenerator clone()
		{
			AlternationDistribution n = new AlternationDistribution(this);
			n.deterministicP=deterministicP;
			return n;
		}
		
		public String[] extrude(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			LinkedList<String> out = new LinkedList<String>();
			
			String[]  eout;
			for (int i=0;i<subGenerators.length;i++)
			{
				eout = subGenerators[i].extrude(log, precaptured, captureNames);
				if (eout==null)
					continue;
				for (String sout:eout)
				{
					if (sout!=null && passesAvoidance(sout))
					{
						out.add(sout);
					}
				}
				
			}
			if (out.size()==0)
				return null;
			else
				return out.toArray(new String[0]);
			
		}
		public String sample(Hashtable<String, Hashtable<String, Integer>> log, Hashtable<String, LinkedList<String>> precaptured, HashSet<String> captureNames)
		{
			
			
			
			if (deterministicP)
			{
				String sout= null;
				for (int i=0;i<subGenerators.length;i++)
				{
					sout = subGenerators[i].sample(log, precaptured, captureNames);
					if (sout!=null && passesAvoidance(sout))
					{
						updateLog(subGenerators[i].patternName, log);
						return sout;
					}
				}
				return null;
			}
			else
			{
				LinkedList<Integer> chooses = new LinkedList<Integer>();
				for (int i=0;i<subGenerators.length;i++)
					chooses.add(new Integer(i));
				int reps = (int)(Math.random()*(subGenerators.length));
				String sout= null;
				int choice;
				while (chooses.size()>0)
				{
					choice = chooses.get(reps);
					chooses.remove(reps);
					sout =  subGenerators[choice].sample(log, precaptured, captureNames);
					if (sout!=null && passesAvoidance(sout))
					{
						updateLog(subGenerators[choice].patternName, log);
						return sout;
					}
					reps = (int)(Math.random()*(chooses.size()));
				}
			
				return null;
			}
		}
	}
	
	
	
	public static PatternGenerator compile(PatternBuilder parentBuilder, String patternComponent)
	{
		
		String[] parts;
		PatternGenerator[] subStates;
		
		parts = Parser.disjuctionofLiterals(patternComponent);
		if (parts!=null)
		{
			return new LiteralDistribution(parentBuilder, patternComponent, parts);
		}
		
		parts = Parser.disjuctionofLiteralsSimplified(patternComponent);
		if (parts!=null)
		{
			return new LiteralDistribution(parentBuilder, patternComponent, parts);
		}
		
		parts= parentBuilder.negativePattern(patternComponent);
		if (parts!=null)
		{
			
			PatternGenerator pos = compile(parentBuilder, parts[1]).clone();
			pos.setAvoidPattern(parts[0]+", '^'");
			return pos;
		}
		
		
		parts = Parser.isConjunction(patternComponent);
		if (parts!=null)
		{
			subStates = new PatternGenerator[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=compile(parentBuilder, parts[i]);
			return new ConjunctionDistribution(parentBuilder, patternComponent, subStates);
		}
		String mappedGrammar = Parser.isNonterminal(patternComponent, parentBuilder.getNonterminalMap());
		
		if (mappedGrammar!=null)
		{	
			String[] subParts = Parser.isAlternation(mappedGrammar);
			
			Hashtable<String, PatternGenerator> input = new Hashtable<String, PatternGenerator>();
			PatternGenerator gen = null;
			boolean match=true;
			if (subParts!=null)
			{
				match=true;
				for (String s:subParts)
				{
					if (Parser.isNonterminal(s, parentBuilder.getNonterminalMap())!=null)
					{
						gen = new NonterminalDistribution(parentBuilder, s);
						input.put(s, gen);
					}
					else
					{
						match=false;
						break;
					}
				}
			}
			else
				match=false;
			
			if (match)
			{
				DisjunctiveNonterminalDistribution d = new DisjunctiveNonterminalDistribution(parentBuilder, patternComponent, input);
				
				return d;
			}
			else
				return new NonterminalDistribution(parentBuilder, patternComponent);
		}
		
		parts = Parser.isBackReference(patternComponent);
		
		if (parts!=null)
		{
			int index = Integer.parseInt(parts[1]);
			
			return new NonterminalDistribution(parentBuilder,patternComponent, parts[0], index);
		}
		
		mappedGrammar = Parser.isGroup(patternComponent);
		if (mappedGrammar!=null)
		{
			parts = Parser.segmentGroup(mappedGrammar);
			subStates = new PatternGenerator[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=compile(parentBuilder, parts[i]);
			return new ConjunctionDistribution(parentBuilder, patternComponent, subStates);
		}
		
		
		parts = Parser.isAlternation(patternComponent);
		if (parts!=null)
		{
			subStates = new PatternGenerator[parts.length];
			for (int i=0;i<subStates.length;i++)
				subStates[i]=compile(parentBuilder, parts[i]);
			return new AlternationDistribution(parentBuilder, patternComponent, subStates);
		}
		QuantifierInfo qInfo;
		qInfo = Parser.isQuantifier(patternComponent);
		if (qInfo!=null)
		{
			PatternGenerator generator = compile(parentBuilder, qInfo.grammar);
			return new QuantifierDistribution(parentBuilder, patternComponent, generator, (qInfo.miniMatches==null)?0:qInfo.miniMatches, (qInfo.maxMatches==null)?defaultQuantifierMaxReps:qInfo.maxMatches);
		}
		
		return null;
	}
	
	
	
}
