package model.lstar;

import java.util.concurrent.BlockingQueue;

import model.DFA;


public class LStarConsumerThread implements Runnable {

	private BlockingQueue<DFA> queue;
	private String[] alphabet;
	private int numberOfDFAsToSOlve;
	public long[] membershipQueries;
	public long[] equivalenceQueries;
	
	public LStarConsumerThread(BlockingQueue<DFA> queue, String[] alphabet, int numberOfDFAsToSolve)
	{
		this.queue = queue;
		this.alphabet = alphabet;
		this.numberOfDFAsToSOlve = numberOfDFAsToSolve;
		this.membershipQueries = new long[numberOfDFAsToSolve];
		this.equivalenceQueries = new long[numberOfDFAsToSolve];
	}

	@Override
	public void run() {

		try {
			for (int i = 0; i < numberOfDFAsToSOlve; i++) {
				DFA dfa = queue.take();
				Teacher teacher = new Teacher(dfa);
				Learner learner = new Learner(teacher, alphabet);
				learner.learn();
				membershipQueries[i] = teacher.getNumberOfMembershipQueries();
				equivalenceQueries[i] = teacher.getNumberOfEquivalenceQueries();
				System.out.print("|");
				if (i % 50 == 0)
				{
					System.out.print("\n");
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
