/*
 * PhyloTree.java
 *
 * Defines a phylogenetic tree, which is a strictly binary tree
 * that represents inferred hierarchical relationships between species
 *
 * There are weights along each edge; the weight from parent to left child
 * is the same as parent to right child.
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util
 *     java.io
 *
 * Use of any additional Java Class Library components is not permitted
 *
 * Student Name Goes Here
 *
 */

import java.io.File;
import java.util.Scanner;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

public class PhyloTree {
    private PhyloTreeNode overallRoot;    // The actual root of the overall tree
    private int printingDepth;            // How many spaces to indent the deepest node when printing

    // CONSTRUCTOR

    // PhyloTree
    // Pre-conditions:
    //        x speciesFile contains the path of a valid FASTA input file
    //        x printingDepth is a positive number
    // Post-conditions:
    //        x this.printingDepth has been set to printingDepth
    //        x A linked tree structure representing the inferred hierarchical
    //          species relationship has been created, and overallRoot points to
    //          the root of this tree
    // Notes:
    //        x A lot happens in this step!  See assignment description for details
    //          on the input format file and how to construct the tree
    //        x If you encounter a FileNotFoundException, print to standard error
    //          "Error: Unable to open file " + speciesFilename
    //          and exit with status (return code) 1
    //        x Most of this should be accomplished by calls to loadSpeciesFile and buildTree
    public PhyloTree(String speciesFile, int printingDepth) {

        // Set printing depth
        this.printingDepth = printingDepth;

        // Build the tree using load file for data
        buildTree(loadSpeciesFile(speciesFile));

    } // END PhyloTree CONSTRUCTOR

    // ACCESSORS

    // getOverallRoot
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the overall root
    public PhyloTreeNode getOverallRoot() {
        return this.overallRoot;
    }

    // toString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns a string representation of the tree
    // Notes:
    //    - See assignment description for proper format
    //        (it will be a kind of reverse in-order [RNL] traversal)
    //    - Can be a simple wrapper around the following toStringspecies
    //    - Hint: StringBuilder is much faster than repeated concatenation
    public String toString() {
        return toString(this.overallRoot, 0, nodeHeight(this.overallRoot));
    }

    // toString
    // Pre-conditions:
    //    - node points to the root of a tree you intend to print
    //    - weightedDepth is the sum of the edge weights from the
    //      overall root to the current root
    //    - maxDepth is the weighted depth of the overall tree
    // Post-conditions:
    //    - Returns a string representation of the tree
    // Notes:
    //    - See assignment description for proper format
    private String toString(PhyloTreeNode node, double weightedDepth, double maxDepth) {
        StringBuilder builder = new StringBuilder();
        double k = java.lang.Math.round(this.printingDepth * (weightedDepth / weightedNodeHeight(this.overallRoot)));

        if (node.getRightChild() != null) {
            builder.append(toString(node.getRightChild(), weightedDepth + node.getDistanceToChild(), maxDepth));
        }

        for (int i = 0; i < k; i++) {
            builder.append(".");
        }
        builder.append(node.toString());
        builder.append("\n");

        if (node.getLeftChild() != null) {
            builder.append(toString(node.getLeftChild(), weightedDepth + node.getDistanceToChild(), maxDepth));
        }

        return builder.toString();
    }

    // toTreeString
    // Pre-conditions:
    //    x None
    // Post-conditions:
    //    x Returns a string representation in tree format
    // Notes:
    //    x See assignment description for format details
    //    x Can be a simple wrapper around the following toTreeString
    public String toTreeString() {
        return toTreeString(this.overallRoot);
    }

