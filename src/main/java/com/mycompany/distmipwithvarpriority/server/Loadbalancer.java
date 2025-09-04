/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.server;


import static com.mycompany.distmipwithvarpriority.Constants.ZERO;
import com.mycompany.distmipwithvarpriority.subtree.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class Loadbalancer {
    
    private static TreeMap<Double, List<FarmedLeaf>> migrationCandidates = new  TreeMap<Double, List<FarmedLeaf>> ();
    
    public static Map <String, Set<BranchingCondition> > assignments = new HashMap <String, Set<BranchingCondition> > ();
    public static Map <String, Set<String> > pruneInstructions = new HashMap <String, Set<String> > ();
    
    public static void initialize() {
        migrationCandidates.clear();
        assignments.clear();
        pruneInstructions.clear();
    }
    
    public static void printResults (){
        for (Map.Entry <String, Set<String> > entry : pruneInstructions.entrySet()){
            System.out.println("prune instrs for "+ entry.getKey()) ;
            for (String nidd : entry.getValue()){
                System.out.println("node id "+ nidd) ;
            }
        }
        
        for (Map.Entry <String, Set<BranchingCondition> > entry : assignments.entrySet()){
            System.out.println("assignments  for "+ entry.getKey()) ;
            for (BranchingCondition bc : entry.getValue()){
                System.out.println("bc is  "+ bc.toString()) ;
            }
        }
    }
    
    public static void printMigrationCandidates () {
        for (Map.Entry<Double, List<FarmedLeaf>> entry : migrationCandidates.entrySet()){
            System.out.println(entry.getKey() + " avail size "+ entry.getValue().size()) ;
        }
    }
    
    public static void balance (Set<String> idleClients){
        System.out.println ("there are thi smany idle clients "+ idleClients.size()) ;
        for (String client : idleClients){
            if (migrationCandidates.size()>ZERO){
                Double bestKey = migrationCandidates.firstEntry().getKey();
                List<FarmedLeaf> bestLeafs= migrationCandidates.firstEntry().getValue();
                FarmedLeaf migrationCandidate = bestLeafs.remove(ZERO);
                if (bestLeafs.isEmpty()){
                    migrationCandidates.remove(bestKey);
                }else {
                    migrationCandidates.put (bestKey,bestLeafs );
                }
                
                assignments.put (client, migrationCandidate.varFixings);
                updatePruneInstructions (migrationCandidate.machineName, migrationCandidate.nodeID) ;
            }
        }
        //System.out.println();
    }
    
    public static void addMigrationCandidates (Set<FarmedLeaf>  leafs){
        for (FarmedLeaf fl:  leafs){
            addMigrationCandidate (fl );    
        }
    }
    
    public static void addMigrationCandidate (FarmedLeaf fl){
        List<FarmedLeaf> current = migrationCandidates.get (fl.lpRelax) ;
        if (null==current){
            current = new ArrayList<FarmedLeaf> ( );
        }
        current.add (fl );
        migrationCandidates.put (fl.lpRelax, current) ;
    }
    
    private static void updatePruneInstructions (String machineName, String nodeID) {
        Set<String> currentInstructions = pruneInstructions.get( machineName);
        if (currentInstructions==null){
            currentInstructions = new HashSet<String> ();
        }
        currentInstructions.add (nodeID);
        pruneInstructions.put (machineName,currentInstructions );
    }
    
}
