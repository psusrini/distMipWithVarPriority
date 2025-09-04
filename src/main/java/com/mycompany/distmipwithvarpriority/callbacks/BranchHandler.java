/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.callbacks;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BranchCallback;
import ilog.cplex.IloCplex.NodeId;
import java.util.Set;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.subtree.*;

/**
 *
 * @author tamvadss
 */
public class BranchHandler extends BranchCallback {
    
    private Set<String> nodeIDs_for_pruning = null;
    
    private RepositoryStatistics repoStatistics =null;
    
    public BranchHandler (Set<String> nodes_for_pruning){
        this.nodeIDs_for_pruning = nodes_for_pruning;
    }
    
    public void setRepositoryStatistics (RepositoryStatistics repoStatistics){
        this.repoStatistics = repoStatistics;
    }
    
    private boolean isPruneTarget () throws IloException {
        return nodeIDs_for_pruning!=null && nodeIDs_for_pruning.contains(getNodeId().toString() );
    }
 
    protected void main() throws IloException {
        // 
        if ( getNbranches()> ZERO && ! isPruneTarget()){  
            
            String thisNodeID=getNodeId().toString();
            if (thisNodeID.equals( MIPROOT_NODE_ID)){
                //root node
                NodeAttachment attachment = new   NodeAttachment ( );
                setNodeData (attachment );
            } 
            
            //get the branches about to be created
            IloNumVar[][] vars = new IloNumVar[TWO][] ;
            double[ ][] bounds = new double[TWO ][];
            IloCplex.BranchDirection[ ][]  dirs = new  IloCplex.BranchDirection[ TWO][];
            getBranches(  vars, bounds, dirs);
            
            NodeAttachment thisNodesAttachment = null;
            try {
                thisNodesAttachment  = (NodeAttachment) getNodeData () ;
            }        catch (Exception ex){
                //stays null
            }
            
            if (null != repoStatistics) {
                String thisBranchingVariable = vars[ZERO][ZERO].getName();
                if (repoStatistics.varsInRepository.contains( thisBranchingVariable)){
                    repoStatistics.numRepoHits++;
                }else{
                    repoStatistics.numRepoMisses++;
                }
            }
            
            //now allow  both kids to spawn
            for (int childNum = ZERO ;childNum<getNbranches();  childNum++) {   

                IloNumVar var = vars[childNum][ZERO];
                double bound = bounds[childNum][ZERO];
                IloCplex.BranchDirection dir =  dirs[childNum][ZERO];     

                boolean isDownBranch = dir.equals(   IloCplex.BranchDirection.Down);

                IloCplex.NodeId  kid = null;
                if (null==thisNodesAttachment){
                    //default
                    kid = makeBranch(var,bound, dir ,getObjValue());
                }else {
                    NodeAttachment attach = new NodeAttachment ( );
                    attach.parentNode = thisNodesAttachment;
                    
                    if (isDownBranch) {
                        attach.amITheDownBranchChild = true;
                        thisNodesAttachment.branchingVarName= var.getName();
                        thisNodesAttachment.upperBound= bound;                         
                    } else {
                        if (thisNodesAttachment.branchingVarName==null){
                            thisNodesAttachment.branchingVarName= var.getName();
                            thisNodesAttachment.upperBound= bound- ONE; 
                        }
                    }
                   
                    //create the kid
                    kid = makeBranch(var,bound, dir ,getObjValue(), attach); 
                }

                //System.out.println("Node " + getNodeId() + " created " + kid + " isdown " + isDownBranch + " var " + var.getName()) ;

            } 

        } else if (getNbranches()> ZERO){
            //prune this node
            prune();
            nodeIDs_for_pruning.remove (getNodeId().toString());
            
            System.out.println("pruned leaf "+getNodeId() ) ;
            
        }
    }
    
}
