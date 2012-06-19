package com.evolved.automata.algorithms.interruptible;

import java.util.*;
import com.evolved.automata.filetools.StandardTools;

public class BeamSearch {

	
	enum BEAM_STATE
	{
		INITIAL_STATE,
		INITIALIZING_BRANCH_STACK,
		CHECKING_IF_NODE_IS_GOAL,
		SUCCESSFUL_FINAL_STATE,
		GETTING_NEXT_CHOICE_POINTER,
		RETURN_TO_PRIOR_CHOICE_BRANCH,
		FAILURE_TO_FIND_GOAL,
		RESOLVING_CHOICE_POINTER_TO_CHOICE_NODE
	}
	ChoiceNode a_InitialNode;
	LinkedList<ChoiceNode> a_BranchStack;
	Hashtable<BEAM_STATE, SearchState> a_StateMap;
	Hashtable<SearchState,String> a_StringtateMap;
	ChoiceNode a_CurrentNode;
	LinkedList<ChoicePointer> a_ChoicePath;
	ChoicePointer a_CurrentPointer;
	int a_MaxBranchDepth=100000;
	SearchState a_CurrentState;
	boolean a_AllowDuplicateStatesP;
	
	public BeamSearch(ChoiceNode initialNode)
	{
		a_InitialNode=initialNode;
		a_StateMap=new Hashtable<BEAM_STATE, SearchState>();
		a_StringtateMap = new Hashtable<SearchState,String>();
		InitializeStates();
		a_CurrentState = GetState(BEAM_STATE.INITIAL_STATE);
		a_AllowDuplicateStatesP=false;
	}
	

	
	
	public BeamSearch(ChoiceNode initialNode, int maxBranchDepth)
	{
		a_InitialNode=initialNode;
		a_StateMap=new Hashtable<BEAM_STATE, SearchState>();
		a_StringtateMap = new Hashtable<SearchState,String>();
		a_MaxBranchDepth=maxBranchDepth;
		a_AllowDuplicateStatesP=false;
		InitializeStates();
		a_CurrentState = GetState(BEAM_STATE.INITIAL_STATE);
	}
	
	public BeamSearch(ChoiceNode initialNode,boolean allowDuplicateStatesP)
	{
		a_InitialNode=initialNode;
		a_StateMap=new Hashtable<BEAM_STATE, SearchState>();
		a_StringtateMap = new Hashtable<SearchState,String>();
		InitializeStates();
		a_CurrentState = GetState(BEAM_STATE.INITIAL_STATE);
		a_AllowDuplicateStatesP=allowDuplicateStatesP;
	}
	

	
	
	public BeamSearch(ChoiceNode initialNode, int maxBranchDepth,boolean allowDuplicateStatesP)
	{
		a_InitialNode=initialNode;
		a_StateMap=new Hashtable<BEAM_STATE, SearchState>();
		a_StringtateMap = new Hashtable<SearchState,String>();
		a_MaxBranchDepth=maxBranchDepth;
		a_AllowDuplicateStatesP=allowDuplicateStatesP;
		InitializeStates();
		a_CurrentState = GetState(BEAM_STATE.INITIAL_STATE);
	}
	
	
	private void AddState(BEAM_STATE state, SearchState stateDef)
	{
		a_StateMap.put(state, stateDef);
		a_StringtateMap.put(stateDef,state.name());
	}
	
	private SearchState GetState(BEAM_STATE state)
	{
		return a_StateMap.get(state);
	}
	
	
	private boolean Finished()
	{
		return (a_CurrentState == GetState(BEAM_STATE.SUCCESSFUL_FINAL_STATE) || a_CurrentState ==GetState(BEAM_STATE.FAILURE_TO_FIND_GOAL));
	}
	
	public LinkedList<ChoicePointer> GetSolutionPath()
	{
		return a_ChoicePath;
	}
	private void InitializeStates()
	{

		AddState(
				BEAM_STATE.INITIAL_STATE, 
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						
						return GetState(BEAM_STATE.INITIALIZING_BRANCH_STACK);
					}
				}
		);
		
		
		
		AddState(
				BEAM_STATE.INITIALIZING_BRANCH_STACK, 
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						a_ChoicePath = new LinkedList<ChoicePointer>();
						a_InitialNode.ResetNode();
						a_BranchStack = new LinkedList<ChoiceNode>();
						a_CurrentNode=a_InitialNode;
						a_BranchStack.add(a_CurrentNode);
						return GetState(BEAM_STATE.CHECKING_IF_NODE_IS_GOAL);
					}
				}
		);
		
		AddState(
				BEAM_STATE.CHECKING_IF_NODE_IS_GOAL,
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						
						ChoicePointer nextPointer;
						if (a_CurrentNode.GoalReachedP())
							return GetState(BEAM_STATE.SUCCESSFUL_FINAL_STATE);
						else
						{
							if (a_BranchStack.size()<=a_MaxBranchDepth)
							{
								// prepare to do recursive call
								a_CurrentPointer=a_CurrentNode.NextChoice();
								
								while (a_CurrentPointer!=null)
								{
									if (a_AllowDuplicateStatesP||!CyclicPointerP(a_CurrentPointer))
										return GetState(BEAM_STATE.RESOLVING_CHOICE_POINTER_TO_CHOICE_NODE);
									a_CurrentPointer=a_CurrentNode.NextChoice();
								}
							}
							 // unwind stack one level
							return GetState(BEAM_STATE.RETURN_TO_PRIOR_CHOICE_BRANCH);
						}
					}
				}
		);
		
		
		AddState(
				BEAM_STATE.SUCCESSFUL_FINAL_STATE,
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						return null;
					}
					
				}
		);

		AddState(
				BEAM_STATE.RETURN_TO_PRIOR_CHOICE_BRANCH,
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						
						if (a_ChoicePath.size()>0)
							a_ChoicePath.removeLast();
						a_BranchStack.removeLast();
						if (a_BranchStack.size()==0)
						{
							a_ChoicePath=null;
							return GetState(BEAM_STATE.FAILURE_TO_FIND_GOAL);
						}
						
						a_CurrentNode=a_BranchStack.getLast();
						return GetState(BEAM_STATE.CHECKING_IF_NODE_IS_GOAL);
					}
					
				}
		);
		
		AddState(
				BEAM_STATE.FAILURE_TO_FIND_GOAL,
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						return null;
					}
					
				}
		);


		AddState(
				BEAM_STATE.RESOLVING_CHOICE_POINTER_TO_CHOICE_NODE,
				new SearchState()
				{
					public SearchState GetNextState(EventType etype, Object eventData)
					{
						a_ChoicePath.add(a_CurrentPointer);
						a_CurrentNode = a_CurrentPointer.ResolveNode();
						a_CurrentNode.ResetNode();
						// Do recursive call
						a_BranchStack.add(a_CurrentNode);
						return GetState(BEAM_STATE.CHECKING_IF_NODE_IS_GOAL);
						
					}
					
				}
		);
		
		
	}
	
	public boolean ExecuteNextStep()
	{
		if (!Finished()&&a_CurrentState!=null)
		{
			a_CurrentState=a_CurrentState.GetNextState(null, null);
			return true;
		}
		else
			return false;
	}
	
	public boolean CyclicPointerP(ChoicePointer pointer)
	{
		ChoiceNode nextNode = pointer.ResolveNode();
		return a_BranchStack.contains(nextNode);
	}
	public void ResetSearchState()
	{
		a_CurrentState=GetState(BEAM_STATE.INITIAL_STATE);
	}
}
