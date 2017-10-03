/* 
 * Lattice.java
 *
 * Defines a new "Lattice" type, which is a directed acyclic graph that
 * compactly represents a very large space of speech recognition hypotheses
 *
 * Note that the Lattice type is immutable: after the fields are initialized
 * in the constructor, they cannot be modified.
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util 
 *     java.io
 *     
 * as well as the class java.math.BigInteger
 * 
 * Use of any additional Java Class Library components is not permitted 
 * 
 * Haley Beavers
 *
 */
import java.lang.StringBuilder;
import java.math.BigInteger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.HashSet;

public class Lattice {
    private String utteranceID;       // A unique ID for the sentence
    private int startIdx, endIdx;     // Indices of the special start and end tokens
    private int numNodes, numEdges;   // The number of nodes and edges, respectively
    private Edge[][] adjMatrix;       // Adjacency matrix representing the lattice
                                      //   Two dimensional array of Edge objects
                                      //   adjMatrix[i][j] == null means no edge (i,j)
    private double[] nodeTimes;       // Stores the timestamp for each node
    private int[] iCoor;              // Stores coordinates of filled matrix in i
    private int[] jCoor;			  // Stores coordinates of filled matrix in j

    // Constructor
    /* Lattice
    // Preconditions:
    //     - latticeFilename contains the path of a valid lattice file
    // Post-conditions
    //     - Field id is set to the lattice's ID
    //     - Field startIdx contains the node number for the start node
    //     - Field endIdx contains the node number for the end node
    //     - Field numNodes contains the number of nodes in the lattice
    //     - Field numEdges contains the number of edges in the lattice
    //     - Field adjMatrix encodes the edges in the lattice:
    //        If an edge exists from node i to node j, adjMatrix[i][j] contains
    //        the address of an Edge object, which itself contains
    //           1) The edge's label (word)
    //           2) The edge's acoustic model score (amScore)
    //           3) The edge's language model score (lmScore)
    //        If no edge exists from node i to node j, adjMatrix[i][j] == null
    //     - Field nodeTimes is allocated and populated with the timestamps for each node
    // Notes:
    //     - If you encounter a FileNotFoundException, print to standard error
    //         "Error: Unable to open file " + latticeFilename
    //       and exit with status (return code) 1
    //     - If you encounter a NoSuchElementException, print to standard error
    //         "Error: Not able to parse file " + latticeFilename
    //       and exit with status (return code) 2 */
    public Lattice(String latticeFilename) {
    	try { 
    		// reading from input lattice file 
        	File latticeFile      = new File(latticeFilename);
        	Scanner latticeReader = new Scanner(latticeFile);
    
    		// traversal vars
        	int z = 0;
        	int i = 0;
        	int j = 0;
        
        	// initializing class fields 
        	while(latticeReader.hasNextLine()) {
            	switch(latticeReader.next()) {
        			case "id": 
        		           this.utteranceID = latticeReader.next(); 
        		           break;
        			case "start": 
        		           this.startIdx = latticeReader.nextInt();
        		           break;
        			case "end": 
        		           this.endIdx = latticeReader.nextInt();
        		           break;
        			case "numNodes": 
        		           this.numNodes = latticeReader.nextInt();
        		           this.nodeTimes = new double[this.endIdx + 1];
        		           this.adjMatrix = new Edge[this.endIdx + 1][this.endIdx + 1];
        		           break;
        			case "numEdges": 
        		           this.numEdges = latticeReader.nextInt();
        		           this.iCoor = new int[getNumEdges()];
        		           this.jCoor = new int[getNumEdges()];
        		           break;
        			case "node": 
        		           this.nodeTimes[latticeReader.nextInt()] = latticeReader.nextDouble();
        		           break;
        			case "edge": 
        		           // storing valid coordinates to be iterated through 
        		           i = latticeReader.nextInt();
        		           j = latticeReader.nextInt();
        		           this.iCoor[z] = i;
        		           this.jCoor[z] = j; 
        		           this.adjMatrix[i][j] = new Edge(latticeReader.next(), latticeReader.nextInt(), latticeReader.nextInt());
        		           z++;
        		           break;
        		  default: System.out.println("Error: hit default case.");
        		           break;
        		} // END switch statement
        	} // END while loop
        } // END try 

		catch (FileNotFoundException e) {
        	System.out.println("Error: Unable to open file " + latticeFilename);
        	System.exit(1);
        } // END catch KILL
        
        catch (NoSuchElementException e) {
        	System.out.println("Error: Unable to open file " + latticeFilename);
        	System.exit(2);
        } // END catch KILL
    } // END Lattice() obj CONSTRUCTOR 
    
