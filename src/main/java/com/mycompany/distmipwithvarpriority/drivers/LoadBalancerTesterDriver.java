/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.drivers;

import com.mycompany.distmipwithvarpriority.server.*;
import com.mycompany.distmipwithvarpriority.subtree.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tamvadss
 */
public class LoadBalancerTesterDriver {
    
    public static void main(String[] args) throws Exception {
        Loadbalancer.initialize();
        
        Set<FarmedLeaf> availableNodes = new HashSet <FarmedLeaf> ();
        Loadbalancer.addMigrationCandidates(availableNodes );
        
        FarmedLeaf f1 = new FarmedLeaf ();
        f1.machineName= "n01";
        f1.nodeID = "n01_1";
        f1.varFixings = new HashSet<BranchingCondition>();
        BranchingCondition bc11 = new BranchingCondition();
        bc11.varName ="bc11";
        f1.varFixings.add( bc11);
        f1.lpRelax = 1.1;
        
                
        FarmedLeaf f2 = new FarmedLeaf ();
        f2.machineName= "n02";
        f2.nodeID = "n02_1";
        f2.varFixings = new HashSet<BranchingCondition>();
        BranchingCondition bc12 = new BranchingCondition();
        bc12.varName ="bc12";
        f2.varFixings.add( bc12);
        f2.lpRelax = 2.2;
         
                
        FarmedLeaf f3 = new FarmedLeaf ();
        f3.machineName= "n02";
        f3.nodeID = "n02_2";
        f3.varFixings = new HashSet<BranchingCondition>();
        BranchingCondition bc13 = new BranchingCondition();
        bc13.varName ="bc13";
        f3.varFixings.add( bc13);
        f3.lpRelax = 3.3;
        
        availableNodes.add (f1);
        availableNodes.add (f2);
        availableNodes.add (f3);
        Loadbalancer.addMigrationCandidates(availableNodes );
       
        
        Set<String> idleClients  = new HashSet<String> ();
        idleClients.add ("n06") ;
        idleClients.add ("n03") ;
        //idleClients.add ("n04") ;
        //idleClients.add ("n05") ;
        Loadbalancer.balance(idleClients);
        
        System.out.println();
        Loadbalancer.printResults();
    }
    
}
