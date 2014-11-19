package model.lstar;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import model.DFA;
import model.DfaEquivalenceChecker;


public class Teacher {

	public DFA hiddenDfa;
	
	private long membershipQueryCounter;
	
	private int equivalenceQueryCounter;
	
	private DfaEquivalenceChecker equivalenceChecker;
	
	private HashSet<String> queriedStrings;
	
	private List<Long> equivalenceQueries;
	
	public Teacher(DFA correctDfa)
	{
		this.hiddenDfa = correctDfa;
		this.membershipQueryCounter = 0;
		this.equivalenceQueryCounter = 0;
		this.equivalenceChecker = new DfaEquivalenceChecker(this.hiddenDfa);
		this.queriedStrings = new HashSet<String>();
		this.equivalenceQueries = new LinkedList<Long>();
	}
	
	public long getNumberOfMembershipQueries()
	{
		return this.membershipQueryCounter;
	}
	
	public int getNumberOfEquivalenceQueries()
	{
		return this.equivalenceQueryCounter;
	}
	
	public List<Long> getEquivalenceQueriesIntervals()
	{
		return equivalenceQueries;
	}
	
	public boolean performMembershipQuery(String string)
	{
		membershipQueryCounter++;
		
		assert !this.queriedStrings.contains(string) : "already performed this query : " + string;
		this.queriedStrings.add(string);
		
		int resultingState = hiddenDfa.getResultingStateFromString(string);
		return hiddenDfa.isAcceptingState(resultingState);
	}
	
	public String performEquivalenceQuery(DFA dfa)
	{
		assert dfa.getNumberOfStates() <= hiddenDfa.getNumberOfStates() : "the learned DFA is not minimal any more";

		this.equivalenceQueries.add(this.membershipQueryCounter);
		equivalenceQueryCounter++;
		return equivalenceChecker.findCounterexampleForEquivalence(dfa);
	}
}
