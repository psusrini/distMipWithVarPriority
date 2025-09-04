/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.subtree;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author sst119
 */
public class FarmedLeaf implements Serializable {
    
    public String machineName ;
    public String nodeID;
    public double lpRelax;
    public Set<BranchingCondition> varFixings;
    
    public void printMe (){
        System.out.print(" " + machineName + ", "+ nodeID + " ,"+ lpRelax + "  \n") ;
        for (BranchingCondition bc: varFixings){
            System.out.print(bc);
        }
        System.out.println() ;
    }
    
}