    // toTreeString
    // Pre-conditions:
    //    x node points to the root of a tree you intend to print
    // Post-conditions:
    //    - Returns a string representation in tree format
    // Notes:
    //    - See assignment description for proper format
    private String toTreeString(PhyloTreeNode node) {

        StringBuilder builder = new StringBuilder();

        if (node.getParent() == null) {
            builder.append("(");
            builder.append(toTreeString(node.getRightChild()));
            builder.append(",");
            builder.append(toTreeString(node.getLeftChild()));
            builder.append(")");
        } else if (node.isLeaf()) {
            builder.append(String.format(node.getLabel() + ":%.5f", node.getParent().getDistanceToChild()));
        } else {
            builder.append("(");
            builder.append(toTreeString(node.getRightChild()));
            builder.append(",");
            builder.append(toTreeString(node.getLeftChild()));
            builder.append(String.format("):%.5f", node.getParent().getDistanceToChild()));
        }
        return builder.toString();
    }


    // getHeight
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the tree height as defined in class
    // Notes:
    //    - Can be a simple wrapper on nodeHeight
    public int getHeight() {
        return nodeHeight(this.overallRoot);
    }

    // getWeightedHeight
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the sum of the edge weights along the
    //      "longest" (highest weight) path from the root
    //      to any leaf node.
    // Notes:
    //   - Can be a simple wrapper for weightedNodeHeight
    public double getWeightedHeight() {
        return weightedNodeHeight(this.overallRoot);
    }

    // countAllSpecies
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of species in the tree
    // Notes:
    //    - Non-terminals do not represent species
    //    - This functionality is provided for you elsewhere
    //      just call the appropriate method
    public int countAllSpecies() {
        return this.overallRoot.getNumLeafs();
    }

    // getAllSpecies
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns an ArrayList containing all species in the tree
    // Notes:
    //    - Non-terminals do not represent species
    public java.util.ArrayList<Species> getAllSpecies() {
        ArrayList<Species> allSpecies = new ArrayList<Species>();
        getAllDescendantSpecies(this.overallRoot, allSpecies);
        return allSpecies;
    }

    // findTreeNodeByLabel
    // Pre-conditions:
    //    - label is the label of a tree node you intend to find
    //    - Assumes labels are unique in the tree
    // Post-conditions:
    //    - If found: returns the PhyloTreeNode with the specified label
    //    - If not found: returns null
    public PhyloTreeNode findTreeNodeByLabel(String label) {
        return findTreeNodeByLabel(this.overallRoot, label);
    }

    // findLeastCommonAncestor
    // Pre-conditions:
    //    - label1 and label2 are the labels of two species in the tree
    // Post-conditions:
    //    - If either node cannot be found: returns null
    //    - If both nodes can be found: returns the PhyloTreeNode of their
    //      common ancestor with the largest depth
    //      Put another way, the least common ancestor of nodes A and B
    //      is the only node in the tree where A is in the left tree
    //      and B is in the right tree (or vice-versa)
    // Notes:
    //    - Can be a wrapper around the static findLeastCommonAncestor
     public PhyloTreeNode findLeastCommonAncestor(String label1, String label2) {
        PhyloTreeNode node1 = findTreeNodeByLabel(label1);
        PhyloTreeNode node2 = findTreeNodeByLabel(label2);

        return findLeastCommonAncestor(node1, node2);
    }

    // findEvolutionaryDistance
    // Pre-conditions:
    //    - label1 and label2 are the labels of two species in the tree
    // Post-conditions:
    //    - If either node cannot be found: returns POSITIVE_INFINITY
    //    - If both nodes can be found: returns the sum of the weights
    //      along the paths from their least common ancestor to each of
    //      the two nodes
     public double findEvolutionaryDistance(String label1, String label2) {
        PhyloTreeNode node1 = findTreeNodeByLabel(label1);
        PhyloTreeNode node2 = findTreeNodeByLabel(label2);

        if (node1 == null || node2 == null) {
            return Double.POSITIVE_INFINITY;
        }

        PhyloTreeNode common = findLeastCommonAncestor(node1, node2);

        double path1 = 0.0;
        while (node1 != common) {

            node1 = node1.getParent();
            path1 += node1.getDistanceToChild();
        }

        double path2 = 0.0;
        while (node2 != common) {
            node2 = node2.getParent();
            path2 += node2.getDistanceToChild();
        }

        return path1 + path2;
    }

    // MODIFIER

