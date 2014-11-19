package test.moore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import model.DFA;
import model.MooreAutomaton;
import model.MooreEquivalenceChecker;
import model.mySet;


public class RandomMooreGenerator {
	
	private HashSet<Integer> finishedHashes;
	private HashSet<Integer> notConnectedHashes;
	
	public RandomMooreGenerator()
	{
		this.finishedHashes = new HashSet<Integer>();
		this.notConnectedHashes = new HashSet<Integer>();
	}
	
	/**
	 * Creates a fully connected Moore automaton.
	 * State names are zero-based index numbers.
	 * Not all symbols from the output alphabet are essentially used in the final Moore automaton
	 * @param numberOfStates
	 * @param outputVectorLength
	 * @param inputAlphabet
	 * @param outputAlphabet mapping of all types to the possible values corresponding to each type
	 * @param difficultyRatio
	 * @return
	 */
	public MooreAutomaton generateUnique(
				int numberOfStates,
				int outputVectorLength,
				String[] inputAlphabet,
				HashMap<String, Set<String>> outputAlphabet,
				float difficultyRatio
			)
	{

		// predefined attributes of the generated Moore automaton
		String startState = "0";
		mySet<String> Sigma = new mySet<String>();
		for (int i = 0; i < inputAlphabet.length; i++) Sigma.add(inputAlphabet[i]);
		mySet<String> stateSet = new mySet<String>();
		for (int i = 0; i < numberOfStates; i++)
		{
			stateSet.add("" + i);
		}
		ArrayList<String> typeList = new ArrayList<String>(outputAlphabet.keySet());
		HashMap<String, Set<String>> invertedOutputTypeMap = outputAlphabet;
			
		
		int hash = 0;
		MooreAutomaton moore = new MooreAutomaton(outputVectorLength);
		moore.startState = startState;
		moore.Sigma = Sigma;
		moore.stateSet = stateSet;
		moore.setInvertedOutputTypeMap(invertedOutputTypeMap);
		moore.setTypeList(typeList);
		
		ArrayList<String[]> outputAlphabetValues = new ArrayList<String[]>(outputVectorLength);
		ArrayList<float[]> outputAlphabetDistributions = new ArrayList<float[]>(outputVectorLength);
		HashMap<String,Integer> outputTypeIndexMap = new HashMap<String, Integer>();
		int index = 0;
		for (String type : outputAlphabet.keySet()) {
			Set<String> values = outputAlphabet.get(type);
			outputTypeIndexMap.put(type, index++);
			outputAlphabetDistributions.add(determineOutputAlphabetDistribution(values.size(), difficultyRatio));
			String[] valuesArray = new String[values.size()];
			int arrayIndex = 0;
			for (String value : values)
			{
				valuesArray[arrayIndex++] = value;
			}
			
			outputAlphabetValues.add(valuesArray);
		}
			
		do {
			// create connected graph
			hash = moore.serialize().hashCode();
			while (this.finishedHashes.contains(hash) || this.notConnectedHashes.contains(hash) || !isConnectedGraph(moore)) {
				
				this.notConnectedHashes.add(hash);
				
				moore.transitionTable.clear();
				

				// try to randomly create a connected graph
				for (int stateIndex = 0; stateIndex < numberOfStates; stateIndex++) {
					String state = "" + stateIndex;
					for (int stringIndex = 0; stringIndex < inputAlphabet.length; stringIndex++) {
						int randomEndStateIndex = (int) (Math.random() * numberOfStates);
						String inputSymbol = inputAlphabet[stringIndex];
						String endState = "" + randomEndStateIndex;
						
						// the mapping delta : State x Sigma -> state
						moore.transitionTable.put(state + inputSymbol, endState);
					}
				}
				
				
				
				hash = moore.serialize().hashCode();
			}

			String serial = moore.serialize();
			hash = serial.hashCode();
		} while (this.finishedHashes.contains(hash));
		
		this.finishedHashes.add(hash);
		
		
		// output function (lambda map)
		// lambda : State -> Output 
		// assign symbol vectors to the states
		for (int stateIndex = 0; stateIndex < numberOfStates; stateIndex++)
		{
			String state = "" + stateIndex;
			ArrayList<String> output = new ArrayList<String>();
			for (String type : outputAlphabet.keySet())
			{
				int typeIndex = outputTypeIndexMap.get(type);
				String[] values = outputAlphabetValues.get(typeIndex);
				float[] distribution = outputAlphabetDistributions.get(typeIndex);
				
				float randomPercentage = (float) Math.random();
				int outputSymbolIndex = -1;
				for (int i = 0; i < distribution.length; i++) {
					if (randomPercentage <= distribution[i])
					{
						outputSymbolIndex = i;
						break;
					}
				}
				String outputSymbol = values[outputSymbolIndex];
				output.add(outputSymbol);
			}
			
			moore.lambdaMap.put(state, output);
		} 
		
		return moore;
	}
	
