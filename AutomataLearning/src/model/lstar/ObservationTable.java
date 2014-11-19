package model.lstar;

import java.lang.annotation.IncompleteAnnotationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.DFA;
import model.StringUtil;


public class ObservationTable {
	
	public static final String LAMBDA = "";

	public enum Observation
	{
		accepted,
		rejected,
		unknown
	}
	
	private String[] alphabet;
	private ArrayList<ArrayList<Observation>> redTable;
	private ArrayList<ArrayList<Observation>> blueTable;
	
	private HashMap<String, Integer> suffixes;
	private int suffixCount;
	
	private HashMap<String, Integer> redPrefixes;
	
	private int redPrefixCount;
	
	private HashMap<String, Integer> bluePrefixes;
	private int bluePrefixCount;
	
	private LinkedList<String> unknownStringsPrefix;
	private LinkedList<String> unknownStringsSuffix;
	
	private HashMap<String, HashSet<String>> equivalentRedPrefixes;
	private HashSet<String> inconsistentSuffixes;
	private HashSet<String> incompleteBluePrefixes;
	
	public ObservationTable(String[] alphabet)
	{
		this.alphabet = alphabet;
		suffixCount = 0;
		suffixes = new HashMap<String, Integer>();
		redPrefixCount = 0;
		redPrefixes = new HashMap<String, Integer>();
		bluePrefixCount = 0;
		bluePrefixes = new HashMap<String, Integer>();
		unknownStringsPrefix = new LinkedList<String>();
		unknownStringsSuffix = new LinkedList<String>();
		equivalentRedPrefixes = new HashMap<String, HashSet<String>>();
		inconsistentSuffixes = new HashSet<String>();
		incompleteBluePrefixes = new HashSet<String>();
		redTable = new ArrayList<ArrayList<Observation>>();
		blueTable = new ArrayList<ArrayList<Observation>>();
		
		this.initTable();
	}
	
	public long getTableWidth()
	{
		return this.suffixCount;
	}
	
	public long getTableHeight()
	{
		long blueHeight = this.bluePrefixes.size();
		long redHeight = this.redPrefixes.size();
		return blueHeight + redHeight;
	}
	
	private void initTable()
	{
		// the only suffix is lambda
		suffixes.put(LAMBDA, suffixCount++);
		
		// the red table only contains lambda
		ArrayList<Observation> redRow = new ArrayList<Observation>();
		redRow.add(Observation.unknown);
		addREDRow(redRow, LAMBDA);
		
		// the blue table contains all symbols of the alphabet
		for (int i = 0; i < alphabet.length; i++)
		{
			String symbol = alphabet[i];
			ArrayList<Observation> blueRow = new ArrayList<Observation>();
			blueRow.add(Observation.unknown);
			addBLUERow(blueRow, symbol);
		}
	}
	
	private String getSuffixAtIndex(int index)
	{
		for (String suffix : this.suffixes.keySet())
		{
			if (suffixes.get(suffix) == index)
			{
				return suffix;
			}
		}
		
		return null;
	}
	
	private void addUnknownString(String prefix, String suffix)
	{
		this.unknownStringsPrefix.addLast(prefix);
		this.unknownStringsSuffix.addLast(suffix);
	}
	
	private void enterObservation(String prefix, String suffix, Observation observation)
	{
		int suffixIndex = this.suffixes.get(suffix);
		ArrayList<Observation> row = getRowFromPrefix(prefix);
		row.set(suffixIndex, observation);
	}
	
	public void enterObservation(String prefix, String suffix, boolean accepted)
	{
		List<String> prefixList = new ArrayList<String>(1);
		prefixList.add(prefix);
		List<String> suffixList = new ArrayList<String>(1);
		suffixList.add(suffix);
		this.unknownStringsPrefix.removeAll(prefixList);
		this.unknownStringsSuffix.removeAll(suffixList);
		
		Observation observation = Observation.rejected;
		if (accepted)
		{
			observation = Observation.accepted;
		}
		
		enterObservation(prefix, suffix, observation);
		
		if (redPrefixes.containsKey(prefix))
		{			
			checkIdenticalREDRowTo(prefix);
		}
	}
	
