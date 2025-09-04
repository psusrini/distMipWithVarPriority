/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.server;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.Parameters;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import com.mycompany.distmipwithvarpriority.client.*;
import com.mycompany.distmipwithvarpriority.rampup.Rampup;
import com.mycompany.distmipwithvarpriority.subtree.*;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Server {
        
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Server .class); 
    
    //key is client name
    public static Map < String, ClientRequestObject >   map_Of_IncomingRequests   = Collections.synchronizedMap(new HashMap< String, ClientRequestObject > ()); 
    public static Map < String, ServerResponseObject >   responseMap  = Collections.synchronizedMap(new HashMap< String, ServerResponseObject > ()); 
            
    public static double globalIncombent = BILLION;
    public static double lowestBoundOfAllClients = BILLION;
    
    public static long   numNodesProcessed_BY_Completed_Subtrees= ZERO ;
    public static long   numNodesProcessed_BY_InProgress_Subtrees= ZERO ;
    
    //public static double relativeMipGap = BILLION;
    public static List< FarmedLeaf> leafPool_FromRampUp =new ArrayList<FarmedLeaf> ();
    public static int iterationNumber = ZERO;
    
    
    //check how many repo hits
    public int numBranchesMade = ZERO;
    public int numBranchesMade_fromRepo = ZERO;
    
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+  Server.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }        
       
    } 
    
    
    public static void main(String[] args) throws Exception {  
        Rampup ramp_up = new Rampup();
        leafPool_FromRampUp= ramp_up.doRampUp ();
        globalIncombent = ramp_up.getSolutionValue();
        
        if (USE_WELL_KNOWN_OPTIMAL_AT_START < BILLION) globalIncombent=USE_WELL_KNOWN_OPTIMAL_AT_START ;
        
        ExecutorService executor = null;
        try (
                //try with resources 
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT_NUMBER);               
                
            ) {
            String hostname =  InetAddress.getLocalHost(). getHostName() ;
            System.out.println("The   server is running..." + hostname);
            logger.info ("TARGET_BEST_BOUND_FOR_WORKERS "+ TARGET_BEST_BOUND_FOR_WORKERS + 
                    " USE_WELL_KNOWN_OPTIMAL_AT_START " + USE_WELL_KNOWN_OPTIMAL_AT_START) ;
            logger.info ("EMPAHSIS "+ Parameters.CPX_PARAM_MIPEMPHASIS + " RANDOM_SEED "+ RANDOM_SEED) ;
            logger.info("Count repo hits "+Parameters.COUNT_REPO_HITS);
            
            executor = Executors.newFixedThreadPool(  NUM_WORKERS );     
            
            while (true ){
                Socket clientSocket = serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler(clientSocket) ;
                executor.execute(requestHandler);                  
            }
            
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }finally{
            if (executor!=null){
                executor.shutdown();
            }
        }
        
    }
    
}
