/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.utils;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.Parameters;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import com.mycompany.distmipwithvarpriority.subtree.BranchingCondition;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author sst119
 */
public class CplexUtils {
    
    public static IloCplex getCPlex (Set<BranchingCondition> varFixings, Set<String> varsInRepository) throws Exception {
        
        IloCplex cplex = new IloCplex();
        cplex.importModel(  MIP_FOLDER + MIP_FILENAME);
        
        applyVarFixings (cplex, varFixings) ;
        
        cplex.setParam( IloCplex.Param.MIP.Strategy.File,  CPX_PARAM_NODEFILEIND );
        cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq ,CPX_PARAM_HEURFREQ); 
        cplex.setParam(IloCplex.Param.Emphasis.MIP, CPX_PARAM_MIPEMPHASIS) ; 
        if (USE_BARRIER_FOR_SOLVING_LP) {
            cplex.setParam( IloCplex.Param.NodeAlgorithm  ,  IloCplex.Algorithm.Barrier);
            cplex.setParam( IloCplex.Param.RootAlgorithm  ,  IloCplex.Algorithm.Barrier);
        }
        
        cplex.setParam(IloCplex.Param.MIP.Strategy.Search, CPX_PARAM_MIPSEARCH ) ; 
        cplex.setParam(IloCplex.Param.MIP.Strategy.VariableSelect,  CPX_PARAM_VARSEL ) ; 
        
        if (Parameters.RANDOM_SEED> ZERO) cplex.setParam( IloCplex.Param.RandomSeed, Parameters.RANDOM_SEED);
        
        if (USE_MODERATE_CUTS)        setModerateCuts (cplex) ; 
        
        cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, REALTIVE_MIP_GAP_THRESHOLD);
        
        if (USE_VAR_PRIORITY_LIST){
            File file = new File(PRIORITY_LIST_FILENAME ); 
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(PRIORITY_LIST_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<String>  recreatedVarPriorityList = (List<String>) ois.readObject();
                ois.close();
                fis.close();
                
                int priorityValue = ONE + MAX_PRIORITY_VARS;
                Map<String, IloNumVar> varMap = CplexUtils. getVariables (  cplex);
                for (String varname :  recreatedVarPriorityList){
                    cplex.setPriority(varMap.get (varname) , priorityValue-- );
                    //make a note of the vars included in the repo
                    varsInRepository.add(varname);
                }
            }else {
                System.err.println("cannot find var priority list ! ") ;
                exit (1);
            }
        }
        
        return cplex;        
    }
    
    
    public static void setModerateCuts  (IloCplex cplex) throws IloException {
        cplex.setParam( IloCplex.Param.MIP.Cuts.BQP  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.Cliques  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.Covers  , ONE);	
        cplex.setParam( IloCplex.Param.MIP.Cuts.Disjunctive  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.FlowCovers  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.PathCut  , ONE);	
        cplex.setParam( IloCplex.Param.MIP.Cuts.Gomory  , ONE);        
        cplex.setParam( IloCplex.Param.MIP.Cuts.GUBCovers  , ONE);	 
        cplex.setParam( IloCplex.Param.MIP.Cuts.Implied  , ONE);        
        cplex.setParam( IloCplex.Param.MIP.Cuts.LocalImplied  , ONE);        
        cplex.setParam( IloCplex.Param.MIP.Cuts.LiftProj  , ONE);        
        cplex.setParam( IloCplex.Param.MIP.Cuts.MIRCut  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.MCFCut  , ONE);        
        cplex.setParam( IloCplex.Param.MIP.Cuts.RLT  , ONE);
        cplex.setParam( IloCplex.Param.MIP.Cuts.ZeroHalfCut  , ONE);
    
    }
    
    public static boolean areAllObjectiveCoeffsIntgeral (IloCplex cplex) throws IloException {
        
        boolean result = true;
        
        //TreeMap<String, Double>  objectiveMap = new TreeMap<String, Double>();
        
        IloObjective  obj = cplex.getObjective();
        
        double constantterm = obj.getConstant();
        boolean isConstantIntegral = (constantterm == Math.floor(constantterm));
        if (! isConstantIntegral) result = false;
       
        IloLinearNumExpr expr = (IloLinearNumExpr) obj.getExpr();
                 
        IloLinearNumExprIterator iter = expr.linearIterator();
        while (iter.hasNext() && isConstantIntegral) {
           IloNumVar var = iter.nextNumVar();
           
         
           double val = iter.getValue();
           if (val > Math.floor(val)){
               result = false;
               break;
           }
           
           if (var.getType().equals( IloNumVarType.Float)) {
               result = false;
               break;
           }
           
           //objectiveMap.put(var.getName(),   val   );
           
        }
        
        //if (result)  System.out.println("All Objective Coeffs are Intgeral") ;
        return  result  ;        
         
    }
    
    public static void applyVarFixings ( IloCplex cplex, Set<BranchingCondition> varFixings) throws IloException {
        Map<String, IloNumVar> varMap = CplexUtils. getVariables (  cplex);
        
        for (BranchingCondition condition : varFixings ){
            
            IloNumVar var= varMap.get (condition.varName);
            double newBound= condition.bound;
            if (  condition.isBranchDirectionDown ){
                CplexUtils.updateVariableBounds( var,   newBound, true  )   ;
            }else {
                CplexUtils.updateVariableBounds( var,   newBound, false  )   ;
            }
            
        }
    }
    
           
    /**
     * 
     *  Update variable bounds as specified    
    */
    public static   void updateVariableBounds(IloNumVar var, double newBound, boolean isUpperBound   )      throws IloException{
 
        if (isUpperBound){
            if ( var.getUB() > newBound ){
                //update the more restrictive upper bound
                var.setUB( newBound );
                //System.out.println(" var " + var.getName() + " set upper bound " + newBound ) ;
            }
        }else{
            if ( var.getLB() < newBound){
                //update the more restrictive lower bound
                var.setLB(newBound);
                //System.out.println(" var " + var.getName() + " set lower bound " + newBound ) ;
            }
        }  

    } 
    
    public static Map<String, IloNumVar> getVariables (IloCplex cplex) throws IloException{
        Map<String, IloNumVar> result = new HashMap<String, IloNumVar>();
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        for (IloNumVar var :variables){
            result.put(var.getName(),var ) ;
        }
        return result;
    }
    
}