	private void checkIdenticalREDRowTo(String redRowPrefix)
	{
		// remove the outdated list
		if (this.equivalentRedPrefixes.containsKey(redRowPrefix))
		{
			this.equivalentRedPrefixes.remove(redRowPrefix);
		}
		
		// create and add a new list
		ArrayList<Observation> redRow = getRowFromPrefix(redRowPrefix);
		HashSet<String> equivalentPrefixes = new HashSet<String>();
		
		for (String otherRedRowPrefix : this.redPrefixes.keySet())
		{
			if (redRowPrefix.equals(otherRedRowPrefix))
			{
				// skip reflexive items
				continue;
			}
			
			int otherRedRowIndex = redPrefixes.get(otherRedRowPrefix);
			ArrayList<Observation> otherRedRow = redTable.get(otherRedRowIndex);
			
			if (this.equalRows(redRow, otherRedRow))
			{
				// the rows are equal so add the equivalence classes symmetrically
				equivalentPrefixes.add(otherRedRowPrefix);
				
				if (this.equivalentRedPrefixes.containsKey(otherRedRowPrefix))
				{
					HashSet<String> otherEquivalentPrefixes = this.equivalentRedPrefixes.get(otherRedRowPrefix);
					otherEquivalentPrefixes.add(redRowPrefix);
				}
				else
				{
					HashSet<String> otherEquivalentPrefixes = new HashSet<String>();
					otherEquivalentPrefixes.add(redRowPrefix);
					this.equivalentRedPrefixes.put(otherRedRowPrefix, otherEquivalentPrefixes);
				}
			}
			else
			{
				// the rows are not equal so remove the current prefix from the other equivalence class if present
				if (this.equivalentRedPrefixes.containsKey(otherRedRowPrefix))
				{
					HashSet<String> otherEquivalentPrefixes = this.equivalentRedPrefixes.get(otherRedRowPrefix);
					if (otherEquivalentPrefixes.contains(redRowPrefix))
					{
						otherEquivalentPrefixes.remove(redRowPrefix);
						if (otherEquivalentPrefixes.isEmpty())
						{
							this.equivalentRedPrefixes.remove(otherRedRowPrefix);
						}
					}
				}
			}
		}
		
		if (!equivalentPrefixes.isEmpty())
		{
			this.equivalentRedPrefixes.put(redRowPrefix, equivalentPrefixes);
		}
	}
	
	public Observation getObservation(String prefix, String suffix)
	{
		int suffixIndex = this.suffixes.get(suffix);
		Observation observation = Observation.unknown;
		
		if (redPrefixes.containsKey(prefix))
		{
			int refPrefixIndex = redPrefixes.get(prefix);
			observation = redTable.get(refPrefixIndex).get(suffixIndex);
		}
		else
		{
			int bluePrefixIndex = bluePrefixes.get(prefix);
			observation = blueTable.get(bluePrefixIndex).get(suffixIndex);
		}
		
		return observation;
	}
	