    // ACCESSORS 
    /* getUtteranceID
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the utterance ID */
    public String getUtteranceID() {
        return this.utteranceID;
    } // END getUtteranceID() ACCESSOR

    /* getNumNodes
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of nodes in the lattice */
    public int getNumNodes() {
        return this.numNodes;
    } // END getNumNodes() ACCESSOR
    
    /* getNumEdges
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of edges in the lattice */
    public int getNumEdges() {
        return this.numEdges;
    } // END getNumEdges() ACCESSOR
    
    /* toString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Constructs and returns a string describing the lattice in the same
    //      format as the input files.  Nodes should be sorted ascending by node 
    //      index, edges should be sorted primarily by start node index, and 
    //      secondarily by end node index 
    // Notes:
    //    - Do not store the input string verbatim: reconstruct it on the fly
    //      from the class's fields
    //    - toString simply returns a string, it should not print anything itself
    // Hints:
    //    - You can use the String.format method to print a floating point value 
    //      to two decimal places
    //    - A StringBuilder is asymptotically more efficient for accumulating a
    //      String than repeated concatenation */
    public String toString() {
        int edges = getNumEdges();
    	StringBuilder s = new StringBuilder()
                   .append("id " + getUtteranceID() + "\n")
                   .append("start " + this.startIdx + "\n")
                   .append("end " + this.endIdx + "\n")
                   .append("numNodes " + getNumNodes() + "\n")
                   .append("numEdges " + edges + "\n");
                   
        // appending nodes  
        for (int i = this.startIdx; i <= this.endIdx; i++) {
        	s.append("node " + i + " " + this.nodeTimes[i] + "\n"); 
        } // END for loop 
        
        // appending edges
        int i = 0;
        int j = 0;
        for (int t = 0; t < edges; t++) {
        	if (t == edges - 1) {
        		i = this.iCoor[t];
            	j = this.jCoor[t];
            	Edge e = new Edge(this.adjMatrix[i][j]);
        		s.append("edge " + i + " " + j + " " + e.getLabel() + " " + e.getAmScore() + " " + e.getLmScore());
        		break;
        	} // END if 
            i = this.iCoor[t];
            j = this.jCoor[t];
            Edge e = new Edge(this.adjMatrix[i][j]);
        	s.append("edge " + i + " " + j + " " + e.getLabel() + " " + e.getAmScore() + " " + e.getLmScore() + "\n");
        } // END for loop
                   
        String ns = s.toString(); 
        return ns;
    } // END toString()

    /* decode
    // Pre-conditions:
    //    - lmScale specifies how much lmScore should be weighted
    //        the overall weight for an edge is amScore + lmScale * lmScore
    // Post-conditions:
    //    - A new Hypothesis object is returned that contains the shortest path
    //      (aka most probable path) from the startIdx to the endIdx
    // Hints:
    //    - You can create a new empty Hypothesis object and then
    //      repeatedly call Hypothesis's addWord method to add the words and 
    //      weights, but this needs to be done in order (first to last word)
    //      Backtracking will give you words in reverse order.
    //    - java.lang.Double.POSITIVE_INFINITY represents positive infinity
    // Notes:
    //    - It is okay if this algorithm has time complexity O(V^2) */
    public Hypothesis decode(double lmScale) {
        Hypothesis hypothesis = new Hypothesis();
    	double[] d = new double[this.endIdx + 1];
    	int[] p = new int[this.endIdx + 1];
    	
    	for (int k = 0; k <= this.endIdx; k++) {
    		d[k] = java.lang.Double.POSITIVE_INFINITY;
    		p[k] = 0;
    	} // END for loop
    
    	d[this.startIdx] = 0;
    	int[] sorted = this.topologicalSort();
    	for (int i : sorted) {
    		for (int j = 0; j <= this.endIdx; j++) {
    			if (this.adjMatrix[i][j] != null) {
    				if (d[i] + this.adjMatrix[i][j].getCombinedScore(lmScale) < d[j]) {
    					d[j] = d[i] + this.adjMatrix[i][j].getCombinedScore(lmScale);
    					p[j] = i;
    				} // END if SHORTER PATH
    			} // END if NON NULL
    		} // END for ADJSET(n)
    	} // END for NODES IN PLAUSIBLE ORDERINGS
    	
    	Stack<Integer> s = new Stack<Integer>();
    	int n = this.endIdx;
    	
    	while (n != this.startIdx) {
    		s.push(n);
    		n = p[n];
    	} // END while loop
    	
    	s.push(this.startIdx);
    	
    	int i = 0;
    	int j = 0;
    	while (j != this.endIdx) {
    		i = s.peek();
    		s.pop();
    		j = s.peek();
    		hypothesis.addWord(this.adjMatrix[i][j].getLabel(), this.adjMatrix[i][j].getCombinedScore(lmScale));
    	} // END while loop
    	
        return hypothesis;
    } // END decode()
    