	/**
	 * Creates a distribution for the output alphabet. Each field has the accumulated sum of the percentage
	 * @param outputAlphabetLength
	 * @param difficulty
	 * @return
	 */
	private static float[] determineOutputAlphabetDistribution(int outputAlphabetLength, float difficulty)
	{
		assert difficulty < 1 && difficulty > 0;
		
		// create distribution with shrinking values towards the end of the alphabet
		float[] distribution = new float[outputAlphabetLength];
		for (int i = 0; i < distribution.length; i++) {
			// create linear trend in distribution
			distribution[i] = outputAlphabetLength - (i * difficulty);
			// TODO use shifted gaussian normal distribution
		}
		
		// normalize
		float sum = 0;
		for (int i = 0; i < distribution.length; i++) {
			sum += distribution[i];
		}
		for (int i = 0; i < distribution.length; i++) {
			distribution[i] /= sum;
		}
		
		// accumulate
		for (int i = 1; i < distribution.length; i++) {
			distribution[i] = distribution[i - 1] + distribution[i];
		}
		
		return distribution;
	}
	
	private static boolean isConnectedGraph(MooreAutomaton a)
	{
		int numberOfStates = a.stateSet.size();
		
		// execute BFS on graph
		HashSet<String> foundStates = new HashSet<String>(numberOfStates);
		LinkedList<String> statesToProcess = new LinkedList<String>();
		statesToProcess.add(a.startState);
		
		// try to reach each state
		while (!statesToProcess.isEmpty())
		{
			String reachedState = statesToProcess.pop();
			if (foundStates.contains(reachedState))
			{
				continue;
			}
			
			foundStates.add(reachedState);
			
			for (String symbol : a.Sigma)
			{
				String nextState = a.transitionTable.get(reachedState + symbol);
				if (!foundStates.contains(nextState))
				{
					statesToProcess.push(nextState);					
				}
			}
		}
		
		return foundStates.size() == numberOfStates;
	}
	
	
	public static void main(String[] args)
	{
		float difficultyRatio = 0.99f;
		int numberOfStates = 10;
		int outputVectorLength = 1;
		String[] inputAlphabet = {"a", "b"};
		HashMap<String,Set<String>> outputAlphabet = new HashMap<String, Set<String>>();
		Set<String> valuesA = new HashSet<String>();
		valuesA.add("X");
		valuesA.add("Y");
		valuesA.add("Z");
		outputAlphabet.put("typeA", valuesA);
		Set<String> valuesB = new HashSet<String>();
		valuesB.add("Q");
		valuesB.add("O");
		outputAlphabet.put("typeB", valuesB);
		
		RandomMooreGenerator generator = new RandomMooreGenerator();
		MooreAutomaton moore = generator.generateUnique(numberOfStates, outputVectorLength, inputAlphabet, outputAlphabet, difficultyRatio);
		System.out.println(moore);
	}
}
