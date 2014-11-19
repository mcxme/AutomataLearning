package model.lstar;

import java.util.LinkedList;
import java.util.List;

import model.DFA;


public class LStarEvaluationThread implements Runnable {

	private DFA dfa;
	private String[] alphabet;
	
	public long timeMillisDelta;
	public long membershipQueries;
	public long tableEntries;
	public long equivalenceQueries;
	public int learnedDfaStateSize;
	public long membershipQueriesPerEquivalenceQuery;
	public List<Long> equivalenceQueriesOccurence;
	
	public LStarEvaluationThread(DFA dfa, String[] alphabet)
	{
		this.dfa = dfa;
		this.alphabet = alphabet;
		this.equivalenceQueriesOccurence = new LinkedList<Long>();
	}
	
	@Override
	public void run() {

		Teacher teacher = new Teacher(dfa);
		Learner learner = new Learner(teacher, alphabet);
		DFA learnedDfa = learner.learn();
		
		this.timeMillisDelta = learner.endLearningTimestamp - learner.startLearningTimestamp;
		this.membershipQueries = teacher.getNumberOfMembershipQueries();
		this.tableEntries = learner.tableDepth * learner.tableWidth;
		this.membershipQueriesPerEquivalenceQuery = teacher.getNumberOfMembershipQueries() / teacher.getNumberOfEquivalenceQueries();
		this.equivalenceQueries = teacher.getNumberOfEquivalenceQueries();
		this.learnedDfaStateSize = learnedDfa.getNumberOfStates();
		this.equivalenceQueriesOccurence.addAll(teacher.getEquivalenceQueriesIntervals());
	}
}
