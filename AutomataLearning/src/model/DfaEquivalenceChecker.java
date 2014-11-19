package model;

import java.util.HashMap;


public class DfaEquivalenceChecker {
	
	private DFA minimalOriginalDFA;
	private DFA minimalOriginalDFAComplement;
	
	public DfaEquivalenceChecker(DFA originalDFA)
	{
		this.minimalOriginalDFA = minimizeDFA(originalDFA);
		this.minimalOriginalDFAComplement = this.minimalOriginalDFA.getComplementDFA();
	}

	private static DFA minimizeDFA(DFA dfa)
	{
		return dfa;
	}
	
	public String findCounterexampleForEquivalence(DFA dfa1)
	{
		DFA dfa2 = this.minimalOriginalDFA;
		// (1. minimize) -- not necessary because learned DFAs are always minimal
		// 2. check whether the automata are isomorphic and find a counterexample if not
		// calculate a merged DFA that shows the differences between the DFAs
		
		// intersect DFA 1 and complement of DFA 2
		DFA dfa2Complement = this.minimalOriginalDFAComplement;
		DFA intersection1Not2 = mergeAutomata(dfa1, dfa2Complement, false);
		
		// intersect DFA 2 and complement of DFA 1
		DFA dfa1Complement = dfa1.getComplementDFA();
		DFA intersection2Not1 = mergeAutomata(dfa1Complement, dfa2, false);
		
		// check whether the one of the intersections has an accepted state that can be reached
		String shortestWrongSequence1 = intersection1Not2.findShortestSymbolSequenceToAnAcceptingState();
		String shortestWrongSequence2 = intersection2Not1.findShortestSymbolSequenceToAnAcceptingState();
		
		// provide the shortest counterexample
		String counterexample = null;
		if (shortestWrongSequence1 == null && shortestWrongSequence2 != null)
		{
			counterexample = shortestWrongSequence2;
		}
		else if (shortestWrongSequence2 == null && shortestWrongSequence1 != null)
		{
			counterexample = shortestWrongSequence1;
		}
		else if (shortestWrongSequence1 != null && shortestWrongSequence2 != null)
		{
			if (shortestWrongSequence1.length() <= shortestWrongSequence2.length())
			{
				counterexample = shortestWrongSequence1;
			}
			else
			{
				counterexample = shortestWrongSequence2;
			}
		}
		
		return counterexample;
	}
	
	/**
	 * 
	 * @param dfa1
	 * @param dfa2
	 * @param isUNION if TRUE than the UNION of the automata is calculate, if FALSE the INTERSECTION of the automata is calculated
	 * @return
	 */
	private DFA mergeAutomata(DFA dfa1, DFA dfa2, boolean isUNION)
	{
		// algorithm: page 18 in algorithms for testing equivalence of finite automata
		DFA union = new DFA();
		union.setAlphabet(dfa1.getAlphabet());
		int unionStates = dfa1.getNumberOfStates() * dfa2.getNumberOfStates();
		union.setTotalNumberOfStates(unionStates);
		
		// create a mapping for the old states to the new states
		HashMap<String,Integer> unionStateMapping = new HashMap<String, Integer>();
		int unionStateCounter = 0;
		for (int state1 = 0; state1 < dfa1.getNumberOfStates(); state1++)
		{
			for (int state2 = 0; state2 < dfa2.getNumberOfStates(); state2++)
			{
				unionStateMapping.put("" + state1 + "|" + state2, unionStateCounter++);
			}
		}
		
		// the start state is (startState1|startState2)
		int startState1 = dfa1.getCurrentState();
		int startState2 = dfa2.getCurrentState();
		int unionStartState = unionStateMapping.get("" + startState1 + "|" + startState2);
		union.setInitialState(unionStartState);
		
		// calculate the transitions
		for (int state1 = 0; state1 < dfa1.getNumberOfStates(); state1++)
		{
			for (int state2 = 0; state2 < dfa2.getNumberOfStates(); state2++)
			{
				int unionInitialState = unionStateMapping.get("" + state1 + "|" + state2);
				
				for (int alphabetIndex = 0; alphabetIndex < union.getAlphabet().length; alphabetIndex++)
				{
					String symbol = union.getAlphabet()[alphabetIndex];
					
					// transmission rule:
					// delta((q1,q2), a) = (delta1(q1, a), delta2(q2, a))
					// a is symbol, q1 is a state from DFA1 and q2 is a state from DFA2
					int resultingState1 = dfa1.getNextState(symbol, state1);
					int resultingState2 = dfa2.getNextState(symbol, state2);
					int unionResultingState = unionStateMapping.get("" + resultingState1 + "|" + resultingState2);
					union.setTransition(unionInitialState, unionResultingState, symbol);
				}
			}
		}
		
		// calculate the accepting states
		// each union state is accepting were one auf the automata is accepting
		for (int state1 = 0; state1 < dfa1.getNumberOfStates(); state1++)
		{
			for (int state2 = 0; state2 < dfa2.getNumberOfStates(); state2++)
			{				
				// check whether the union or the intersection should be
				// calculated:
				if (isUNION) {
					if (dfa1.isAcceptingState(state1) || dfa2.isAcceptingState(state2)) {
						int unionState = unionStateMapping.get("" + state1 + "|" + state2);
						union.addAcceptingState(unionState);
					}
				} else {
					if (dfa1.isAcceptingState(state1) && dfa2.isAcceptingState(state2)) {
						int unionState = unionStateMapping.get("" + state1 + "|" + state2);
						union.addAcceptingState(unionState);
					}
				}
			}
		}
		
		return union;
	}
}
