/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority;

import static com.mycompany.distmipwithvarpriority.Constants.*;

/**
 *
 * @author sst119
 */
public class Parameters {
    
    
    // public static final String MIP_NAME = "sing44";
    //public static final String MIP_NAME = "rmatr200-p20";
    //public static final String MIP_NAME = "satellites4-25";
    // public static final String MIP_NAME = "opm2-z10-s4";
    //public static final String MIP_NAME = "splice1k1";
    //public static final String MIP_NAME = "rococoC11";
    public static final String MIP_NAME = "protfold";
    //public static final String MIP_NAME = "neosembley";
    //  public static final String MIP_NAME = "sing44";
    // public static final String MIP_NAME = "roi5alpha10n8";
    //public static final String MIP_NAME = "neosnidda";
    // public static final String MIP_NAME = "sorrell3";
    // public static final String MIP_NAME = "rmatr200-p20";
    
    //set to well known optimum to only push bound
    public static final double USE_WELL_KNOWN_OPTIMAL_AT_START =   -31  ;
    
    public static final int RANDOM_SEED_ADD = 12  ; 
    public static final int RANDOM_SEED =     10000 +  RANDOM_SEED_ADD; 
    
    public static int CPX_PARAM_MIPEMPHASIS =  2  ;//optimality  
    
    public static final boolean USE_VAR_PRIORITY_LIST = false;
     
  
    public static final boolean COUNT_REPO_HITS = false;
    
    
    
    
    public static final String MIP_FILENAME =MIP_NAME  + ".pre.sav";    
    public static String PRIORITY_LIST_FILENAME = MIP_NAME + "_priorityList.ser";
    public static final String MIP_FOLDER = 
             System.getProperty("os.name").toLowerCase().contains("win") ?  "F:\\temporary files here recovered\\": "";
    
    public static final int NUM_WORKERS = 5;
    public static final int MAX_FARMED_LEAVES_COUNT = NUM_WORKERS - 1;
    public static final String SERVER_NAME = "miscan-head";
    public static final int SERVER_PORT_NUMBER = 4444;
    public static final double TARGET_BEST_BOUND_FOR_WORKERS = BILLION ;
    
    public static int CPX_PARAM_VARSEL = 2; //pseudo cost branching
    public static int CPX_PARAM_NODEFILEIND = 3; //disk and compressed
    public static int CPX_PARAM_HEURFREQ = -1; //disabled
    public static int CPX_PARAM_MIPSEARCH = 1 ; //traditional 
    //public static int CPXPARAM_DistMIP_Rampup_Duration = 1;// unused here, forces dist mip
    
    public static boolean USE_MODERATE_CUTS = false;//  MIP_NAME.contains("satellite" )   ; 
     
    
    public static  int MAX_THREADS =   System.getProperty("os.name").toLowerCase().contains("win") ? 1 : (COUNT_REPO_HITS?1:32);
    public static boolean USE_BARRIER_FOR_SOLVING_LP =   MIP_NAME.contains("neoshuahum" ) 
                                                     ||  MIP_NAME.contains("roi5alpha" ) 
                                                     ||  MIP_NAME.contains("bnatt500" ) 
                                                     ||  MIP_NAME.contains("neosirwell" ) 
                                                     ||  MIP_NAME.contains("neosembley" ) 
                                                     ||  MIP_NAME.contains("rococo" )   ||  MIP_NAME.equals("rocococ11" )  ;
    public static int MAX_PRIORITY_VARS  = 50; 
        
}
