package test.dfa;

import model.DFA;


public class DFAManualTest {

	private static final String[] defaultAlphabet = {"a", "b", "c"};
	
	public static DFA getCircularDFA(int stateNumber)
	{
		DFA dfa = new DFA();
		dfa.setAlphabet(defaultAlphabet);
		dfa.setTotalNumberOfStates(stateNumber);
		dfa.setInitialState(0);
		dfa.addAcceptingState(0);
		
		for (int initialState = 0; initialState < stateNumber; initialState++)
		{
			int nextState = (initialState + 1) % (stateNumber);
			for (String string : defaultAlphabet)
			{
				dfa.setTransition(initialState, nextState, string);
			}
		}
		
		return dfa;
	}
	
	
	public static void main(String[] args)
	{
		DFA dfa = getCircularDFA(5);
		System.out.println(dfa);

		String serial = dfa.serialize();
		System.out.println(serial);
		
		DFA deserialized = DFA.deserialize(serial);
		System.out.println(deserialized);
	}
}
