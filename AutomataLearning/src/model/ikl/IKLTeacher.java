package model.ikl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import model.DFA;
import model.DfaEquivalenceChecker;
import model.MooreAutomaton;
import model.MooreEquivalenceChecker;


public class IKLTeacher {

	public MooreAutomaton correctAutomaton;
	
	private long membershipQueryCounter;
	
	private int equivalenceQueryCounter;
	
	private MooreEquivalenceChecker equivalenceChecker;
	
	private HashSet<String> queriedStrings;
	
	private List<Long> equivalenceQueries;
	
	public IKLTeacher(MooreAutomaton correctAutomaton)
	{
		this.correctAutomaton = correctAutomaton;
		this.membershipQueryCounter = 0;
		this.equivalenceQueryCounter = 0;
		this.equivalenceChecker = new MooreEquivalenceChecker(this.correctAutomaton);
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
	
	public ArrayList<String> performMembershipQuery(ArrayList<String> strings)
	{
		membershipQueryCounter++;
		
		String concatenatedString = "";
		for (String s : strings) concatenatedString += s;
		
		assert !this.queriedStrings.contains(concatenatedString) : "already performed this query : " + concatenatedString;
		this.queriedStrings.add(concatenatedString);
		
		// calculate the resulting state from executing the input strings
		String currentState = correctAutomaton.startState;
		for (String inputSymbol : strings)
		{
			currentState = correctAutomaton.transitionTable.get(currentState + inputSymbol);
		}
		
		// get the output of the resulting state
		ArrayList<String> outputSymbols = correctAutomaton.lambdaMap.get(currentState);
		return outputSymbols;
	}
	
	public String performEquivalenceQuery(MooreAutomaton moore)
	{
		//assert dfa.getNumberOfStates() <= correctAutomaton.getNumberOfStates() : "the learned Moore automaton is not minimal any more";

		this.equivalenceQueries.add(this.membershipQueryCounter);
		equivalenceQueryCounter++;
		return equivalenceChecker.findCounterexampleForEquivalence(moore);
	}
}
