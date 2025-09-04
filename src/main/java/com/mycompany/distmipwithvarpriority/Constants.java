/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority;

//import org.apache.log4j.Level;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import org.apache.log4j.Level;

/**
 *
 * @author sst119
 */
public class Constants {
        
    public static final int SOLUTION_CYCLE_TIME_MINUTES = MIP_NAME.contains("satellites")? 120 :  30;
    
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final double  DOUBLE_ONE = 1;
    public static final int TWO = 2;
    public static final int TEN = 10;
    public static final int FIFTY = 50;
    public static final int SIXTY = 60;
    public static final int THOUSAND = 1000;
    public static final int BILLION = 1000 * 1000 * 1000;
    
    public static final double REALTIVE_MIP_GAP_THRESHOLD = 0.0001  ;
    
    public static   final String LOG_FOLDER="./logs"+RANDOM_SEED_ADD +"/"  ; 
    public static   final String LOG_FILE_EXTENSION = ".log";
    public static   final Level LOGGING_LEVEL= Level.INFO ;    
    
    public static final String MIPROOT_NODE_ID = "Node0";
    
   
    
}