    /* topologicalSort
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - A new int[] is returned with a topological sort of the nodes
    //      For example, the 0'th element of the returned array has no 
    //      incoming edges.  More generally, the node in the i'th element 
    //      has no incoming edges from nodes in the i+1'th or later elements */
    public int[] topologicalSort() {
        ArrayList<Integer> s = new ArrayList<Integer>();
        ArrayList<Integer> a = new ArrayList<Integer>();
    	int[] inDegree = new int[this.endIdx + 1];
    	int r;
    	
    	// creating array of in-degrees indexed by node
    	for (int i = 0; i <= this.endIdx; i++) {
    		for (int j = 0; j <= this.endIdx; j++) {
    			if (this.adjMatrix[i][j] != null) {
    				inDegree[j]++;
    			} // END if NON-NULL COOR
    		} // END for COLUMNS
    	} // END for ROWS
    
    	s.add(this.startIdx);
    	boolean found = false;
    	while (!s.isEmpty()) {
    		Collections.sort(s);
    		a.add(s.get(0));
    		r = s.get(0);
    		s.remove(0);
    		for (int c = 0; c <= this.endIdx; c++) {
    			if (this.adjMatrix[r][c] != null) {
    				inDegree[c]--;
    				if (inDegree[c] == 0) {
    					s.add(c);
    				} // END if 
    			} // END if NON-NULL ADJ NODE
    		} // END for loop ADJ TO R
    	} // END while loop NODES w/ IN-DEGREE 0
    	
    	Arrays.sort(inDegree);
    	if (inDegree[0] != 0) {
    		System.out.println("Error: cycle detected.");
    		System.exit(1);
    	} // END if CYCLE DETECTOR
    	
    	int[] ar = new int[a.size()];
    	for (int t = 0; t < a.size(); t++) {
    		ar[t] = a.get(t);
    	} // END for loop	
    	
        return ar;
    } // END topologicalSort()
	
    /*countAllPaths
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the total number of distinct paths from startIdx to endIdx
    //       (do not count other subpaths)
    // Hints:
    //    - The straightforward recursive traversal is prohibitively slow
    //    - This can be solved efficiently using something similar to the 
    //        shortest path algorithm used in decode
    //        Instead of min'ing scores over the incoming edges, you'll want to 
    //        do some other operation...*/
    public java.math.BigInteger countAllPaths() {
		int[] sorted = this.topologicalSort();
		BigInteger one = new BigInteger("1");
		BigInteger zero = new BigInteger("0");
		BigInteger[] br = new BigInteger[getNumNodes()];
		
		for (int k = 0; k < br.length; k++) {
			br[k] = zero;
		} // END for loop 
		
		br[this.startIdx] = one;
		
		if (this.startIdx == this.endIdx) {
			return one;
		} // END if 
		else {
			for (int i : sorted) {
				for (int j = 0; j <= this.endIdx; j++) {
					if (adjMatrix[i][j] != null) {
						br[j] = br[i].add(br[j]);
					} // END if NON-NULL 
				} // END for loop COL
			} // END for loop ROW
		} // END else 
		
		return br[this.endIdx];
    } // END countAllPaths()
    
    /* getLatticeDensity
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the lattice density, which is defined to be:
    //      (# of non -silence- words in lattice) / (# seconds from start to end index)
	//      Note that multiwords (e.g. to_the) count as a single non-silence word */
    public double getLatticeDensity() {
    	Edge e;
    	int words = 0;
    	
        for (int i = 0; i < this.getNumEdges(); i++) {
        	e = new Edge(this.adjMatrix[this.iCoor[i]][this.jCoor[i]]);
        	if (!e.getLabel().equals("-silence-")) {
        		words++;  
        	} // END if   
        } // END for loop 
        
        return (words / (this.nodeTimes[this.nodeTimes.length - 1] - this.nodeTimes[0]));
    } // END getLatticeDensity()
    
