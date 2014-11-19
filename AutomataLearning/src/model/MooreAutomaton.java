package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class MooreAutomaton {

	/**
	 * @param args
	 */
	

	
	public int k; // to hold length of the output vector of the Moore Automaton
	public String startState; // initial state
	public mySet<String> Sigma; // input alphabet
	public mySet<String> stateSet; // state set
	
	public ArrayList<String> typeList; // local copy of output type list

	private HashMap<String, Set<String>> outputTypeMap; // map symbolic output values to their types
	private HashMap<String, Set<String>> invertedOutputTypeMap; 
	// map output types to set of all output values of that type
	
	
	public HashMap<String, String> transitionTable = new HashMap<String, String>();
	// the mapping delta : State x Sigma -> state
	// concatenate state string with input symbol to get next state 
	// i.e. delta ( q, i ) = transitionTable ( q.i )
	
	public HashMap<String, ArrayList<String>> lambdaMap = new HashMap<String, ArrayList<String>>();
	// output function 
	// lambda : State -> Output 
	
	public MooreAutomaton(int b) {
		// one parameter constructor
		k = b;
		startState = new String();
		Sigma = new mySet<String>();
		stateSet = new mySet<String>();
		transitionTable = new HashMap<String, String>();
		lambdaMap = new HashMap<String, ArrayList<String>>();
		typeList = new ArrayList<String>();
		outputTypeMap = new HashMap<String, Set<String>>();
		invertedOutputTypeMap = new HashMap<String, Set<String>>(); 
		


	}
	
	public String serialize()
	{
		String serial = "";
		
		// part[0] : vector length
		serial += "" + k + ";";
		
		// part[1] : start state
		serial += startState + ";";
		
		// part[2] : input alphabet a,b,c
		for (String inputSymbol : Sigma)
		{
			serial += inputSymbol + ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[3] : state set a,b,c
		for (String state : stateSet)
		{
			serial += state + ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[4] : type list a,b,c
		for (String type : typeList)
		{
			serial += type + ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[5] : transition table a|1,b|2,c|3
		for (String stateXsigma : transitionTable.keySet())
		{
			serial += stateXsigma + "|" + transitionTable.get(stateXsigma) + ",";
			
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[6] : lambda map a|1/11/111,b|2/22/222,c|3/33/333
		for (String state : lambdaMap.keySet())
		{
			serial += state + "|";
			for (String outputSymbol : lambdaMap.get(state))
			{
				serial += outputSymbol + "/";
			}
			serial = serial.substring(0, serial.length() - 1);
			serial += ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[7] : inverted output type map a|1/11/111,b|2/22/222,c|3/33/333
		for (String outputType : invertedOutputTypeMap.keySet())
		{
			serial += outputType + "|";
			for (String outputTypesMember : invertedOutputTypeMap.get(outputType))
			{
				serial += outputTypesMember + "/";
			}
			serial = serial.substring(0, serial.length() - 1);
			serial += ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		serial += ";";
		
		// part[8] : inverted output type map a|1/11/111,b|2/22/222,c|3/33/333
		for (String outputValue : outputTypeMap.keySet())
		{
			serial += outputValue + "|";
			for (String outputType : outputTypeMap.get(outputValue))
			{
				serial += outputType + "/";
			}
			serial = serial.substring(0, serial.length() - 1);
			serial += ",";
		}
		serial = serial.substring(0, serial.length() - 1);
		
		return serial;
	}
	
	public static MooreAutomaton deserialize(String serial)
	{
		String[] parts = serial.split(";");
		assert parts.length == 5 : "wrong serial string";
		
		int k = Integer.parseInt(parts[0]);
		MooreAutomaton moore = new MooreAutomaton(k);
		
		String startState = parts[1];
		moore.startState = startState;
		
		String[] inputSymbols = parts[2].split(",");
		for (String inputSymbol : inputSymbols)
		{
			moore.Sigma.add(inputSymbol);
		}
		
		String[] states = parts[3].split(",");
		for (String state : states)
		{
			moore.stateSet.add(state);
		}
		
		String[] types = parts[4].split(",");
		for (String type : types)
		{
			moore.typeList.add(type);
		}
		
		String[] transitions = parts[5].split(",");
		for (String transition : transitions)
		{
			String[] values = transition.split("|");
			String stateXsigma = values[0];
			String state = values[1];
			moore.transitionTable.put(stateXsigma, state);
		}
		
		String[] lambdas = parts[6].split(",");
		for (String lambda : lambdas)
		{
			String[] values = lambda.split("|");
			String state = values[0];
			String outputsUnsplitted = values[1];
			String[] outputsArray = outputsUnsplitted.split("/");
			ArrayList<String> outputs = new ArrayList<String>();
			for (String output : outputsArray) outputs.add(output);
			moore.lambdaMap.put(state, outputs);
		}
		
		String[] invertedOutputTypes = parts[7].split(",");
		for (String invertedOutputType : invertedOutputTypes)
		{
			String[] values = invertedOutputType.split("|");
			String key = values[0];
			String setUnsplitted = values[1];
			String[] setArray = setUnsplitted.split("/");
			HashSet<String> set = new HashSet<String>();
			for (String output : setArray) set.add(output);
			moore.invertedOutputTypeMap.put(key, set);
		}
		
		String[] outputTypes = parts[8].split(",");
		for (String outputType : outputTypes)
		{
			String[] values = outputType.split("|");
			String key = values[0];
			String setUnsplitted = values[1];
			String[] setArray = setUnsplitted.split("/");
			HashSet<String> set = new HashSet<String>();
			for (String output : setArray) set.add(output);
			moore.outputTypeMap.put(key, set);
		}
		
		return moore;
	}
	
	public void setOutputVectorSize (int size) {
		k = size;		
	}
	
	public void setStartState (String state) {
		startState = new String(state);		
	}
	
	public void setSigma (mySet<String> sigmaSet) {
		Sigma.addAll(sigmaSet);		
	}
	
	public void setTransitionTable (HashMap<String, String> inTransitionTable) {
		transitionTable.putAll(inTransitionTable);		
	}
	
	public void setStateSet (mySet<String> inStateSet) {
		stateSet.addAll(inStateSet);		
	}
	
	public void setLambdaMap (HashMap<String, ArrayList<String>> inLambdaMap) {
		lambdaMap.putAll(inLambdaMap);		
	}
	
	public void setTypeList (ArrayList<String> inTypeList) {
		typeList.addAll(inTypeList);		
	}
	
	public void setInvertedOutputTypeMap (HashMap<String, Set<String>> inInvOutTypeMap) {
		invertedOutputTypeMap.putAll(inInvOutTypeMap);
		
	}
	
	public void show() {
		System.out.println("\n\nShow");
		System.out.println("\n\nHere is the Moore automaton Detail");
		System.out.println("Input Alphabet: " + Sigma);
		System.out.println("State Set: " + stateSet);
		System.out.println("lambda map: " + lambdaMap);
		System.out.println("Start State: " + startState);
		System.out.println("Transition Table: " + transitionTable);
	}
	
	
	// Writes a Kripke structure to an smv formatted file.
	// Note that if stateSet is of size one, a dummystate is added
	// to get around the fact that NuSMV converts a one element data set 
	// to a symbolic constant (which stops all model checking)

	public void MooreToSMV(String ltl, String smvpath, String filePrefix) {
		
		String destination; // Holds the next state
		String symbolicOutput; // Holds a single Boolean output value
		StringBuilder outputDeclaration;
		
		try {

			File file = new File(smvpath+File.separator+ filePrefix + ".smv");
			PrintWriter pv = new PrintWriter(file);

			pv.println("MODULE main");
			pv.println("\n");
			pv.println("VAR");
			
			// Assemble the set of state names
			StringBuilder stateDeclaration = new StringBuilder();
			for (String st : this.stateSet) 				
				stateDeclaration.append(st + ",");
			
			////////////////////////////////////////////////////////////////////
			// Next line is to handle one state automata in NuSMV
			// This will be a dead state.
			if (this.stateSet.size() == 1) {
				stateDeclaration.append("dummystate" + ",");
				
			}
			

			
						
			// remove last comma symbol
			stateDeclaration.deleteCharAt(stateDeclaration.length() - 1);
			pv.println("state: {" + stateDeclaration + "};");
			
			//System.out.println("MooreToSMV: declared states ");
			
			// Now declare all output types
			for (int count = 0; count < k; count++) {
				// For each output type typeList.get(count) do
				
				outputDeclaration = new StringBuilder();
				// declare all values of type typeList.get(count)
				
				//System.out.println("MooreToSMV: type is " + typeList.get(count));
				
				//System.out.println("MooreToSMV: invertedOutputTypeMap is " + invertedOutputTypeMap);
				
				for (String symbolicOutputValue : invertedOutputTypeMap.get(typeList.get(count)))  {
					// concatenate all output values of type typeList.get(count)
					outputDeclaration.append(symbolicOutputValue + ",");					
				}
				// remove last comma symbol
				outputDeclaration.deleteCharAt(outputDeclaration.length()-1);
								
				pv.println( typeList.get(count) + " : {" + outputDeclaration + "};");
												
				
			}
			
			//System.out.println("MooreToSMV: declared output types ");
			

			
			//System.out.println("MooreToSMV: finished state declaration ");
			
			// Declare the label array	
			// pv.println("label : array 0.."+(this.k)+" of boolean;\n\n");
			
			// Declare the input alphabet for the 
			// input variable "input"
			pv.println("IVAR");

			StringBuilder sig = new StringBuilder();
			for (String sigm : this.Sigma) 
					sig.append(sigm + ",");
			
			sig.deleteCharAt(sig.length() - 1);
			
			pv.println("input : {" + sig + "};\n");
			pv.println("\n");
			
			// Declare all assignment statements
			pv.println("ASSIGN");
			
			// Declare the initial state
			pv.println("init(state) := " + this.startState + ";");
			

			// Now initialise all output variables
			for (int count = 0; count < k; count++) {
				// For each output type typeList.get(count) do
					
				// Initiate output variable typeList.get(count)
				// to its initial value according to lambdaMap
				pv.println("init("+ typeList.get(count) + ") := " + 
				lambdaMap.get(startState).get(count) + ";");
																
			}

			pv.println("\n");
			
			// Declare the next state function
			pv.println("next(state):= case");

			// declare all transitions

			for (String state : this.stateSet) {
				for (String inputSymbol  : this.Sigma) {
					destination = this.transitionTable.get(state + inputSymbol);
					pv.println("\tstate = " + state + " & input = " + inputSymbol + " : " 
					+ destination + ";");

				}

			}
			
			
			////////////////////////////////////////////////////////////////////
			// Next line is a new addition to handle one state automata in NuSMV
			if (this.stateSet.size() == 1) {
				for (String inputSymbol  : this.Sigma) {
			
					destination = "dummystate";
					pv.println("\tstate = dummystate & input = " + 
					inputSymbol + " : " + destination + ";");

				}
			}
			
			pv.println("\tesac;");
			
			//System.out.println("smvWrite: finished state transition declaration ");
			
			// Declare output function
			pv.println("\n");
			//System.out.println("smvWrite: starting output declaration, k = " + k);
			
			for(int count = 0; count < this.k; count++){
				pv.println("next(" + typeList.get(count) + "):= case");
				
				for (String state : this.stateSet) {
					// For each state declare all its output values
					// on the *next state* for each possible input
					
					for (String inputSymbol : this.Sigma) {
						// For each inputSymbol to state declare the output values
						// of the *next* state.
						destination = this.transitionTable.get(state + inputSymbol); // next state
						
						symbolicOutput = this.lambdaMap.get(destination).get(count);
						pv.println("\tstate = " + state + " & input = " + inputSymbol + " : " + symbolicOutput + ";");
					}

				}
				
				//System.out.println("smvWrite: handling one state machine ");
				
				// Handle one state automaton
				if (this.stateSet.size() == 1) {
	
					
					for (String inputSymbol : this.Sigma) {
						// For each inputSymbol to state declare the output values
						// of the *next* state.
						destination = "dummystate"; // next state
						
						symbolicOutput = this.lambdaMap.get(startState).get(count);
						// output same as initial state
						
						pv.println("\tstate = dummystate & input = " + inputSymbol + " : " + symbolicOutput + ";");
					}
					
				}
				pv.println("\tesac;");
			}
			
			//System.out.println("smvWrite: finished output declaration ");
			
			pv.println("\n");
			pv.println("LTLSPEC");
			pv.println(ltl);
			//pv.flush();
			pv.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void MooreToDot(String pathname, String filename){
		
		try {
			//Scanner input = new Scanner(System.in);
			//String filename = new String();
			//System.out.println("Enter the filename in which you want to write results to....");
			//filename = input.next();
			
	        //File file = new File(j + configurationFilePrefix + ".dot");
	        //PrintWriter pv = new PrintWriter(file);
			
	        File file = new File(pathname + filename + ".dot");
	        PrintWriter pv = new PrintWriter(file);
	        
	        pv.println("digraph finite_state_machine{");
	        pv.println("randir = LR");
	        pv.println("node[shape = circle];");
	        pv.println("a[style = \"invis\"];"); // for automaton entry arrow
	        
	       	//to print a string representation of the start state value       
	        String initialValue = vectorToString(this.lambdaMap.get(startState));
	        pv.println("a->" + "\"" + startState + "\\n\\n" + initialValue + "\"" + ";");
	        
	        // to print a string representation of the other state value labels
	        // and transitions
	        for(String state : stateSet) {
	        	
	        	for(String letter : Sigma) {
	        		pv.println("\""+ state +"\\n\\n" + vectorToString(lambdaMap.get(state))+"\"" +
	        				"->" + "\"" + transitionTable.get(state + letter) + "\\n\\n" + 
	        				vectorToString(lambdaMap.get(transitionTable.get(state + letter))) + 
	        				"\"" + "\t" + "[label=" + letter + "];" );	
	        	} // for letter
	        	
	        }// for state
	        
	        pv.println("}");		
	        pv.close();
	    } 
		catch (IOException e) {
	        e.printStackTrace();
	    }


	}
	

	
	///New method for getting the output trace generated by an input string
	public ArrayList<ArrayList<String>> traceOutput(String inTrace) {
		
		ArrayList<ArrayList<String>> resultTrace = new ArrayList<ArrayList<String>>();
		String Q = this.startState;
		String inputSymbol = new String();
		int count;
		
		//System.out.println("traceOutput: New tracing :" + inTrace);
		
		resultTrace.add(this.lambdaMap.get(Q)); 
		// start from output for the initial state
		
		if (inTrace.isEmpty()) {
			Q = transitionTable.get(Q); // empty input gives initial state
			resultTrace.add(this.lambdaMap.get(Q));
		}
		else {
			
			count = 0;
			for(count = 0; count < inTrace.length(); count++ ) {
				inputSymbol = inTrace.substring(count, count+1);  // get next symbol
				Q = transitionTable.get(Q.concat(inputSymbol)); // get next state						
				resultTrace.add(this.lambdaMap.get(Q));
			}
			
		}
		//System.out.println("traceOutput: resultTrace :" + resultTrace);
		return resultTrace;

	}
	
	///New method for getting the state trace generated by an input string
	public ArrayList<String> newTraceState(String a) {
		
		ArrayList<String> result = new ArrayList<String>();
		String Q = this.startState;
		String inputSymbol = new String();
		int count;
		
		System.out.println("New state tracing :" + a);
		
		if (a.isEmpty() || a.equals("\u03bb")) {
			Q = transitionTable.get(Q); // // empty input gives initial state
			result.add(Q);
		}
		else {
			
			count = 0;
			while ( count < a.length() ) {
				inputSymbol = a.substring(count, count+1);  // get next symbol
				Q = transitionTable.get(Q.concat(inputSymbol)); // next state
				result.add(Q);
				count++;
			}
			
		}
		return result;

	}
	
	public String vectorToString(ArrayList<String> inVector) {
		
		String result = new String();
		
		result = result + inVector.get(0);
		
		for (int i = 1; i < inVector.size(); i++)
			result = result + ";" + inVector.get(i);
				
		return result;
		
	}
	

	@Override
	public String toString()
	{
		String repr = "Moore automaton:\n";
		repr += " - states: 0 - " + this.stateSet.size() + "\n";
		repr += " - transition map: (state . input symbol -> state)\n";
		for (String stateXsymbol : this.transitionTable.keySet())
		{
			repr += stateXsymbol + " -> " + transitionTable.get(stateXsymbol) + "\n";
		}
		repr += " - output map: (state -> output vector)\n";
		for (String state : this.lambdaMap.keySet())
		{
			repr += state + " -> (";
			for (String outputSymbol : this.lambdaMap.get(state))
			{
				repr += outputSymbol + "|";
			}
			repr = repr.substring(0, repr.length() - 1);
			repr += ")\n";
		}
		
		return repr;
	}
}
