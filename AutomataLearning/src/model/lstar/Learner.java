package model.lstar;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.DFA;
import model.lstar.ObservationTable.Observation;


public class Learner {

	private Teacher teacher;
	private String[] alphabet;
	public long startLearningTimestamp;
	public long endLearningTimestamp;
	public long tableWidth;
	public long tableDepth;
	public long completeErrors;
	public long consistentErrors;
	
	public Learner(Teacher teacher, String[] alphabet)
	{
		this.teacher = teacher;
		this.alphabet = alphabet;
		this.completeErrors = 0;
		this.consistentErrors = 0;
	}
	
	public DFA learn()
	{
		this.startLearningTimestamp = System.currentTimeMillis();
		
		ObservationTable observations = new ObservationTable(alphabet);
		resolveUnknownEntries(teacher, observations);
		DFA learnedDFA = null;
		boolean foundCorrectDFA = false;
		
		while (!foundCorrectDFA)
		{
			assert !observations.containsUnknownField() : "the table should not contain unknown fields at the beginning of the loop";
			
			while (!observations.isComplete() || !observations.isConsistent())
			{
				
				// 1. shift all incomplete rows
				observations.clearIncompletePrefixes();
				while (!observations.isComplete()) {
					assert observations.getIncompletePrefixes().size() > 0;
					LinkedList<String> incompletePrefixes = new LinkedList<String>();
					incompletePrefixes.addAll(observations.getIncompletePrefixes());
					Collections.sort(incompletePrefixes);

					// get the shortest prefix and make it complete
					String prefix = incompletePrefixes.getFirst();
					if (prefix != null) {
						observations.moveIncompletePrefix(prefix);
						this.completeErrors++;
						assert completeErrors < this.teacher.hiddenDfa.getNumberOfStates() : "The table is not closed / complete max. n - 1 times (n is hidden state size)";
						resolveUnknownEntries(teacher, observations);
					}
					observations.clearIncompletePrefixes();
				}

				// 2. add one inconsistent column
				observations.clearInconsistentSuffixes();
				if (!observations.isConsistent()) {
					assert observations.getInconsistentSuffixes().size() > 0;
					LinkedList<String> inconsistentSuffixes = new LinkedList<String>();
					inconsistentSuffixes.addAll(observations.getInconsistentSuffixes());
					Collections.sort(inconsistentSuffixes);

					// get the shortest suffix and make it consistent
					String suffix = inconsistentSuffixes.getFirst();
					observations.addNewColumnWithSuffix(suffix);
					this.consistentErrors++;
					assert consistentErrors < this.teacher.hiddenDfa.getNumberOfStates() : "The table is not consistent max. n - 1 times (n is hidden state size)";
					resolveUnknownEntries(teacher, observations);

				}

				assert !observations.containsUnknownField() : "the table should not contain unknown fields at the end of the loop";
			}
			
			learnedDFA = observations.buildDFA();
			String counterexample = teacher.performEquivalenceQuery(learnedDFA);
			
			if (counterexample == null)
			{
				foundCorrectDFA = true;
			}
			else
			{
				// add counterexample and all prefixes to RED
				observations.addStringAndAllPrefixesToRED(counterexample);
				resolveUnknownEntries(teacher, observations);
			}
		}
		
		this.endLearningTimestamp = System.currentTimeMillis();
		this.tableWidth = observations.getTableWidth();
		this.tableDepth = observations.getTableHeight();
		
		return learnedDFA;
	}
	
	public DFA learnMultiple()
	{
		this.startLearningTimestamp = System.currentTimeMillis();
		
		ObservationTable observations = new ObservationTable(alphabet);
		resolveUnknownEntries(teacher, observations);
		DFA learnedDFA = null;
		boolean foundCorrectDFA = false;
		
		while (!foundCorrectDFA)
		{
			assert !observations.containsUnknownField() : "the table should not contain unknown fields at the beginning of the loop";
			
			while (!observations.isComplete() || !observations.isConsistent())
			{
				
				// 1. shift incomplete rows
				HashSet<String> incompletePrefixes = new HashSet<String>();
				incompletePrefixes.addAll(observations.getIncompletePrefixes());
				HashSet<ArrayList<Observation>> newRows = new HashSet<ArrayList<Observation>>();
				for (String prefix : incompletePrefixes)
				{
					ArrayList<Observation> row = observations.getRowFromPrefix(prefix);
					if (newRows.contains(row))
					{
						// skip all incomplete prefixes that belong to rows that have already been made complete.
						continue;
					}
					
					assert !observations.isComplete() : "The table is already complete";
					observations.moveIncompletePrefix(prefix);
					newRows.add(row);
					this.completeErrors++;
					assert completeErrors < this.teacher.hiddenDfa.getNumberOfStates() : "The table is not closed / complete max. n - 1 times (n is hidden state size)";
					resolveUnknownEntries(teacher, observations);
				}
				
				// remove remaining skipped prefixes
				observations.clearIncompletePrefixes();
				
				// 2. add rows because of inconsistency
				HashSet<String> inconsistentSuffixes = new HashSet<String>();
				inconsistentSuffixes.addAll(observations.getInconsistentSuffixes());
				for (String suffix : inconsistentSuffixes)
				{
					if (observations.isConsistent())
					{
						break;
					}
					
					observations.addNewColumnWithSuffix(suffix);
					this.consistentErrors++;
					assert consistentErrors < this.teacher.hiddenDfa.getNumberOfStates() : "The table is not consistent max. n - 1 times (n is hidden state size)";
					resolveUnknownEntries(teacher, observations);
				}

				// remove remaining skipped suffixes
				observations.clearInconsistentSuffixes();

				assert !observations.containsUnknownField() : "the table should not contain unknown fields at the end of the loop";
			}
			
			learnedDFA = observations.buildDFA();
			String counterexample = teacher.performEquivalenceQuery(learnedDFA);
			
			if (counterexample == null)
			{
				foundCorrectDFA = true;
			}
			else
			{
				// add counterexample and all prefixes to RED
				observations.addStringAndAllPrefixesToRED(counterexample);
				resolveUnknownEntries(teacher, observations);
			}
		}
		
		this.endLearningTimestamp = System.currentTimeMillis();
		this.tableWidth = observations.getTableWidth();
		this.tableDepth = observations.getTableHeight();
		
		return learnedDFA;
	}
	
	private void resolveUnknownEntries(Teacher teacher, ObservationTable observations)
	{
		List<String> unknownPrefixes = new ArrayList<String>();
		unknownPrefixes.addAll(observations.getUnknownStringPrefixes());
		List<String> unknownSuffixes = new ArrayList<String>();
		unknownSuffixes.addAll(observations.getUnknownStringSuffixes());
		Iterator<String> prefixeIterator = unknownPrefixes.iterator();
		Iterator<String> suffixesIterator = unknownSuffixes.iterator();
		HashMap<String, Boolean> finishedObservations = new HashMap<String, Boolean>();
		
		while (prefixeIterator.hasNext())
		{
			String currentPrefix = prefixeIterator.next();
			String currentSuffix = suffixesIterator.next();
			String concatenated = currentPrefix + currentSuffix;
			boolean observation;
			if (finishedObservations.containsKey(concatenated))
			{
				observation = finishedObservations.get(concatenated);
			}
			else
			{
				observation = teacher.performMembershipQuery(currentPrefix + currentSuffix);
				finishedObservations.put(concatenated, observation);
			}
			
			observations.enterObservation(currentPrefix, currentSuffix, observation);
		}
	}
}
