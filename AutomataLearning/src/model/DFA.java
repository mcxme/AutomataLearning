package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class DFA {

	private int stateCount;
	
	private HashSet<Integer> acceptingStates;
	
	private HashMap<String, Integer> transitions;
	
	private int currentState;
	
	private String[] alphabet;
	
	public DFA()
	{
		this.stateCount = -1;
		this.currentState = -1;
		this.transitions = new HashMap<String, Integer>();
		this.acceptingStates = new HashSet<Integer>();
	}
	
	public String[] getAlphabet()
	{
		return this.alphabet;
	}
	
	public int getNumberOfStates()
	{
		return this.stateCount;
	}
	
	public void setAlphabet(String[] alphabet)
	{
		this.alphabet = alphabet;
	}
	
	private static String getTransitionString(int state, String input)
	{
		return "" + state + "|" + input;
	}
	
	public void setTotalNumberOfStates(int stateNumber)
	{
		this.stateCount = stateNumber;
	}
	
	public void setTransition(int initialState, int finalState, String transitionString)
	{
		if (initialState < 0 || finalState < 0 || initialState >= stateCount || finalState >= stateCount)
		{
			throw new IllegalArgumentException("state out of bounds");
		}
		
		String input = getTransitionString(initialState, transitionString);
		transitions.put(input, finalState);
	}
	
	protected void setTransitions(HashMap<String, Integer> transitions)
	{
		this.transitions = transitions;
	}
	
	public void setInitialState(int initialState)
	{
		this.currentState = initialState;
	}
	
	public void addAcceptingState(int acceptingState)
	{
		this.acceptingStates.add(acceptingState);
	}
	
	public boolean isCurrentStateAccepting()
	{
		return this.acceptingStates.contains(getCurrentState());
	}
	
	public boolean isAcceptingState(int state)
	{
		return this.acceptingStates.contains(state);
	}
	
	public void removeAcceptingState(int acceptingState)
	{
		this.acceptingStates.remove(acceptingStates);
	}
	
	public int getCurrentState()
	{
		return this.currentState;
	}
	
	public int getNextState(String string)
	{
		String input = getTransitionString(currentState, string);
		int nextState = this.transitions.get(input);
		
		this.currentState = nextState;
		return nextState;
	}
	
	public int getNextState(String string, int state)
	{
		String input = getTransitionString(state, string);
		
		if (this.transitions.containsKey(input))
		{
			return this.transitions.get(input);
		}
		else
		{
			return -1;
		}
	}
	
	public int getResultingStateFromString(String stringSequence)
	{
		int currentState = getCurrentState();
		String stringSequenceCopy = new String(stringSequence);
		
		while (stringSequenceCopy.length() > 0)
		{
			for (int i = 0; i < alphabet.length; i++) {
				String symbol = alphabet[i];
				
				if (stringSequenceCopy.startsWith(symbol))
				{
					currentState = getNextState(symbol, currentState);
					stringSequenceCopy = stringSequenceCopy.substring(symbol.length());
					break;
				}
			}
		}

		return currentState;
	}
	
	public String getResultingStateTextualSequence(String[] stringSequence)
	{
		List<String> strings = Arrays.asList(stringSequence);
		return getResultingStateTextualSequence(strings);
	}
	
	public void removeTransition(int startState, int endState, String input)
	{
		if (hasTransition(startState, endState, input))
		{
			String in = getTransitionString(startState, input);
			this.transitions.remove(in);
		}
	}
	
	public void removeAllTransitions()
	{
		this.transitions.clear();
	}
	
	public boolean hasTransition(int startState, int endState, String input)
	{
		String in = getTransitionString(startState, input);
		boolean containsTransition = false;
		
		if (this.transitions.containsKey(in))
		{
			int supposedEndState = this.transitions.get(in);
			containsTransition = supposedEndState == endState;
		}
		
		return containsTransition;
	}
	
	public boolean isConnectedGraph()
	{
		HashSet<Integer> foundStates = new HashSet<Integer>(this.stateCount);
		LinkedList<Integer> statesToProcess = new LinkedList<Integer>();
		statesToProcess.add(this.currentState);
		
		// try to reach each state
		while (!statesToProcess.isEmpty())
		{
			int reachedState = statesToProcess.pop();
			if (foundStates.contains(reachedState))
			{
				continue;
			}
			
			foundStates.add(reachedState);
			
			for (String string : this.alphabet)
			{
				int nextState = getNextState(string, reachedState);
				if (nextState == -1)
				{
					return false;
				}
				
				statesToProcess.push(nextState);
			}
		}
		
		return foundStates.size() == this.stateCount;
	}
	
	public DFA getComplementDFA()
	{
		DFA complement = new DFA();
		complement.setAlphabet(alphabet);
		complement.setInitialState(currentState);
		complement.setTotalNumberOfStates(stateCount);
		complement.setTransitions(transitions);
		
		// all accepting states should now be rejecting
		for (int state = 0; state < stateCount; state++)
		{
			if (!acceptingStates.contains(state))
			{
				complement.addAcceptingState(state);
			}
		}
		
		return complement;
	}
	
	public String getResultingStateTextualSequence(List<String> stringSequence)
	{
		String output = "sequence: ";
		int currentState = getCurrentState();
		int nextState = -1;
		output += "" + currentState;
		
		for (String string : stringSequence)
		{
			nextState = getNextState(string, currentState);
			output += " -" + string + "-> ";
			
			if (isAcceptingState(nextState))
			{
				output += "(" + nextState + ")";
			}
			else
			{
				output += "" + nextState;
			}
			
			currentState = nextState;
		}
		
		return output;
	}
	
	@Override
	public String toString()
	{
		String output = "";
		output += "The DFA has " + this.stateCount + " states (numerated from 0 to " + (this.stateCount - 1) + ").\n";
		output += "The DFA has " + this.acceptingStates.size() + " accepting states.\n";
		output += "Transitions are:\n";
		
		for (int state = 0; state < this.stateCount; state++)
		{
			output += "- state " + state;
			if (isAcceptingState(state))
			{
				output += " (accepting state)";
			}
			if (currentState == state)
			{
				output += " (current state)";
			}
			
			output += "\n";
			
			for (String string : this.alphabet)
			{
				output += "   -" + string + "-> " + this.transitions.get(getTransitionString(state, string)) + "\n";
			}
		}
		
		return output;
	}
	
	public String findShortestSymbolSequenceToAnAcceptingState()
	{
		if (this.acceptingStates.size() == 0)
		{
			// there are no accepting states to reach
			return null;
		}
		
		// perform a BFS
		LinkedList<BFSNode> nodeQueue = new LinkedList<BFSNode>();
		BFSNode currentNode = new BFSNode(null, currentState);
		nodeQueue.addLast(currentNode);
		HashSet<Integer> processedStates = new HashSet<Integer>();
		
		while (!nodeQueue.isEmpty() && !this.isAcceptingState(currentNode.state))
		{
			currentNode = nodeQueue.pop();
			int currentState = currentNode.state;
			processedStates.add(currentState);
			
			// add all successor states
			for (String symbol : this.alphabet)
			{
				int nextState = getNextState(symbol, currentState);
				if (processedStates.contains(nextState))
				{
					// skip already processed states
					continue;
				}
				
				BFSNode nextNode = new BFSNode(currentNode, nextState);
				nodeQueue.addLast(nextNode);
			}
		}
		
		if (!this.isAcceptingState(currentNode.state))
		{
			// the BFS could not reach an accepting state
			return null;
		}
		else
		{
			// assemble the transitions to a symbol sequence
			String symbolSequence = "";
			BFSNode nextNode = currentNode.previous;
			while (currentNode.previous != null)
			{
				String transitionSymbol = getSymbolForTransition(nextNode.state, currentNode.state);
				symbolSequence = transitionSymbol + symbolSequence;
				currentNode = nextNode;
				nextNode = currentNode.previous;
			}
			
			return symbolSequence;
		}
	}
	
	public String serialize()
	{
		String serial = "";
		
		// sym1,sym2,...;stateNumber;currentState;acceptingState1,...;init1/sym1/end1,...
		
		// add symbols
		for (int i = 0; i < alphabet.length; i++) {
			serial += alphabet[i] + ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// add state number
		serial += "" + stateCount + ";";
		
		// add current state
		serial += "" + currentState + ";";
		
		// add accepting states
		for (Integer acceptingState : acceptingStates)
		{
			serial += "" + acceptingState + ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// add transitions
		for (int state = 0; state < stateCount; state++)
		{
			for (String symbol : alphabet)
			{
				int resultingState = getNextState(symbol, state);
				serial += "" + state + "/" + symbol + "/" + resultingState + ",";
			}
		}
		serial = serial.substring(0, serial.length() - 1);
		
		return serial;
	}
	
	public static DFA deserialize(String serialization)
	{
		// sym1,sym2,...;stateNumber;currentState;acceptingState1,...;init1/sym1/end1,...
		String[] parts = serialization.split(";");
		String symbols = parts[0];
		String stateNumberString = parts[1];
		String currentStateString = parts[2];
		String acceptingStatesString = parts[3];
		String transitionsString = parts[4];
		
		// reconstruct
		String[] alphabet = symbols.split(",");
		int stateNumber = Integer.parseInt(stateNumberString);
		int currentState = Integer.parseInt(currentStateString);
		
		DFA dfa = new DFA();
		dfa.setAlphabet(alphabet);
		dfa.setTotalNumberOfStates(stateNumber);
		dfa.setInitialState(currentState);
		
		// reconstruct accepting states
		String[] acceptingStatesSplitted = acceptingStatesString.split(",");
		for (String acceptingStateS : acceptingStatesSplitted)
		{
			int acceptingState = Integer.parseInt(acceptingStateS);
			dfa.addAcceptingState(acceptingState);
		}
		
		// reconstruct transitions
		String[] transitionsSplitted = transitionsString.split(",");
		for (String splittedTransition : transitionsSplitted)
		{
			String[] transitionParts = splittedTransition.split("/");
			int startState = Integer.parseInt(transitionParts[0]);
			String symbol = transitionParts[1];
			int endState = Integer.parseInt(transitionParts[2]);
			dfa.setTransition(startState, endState, symbol);
		}
		
		return dfa;
	}
	
	private String getSymbolForTransition(int initialState, int finalState)
	{
		for (String symbol : this.alphabet)
		{
			if (this.hasTransition(initialState, finalState, symbol))
			{
				return symbol;
			}
		}
		
		return null;
	}
	
	private class BFSNode
	{
		public BFSNode previous;
		public int state;
		
		public BFSNode(BFSNode previous, int state)
		{
			this.previous = previous;
			this.state = state;
		}
	}
}
