/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.drivers;
import static com.mycompany.distmipwithvarpriority.Constants.*;
import static com.mycompany.distmipwithvarpriority.Parameters.USE_WELL_KNOWN_OPTIMAL_AT_START;
import com.mycompany.distmipwithvarpriority.subtree.BranchingCondition;
import com.mycompany.distmipwithvarpriority.subtree.SubTree;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tamvadss
 */
public class SequentialDriver {
    
    public static void main(String[] args) throws Exception {
        //
        SubTree tree = new SubTree (new HashSet<BranchingCondition> ()) ;
        tree.sequentialSolve(BILLION, USE_WELL_KNOWN_OPTIMAL_AT_START); // 100 30 minute iterations = 50 hours
        
    }
    
}
