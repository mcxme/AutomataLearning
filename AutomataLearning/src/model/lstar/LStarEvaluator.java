package model.lstar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import model.DFA;
import test.dfa.DFAGeneratorThread;
import test.dfa.RandomDFAGenerator;


public class LStarEvaluator {

	private static final String[] STANDARD_ALPHABET = new String[] {"a", "b", "c", "d", "e", "1", "2", "3", "4", "5"};
	private static float STANDARD_ACCEPTING_PERCENTAGE = 0.5f;
	
	private static final boolean PRINT_LEARNED_DFA = true;
	
	public LStarEvaluator()
	{
	}
	
	public void runAcceptingPercentageEvaluation(int maxAlphabetSize, int alphabetStepSize, int alphabetStart, int maxStateSize, int stateStepSize, int stateStart, int numberOfRuns, int outputAfterTurns, boolean partialEvaluation)
	{
		STANDARD_ACCEPTING_PERCENTAGE = 0.01f;
		System.out.println("accepting ration: " + STANDARD_ACCEPTING_PERCENTAGE);
		runEvaluation(maxAlphabetSize, alphabetStepSize, alphabetStart, maxStateSize, stateStepSize, stateStart, numberOfRuns, outputAfterTurns, partialEvaluation);
		
		for (float percentage = 0.1f; percentage < 0.99f; percentage += 0.1f)
		{
			STANDARD_ACCEPTING_PERCENTAGE = percentage;
			System.out.println("accepting ration: " + STANDARD_ACCEPTING_PERCENTAGE);
			runEvaluation(maxAlphabetSize, alphabetStepSize, alphabetStart, maxStateSize, stateStepSize, stateStart, numberOfRuns, outputAfterTurns, partialEvaluation);
		}
		
		STANDARD_ACCEPTING_PERCENTAGE = 0.95f;
		System.out.println("accepting ration: " + STANDARD_ACCEPTING_PERCENTAGE);
		runEvaluation(maxAlphabetSize, alphabetStepSize, alphabetStart, maxStateSize, stateStepSize, stateStart, numberOfRuns, outputAfterTurns, partialEvaluation);
		
	}
	
