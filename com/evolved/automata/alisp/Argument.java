package com.evolved.automata.alisp;

public class Argument 
{
	public static final int CONS=0;
	public static final int ATOM=2;
	public static final int EXPRESSION=1;
	public static final int NIL=3;
	public static final int EVAL=4;
	
	
	public String sValue = null;
	public Object oValue=null;
	public Argument[] innerList = null;
	public boolean constantP=false;
	public CompiledEvaluator evaluator=null;
	public int TYPE = ATOM;
	public boolean breakValue=false;
	public boolean identifierString=false;
	public boolean lambdaP=false;
	
	public boolean continuationP=false;
	
	public boolean isContinuation()
	{
		return continuationP;
	}
	
	public Argument parent=null;
	
	public int argumentIndex=-1;
	public int subArgumentIndex=-1;
	
	
	public Argument(Argument p, int aIndex, int sIndex)
	{
		subArgumentIndex=sIndex;
		argumentIndex = aIndex;
		parent=p;
		continuationP = true;
	}
	
	public Argument(Argument p)
	{
		
		parent=p;
		continuationP = true;
	}
	
	public Argument(boolean breaks)
	{
		TYPE = NIL;
		breakValue = breaks;
	}
	
	public Argument()
	{
		TYPE = NIL;
		breakValue = false;
	}
	
	public boolean isConstantP()
	{
		return constantP;
	}
	
	public Argument setConstant()
	{
		constantP=true;
		return this;
	}
	
	public Argument clearConstant()
	{
		constantP=false;
		return this;
	}
	
	public void setIdentifier(boolean id)
	{
		identifierString = id;
	}
	
	public boolean isIdentifier()
	{
		return identifierString;
	}
	public void setBreak(boolean exitBlock)
	{
		breakValue =exitBlock;
	}
	
	public boolean isBreak()
	{
		return breakValue;
	}
	
	public boolean isEvaluator()
	{
		return TYPE==EVAL;
	}
	
	public boolean isLambda()
	{
		return lambdaP;
	}
	
	public void setLambda(boolean status)
	{
		lambdaP=status;
	}
	
	public Argument(CompiledEvaluator eval)
	{
		evaluator=eval;
		TYPE=EVAL;
	}
	
	public Argument(String expression, Object value, Argument[] list)
	{
		if (expression!=null)
		{
			sValue = expression;
			TYPE = EXPRESSION;
		}
		else
		{
			if (value!=null)
			{
				oValue = value;
				TYPE = ATOM;
			}
			else
			{
				if (list!=null)
				{
					innerList = list;
					TYPE = CONS;
				}
				else
				{
					TYPE = NIL;
				}
			}
		}
		
	}
	
	public int getType()
	{
		return TYPE;
	}
	
	public boolean isNull()
	{
		return TYPE==NIL;
	}
	
	public boolean isExpression()
	{
		return TYPE==EXPRESSION;
	}
	
	public boolean isCons()
	{
		return TYPE==CONS;
	}
	
	public boolean isAtom()
	{
		return TYPE==ATOM;
	}
	
	public String toString()
	{
		switch (TYPE)
		{
			case EXPRESSION:
				return "["+ sValue+ "]";
			case CONS:
				StringBuilder sBuilder = new StringBuilder("(");
				
				for (int i=0;i<innerList.length;i++)
				{
					if (i==0)
						sBuilder.append(printConsValue(innerList[i]));
					else
					{
						sBuilder.append(", ");
						sBuilder.append(printConsValue(innerList[i]));
					}
				}
				sBuilder.append(")");
				return sBuilder.toString();
			case ATOM:
				if (oValue == null)
					return "null";
				if (oValue instanceof String)
					return "\""+oValue.toString()+"\"";
				else
					return oValue.toString();
			case EVAL:
				return evaluator.toString();
			default:
				return "nil";
		}
	}
	
	private String printConsValue(Argument value)
	{
		if (value==null)
			return "null";
		else
			return value.toString();
	}
	
	public CompiledEvaluator getEvaluator()
	{
		return evaluator;
	}
}
