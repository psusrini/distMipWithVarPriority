/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.subtree;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.Parameters;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import com.mycompany.distmipwithvarpriority.callbacks.*;
import com.mycompany.distmipwithvarpriority.utils.CplexUtils;
import static com.mycompany.distmipwithvarpriority.utils.CplexUtils.getCPlex;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.NodeId;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author sst119
 */
public class SubTree {
    
    private IloCplex cplex;
    private boolean isCompletelySolved = false;
    private boolean areAllObjCoeffsIntegral = false;
       
    protected static Logger logger;
    
    public static String hostname;
    public  static Set<BranchingCondition> myRootVarFixings =null;
 
    public static Map<String, NodeId > mapOfFarmedNodeIDs = null;
    
    public RepositoryStatistics repoStatistics = new RepositoryStatistics ();
    
    
    public SolveResult solveResult= null;
    public Set<FarmedLeaf> farmedLeafs = null;
     
    static {
        logger=Logger.getLogger(SubTree.class);
        logger.setLevel(LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  
                RollingFileAppender(layout,LOG_FOLDER+SubTree.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);     
            
            hostname =  InetAddress.getLocalHost(). getHostName() ;
             
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    }
    
    public SubTree (Set<BranchingCondition> varFixings) throws Exception {
        myRootVarFixings = varFixings;
        cplex = getCPlex (varFixings , repoStatistics. varsInRepository); 
        areAllObjCoeffsIntegral = CplexUtils.areAllObjectiveCoeffsIntgeral (cplex);
        
        logger.info("CPX_PARAM_MIPEMPHASIS "+ CPX_PARAM_MIPEMPHASIS) ;
        if (areAllObjCoeffsIntegral) logger.info ("areAllObjCoeffsIntegral " ) ;
    }
    
    public boolean isCompletelySolved ( ) throws IloException {
        return isCompletelySolved (BILLION)     ;
    }
    
    public boolean isCompletelySolved ( double upperCutoff) throws IloException {
        boolean condition1 = false;
        boolean condition2 = false;
        boolean condition3 = false;
        if (!isCompletelySolved) {
            condition1 =    (cplex.getStatus().equals( IloCplex.Status.Infeasible ) || cplex.getStatus().equals( IloCplex.Status.Optimal )); 
            condition2 = TARGET_BEST_BOUND_FOR_WORKERS < cplex.getBestObjValue();
            condition3 = areAllObjCoeffsIntegral && (   upperCutoff <= Math.ceil(cplex.getBestObjValue() )) ;
            boolean condition4 =  isWithinMIpGapThreshold(  upperCutoff);
            isCompletelySolved = condition2 || condition1 || condition3|| condition4;
        }
        return isCompletelySolved;
    }
    
    public void end () {
        if (! isCompletelySolved) {
            cplex.end ();
        }
        isCompletelySolved = true;
    }
    
    public SolveResult solve ( double upperCutoff) throws IloException {
        return sequentialSolve(ONE, upperCutoff);
    }
    public SolveResult solve (  ) throws IloException {
        return sequentialSolve(ONE, BILLION);
    }
    
    public Set<FarmedLeaf> farm () throws IloException{
                  
        FarmNodecallback farmingCallback =new FarmNodecallback () ;
        
        cplex.use(farmingCallback);
        cplex.setParam( IloCplex.Param.Threads, ONE);
        //allow unlimited time, although only a few seconds will be needed
        cplex.setParam( IloCplex.Param.TimeLimit, SIXTY * SIXTY* SOLUTION_CYCLE_TIME_MINUTES );
        
        //change to faster emphasis
        cplex.setParam(IloCplex.Param.Emphasis.MIP, ZERO) ; 
        
        cplex.solve();
        
        //restore emphasis
        cplex.setParam(IloCplex.Param.Emphasis.MIP, CPX_PARAM_MIPEMPHASIS) ; 
      
        //reset callbacks, we need to remove the node callback
        cplex.clearCallbacks();
        
        mapOfFarmedNodeIDs = farmingCallback.mapOfFarmedLeaves.getFarmedLeaf_NodeIDs();
        
        farmedLeafs = farmingCallback.mapOfFarmedLeaves.getFarmedLeafs();
        return farmedLeafs;
    }
    
    public void prune (Set<String> pruneList) throws IloException{
        //
        Set<NodeId> nodeIDs = new HashSet <NodeId> () ;
        for (String str: pruneList){
            nodeIDs.add (mapOfFarmedNodeIDs.get(str));
        }
        
        cplex.use (new BranchHandler(pruneList)) ;
        cplex.use (new PruneNodecallback(nodeIDs));
        cplex.setParam( IloCplex.Param.Threads, ONE);
        //allow unlimited time, although only a few seconds will be needed
        cplex.setParam( IloCplex.Param.TimeLimit, SIXTY * SIXTY* SOLUTION_CYCLE_TIME_MINUTES );
        
        //change to faster emphasis
        cplex.setParam(IloCplex.Param.Emphasis.MIP, ZERO) ; 
        
        cplex.solve();    
        
        //restore emphasis
        cplex.setParam(IloCplex.Param.Emphasis.MIP, CPX_PARAM_MIPEMPHASIS) ; 
        
        //reset callbacks, we need to remove the node callback
        cplex.clearCallbacks();
        
    }
    
    public SolveResult sequentialSolve (int iterationLimit ) throws IloException {
        return sequentialSolve (iterationLimit, BILLION) ;
    }
    
    public SolveResult sequentialSolve (int iterationLimit,  double upperCutoff) throws IloException {
        
        SolveResult result = new SolveResult();
        
        BranchHandler bHandler = new BranchHandler(null);
        cplex.use (bHandler) ;
        
        if (Parameters.COUNT_REPO_HITS) bHandler.setRepositoryStatistics(repoStatistics);
        
        cplex.setParam( IloCplex.Param.Threads, MAX_THREADS);
        cplex.setParam( IloCplex.Param.TimeLimit, SIXTY* SOLUTION_CYCLE_TIME_MINUTES );
        cplex.setParam(IloCplex.Param.MIP.Tolerances.UpperCutoff, upperCutoff);
        
        for (int hour = ONE; hour <=iterationLimit ; hour ++){    
            
            cplex.solve();    
            
            double bestSolutionFound = BILLION;
            double relativeMipGap = BILLION;
            if (cplex.getStatus().equals( IloCplex.Status.Feasible ) || cplex.getStatus().equals( IloCplex.Status.Optimal )){
                bestSolutionFound =cplex.getObjValue();
                relativeMipGap=  cplex.getMIPRelativeGap();
            } 
           
            logger.info("" + hour + ","+  bestSolutionFound + ","+  
                cplex.getBestObjValue() + "," + cplex.getNnodesLeft64() +
                "," + cplex.getNnodes64() + "," + relativeMipGap ) ;
            
            
            result.bound= cplex.getBestObjValue();
            result.bestKnownSolution= bestSolutionFound;
            result.numLeafs=cplex.getNnodesLeft64();
            result.numNodesExplored =cplex.getNnodes64();
            result.relativeMIPgap= relativeMipGap;
                           
            if ( this.isCompletelySolved(upperCutoff)){  
                
                logger.info (" Repo hits and misses " + this.repoStatistics.numRepoHits + "," + this.repoStatistics.numRepoMisses);
                
                end();
                break;
            }

        }     
        
        solveResult = result;
        return result;
    }
    
    private boolean isWithinMIpGapThreshold(double upperCutoff) throws IloException {   
       
        boolean condition1 =  cplex.getStatus().equals( IloCplex.Status.Infeasible) || 
               cplex.getStatus().equals( IloCplex.Status.Optimal);
        boolean condition2 = false;
        
        double localCutoff = BILLION;
        if (upperCutoff < BILLION) localCutoff= upperCutoff;
        if (cplex.getStatus().equals( IloCplex.Status.Feasible)){
            double bestLocalSoln = cplex.getObjValue();
            if (bestLocalSoln < localCutoff){
                localCutoff = bestLocalSoln;
            }
        }
        
        if (  !condition1 && localCutoff  < BILLION){
            //|bestbound-upperCutoff|/(1e-10+|upperCutoff|) 
            double dist_mip_gap = Math.abs( cplex.getBestObjValue() - localCutoff);
            
            
            
            double denominator =  DOUBLE_ONE/ (BILLION) ;
            denominator = denominator /TEN;
            denominator = denominator +  Math.abs(localCutoff);
            dist_mip_gap = dist_mip_gap /denominator;
            logger.info ( " dist_mip_gap is " + dist_mip_gap );
            condition2 = dist_mip_gap <  REALTIVE_MIP_GAP_THRESHOLD;
        }
        
        
        
        return condition2 || condition1;
    }
    
    
}
