/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.drivers;

/**
 *
 * @author sst119
 */

import com.mycompany.distmipwithvarpriority.subtree.*; 
import ilog.cplex.IloCplex.NodeId;
import static java.lang.System.exit;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tamvadss
 */
public class TestDriver {
     
    public static void main(String[] args) throws Exception {
        
        /*SizeConstrainedMap scMap = new SizeConstrainedMap ();
        
        FarmedLeaf leaf1 = new FarmedLeaf ();
        leaf1.lpRelax = 100;
        leaf1.machineName= "testmach";
        leaf1.nodeID = "100";
        leaf1.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf1, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf2 = new FarmedLeaf ();
        leaf2.lpRelax = 99;
        leaf2.machineName= "testmach";
        leaf2.nodeID = "99";
        leaf2.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf2, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf3 = new FarmedLeaf ();
        leaf3.lpRelax = 98;
        leaf3.machineName= "testmach";
        leaf3.nodeID = "98";
        leaf3.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf3, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf4 = new FarmedLeaf ();
        leaf4.lpRelax = 100;
        leaf4.machineName= "testmach";
        leaf4.nodeID = "100a";
        leaf4.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf4, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf5 = new FarmedLeaf ();
        leaf5.lpRelax = 99;
        leaf5.machineName= "testmach";
        leaf5.nodeID = "99a";
        leaf5.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf5, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf6 = new FarmedLeaf ();
        leaf6.lpRelax = 99.5;
        leaf6.machineName= "testmach";
        leaf6.nodeID = "99.5";
        leaf6.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf6, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf7 = new FarmedLeaf ();
        leaf7.lpRelax = 99.4;
        leaf7.machineName= "testmach";
        leaf7.nodeID = "99.4";
        leaf7.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf7, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        FarmedLeaf leaf8 = new FarmedLeaf ();
        leaf8.lpRelax = 98;
        leaf8.machineName= "testmach";
        leaf8.nodeID = "98a";
        leaf8.varFixings = new  HashSet<BranchingCondition> ();
        scMap.add (leaf8, null);
        System.out.println("" + scMap.size + ","+ scMap.largestLPrelax) ;
        
        for (FarmedLeaf fn :   scMap.getFarmedLeafs()){
            fn.printMe();
        }
        for (String str : scMap.getFarmedLeaf_NodeIDs().keySet()){
            System.out.println(str) ;
        }*/
        
        //
        SubTree tree = new SubTree (new HashSet<BranchingCondition> ()) ;
        tree.solve();
        Set<FarmedLeaf> farmedNodes = tree.farm();
        FarmedLeaf someLeaf = null;
        for (FarmedLeaf fn :  farmedNodes){
            fn.printMe();
            someLeaf = fn;
        }
        
        tree.prune(SubTree.mapOfFarmedNodeIDs.keySet());
        
        SubTree newTree = new SubTree (someLeaf.varFixings) ;
        newTree.solve();
        farmedNodes = newTree.farm();
        someLeaf = null;
        for (FarmedLeaf fn :  farmedNodes){
            fn.printMe();
            someLeaf = fn;
        }
        
        
        
        
    }
    
}

