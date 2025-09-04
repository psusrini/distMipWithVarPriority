/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.rampup;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import com.mycompany.distmipwithvarpriority.subtree.*;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tamvadss
 */
public class RampupNodecallback extends IloCplex.NodeCallback {
    
    public List<FarmedLeaf> result = new ArrayList<FarmedLeaf>();
 
    protected void main() throws IloException {
        //
       
        if (getNremainingNodes64()==NUM_WORKERS){
            //prepare leafs
            for (int leafNum = ZERO; leafNum < NUM_WORKERS ; leafNum ++){
                //
                NodeAttachment attach = (NodeAttachment) getNodeData (leafNum) ;
                FarmedLeaf leaf = new FarmedLeaf ();
                leaf .varFixings =  getLeafBranchingCOnditions (attach) ;
                result.add (leaf );
            }

            abort();
        }
    }
    
  
    private  Set<BranchingCondition> getLeafBranchingCOnditions(NodeAttachment attachment){
        Set<BranchingCondition> branchingConditions = new HashSet<BranchingCondition> ();
        
        NodeAttachment current = attachment;
        NodeAttachment parent = current.parentNode;
        while (null != parent){
            BranchingCondition cond = new BranchingCondition ();
            if (current.amITheDownBranchChild) {
                cond.bound = parent.upperBound;
                cond.isBranchDirectionDown= true;
                cond.varName = parent.branchingVarName;
            }else {
                cond.bound = ONE + parent.upperBound;
                cond.isBranchDirectionDown= false;
                cond.varName = parent.branchingVarName;
            }
            
            branchingConditions.add (cond);
            
            current = parent;
            parent = parent.parentNode;
            
        }
        
        return branchingConditions;
    }
}