    /* writeAsDot - write lattice in dot format
    // Pre-conditions:
    //    - dotFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice is written in the specified dot format to dotFilename
    // Notes:
    //    - See the assignment description for the exact formatting to use
    //    - For context on the dot format, see    
    //        - http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29
    //        - http://www.graphviz.org/pdf/dotguide.pdf */
    public void writeAsDot(String dotFilename) {
        int edges = getNumEdges();
    	StringBuilder s = new StringBuilder()
                   .append("digraph g {\n")
                   .append("\trankdir=\"LR\"\n");
                   
        // appending edges
        int i = 0;
        int j = 0;
        for (int t = 0; t < edges; t++) {
			i = this.iCoor[t];
            j = this.jCoor[t];
            Edge e = new Edge(this.adjMatrix[i][j]);
            if ( t == edges - 1) {
            	s.append("\t" + i + " -> " + j + " [label = \"" + e.getLabel() + "\"]");
            	break;
            } // END if 
        	s.append("\t" + i + " -> " + j + " [label = \"" + e.getLabel() + "\"]\n");
        } // END for loop
        
        s.append("\n}");       
        
        // writing to file
		try {
        	PrintWriter output = new PrintWriter(dotFilename);
        	output.print(s.toString());
      		output.close();
      	} // END try 
      	catch (FileNotFoundException e) {
      		System.out.println("Error: Unable to open file" + dotFilename);
         	System.exit(1);
      	} // END catch 
    } // END writeAsDot()
	
    /* saveAsFile - write in the simplified lattice format (same as input format)
    // Pre-conditions:
    //    - latticeOutputFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice's toString() representation is written to the output file
    // Note:
    //    - This output file should be in the same format as the input .lattice file */
    public void saveAsFile(String latticeOutputFilename) {
    	try {
        	PrintWriter output = new PrintWriter(latticeOutputFilename);
        	output.print(this.toString());
      		output.close();
      	} // END try 
      	catch (FileNotFoundException e) {
      		System.out.println("Error: Unable to open file" + latticeOutputFilename);
         	System.exit(1);
      	} // END catch 
    } // END saveAsFile()
	
    /* uniqueWordsAtTime - find all words at a certain point in time
    // Pre-conditions:
    //    - time is the time you want to query
    // Post-conditions:
    //    - A HashSet is returned containing all unique words that overlap 
    //      with the specified time
    //     (If the time is not within the time range of the lattice, the Hashset should be empty) */
    public java.util.HashSet<String> uniqueWordsAtTime(double time) {
        HashSet<String> hs = new HashSet<String>();
        HashSet<Integer> validIdx = new HashSet<Integer>();
    	
    	// is within valid range?
    	if (time < this.nodeTimes[0] || time > this.nodeTimes[this.nodeTimes.length - 1]) {
    		return hs;
    	} // END if 
    	else {
    		int initial = Arrays.binarySearch(this.nodeTimes, time);
    		int end = initial;
    		
			// finding range of valid indices within nodeTimes
    		for (int i = initial; i < getNumNodes(); i++) {
    			if (this.nodeTimes[end] == time) {
    				end++;
    			} // END if 
    			else {
    				break;
    			} // END else
    		} // END while loop
    		
    		// finding indices of valid coordinates 
    		for (int i = 0; i < this.jCoor.length; i++) {
    			 if (this.jCoor[i] >= initial && this.jCoor[i] <= end) {
    			 	validIdx.add(i);
    			 } //END if
    		} // END for loop
    		
    		// adding words from found valid coordinate indices  
    		for (int s : validIdx) {
    			hs.add(this.adjMatrix[this.iCoor[s]][this.jCoor[s]].getLabel());
    		} // END for loop 
    	} // END else
    	
        return hs;
    } // END uniqueWordsAtTime()
	
    /* printSortedHits - print in sorted order all times where a given token appears
    // Pre-conditions:
    //    - word is the word (or multiword) that you want to find in the lattice
    // Post-conditions:
    //    - The midpoint (halfway between start and end time) for each instance of word
    //      in the lattice is printed to two decimal places in sorted (ascending) order
    //      All times should be printed on the same line, separated by a single space character
    //      (If no instances appear, nothing is printed)
    // Note:
	//    - java.util.Arrays.sort can be used to sort
    //    - PrintStream's format method can print numbers to two decimal places*/
    public void printSortedHits(String word) {
    	double[] ar = new double[getNumEdges()];
    	int found = 0;
    	
    	// traversing valid coordinates
    	for (int g = 0; g < getNumEdges(); g++) {
    		// traversing valid coordinates progressing if found edge with desired label
    		if (this.adjMatrix[this.iCoor[g]][this.jCoor[g]].getLabel().equals(word)) {
    			ar[found] = (this.nodeTimes[this.iCoor[g]] + this.nodeTimes[this.jCoor[g]]) / 2;
    			found++;
    		} // END if
    	} // END for loop
    	 
    	// preparing valid nodes for sorting 
    	double[] sortable = new double[found];
    	for (int z = 0; z < found; z++) {
    		sortable[z] = ar[z];
    	} // END for loop 
    	
    	// sorting array containing only valid times
    	Arrays.sort(sortable);
    	
    	// printing sorted array of valid times
    	for (int x = 0; x < found; x++) {
    		System.out.printf("%.2f ", sortable[x]);
    	} // END for loop 
    
    	System.out.print("\n");
    } // END printSortedHits()
} // END Lattice class
