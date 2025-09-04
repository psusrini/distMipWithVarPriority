/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.rampup;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.callbacks.*;
import com.mycompany.distmipwithvarpriority.subtree.*;
import com.mycompany.distmipwithvarpriority.utils.CplexUtils;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Rampup {
    private static Logger logger = Logger.getLogger(Rampup.class);
    private  static  IloCplex cplex  ;
    
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+Rampup.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public List< FarmedLeaf> doRampUp () throws   Exception{
        List<FarmedLeaf> result = new ArrayList<FarmedLeaf>();
        
        cplex = CplexUtils.getCPlex(new HashSet<BranchingCondition>(), new HashSet<String> ());
         
        RampupNodecallback rn = new RampupNodecallback ();
        BranchHandler bh = new BranchHandler (new HashSet<String>  ()) ;
        cplex.use (rn);
        cplex.use (bh );
        cplex.solve ();
        //cplex.end();
        
        
        for (FarmedLeaf leaf: rn.result){
            
            result.add (leaf);
        }
        
        return result;
    }
    
    public double getSolutionValue() throws IloException {
        return cplex.getStatus().equals(IloCplex.Status.Feasible) ? cplex.getObjValue() : BILLION;
    }
    
}

