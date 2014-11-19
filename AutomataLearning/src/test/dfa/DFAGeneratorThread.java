package test.dfa;

import java.util.concurrent.BlockingQueue;

import model.DFA;


public class DFAGeneratorThread implements Runnable {

	private int numberOfStates;
	private String[] alphabet;
	private float acceptingStatesPercentage;
	private int numberOfDFAs;
	private BlockingQueue<DFA> queue;
	private RandomDFAGenerator generator;
	
	public DFAGeneratorThread(int numberOfStates, String[] alphabet, float acceptingStatesPercentage, int numberOfDFAs, BlockingQueue<DFA> queue)
	{
		this.generator = new RandomDFAGenerator();
		this.queue = queue;
		this.numberOfDFAs = numberOfDFAs;
		this.numberOfStates = numberOfStates;
		this.acceptingStatesPercentage = acceptingStatesPercentage;
		this.alphabet = alphabet;
	}

	@Override
	public void run() {

		for (int i = 0; i < numberOfDFAs; i++) {
			DFA dfa = generator.generateUnique(numberOfStates, alphabet, acceptingStatesPercentage);
			queue.offer(dfa);
			System.out.print("-");
			if (i % 50 == 0)
			{
				System.out.print("\n");
			}
		}
	}
	
}