	public void runEvaluation(int maxAlphabetSize, int alphabetStepSize, int alphabetStart, int maxStateSize, int stateStepSize, int stateStart, int numberOfRuns, int outputAfterTurns, boolean partialEvaluation)
	{
		int alphabetSteps = maxAlphabetSize / alphabetStepSize;
		int stateSteps = maxStateSize / stateStepSize;
		long[][] totalTime = new long[alphabetSteps][stateSteps];
		long[][] totalMembershipQueries = new long[alphabetSteps][stateSteps];
		long[][] totalEquivalenceQueries = new long[alphabetSteps][stateSteps];
		long[][] totalQueryRatio = new long[alphabetSteps][stateSteps];
		int[][] totalDfaSize = new int[alphabetSteps][stateSteps];
		long[][] totalConsistentErrors = new long[alphabetSteps][stateSteps];
		long[][] totalCompleteErrors = new long[alphabetSteps][stateSteps];
		
		String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3",
				"4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
				"Z"};
		float acceptingPercentage = STANDARD_ACCEPTING_PERCENTAGE;
		for (int alphabetSize = alphabetStart; alphabetSize <= maxAlphabetSize; alphabetSize += alphabetStepSize)
		{
			String[] currentAlphabet = Arrays.copyOf(alphabet, alphabetSize);
			for (int stateSize = stateStart; stateSize <= maxStateSize; stateSize += stateStepSize)
			{
				RandomDFAGenerator generator = new RandomDFAGenerator();
				for (int runCount = 0; runCount < numberOfRuns; runCount++)
				{
					DFA randomDFA = generator.generateUnique(stateSize, currentAlphabet, acceptingPercentage);
					if (runCount % outputAfterTurns == 0)
					{						
						System.out.print(">");
					}
					Teacher teacher = new Teacher(randomDFA);
					Learner learner = new Learner(teacher, currentAlphabet);
					DFA learned = null;
					try
					{
						learned = learner.learn();						
					}
					catch (AssertionError e)
					{
						System.out.println("\nserialized DFA:\n" + randomDFA.serialize());
						throw e;
					}
					long membershipQueries = teacher.getNumberOfMembershipQueries();
					long equivalenceQueries = teacher.getNumberOfEquivalenceQueries();
					int dfaSize = learned.getNumberOfStates();
					double time = learner.endLearningTimestamp - learner.startLearningTimestamp;
					int alphabetField = (alphabetSize / alphabetStepSize) - 1;
					int stateField = (stateSize / stateStepSize) - 1;
					totalTime[alphabetField][stateField] += time;
					totalMembershipQueries[alphabetField][stateField] += membershipQueries;
					totalEquivalenceQueries[alphabetField][stateField] += teacher.getNumberOfEquivalenceQueries();
					totalQueryRatio[alphabetField][stateField] += membershipQueries / equivalenceQueries;
					totalDfaSize[alphabetField][stateField] += dfaSize;
					totalCompleteErrors[alphabetField][stateField] += learner.completeErrors;
					totalConsistentErrors[alphabetField][stateField] += learner.consistentErrors;
					
					if (runCount % outputAfterTurns == 0)
					{						
						if ((runCount / outputAfterTurns) % 10 == 9)
						{
							System.out.print("+");
							
							if (partialEvaluation)
							{
								// partial evaluation
								System.out.println("\npartial evaluation : " + runCount + "/" + numberOfRuns);
								int j = stateSize / stateStepSize - 1;
								int i = alphabetSize / alphabetStepSize - 1;
								long averageMembershipQueries = totalMembershipQueries[i][j] / (long)runCount;
								int averageDfaSize = totalDfaSize[i][j] / runCount;
								float averageEquivalenceQueries = (float)totalEquivalenceQueries[i][j] / (float)runCount;
								int averageQueryRatio = (int)(totalQueryRatio[i][j] / runCount);
								float averageTime = ((float)totalTime[i][j] / ((float)runCount * 1000 * 60));
								long averageConsistentErrors = totalConsistentErrors[i][j]/runCount;
								long averageCompleteErrors = totalCompleteErrors[i][j]/runCount;
								System.out.println("[S=" + stateSize + "|A=" + alphabetSize + "] = "
										+ averageTime + " minutes, "
										+ averageMembershipQueries + " MQ, "
										+ averageEquivalenceQueries + " EQ, " 
										+ averageQueryRatio + " EQ/MQ, "
										+ " consistent errors" + averageConsistentErrors
										+ ", complete errors " + averageCompleteErrors
										+ ", learned DFA state size " + averageDfaSize + "\n");
							}
						}
						else
						{
							System.out.print("|");							
						}
					}
				}
				System.out.print("\n");
				System.out.println("finished " + numberOfRuns + " rounds of learning an automaton with " + stateSize + " states and " + alphabetSize + " symbols.");
				
				int j = stateSize / stateStepSize - 1;
				int i = alphabetSize / alphabetStepSize - 1;
				long averageMembershipQueries = totalMembershipQueries[i][j] / (long)numberOfRuns;
				int averageDfaSize = totalDfaSize[i][j] / numberOfRuns;
				float averageEquivalenceQueries = (float)totalEquivalenceQueries[i][j] / (float)numberOfRuns;
				int averageQueryRatio = (int)(totalQueryRatio[i][j] / numberOfRuns);
				float averageTime = ((float)totalTime[i][j] / ((float)numberOfRuns * 1000 * 60));
				long averageConsistentErrors = totalConsistentErrors[i][j]/numberOfRuns;
				long averageCompleteErrors = totalCompleteErrors[i][j]/numberOfRuns;
				System.out.println("[S=" + stateSize + "|A=" + alphabetSize + "] = " + averageTime
						+ " minutes, " + averageMembershipQueries + " MQ, "
						+ averageEquivalenceQueries + " EQ, " + averageQueryRatio + " EQ/MQ, "
						+ " consistent errors" + averageConsistentErrors + ", complete errors "
						+ averageCompleteErrors + "learned DFA state size " + averageDfaSize);
			}
		}
	}
	
	public void runEvaluationParallel(int maxAlphabetSize, int alphabetStepSize, int alphabetStart, int maxStateSize, int stateStepSize, int stateStart, int numberOfRuns, int outputAfterTurns, boolean partialEvaluation)
	{
		int alphabetSteps = maxAlphabetSize / alphabetStepSize;
		int stateSteps = maxStateSize / stateStepSize;
		long[][] totalTime = new long[alphabetSteps][stateSteps];
		long[][] totalMembershipQueries = new long[alphabetSteps][stateSteps];
		long[][] totalEquivalenceQueries = new long[alphabetSteps][stateSteps];
		long[][] totalQueryRatio = new long[alphabetSteps][stateSteps];
		int[][] totalDfaSize = new int[alphabetSteps][stateSteps];
		long[][] totalTableEntries = new long[alphabetSteps][stateSteps];
		
		String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3",
				"4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
				"Z"};
		float acceptingPercentage = STANDARD_ACCEPTING_PERCENTAGE;
		for (int alphabetSize = alphabetStart; alphabetSize <= maxAlphabetSize; alphabetSize += alphabetStepSize)
		{
			String[] currentAlphabet = Arrays.copyOf(alphabet, alphabetSize);
			for (int stateSize = stateStart; stateSize <= maxStateSize; stateSize += stateStepSize)
			{
				RandomDFAGenerator generator = new RandomDFAGenerator();
				LStarEvaluationThread[] runnables = new LStarEvaluationThread[numberOfRuns];
				Thread[] threads = new Thread[numberOfRuns];
				for (int runCount = 0; runCount < numberOfRuns; runCount++)
				{
					DFA randomDFA = generator.generateUnique(stateSize, currentAlphabet, acceptingPercentage);
					
					LStarEvaluationThread learningRunnable = new LStarEvaluationThread(randomDFA, currentAlphabet);
					Thread thread = new Thread(learningRunnable);
					runnables[runCount] = learningRunnable;
					threads[runCount] = thread;
					thread.start();
					
					if (runCount % outputAfterTurns == 0)
					{						
						System.out.print(">");
					}
				}
				
				System.out.println();
				
				// join the threads;
				for (int i = 0; i < numberOfRuns; i++)
				{
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (i % outputAfterTurns == 0)
					{						
						if ((i / outputAfterTurns) % 10 == 9)
						{
							System.out.print("+");
							if (partialEvaluation)
							{
								// partial evaluation
								System.out.println("\npartial evaluation : " + i + "/" + numberOfRuns);
								int y = stateSize / stateStepSize - 1;
								int x = alphabetSize / alphabetStepSize - 1;
								int averageMembershipQueries = (int)(totalMembershipQueries[x][y] / i);
								int averageDfaSize = totalDfaSize[x][y] / i;
								float averageEquivalenceQueries = (float)totalEquivalenceQueries[x][y] / (float)i;
								int averageQueryRatio = (int)(totalQueryRatio[x][y] / i);
								float averageTime = ((float)totalTime[x][y] / ((float)i * 1000 * 60));
								System.out.println("[S=" + stateSize + "|A=" + alphabetSize + "] = "
										+ averageTime + " minutes, "
										+ averageMembershipQueries + " MQ, "
										+ averageEquivalenceQueries + " EQ, " 
										+ averageQueryRatio + " EQ/MQ, "
										+ "learned DFA state size " + averageDfaSize + "\n");
							}
						}
						else
						{
							System.out.print("|");							
						}
					}
					
					int x = (alphabetSize / alphabetStepSize) - 1;
					int y = (stateSize / stateStepSize) - 1;
					totalTime[x][y] += runnables[i].timeMillisDelta;
					totalMembershipQueries[x][y] += runnables[i].membershipQueries;
					totalEquivalenceQueries[x][y] += runnables[i].equivalenceQueries;
					totalDfaSize[x][y] += runnables[i].learnedDfaStateSize;
					totalTableEntries[x][y] += runnables[i].tableEntries;
					totalQueryRatio[x][y] += runnables[i].membershipQueriesPerEquivalenceQuery;
				}
				
				System.out.print("\n");
				System.out.println("finished " + numberOfRuns + " rounds of learning an automaton with " + stateSize + " states and " + alphabetSize + " symbols.");
				
				int j = stateSize / stateStepSize - 1;
				int i = alphabetSize / alphabetStepSize - 1;
				int averageMembershipQueries = (int)(totalMembershipQueries[i][j] / numberOfRuns);
				int averageDfaSize = totalDfaSize[i][j] / numberOfRuns;
				float averageEquivalenceQueries = (float)totalEquivalenceQueries[i][j] / (float)numberOfRuns;
				int averageQueryRatio = (int)(totalQueryRatio[i][j] / numberOfRuns);
				long averageTableEntries = totalTableEntries[i][j] / numberOfRuns;
				float averageTime = ((float)totalTime[i][j] / ((float)numberOfRuns * 1000 * 60));
				System.out.println("[S=" + stateSize + "|A=" + alphabetSize + "] = "
						+ averageTime + " minutes, "
						+ averageMembershipQueries + " MQ, "
						+ averageEquivalenceQueries + " EQ, " 
						+ averageQueryRatio + " EQ/MQ, "
						+ "learned DFA state size " + averageDfaSize
						+ " observation table entries: " + averageTableEntries);
			}
		}
		
		System.out.println("Evaluation [state size | alphabet size] = average time , average membership queries");
		for (int i = 0; i < alphabetSteps; i++) {
			for (int j = 0; j < stateSteps; j++) {
				int averageMembershipQueries = (int)(totalMembershipQueries[i][j] / numberOfRuns);
				float averageTime = ((float)totalTime[i][j] / (float)numberOfRuns);
				System.out.println("[" + ((j + 1) * stateStepSize) + "|" + ((i + 1) * alphabetStepSize) + "] = " + averageTime + " seconds, " + averageMembershipQueries + " MQ");
			}
		}
	}
	
	public void runEvaluationParallel(int maxAlphabetSize, int alphabetStepSize, int alphabetStart, int maxStateSize, int stateStepSize, int stateStart, int numberOfRuns, int numberOfDFAWorkers, int numberOfDFAConsumers)
	{		
		String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3",
				"4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
				"Z"};
		float acceptingPercentage = STANDARD_ACCEPTING_PERCENTAGE;
		for (int alphabetSize = alphabetStart; alphabetSize <= maxAlphabetSize; alphabetSize += alphabetStepSize)
		{
			String[] currentAlphabet = Arrays.copyOf(alphabet, alphabetSize);
			for (int stateSize = stateStart; stateSize <= maxStateSize; stateSize += stateStepSize)
			{
				// start the producers
				int totalDFAs = 0;
				BlockingQueue<DFA> queue = new LinkedBlockingQueue<DFA>(numberOfRuns);
				for (int i = 0; i < numberOfDFAWorkers - 1; i++) {
					int dfasToProduce = numberOfRuns / numberOfDFAWorkers;
					totalDFAs += dfasToProduce;
					Thread producer = new Thread(new DFAGeneratorThread(stateSize, currentAlphabet, acceptingPercentage, dfasToProduce, queue));
					producer.start();
				}
				Thread producer = new Thread(new DFAGeneratorThread(stateSize, currentAlphabet, acceptingPercentage, numberOfRuns - totalDFAs, queue));
				producer.start();
				
				int totalLearners = 0;
				LinkedList<Thread> consumers = new LinkedList<Thread>();
				LinkedList<LStarConsumerThread> runnables = new LinkedList<LStarConsumerThread>();
				for (int i = 0; i < numberOfDFAConsumers; i++) {
					int dfasToConsume = numberOfRuns / numberOfDFAConsumers;
					totalLearners += dfasToConsume;
					LStarConsumerThread runnable = new LStarConsumerThread(queue, currentAlphabet, dfasToConsume);
					Thread consumer = new Thread(runnable);
					consumers.add(consumer);
					runnables.add(runnable);
					consumer.start();
				}
				LStarConsumerThread runnable = new LStarConsumerThread(queue, currentAlphabet, numberOfRuns - totalLearners);
				Thread consumer = new Thread(runnable);
				consumers.add(consumer);
				runnables.add(runnable);
				consumer.start();
				
				for (Thread thread : consumers)
				{
					try {
						thread.join();
					} catch (InterruptedException e) {
					}
				}
				
				System.out.print("\n");
				System.out.println("finished " + numberOfRuns + " rounds of learning an automaton with " + stateSize + " states and " + alphabetSize + " symbols.");
				
				long totalMembershipQueries = 0;
				long totalEquivalenceQueries = 0;
				for (LStarConsumerThread r : runnables)
				{
					for (int i = 0; i < r.equivalenceQueries.length; i++)
					{
						totalMembershipQueries += r.membershipQueries[i];
						totalEquivalenceQueries += r.equivalenceQueries[i];
					}
				}
				
				int averageMembershipQueries = (int)(totalMembershipQueries / numberOfRuns);
				float averageEquivalenceQueries = (float)totalEquivalenceQueries / (float)numberOfRuns;
				System.out.println("[S=" + stateSize + "|A=" + alphabetSize + "] = "
						+ averageMembershipQueries + " MQ, "
						+ averageEquivalenceQueries + " EQ, ");
			}
		}
	}
	
	public void runSingleRandomLearning(int states)
	{
		// 0. prepare general variables
		int numberOfStates = states;
		String[] alphabet = STANDARD_ALPHABET;
		float percentageOfAcceptingStates = STANDARD_ACCEPTING_PERCENTAGE;
		
		// 1. create random DFA
		DFA randomDFA = RandomDFAGenerator.generate(numberOfStates, alphabet, percentageOfAcceptingStates);
		System.out.println("Serialized DFA:");
		System.out.println(randomDFA.serialize());
		
		// 2. create teacher and run the learner
		Teacher teacher = new Teacher(randomDFA);
		Learner learner = new Learner(teacher, alphabet);
		System.out.println("started to learn...");
		DFA learnedDfa = learner.learn();
		
		// 3. evaluate the results
		printSingleLearning(learner, teacher, learnedDfa, numberOfStates, alphabet);
	}
	
	public void runSingleLearningKnownDfaWith3States()
	{
		// 1. create the DFA from the example of Chen's thesis
		DFA dfa = new DFA();
		dfa.setAlphabet(STANDARD_ALPHABET);
		dfa.setInitialState(0);
		dfa.setTotalNumberOfStates(3);
		dfa.addAcceptingState(0);
		dfa.addAcceptingState(2);
		dfa.setTransition(0, 0, "a");
		dfa.setTransition(0, 1, "b");
		dfa.setTransition(1, 0, "a");
		dfa.setTransition(1, 2, "b");
		dfa.setTransition(2, 1, "a");
		dfa.setTransition(2, 0, "b");
		
		// 2. create teacher and run the learner
		Teacher teacher = new Teacher(dfa);
		Learner learner = new Learner(teacher, STANDARD_ALPHABET);
		System.out.println("started to learn...");
		DFA learnedDfa = learner.learn();
		
		// 3. evaluate the results
		printSingleLearning(learner, teacher, learnedDfa, 3, STANDARD_ALPHABET);
	}
	
	public void runSingleLearningKnownDfaAccept1Counter()
	{
		// 1. create the DFA from the example of Chen's thesis
		DFA dfa = new DFA();
		dfa.setAlphabet(STANDARD_ALPHABET);
		dfa.setInitialState(0);
		dfa.setTotalNumberOfStates(3);
		dfa.addAcceptingState(1);
		dfa.setTransition(0, 0, "a");
		dfa.setTransition(0, 1, "b");
		dfa.setTransition(1, 1, "a");
		dfa.setTransition(1, 2, "b");
		dfa.setTransition(2, 2, "a");
		dfa.setTransition(2, 2, "b");
		
		// 2. create teacher and run the learner
		Teacher teacher = new Teacher(dfa);
		Learner learner = new Learner(teacher, STANDARD_ALPHABET);
		System.out.println("started to learn...");
		DFA learnedDfa = learner.learn();
		
		// 3. evaluate the results
		printSingleLearning(learner, teacher, learnedDfa, 3, STANDARD_ALPHABET);
	}
	
	public void runSingleLearningFromSerialization(String serial)
	{
		DFA dfa = DFA.deserialize(serial);
		System.out.println("deserialized DFA:");
		System.out.println(dfa);
		
		// 2. create teacher and run the learner
		Teacher teacher = new Teacher(dfa);
		Learner learner = new Learner(teacher, dfa.getAlphabet());
		System.out.println("Started to learn...");
		DFA learnedDFA = learner.learn();
		
		// 3. evaluation
		printSingleLearning(learner, teacher, learnedDFA, dfa.getNumberOfStates(), dfa.getAlphabet());
	}
	
	private void printSingleLearning(Learner learner, Teacher teacher, DFA learnedDfa, int numberOfStates, String[] alphabet)
	{
		float seconds = (float)(learner.endLearningTimestamp - learner.startLearningTimestamp) / 1000f;
		if (PRINT_LEARNED_DFA)
		{
			System.out.println("======== learned DFA ===========");
			System.out.println(learnedDfa);
		}
		System.out.println("================================");
		System.out.println("Evaluation for the L* algorithm:");
		System.out.println("- Size: the DFA had " + numberOfStates + " states and an alphabet of size " + alphabet.length);
		System.out.println("- Time: it took " + seconds + " seconds to learn the DFA");
		System.out.println("- Performance:");
		System.out.println("      + number of membership queries  = " + teacher.getNumberOfMembershipQueries());
		System.out.println("      + number of equivalence queries = " + teacher.getNumberOfEquivalenceQueries());
		System.out.println("================================");
	}
	
	public void runMultipleRandomLearning(int numberOfStates, int numberOfRuns)
	{
		// 0. prepare general variables
		String[] alphabet = STANDARD_ALPHABET;
		float percentageOfAcceptingStates = 0.3f;
		
		long totalNumberOfMembershipQueries = 0;
		int totalNumberOfEquivalenceQueries = 0;
		float totalTimeInSeconds = 0;
		
		for (int i = 0; i < numberOfRuns; i++)
		{
			System.out.print("" + (100 * i / numberOfRuns) + "%" + "\r");
			
			// 1. create random DFA
			DFA randomDFA = RandomDFAGenerator.generate(numberOfStates, alphabet, percentageOfAcceptingStates);
//			System.out.println("Serialized DFA:");
//			System.out.println(randomDFA.serialize());
			
			// 2. create teacher and run the learner
			Teacher teacher = new Teacher(randomDFA);
			Learner learner = new Learner(teacher, alphabet);
			DFA learnedDfa = learner.learn();
			
			float seconds = (float)(learner.endLearningTimestamp - learner.startLearningTimestamp) / 1000f;
			totalTimeInSeconds += seconds;
			totalNumberOfMembershipQueries += teacher.getNumberOfMembershipQueries();
			totalNumberOfEquivalenceQueries += teacher.getNumberOfEquivalenceQueries();
		}
		
		int averageNumberOfMembershipQueries = (int) (totalNumberOfMembershipQueries / numberOfRuns);
		int averageNumberOfEquivalenceQueries = totalNumberOfEquivalenceQueries / numberOfRuns;
		float averageTimeInSeconds = totalTimeInSeconds / (float)numberOfRuns;
		
		
		System.out.println("================================");
		System.out.println("Evaluation for the L* algorithm:");
		System.out.println("- Size: the DFA(s) had " + numberOfStates + " states and an alphabet of size " + alphabet.length);
		System.out.println("- Time: it took " + totalTimeInSeconds + " seconds to learn the " + numberOfRuns + " DFA(s)");
		System.out.println("- Performance (total):");
		System.out.println("      + number of membership queries in total  = " + totalNumberOfMembershipQueries);
		System.out.println("      + number of equivalence queries in total = " + totalNumberOfEquivalenceQueries);
		System.out.println("- Performance (taverage):");
		System.out.println("      + number of membership queries in average  = " + averageNumberOfMembershipQueries);
		System.out.println("      + number of equivalence queries in average = " + averageNumberOfEquivalenceQueries);
		System.out.println("      + time in average = " + averageTimeInSeconds + " seconds");
		System.out.println("================================");
	}
	
	private void runEvaluation(int alphabetSize, int stateSize, int runs, int outputInterval)
	{
		this.runEvaluation(alphabetSize, 1, alphabetSize, stateSize, 1, stateSize, runs, outputInterval, false);
	}
	
	private void runEvaluationParallel(int alphabetSize, int stateSize, int runs, int outputInterval)
	{
		this.runEvaluationParallel(alphabetSize, 1, alphabetSize, stateSize, 1, stateSize, runs, outputInterval, false);
	}
	
	private void runState10Alphabet5()
	{
		// build the dfa
		DFA dfa = new DFA();
		String[] alphabet = new String[] {"a", "b", "c", "d", "e"};
		dfa.setAlphabet(alphabet);
		dfa.setTotalNumberOfStates(10);
		dfa.setInitialState(0);
		dfa.addAcceptingState(9);
		
		// add transitions for input a to successor
		for (int i = 0; i < 10; i++)
		{
			dfa.setTransition(i, (i + 1) % 10, "a");
		}
		
		// add transtions for b to previous if odd or to self if even
		for (int i = 0; i < 10; i++) {
			if (i % 2 == 0) dfa.setTransition(i, i, "b");
			else dfa.setTransition(i, i - 1, "b");
		}
		
		// add transitions for c to second previous state
		for (int i = 1; i < 10; i++) {
			if (i % 2 == 0) dfa.setTransition(i, i - 2, "c");
			else dfa.setTransition(i, (i + 2) % 10, "c");
		}
		dfa.setTransition(0, 8, "c");
		
		// add transitions for d arbitrary
		dfa.setTransition(0, 3, "d");
		dfa.setTransition(1, 1, "d");
		dfa.setTransition(2, 0, "d");
		dfa.setTransition(3, 4, "d");
		dfa.setTransition(4, 0, "d");
		dfa.setTransition(5, 3, "d");
		dfa.setTransition(6, 8, "d");
		dfa.setTransition(7, 9, "d");
		dfa.setTransition(8, 4, "d");
		dfa.setTransition(9, 9, "d");
		
		// add transitions for e to half of the current state (floor)
		for (int i = 0; i < 10; i++) {
			dfa.setTransition(i, i / 2, "e");
		}
		
		// learn the dfa
		Teacher teacher = new Teacher(dfa);
		Learner learner = new Learner(teacher, alphabet);
		DFA learnedDfa = learner.learn();
		
		System.out.println(learnedDfa);
	}
	
	public static void main(String[] args)
	{
		LStarEvaluator eval = new LStarEvaluator();
		
		// correct:
//		eval.runState10Alphabet5();
//		eval.runMultipleRandomLearning(10, 1000);
		/* alphabet MAX, alphabet step, alphabet MIN, state MAX, state step, state MIN, runs, output each X runs */

//		STANDARD_ACCEPTING_PERCENTAGE = 0.01f;
		System.out.println("r = " + STANDARD_ACCEPTING_PERCENTAGE);
		eval.runEvaluation(32, 5, 32, 664, 1, 664, 10, 1, true);
//		eval.runEvaluationParallel(32, 1, 32, 650, 50, 650, 10, 1, 4);
//		eval.runAcceptingPercentageEvaluation(10, 5, 10, 75, 25, 25, 1000, 1, false);
//		eval.runSingleRandomLearning(300);
//		eval.runEvaluation(50, 5, 5, 100, 5, 5, 100, 1, false);
//		eval.runState10Alphabet5();
		
//		eval.runSingleLearningKnownDfaAccept1Counter();
		
//		eval.runSingleRandomLearning(20);
//		eval.runSingleLearningKnownDfaWith3States();
//		eval.runSingleLearningFromSerialization("a,b;8;0;1,2;0/a/7,0/b/3,1/a/6,1/b/7,2/a/4,2/b/6,3/a/1,3/b/6,4/a/0,4/b/0,5/a/7,5/b/3,6/a/2,6/b/2,7/a/5,7/b/7");
//		eval.runSingleLearningFromSerialization("a,b;10;0;2,5,8;0/a/9,0/b/1,1/a/9,1/b/3,2/a/2,2/b/4,3/a/7,3/b/5,4/a/3,4/b/9,5/a/2,5/b/6,6/a/1,6/b/8,7/a/0,7/b/3,8/a/6,8/b/2,9/a/4,9/b/5");
//		eval.runSingleLearningFromSerialization("a,b;5;0;3;0/a/1,0/b/4,1/a/3,1/b/3,2/a/3,2/b/3,3/a/2,3/b/4,4/a/0,4/b/2");
//		eval.runSingleLearningFromSerialization("a,b,c,d,e;5;0;0,1,3;0/a/1,0/b/4,0/c/2,0/d/3,0/e/4,1/a/1,1/b/2,1/c/4,1/d/4,1/e/0,2/a/1,2/b/2,2/c/3,2/d/3,2/e/0,3/a/2,3/b/1,3/c/2,3/d/1,3/e/4,4/a/3,4/b/4,4/c/3,4/d/0,4/e/3");

	}
}
