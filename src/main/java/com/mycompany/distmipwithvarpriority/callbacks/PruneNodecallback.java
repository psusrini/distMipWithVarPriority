/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.callbacks;

import static com.mycompany.distmipwithvarpriority.Constants.ZERO;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.NodeCallback;
import ilog.cplex.IloCplex.NodeId;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author tamvadss
 */
public class PruneNodecallback extends NodeCallback {
    
    private Set<NodeId> nodeIDs_for_pruning = null;
    
    public PruneNodecallback (Set<NodeId> nodes_for_pruning){
        this.nodeIDs_for_pruning = nodes_for_pruning;
    }

  
    protected void main() throws IloException {
        //
        final long LEAFCOUNT =getNremainingNodes64();
        
        if (LEAFCOUNT>ZERO   ) {
            
            IloCplex.NodeId pruneTarget =  getPruneTarget();
            if (pruneTarget!=null){                
                
                nodeIDs_for_pruning.remove(pruneTarget );
                
                selectNode ( getNodeNumber64( pruneTarget )  ) ;
                //System.out.println(" node selected " + pruneTarget ) ;
            }else {
                abort ();
            }
            
        }
    }
    
    //sometimes, cplex silenty prunes infeasible nodes we were planning to prune explicitly
    private  IloCplex.NodeId   getPruneTarget () {
        IloCplex.NodeId  result = null;
           
        for (IloCplex.NodeId nd: nodeIDs_for_pruning){
            try  {
                getNodeNumber64(nd);
                result =nd ;                
                break;
            } catch (IloException iloEx) {
                //System.out.println(" no longer exists -------------- " + nd );
            }
        }         
        return result;
    }
    
}