	public boolean containsUnknownField()
	{
		for (String redPrefix : this.redPrefixes.keySet())
		{
			for (String suffix : this.suffixes.keySet())
			{
				if (getObservation(redPrefix, suffix) == Observation.unknown)
				{
					return true;
				}
			}
		}
		
		for (String bluePrefix : this.bluePrefixes.keySet())
		{
			for (String suffix : this.suffixes.keySet())
			{
				if (getObservation(bluePrefix, suffix) == Observation.unknown)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public List<String> getUnknownStringPrefixes()
	{
		return this.unknownStringsPrefix;
	}
	
	public List<String> getUnknownStringSuffixes()
	{
		return this.unknownStringsSuffix;
	}
	
	public HashSet<String> getIncompletePrefixes()
	{
		return this.incompleteBluePrefixes;
	}
	
	public void clearIncompletePrefixes()
	{
		this.incompleteBluePrefixes.clear();
	}
	
	public void clearInconsistentSuffixes()
	{
		this.inconsistentSuffixes.clear();
	}
	
	public void moveIncompletePrefix(String prefix)
	{
		assert !redPrefixes.containsKey(prefix) : "the prefix that should be moved is already in the RED table";
		
		this.incompleteBluePrefixes.remove(prefix);
		
		// move the prefix to the RED table
		int currentBlueIndex = bluePrefixes.get(prefix);
		bluePrefixes.remove(prefix);
		ArrayList<Observation> blueRow = blueTable.get(currentBlueIndex);
		blueTable.set(currentBlueIndex, null);
		addREDRow(blueRow, prefix);
		checkIdenticalREDRowTo(prefix);
		
		// add all strings (prefix * symbol) to the BLUE table
		for (int i = 0; i < alphabet.length; i++) {
			String symbol = alphabet[i];
			String newPrefix = prefix + symbol;
			if (!containsPrefix(newPrefix))
			{				
				addUnknownStringToBlueTable(newPrefix);
			}
		}
	}
	
	private void addUnknownStringToBlueTable(String prefix)
	{
		// create an unknown row
		ArrayList<Observation> blueRow = new ArrayList<Observation>(suffixCount);
		for (int i = 0; i < suffixCount; i++) {
			String currentSuffix = this.getSuffixAtIndex(i);
			Observation observation = getExistingObservationFor(prefix + currentSuffix);
			blueRow.add(i, observation);
		}
		
		addBLUERow(blueRow, prefix);
	}
	
	private Observation getExistingObservationFor(String concatenated)
	{	
		Observation retrievedObservation = Observation.unknown;
		for (int i = 0; i < concatenated.length(); i++) {
			String prefix = concatenated.substring(0, i);
			String suffix = concatenated.substring(i);
			
			if (hasObservation(prefix, suffix))
			{
				retrievedObservation = getObservation(prefix, suffix);
				if (retrievedObservation != Observation.unknown)
				{
					break;
				}
			}
		}
		
		return retrievedObservation;
	}
	
	public boolean isComplete()
	{
		boolean isComplete = true;
		
		// check for each BLUE row whether there is an equal row in RED
		for (String bluePrefix : this.bluePrefixes.keySet())
		{
			int blueRowIndex = bluePrefixes.get(bluePrefix);
			ArrayList<Observation> blueRow = blueTable.get(blueRowIndex);
			boolean foundREDEquivalent = false;
			for (int redRowIndex = 0; (redRowIndex < redPrefixCount) && !foundREDEquivalent; redRowIndex++)
			{
				ArrayList<Observation> redRow = redTable.get(redRowIndex);
				assert redRow != null;
				
				foundREDEquivalent = this.equalRows(blueRow, redRow);
			}
			
			if (!foundREDEquivalent)
			{
				isComplete = false;
				this.incompleteBluePrefixes.add(bluePrefix);
				return isComplete;
			}
		}
		
		return isComplete;
	}
	
	public HashSet<String> getInconsistentSuffixes()
	{
		return this.inconsistentSuffixes;
	}
	
	private boolean hasObservation(String prefix, String suffix)
	{
		return (isRedPrefix(prefix) || isBluePrefix(prefix)) && isSuffix(suffix);
	}
	
	public HashSet<String> getAllPrefixes()
	{
		HashSet<String> prefixes = new HashSet<String>();
		prefixes.addAll(redPrefixes.keySet());
		prefixes.addAll(bluePrefixes.keySet());
		
		return prefixes;
	}
	
	public void addNewColumnWithSuffix(String suffix)
	{
		this.inconsistentSuffixes.remove(suffix);
		
		suffixes.put(suffix, suffixCount++);
		
		// add the new field to all rows
		for (String redPrefix : redPrefixes.keySet())
		{
			int redRowIndex = redPrefixes.get(redPrefix);
			ArrayList<Observation> redRow = redTable.get(redRowIndex);
			
			// the new value of the RED table field can be retrieved from the blue one
			Observation retrievedObservation = Observation.unknown;
			for (String existingPrefix : this.getAllPrefixes())
			{
			for (String existingSuffix : this.suffixes.keySet())
			{
				if (existingSuffix.equals(suffix))
				{
					// the value for this suffix is not yet in the table.
					continue;
				}
				
				String chainedString = existingPrefix + existingSuffix;
				if (!chainedString.equals(redPrefix + suffix))
				{
					// the current prefix + suffix is not equal to the current fixture of RED prefix + new suffix
					continue;
				}
				
				if (hasObservation(existingPrefix, existingSuffix))
				{
					retrievedObservation = getObservation(existingPrefix, existingSuffix);
					break;
				}
			}
			if (retrievedObservation != Observation.unknown)
			{
				break;
			}
			}
			
			if (retrievedObservation == Observation.unknown)
			{
				addUnknownString(redPrefix + suffix, LAMBDA);
			}
			
			redRow.add(retrievedObservation);
		}
		
		// check for new identical rows
		for (String redPrefix : redPrefixes.keySet())
		{			
			checkIdenticalREDRowTo(redPrefix);
		}
		
		// the new value of the BLUE table field is unknown
		for (String bluePrefix : bluePrefixes.keySet())
		{
			int blueRowIndex = bluePrefixes.get(bluePrefix);
			ArrayList<Observation> blueRow = blueTable.get(blueRowIndex);
			blueRow.add(Observation.unknown);
			addUnknownString(bluePrefix, suffix);
		}
	}
	
	public boolean isConsistent()
	{
		boolean isConsistent = true;
		
		// check for each pair of equal rows whether a symbol can be appended and the strings still end up in an equal state
		for (String redPrefix1 : this.equivalentRedPrefixes.keySet()) {
			HashSet<String> equivalentPrefixes = this.equivalentRedPrefixes.get(redPrefix1);
			for (String redPrefix2 : equivalentPrefixes) {
				// append each symbol to both of the prefixes and check whether
				// they are in the same state
				for (int i = 0; i < alphabet.length; i++) {
					String symbol = alphabet[i];
					for (int j = 0; j < suffixCount; j++) {
						String suffix = getSuffixAtIndex(j);
						String redString1 = redPrefix1 + symbol;
						String redString2 = redPrefix2 + symbol;

						Observation string1Observation = getObservation(redString1, suffix);
						Observation string2Observation = getObservation(redString2, suffix);
						if (string1Observation != string2Observation && string1Observation != Observation.unknown && string2Observation != Observation.unknown) {
							assert getObservation(redString1, suffix) != Observation.unknown && getObservation(redString2, suffix) != Observation.unknown;
							assert !this.suffixes.containsKey(symbol + suffix) : "suffix already known";
							// found an inconsistency
							this.inconsistentSuffixes.add(symbol + suffix);
							isConsistent = false;
							return isConsistent;
						}
					}
				}
			}
		}
		
		return isConsistent;
	}
	
	public void addStringAndAllPrefixesToRED(String string)
	{
		List<String> prefixes = StringUtil.getPrefixes(string, alphabet);
		HashSet<String> blueStringsToAdd = new HashSet<String>();
		
		// add all prefixes to RED if they were not present yet
		for (String prefix : prefixes)
		{
			// do  not add a prefix that is already in the RED table
			if (!isRedPrefix(prefix))
			{
				// is the prefix currently in the BLUE table?
				if (isBluePrefix(prefix))
				{
					moveIncompletePrefix(prefix);
				}
				else
				{
					// add the prefix manually to the RED table
					// TODO some values can be retrieved from the current table
					ArrayList<Observation> redRow = new ArrayList<Observation>();
					for (int i = 0; i < suffixCount; i++) {
						redRow.add(Observation.unknown);
					}
					addREDRow(redRow, prefix);
					
					// look at all potential successor strings later on
					for (int i = 0; i < alphabet.length; i++) {
						blueStringsToAdd.add(prefix + alphabet[i]);
					}
				}
			}
		}
		
		// add all successor strings to BLUE if they were not present
		for (String bluePrefix : blueStringsToAdd)
		{
			if (!this.redTableContains(bluePrefix) && !this.blueTableContains(bluePrefix))
			{
				addUnknownStringToBlueTable(bluePrefix);
			}
		}
		
		// try to retrieve unknown values from existing combinations of prefixes and suffixes
		Iterator<String> prefixIterator = this.unknownStringsPrefix.iterator();
		Iterator<String> suffixIterator = this.unknownStringsSuffix.iterator();
		while (prefixIterator.hasNext()) {
			String unknownPrefix = prefixIterator.next();
			String unknownSuffix = suffixIterator.next();
			String unknownConcatenatedString = unknownPrefix + unknownSuffix;
			Observation retrievedObservation = this.getExistingObservationFor(unknownConcatenatedString);
			if (retrievedObservation != Observation.unknown)
			{
				this.enterObservation(unknownPrefix, unknownSuffix, retrievedObservation);
				prefixIterator.remove();
				suffixIterator.remove();
			}
		}
		
	}
	
	private boolean isRedPrefix(String prefix)
	{
		return this.redPrefixes.containsKey(prefix);
	}
	
	private boolean isBluePrefix(String prefix)
	{
		return this.bluePrefixes.containsKey(prefix);
	}
	
	private boolean isSuffix(String suffix)
	{
		return this.suffixes.containsKey(suffix);
	}
	
	private boolean redTableContains(String string)
	{
		// check whether there is any combination of a red prefix and a suffix that results in the given string
		for (String redPrefix : this.redPrefixes.keySet())
		{
			if (string.startsWith(redPrefix))
			{
				for (String suffix : this.suffixes.keySet())
				{
					if (string.equals(redPrefix + suffix))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean blueTableContains(String string)
	{
		// check whether there is any combination of a red prefix and a suffix that results in the given string
		for (String bluePrefix : this.bluePrefixes.keySet())
		{
			if (string.startsWith(bluePrefix))
			{
				for (String suffix : this.suffixes.keySet())
				{
					if (string.equals(bluePrefix + suffix))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private void addREDRow(ArrayList<Observation> redRow, String prefix)
	{
		redPrefixes.put(prefix, redPrefixCount++);
		redTable.add(redRow);
		
		for (int i = 0; i < redRow.size(); i++) {
			Observation currentObservation = redRow.get(i);
			
			if (currentObservation == Observation.unknown)
			{
				addUnknownString(prefix, getSuffixAtIndex(i));
			}
		}
	}
	
	private void addBLUERow(ArrayList<Observation> blueRow, String prefix)
	{
		bluePrefixes.put(prefix, bluePrefixCount++);
		blueTable.add(blueRow);
		
		for (int i = 0; i < blueRow.size(); i++) {
			Observation currentObservation = blueRow.get(i);
			
			if (currentObservation == Observation.unknown)
			{
				addUnknownString(prefix, getSuffixAtIndex(i));
			}
		}
	}
	
	public boolean containsString(String string)
	{
		return this.blueTableContains(string) || this.redTableContains(string);
	}
	
	public boolean containsPrefix(String prefix)
	{
		boolean isInBLUE = bluePrefixes.containsKey(prefix);
		boolean isInRED = redPrefixes.containsKey(prefix);
		
		return isInBLUE || isInRED;
	}
	
	private boolean equalRows(ArrayList<Observation> row1, ArrayList<Observation> row2)
	{
		boolean equal = true;
		
		for (int i = 0; i < suffixCount && equal; i++)
		{
			if (row1.get(i) != row2.get(i))
			{
				equal = false;
			}
		}
		
		return equal;
	}
	
	public ArrayList<Observation> getRowFromPrefix(String prefix)
	{
		assert prefix != null : "prefix must not be null";
		
		if (redPrefixes.containsKey(prefix))
		{
			int redIndex = redPrefixes.get(prefix);
			return redTable.get(redIndex);
		}
		else
		{
			assert bluePrefixes.containsKey(prefix) : "the given prefix is neither in RED nor in BLUE";
			
			int blueIndex = bluePrefixes.get(prefix);
			return blueTable.get(blueIndex);
		}
	}
	
	public DFA buildDFA()
	{
		assert isComplete() && isConsistent() : "the DFA should only be created, if the DFA is both complete and consistent";
		
		// 1. find all distinguished states
		// equivalent rows are one state (the shortest prefix is the representative for this equivalence class)
		HashSet<String> states = new HashSet<String>();
		HashMap<String, String> equivalenceClasses = new HashMap<String, String>(redPrefixCount);
		for (String currentRedPrefix : this.redPrefixes.keySet())
		{
			if (equivalentRedPrefixes.containsKey(currentRedPrefix))
			{
				// there are multiple prefixes that might be a state
				HashSet<String> equivalentStrings = equivalentRedPrefixes.get(currentRedPrefix);
				
				// is this equivalence class already a state?
				boolean foundClass = false;
				String shortestEquivalentPrefix = currentRedPrefix;
				for (String equivalentPrefix : equivalentStrings)
				{
					if (states.contains(equivalentPrefix))
					{
						foundClass = true;
						break;
					}
					else if (shortestEquivalentPrefix.length() > equivalentPrefix.length())
					{
						shortestEquivalentPrefix = equivalentPrefix;
					}
				}
				
				if (!foundClass)
				{
					// --> no the equivalence class is not a state so add the shortest prefix of this class as a state					
					states.add(shortestEquivalentPrefix);
				}
				
				equivalenceClasses.put(currentRedPrefix, shortestEquivalentPrefix);
			}
			else
			{
				// there is only one prefix for this state so add it
				states.add(currentRedPrefix);
				equivalenceClasses.put(currentRedPrefix, currentRedPrefix);
			}
			
		}
		
		// 2. map all prefixes to a state number
		int stateNumber = 0;
		HashMap<String, Integer> stateNumberMapping = new HashMap<String, Integer>();
		for (String state : states)
		{
			stateNumberMapping.put(state, stateNumber++);
		}
		
		DFA dfa = new DFA();
		dfa.setAlphabet(alphabet);
		dfa.setInitialState(stateNumberMapping.get(LAMBDA));
		dfa.setTotalNumberOfStates(stateNumber);
		
		// 3. get accepting states
		// all states are accepting were the O(equivalence class string, LAMBDA) = accepted
		for (String state : states)
		{
			Observation observation = getObservation(state, LAMBDA);
			if (observation == Observation.accepted)
			{
				dfa.addAcceptingState(stateNumberMapping.get(state));
			}
		}
		
		// 4. get all transitions
		for (String state : states)
		{
			// state u
			for (String symbol : this.alphabet) {
				String appendedPrefix = state + symbol; // va

				// u = va && ROW(u) =? ROW(va)
				// if the rows for the (state) and (other state + symbol) are equal,
				// than there obviously is a transition from the (other state) to the (state) given the current symbol.
				ArrayList<Observation> otherRow = getRowFromPrefix(appendedPrefix);

				// find the equivalence class of the prefix v in RED with the shortest prefix
				String shortestEquivalentString = null;
				// ==> find any RED prefix with the same ROW as the current appended one.
				for (String redPrefix : this.redPrefixes.keySet())
				{
					if (equalRows(otherRow, getRowFromPrefix(redPrefix)))
					{
						shortestEquivalentString = equivalenceClasses.get(redPrefix);
						break;
					}
				}
				
				assert shortestEquivalentString != null : "there should be an equivalence class for each RED prefix";
				
				assert this.equalRows(otherRow, getRowFromPrefix(shortestEquivalentString))
					: "the equivalence class does not have the same ROW";

				// add the transition: delta(q[u], a) --> q[v]
				int startState = stateNumberMapping.get(state);
				int endState = stateNumberMapping.get(shortestEquivalentString);
				dfa.setTransition(startState, endState, symbol);
			}
		}
		
		assert dfa.isConnectedGraph() : "the resulting DFA should be a fully connected graph";
		return dfa;
	}
	
	@Override
	public String toString()
	{
		String table = "\t\t";
		// first line contains all suffixes
		for (int i = 0; i < suffixCount; i++) {
			table += "|" + getSuffixAtIndex(i) + "\t\t\t\t";
		}
		table += "\n";
		
		// draw a line
		for (int i = 0; i < suffixCount + 1; i++) {
			table += "--------------";
		}
		table += "\n";
		
		// create the RED table
		for (String redPrefix : this.redPrefixes.keySet())
		{
			table += redPrefix + "\t\t|";
			int redPrefixIndex = this.redPrefixes.get(redPrefix);
			for (int j = 0; j < suffixCount; j++) {
				table += redTable.get(redPrefixIndex).get(j) + "\t\t";
			}
			table += "\n";
		}
		
		// draw a line
		for (int i = 0; i < suffixCount + 1; i++) {
			table += "--------------";
		}
		table += "\n";
		
		// create the RED table
		for (String bluePrefix : this.bluePrefixes.keySet())
		{
			table += bluePrefix + "\t\t|";
			int bluePrefixCount = bluePrefixes.get(bluePrefix);
			for (int j = 0; j < suffixCount; j++) {
				table += blueTable.get(bluePrefixCount).get(j) + "\t\t";
			}
			table += "\n";
		}
		
		return table;
	}
}
