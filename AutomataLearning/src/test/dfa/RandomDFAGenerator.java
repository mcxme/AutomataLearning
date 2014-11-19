package test.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import model.DFA;
import model.DfaEquivalenceChecker;


public class RandomDFAGenerator {

	private HashSet<Integer> finishedHashes;
	private HashSet<Integer> notConnectedHashes;
	
	public RandomDFAGenerator()
	{
		this.finishedHashes = new HashSet<Integer>();
		this.notConnectedHashes = new HashSet<Integer>();
	}
	
	public DFA generateUnique(int numberOfStates, String[] alphabet, float acceptingStatesPercentage)
	{

		int hash = 0;
		DFA dfa = null;
		do {
			dfa = new DFA();
			dfa.setTotalNumberOfStates(numberOfStates);
			dfa.setInitialState(0);
			dfa.setAlphabet(alphabet);

			// randomize accepting states based on percentage
			ArrayList<Integer> acceptingStates = new ArrayList<Integer>(numberOfStates);
			for (int state = 0; state < numberOfStates; state++) {
				acceptingStates.add(state);
			}
			Collections.shuffle(acceptingStates);
			for (int i = 0; i <= (int) (numberOfStates * acceptingStatesPercentage); i++) {
				dfa.addAcceptingState(acceptingStates.get(i));
			}

			// create connected graph
//			int tries = 0;
			hash = dfa.serialize().hashCode();
			while (this.finishedHashes.contains(hash) || this.notConnectedHashes.contains(hash) || !dfa.isConnectedGraph()) {
				// System.out.println("trying for the " + (tries++) +
				// ". time to connect a graph randomly");
				this.notConnectedHashes.add(hash);
				dfa.removeAllTransitions();

				// try to randomly create a connected graph
				for (int state = 0; state < numberOfStates; state++) {
					for (int stringIndex = 0; stringIndex < alphabet.length; stringIndex++) {
						int randomEndState = (int) (Math.random() * numberOfStates);
						String string = alphabet[stringIndex];

						dfa.setTransition(state, randomEndState, string);
					}
				}
				
				hash = dfa.serialize().hashCode();
			}

			String serial = dfa.serialize();
			hash = serial.hashCode();
		} while (this.finishedHashes.contains(hash));
		
		this.finishedHashes.add(hash);
		return dfa;
	}
	
	public static DFA generate(int numberOfStates, String[] alphabet, float acceptingStatesPercentage)
	{
		DFA dfa = new DFA();
		dfa.setTotalNumberOfStates(numberOfStates);
		dfa.setInitialState(0);
		dfa.setAlphabet(alphabet);
		
		// randomize accepting states based on percentage
		ArrayList<Integer> acceptingStates = new ArrayList<Integer>(numberOfStates);
		for (int state = 0; state < numberOfStates; state++)
		{
			acceptingStates.add(state);
		}
		Collections.shuffle(acceptingStates);
		for (int i = 0; i < (int)(numberOfStates * acceptingStatesPercentage); i++)
		{
			dfa.addAcceptingState(acceptingStates.get(i));
		}
		
		// create fully connected graph
		int tries = 0;
		while (!dfa.isConnectedGraph())
		{
//			System.out.println("trying for the " + (tries++) + ". time to connect a graph randomly");
			dfa.removeAllTransitions();
			
			// try to randomly create a connected graph
			for (int state = 0; state < numberOfStates; state++)
			{
				for (int stringIndex = 0; stringIndex < alphabet.length; stringIndex++)
				{
					int randomEndState = (int)(Math.random() * numberOfStates);
					String string = alphabet[stringIndex];
					
					dfa.setTransition(state, randomEndState, string);
				}
			}
		}
		
		return dfa;
	}
	
	public static void main(String[] args)
	{
		DFA randomDfa = generate(100, new String[] {"a", "b"}, 0.3f);
		System.out.println(randomDfa.serialize());
//		System.out.println(randomDfa);
	}
	
}