    // buildTree
    // Pre-conditions:
    //    - species contains the set of species for which you want to infer
    //      a phylogenetic tree
    // Post-conditions:
    //    - A linked tree structure representing the inferred hierarchical
    //      species relationship has been created, and overallRoot points to
    //      the root of said tree
    // Notes:
    //    - A lot happens in this step!  See assignment description for details
    //      on how to construct the tree.
    //    - Be sure to use the tie-breaking conventions described in the pdf
    //    - Important hint: although the distances are defined recursively, you
    //      do NOT want to implement them recursively, as that would be very inefficient
    private void buildTree(Species[] species) {
        ArrayList<PhyloTreeNode> forest = new ArrayList<PhyloTreeNode>();
        HashMap<String, PhyloTreeNode> labelID = new HashMap<String, PhyloTreeNode>();

        // Create a forest where each tree is a single species
        for (int i = 0; i < species.length; i++) {
            PhyloTreeNode node = new PhyloTreeNode(null, species[i]);
            forest.add(node);
            labelID.put(species[i].getName(), node);
        } // END for i

        MultiKeyMap<Double> distMap = new MultiKeyMap<Double>();
        Species a;
        Species b;

        // Intalize and populate the distance map (utilizing multikeymap)
        for (int i = 0; i < forest.size(); i++) {
            for (int j = 0; j < i; j++) {
                a = forest.get(i).getSpecies();
                b = forest.get(j).getSpecies();
                distMap.put(a.getName(), b.getName(), Species.distance(a, b));
            } // END for j
        } // END for i

        String[] minPair;
        PhyloTreeNode treeA, treeB, newTree;
        // Continue building the trees until only one tree is left in the forest
        while (forest.size() != 1) {
            minPair = getMin(distMap.getMap());

            if (minPair[0].compareTo(minPair[1]) > 0) {
                treeA = labelID.get(minPair[1]);
                treeB = labelID.get(minPair[0]);
            } else {
                treeA = labelID.get(minPair[0]);
                treeB = labelID.get(minPair[1]);
            }

            // Build the new parent PhyloTreeNode
            newTree = new PhyloTreeNode(treeA.getLabel() + "+" + treeB.getLabel(),
                                        null, treeA, treeB,
                                        distMap.get(treeA.getLabel(), treeB.getLabel()) / 2.0);
            // Set the children nodes parent
            newTree.getLeftChild().setParent(newTree);
            newTree.getRightChild().setParent(newTree);

            // Remove the old trees from the forest
            forest.remove(treeA);
            forest.remove(treeB);

            // Add the new tree to our labelID hashset
            labelID.put(newTree.getLabel(), newTree);

            // Get the numbers of species on the left / right side of the tree
            double lNum;
            double rNum;
            lNum = newTree.getLeftChild().getNumLeafs();
            rNum = newTree.getRightChild().getNumLeafs();
            lNum = lNum == 0 ? 1 : lNum;
            rNum = rNum == 0 ? 1 : rNum;

            // Set the total number of species
            double tot = lNum + rNum;

            // Update distMap
            for (int i = 0; i < forest.size(); i++) {
                // get each other tree as we iterate through the forest
                PhyloTreeNode node = forest.get(i);
                // Store all the labels so we dont call them a million times
                String lChildLabel = newTree.getLeftChild().getLabel();
                String rChildLabel = newTree.getRightChild().getLabel();
                String nodeLabel = node.getLabel();

                // calculate the distance between the new tree and the current node
                double dist = ((lNum / tot) * (distMap.get(nodeLabel, lChildLabel)) +
                              (rNum / tot) * (distMap.get(nodeLabel, rChildLabel)));

                // Put the distance into distMap
                distMap.put(newTree.getLabel(), nodeLabel, dist);
            } // END for i

            // Remove all previous occurances of the child trees from distMap
            distMap.removeAll(newTree.getLeftChild().getLabel(), newTree.getRightChild().getLabel());

            // Add the new tree to our forest
            forest.add(newTree);
        } // END while
        this.overallRoot = forest.get(0);
    } // END buildTree()

