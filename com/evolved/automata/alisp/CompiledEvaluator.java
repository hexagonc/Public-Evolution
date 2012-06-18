package com.evolved.automata.alisp;

public interface CompiledEvaluator {
	
	public void setArgs(Argument[] args);
	public Argument getCompiledResult(boolean resume);
	
	public Environment getEnvironment();
	public Argument eval(Argument[] args);
	
	public CompiledEvaluator clone();
	public void setEnvironment(Environment env);
	
}
