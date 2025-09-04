/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.server;


import static com.mycompany.distmipwithvarpriority.Constants.BILLION;
import com.mycompany.distmipwithvarpriority.subtree.BranchingCondition;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class ServerResponseObject  implements Serializable {
    
    public double globalIncumbent = BILLION;
    
    //if assignment is not null , use these conditions to create a subtree and start solving it
    public   Set<BranchingCondition> assignment=null ;
            
    //
    public Set < String> pruneList=null;
    
}
