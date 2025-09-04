/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.server;


import static com.mycompany.distmipwithvarpriority.Constants.*;
import static com.mycompany.distmipwithvarpriority.Parameters.NUM_WORKERS;
import com.mycompany.distmipwithvarpriority.client.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class RequestHandler  implements Runnable{
    
    private Socket clientSocket;
        
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RequestHandler .class); 
    private static RollingFileAppender rfa  = null;
        
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            rfa =new  RollingFileAppender(layout,LOG_FOLDER+  RequestHandler.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            rfa.setImmediateFlush(true);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
        
       
    } 
    public RequestHandler ( Socket clientSocket ){        
        this.clientSocket = clientSocket;       
    }
    
    public void run() {
        try (               
                ObjectOutputStream  outputStream = new ObjectOutputStream( clientSocket.getOutputStream());
                ObjectInputStream inputStream =  new ObjectInputStream(clientSocket .getInputStream());                 
            ){
            
            ClientRequestObject requestFromClient =   (ClientRequestObject) inputStream.readObject() ;
            logger.debug (" request recieved from client " + requestFromClient  ) ;           
            
            while (true){          
                //note the request in our synchronized map
                Server.map_Of_IncomingRequests.put( requestFromClient.clientname , requestFromClient);
               
                //process request and prepare response
                //this includes updating the best known solution to the server 
                ServerResponseObject resp = prepareResponse (   requestFromClient );
                
                //send response to client
                outputStream.writeObject(resp);
                
                 
                
                //read the next request from the same client
                requestFromClient =   (ClientRequestObject) inputStream.readObject() ;
                logger.debug (" request recieved from client " + requestFromClient  ) ;    
                //logger.info (" request recieved from client " + requestFromClient.clientName + " is idle? " +  requestFromClient.isIdle ) ;           
            }
                        
            
        }catch (Exception ex){
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            exit(ONE);
        }finally {
            try {
                clientSocket.close();
            }catch (IOException ioex){
                System.err.println(ioex );
                exit(ONE);
            }
        }
    }
    
    private ServerResponseObject prepareResponse (  ClientRequestObject requestFromClient ) throws Exception{
                
        //first wait for all requests to come in
        if (! waitForAllClients()){
            throw new Exception ("ERROR: Request not recieved from all clients !") ;
            //exit(ONE);
        }
        
        //the first thread that notices an empty response map prepares all the responses
        //if you see a full response map, just skip 
        populateResponseMap ( );
         
        
        //remove this client's response from the response map , and return it  
        //
        //the last thread to send its response must also clear the request map, in preparation for
        //the next solution cycle
        ServerResponseObject resp = null;
        synchronized ( Server.responseMap) {
            resp = Server.responseMap.remove( requestFromClient.clientname);         
            if (Server.responseMap.isEmpty()){
                Server.map_Of_IncomingRequests.clear();
            }
        }
        
         
        
        return resp;
        
    }
        
    private void populateResponseMap () throws Exception{
        
        synchronized ( Server.responseMap) {
            if (Server.responseMap.isEmpty()){
                //populate it
                
                Loadbalancer.initialize();
                
                Set<String> idleClients  = new HashSet<String> ();
                Server.lowestBoundOfAllClients = BILLION;
                Server.numNodesProcessed_BY_InProgress_Subtrees = ZERO;
                
                for (Map.Entry<String, ClientRequestObject> entry : Server.map_Of_IncomingRequests.entrySet()){
                    Server.globalIncombent = Math.min (Server.globalIncombent, entry.getValue().local_incumbent) ;     
                    
                    
                    if (entry.getValue().isIdle) {
                        idleClients.add( entry.getKey());
                        Server.numNodesProcessed_BY_Completed_Subtrees  += entry.getValue().numNodesProcessed;
                                                
                    } else {
                        Server.numNodesProcessed_BY_InProgress_Subtrees+=entry.getValue().numNodesProcessed;
                        Server.lowestBoundOfAllClients= Math.min (Server.lowestBoundOfAllClients, entry.getValue().local_bestBound );
                    }
                    
                    if (entry.getValue().availableNodes!= null){
                        Loadbalancer.addMigrationCandidates(entry.getValue().availableNodes);
                    }
                    
                }
                
                long nodesProcessedSoFar =  Server.numNodesProcessed_BY_Completed_Subtrees + Server.numNodesProcessed_BY_InProgress_Subtrees;
                
                logger.info ("" + (Server.iterationNumber  ++ ) + " ,Best solution and bound and nodes processed so far are , " + 
                        Server.globalIncombent + " , " + Server.lowestBoundOfAllClients + " , " + nodesProcessedSoFar);
                      //  " branches made by completed trees " +  Server.numBranchesMade + " repo hits "+ Server.numBranchesInRepository);
                
                boolean haveAllworkersCompletedTheirAssignment =  
                        (idleClients.size()==NUM_WORKERS && Server.leafPool_FromRampUp.size()==ZERO);
               
                boolean isJustAfterRampup = idleClients.size()==NUM_WORKERS && Server.leafPool_FromRampUp!=null &&
                        Server.leafPool_FromRampUp.size() == NUM_WORKERS;
                
                if (haveAllworkersCompletedTheirAssignment  ) {
                    
                    logger.info ("All workers are complete! "+ " total nodes processed is "+nodesProcessedSoFar);
                                                
                    exit(ZERO);
                    
                    //workers are never sent a halt instruction
                    
                   
                    
                }else if (isJustAfterRampup){
                    //just after ramp up
                    for (Map.Entry<String, ClientRequestObject> entry : Server.map_Of_IncomingRequests.entrySet()){
                        ServerResponseObject resp = new ServerResponseObject () ;  
                        resp.assignment = Server.leafPool_FromRampUp.remove(ZERO).varFixings;
                        resp.globalIncumbent = Server.globalIncombent;
                        Server.responseMap.put( entry.getKey(), resp);
                    }
                    
                    logger.info ("ramp up response complete" );
                    
                    if (Server.leafPool_FromRampUp.size()!=ZERO){
                        //error , should not happen
                        throw new Exception ("Ramp up was loo large ! " );
                    }
                    
                }else if (idleClients.size()==ZERO){
                    //  just run another solution cycle
                    for (Map.Entry<String, ClientRequestObject> entry : Server.map_Of_IncomingRequests.entrySet()){
                        ServerResponseObject resp = new ServerResponseObject () ;  
                        resp.globalIncumbent = Server.globalIncombent;
                        Server.responseMap.put( entry.getKey(), resp);
                    }
                    
                }else {
                    
                    //load balance    
                    //Loadbalancer.printMigrationCandidates();
                    Loadbalancer.balance(idleClients);
                    //Loadbalancer.printResults();
                    
                    for (Map.Entry<String, ClientRequestObject> entry : Server.map_Of_IncomingRequests.entrySet()){
                        ServerResponseObject resp = new ServerResponseObject () ;  
                        resp.globalIncumbent = Server.globalIncombent;  
                        
                        if (null != Loadbalancer.assignments.get (entry.getKey()) ) {
                            resp.assignment= Loadbalancer.assignments.get (entry.getKey()); 
                        }
                        
                        if (null != Loadbalancer.pruneInstructions.get(entry.getKey())) {
                            resp.pruneList = Loadbalancer.pruneInstructions.get(entry.getKey());
                        }
                        
                        Server.responseMap.put( entry.getKey(), resp);
                    }
                    
                    
                }
                
            }//if empty response map
        }//sync
        
    }//method populate response map
         
    private boolean waitForAllClients () throws InterruptedException{
        
        boolean result = false;
        
        for  (int limit = ZERO; limit < SIXTY * TWO*TWO * SIXTY; limit ++ ) {
            
            int countOfRecieved = ZERO;
            
            synchronized ( Server.map_Of_IncomingRequests) {
               countOfRecieved = Server.map_Of_IncomingRequests.size();
            }//synch
            
            if ( countOfRecieved <  NUM_WORKERS){
                //sleep for a second
                Thread.sleep( THOUSAND);
            }else {
                result = true;
                break;
            }
            
           
        }//end for limit
        
        return result;
    }
    
}