    /*
    public void printHash(HashMap<String, Double> map, String i1, String i2, String i3) {
        for (String name: map.keySet()) {
            String key = name.toString();
            String value = map.get(name).toString();
            if (key.equals(i1 + "|" + i2) || key.equals(i2 + "|" + i1) || key.equals(i1 + "|" + i3) || key.equals(i3 + "|" + i1) || key.equals(i2 + "|" + i3) || key.equals(i3 + "|" + i2)) {
                System.out.println(key + " " + value);
            } // END if
        } // END for
    } // END printHash()
    */

    // utility method for buildTree() that gets the minimum pair from distmap
    public String[] getMin(java.util.HashMap<String, Double> map) {
        Map.Entry<String, Double> min = null;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            } else if (min.getValue() == entry.getValue()) {
                if (min.getKey().compareTo(entry.getKey()) > 0) {
                    min = entry;
                }
            } // END if
        } // END for entry
        String key = min.getKey();
        int ind = key.indexOf("|");
        String a = key.substring(0, ind);
        String b = key.substring(ind + 1, key.length());
        String[] ab = {a, b};

        return ab;
    } // END getMin()

    // STATIC

    // nodeDepth
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns -1
    //    - Else: returns the depth of the node within the overall tree
    public static int nodeDepth(PhyloTreeNode node) {

        if (node == null) {
            return -1;
        }

        int depth = 0;
        while (node.getParent()!= null) {
            depth++;
            node = node.getParent();
        }
        return depth;
    }

    // nodeHeight
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns -1
    //    - Else: returns the height subtree rooted at node
    public static int nodeHeight(PhyloTreeNode node) {

        if (node == null) {
            return -1;
        }

        int lHeight = nodeHeight(node.getLeftChild());
        int rHeight = nodeHeight(node.getRightChild());

        return lHeight > rHeight ? lHeight + 1 : rHeight + 1;
    }

    // weightedNodeHeight
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns NEGATIVE_INFINITY
    //    - Else: returns the weighted height subtree rooted at node
    //     (i.e. the sum of the largest weight path from node
    //     to a leaf; this might NOT be the same as the sum of the weights
    //     along the longest path from the node to a leaf)
    public static double weightedNodeHeight(PhyloTreeNode node) {
        if (node == null) {
            return Double.NEGATIVE_INFINITY;
        }
        double lHeight = 0;
        double rHeight = 0;
        double weight = 0;
        if (node.getLeftChild() != null) {
            lHeight = weightedNodeHeight(node.getLeftChild());
            rHeight = weightedNodeHeight(node.getRightChild());
            weight = node.getDistanceToChild();
        }

        return lHeight > rHeight ? lHeight + weight : rHeight + weight;
    }

    // loadSpeciesFile
    // Pre-conditions:
    //    - filename contains the path of a valid FASTA input file
    // Post-conditions:
    //    - Creates and returns an array of species objects representing
    //      all valid species in the input file
    // Notes:
    //    - Species without names are skipped
    //    - See assignment description for details on the FASTA format
        // Intialize allSpecies
    // Hints:
    //    - Because the bar character ("|") denotes OR, you need to escape it
    //      if you want to use it to split a string, i.e. you can use "\\|"
    public static Species[] loadSpeciesFile(String filename) {

        // Intialize allSpecies
        ArrayList<Species> allSpecies = new ArrayList<Species>();

        // Intalize scanner to read from speciesFile
        Scanner sc = null;
        try{
            File file = new File(filename);
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Error: Unable to open file " + filename);
            System.exit(1);
        } // END try catch block

        // Intalize arraylist to hold sequenceString
        ArrayList<String> sequenceString = new ArrayList<String>();

        // Fence post problem
        String line = sc.nextLine();
        String speciesName = line.substring(line.lastIndexOf('|') + 1, line.length());

        // loop through the fasta file
        String[] temp;
        Species newSpecies;
        while(sc.hasNextLine()) {
            // get the next line of the file
            line = sc.nextLine();

            // hitting a carrot means moving to a new species
            if(line.charAt(0) == '>') {

                // save the old species
                temp = sequenceString.toArray(new String[0]);
                newSpecies = new Species(speciesName, temp);
                allSpecies.add(newSpecies);

                // reset sequenceString
                sequenceString = new ArrayList<String>();
                // get the new species name
                speciesName = line.substring(line.lastIndexOf('|') + 1, line.length());
            } else {
                // if its not a new species line then get the amino acids
                for (int i = 0; i < line.length(); i++) {
                    sequenceString.add(Character.toString(line.charAt(i)));
                } // END for loop i
            } // END if else
        } // END while loop

        // add the last species
        temp = sequenceString.toArray(new String[0]);
        newSpecies = new Species(speciesName, temp);
        allSpecies.add(newSpecies);

        Species[] arr = allSpecies.toArray(new Species[0]);

        return arr;
    } // END loadSpeciesFile()

    // getAllDescendantSpecies
    // Pre-conditions:
    //    - node points to a node in a phylogenetic tree structure
    //    - descendants is a non-null reference variable to an empty arraylist object
    // Post-conditions:
    //    - descendants is populated with all species in the subtree rooted at node
    //      in in-/pre-/post-order (they are equivalent here)
    private static void getAllDescendantSpecies(PhyloTreeNode node, java.util.ArrayList<Species> descendants) {
        if (node.getLeftChild() != null) {
            getAllDescendantSpecies(node.getLeftChild(), descendants);
            getAllDescendantSpecies(node.getRightChild(), descendants);
        } else {
            descendants.add(node.getSpecies());
        } // END if
    } // END getAllDescendantSpecies()


    // findTreeNodeByLabel
    // Pre-conditions:
    //    - node points to a node in a phylogenetic tree structure
    //    - label is the label of a tree node that you intend to locate
    // Post-conditions:
    //    - If no node with the label exists in the subtree, return null
    //    - Else: return the PhyloTreeNode with the specified label
    // Notes:
    //    - Assumes labels are unique in the tree
    private static PhyloTreeNode findTreeNodeByLabel(PhyloTreeNode node, String label) {
        PhyloTreeNode node1 = null;
        PhyloTreeNode node2 = null;


        if (node.getLabel().equals(label)) {
            return node;
        } // END if

        if (node.getLeftChild() != null) {
            node1 = findTreeNodeByLabel(node.getLeftChild(), label);
            node2 = findTreeNodeByLabel(node.getRightChild(), label);
        } // END if

        if (node1 != null) {
            return node1;
        }
        else if (node2 != null) {
            return node2;
        }
        else {
            return null;
        }
    } // END findTreeNodeByLabel()




    // findLeastCommonAncestor
    // Pre-conditions:
    //    - node1 and node2 point to nodes in the phylogenetic tree
    // Post-conditions:
    //    - If node1 or node2 are null, return null
    //    - Else: returns the PhyloTreeNode of their common ancestor
    //      with the largest depth
     private static PhyloTreeNode findLeastCommonAncestor(PhyloTreeNode node1, PhyloTreeNode node2) {
         if (node1 == null || node2 == null) {
             return null;
         } // END if null
         int difference = nodeDepth(node1) - nodeDepth(node2);
         // Aligning node depths
         if (difference > 0) {
             for (int i = 0; i < difference; i++) {
                 node1 = node1.getParent();
             } // END for
         } else if (difference < 0) {
             for (int i = 0; i < -1 * difference; i++) {
                 node2 = node2.getParent();
             } // END for
         } // END if
         if (node1 == node2) {
             return node2;
         } // END if
         // Traversing parents until equal, thus finding common ancestor
         while (node1.getParent() != null && node2.getParent() != null) {
             if (node1.getParent() == node2.getParent()) {
                 return node1.getParent();
             } else {
                 node1 = node1.getParent();
                 node2 = node2.getParent();
             } // END if
         } // END while
         return null;
    } // END findLeastCommonAncestor
} // END PhyloTree class
