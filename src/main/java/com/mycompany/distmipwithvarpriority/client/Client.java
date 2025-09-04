/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.client;

import static com.mycompany.distmipwithvarpriority.Constants.*;
import static com.mycompany.distmipwithvarpriority.Parameters.*;
import com.mycompany.distmipwithvarpriority.server.ServerResponseObject;
import com.mycompany.distmipwithvarpriority.subtree.SubTree;
import ilog.concert.IloException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
/**
 *
 * @author sst119
 */
public class Client {
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Client.class); 
     
    //job I will work on, null to begin with
    private static SubTree  mySubTree= null;
    
    public static   String clientName ;
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+ Client.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
            clientName= InetAddress.getLocalHost(). getHostName();
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
        
        
    } 
    
    public static void main(String[] args) throws Exception {
        
        try (
            Socket workerSocket = new Socket(SERVER_NAME, SERVER_PORT_NUMBER);                
            ObjectOutputStream  outputStream = new ObjectOutputStream(workerSocket.getOutputStream());
            ObjectInputStream inputStream =  new ObjectInputStream(workerSocket.getInputStream());
            
        ){
            
            logger.info ("Client is starting ... "+  InetAddress.getLocalHost(). getHostName() ) ;
           
            for (int iteration = ZERO ;  iteration < BILLION ; iteration  ++){
                
                //get work from server
                ClientRequestObject request = prepareRequest(iteration) ;
                logger.info (" sending request "   );
                outputStream.writeObject(request);
               
                ServerResponseObject response = (ServerResponseObject) inputStream.readObject();
                                
                 //solve for SOLUTION_CYCLE_TIME
                long  startTime = System.currentTimeMillis();   
                processResponse(response) ;
                
                //logger.info ("processresponse and prune took seconds" + pruneTime_milliSeconds/THOUSAND);
                
                 
                mySubTree.solve(response.globalIncumbent );
                if (!mySubTree.isCompletelySolved()){
                    //farm some nodes
                    logger.info ("Client not yet completed , farming ... ");
                    mySubTree.farm();
                    
                }else {
                    logger.info ("Client complted assignment ");
                }
                
                long idleTime = THOUSAND * SOLUTION_CYCLE_TIME_MINUTES*SIXTY  - (System.currentTimeMillis() -startTime);
                if ( idleTime > ZERO ) {
                    logger.info ("Client  will be idle for  sec " + idleTime/THOUSAND);
                    sleep(idleTime) ;
                } 
                            
                           
            }//end for iterations
            
            //server exits when done, so actually this code is never reached.
            //Clients discover that the server has gone offline, and exit with a socket closed exception
             
            workerSocket.close();
            logger.info ("Client is stopping ... "+  InetAddress.getLocalHost(). getHostName() ) ;
             
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
        
    }
    
    private static    ClientRequestObject  prepareRequest( int iteration) throws IloException {
        ClientRequestObject req = new ClientRequestObject ();
        req.clientname= Client.clientName;
        
        if (ZERO==iteration) {
            //just starting, master will send us the assignment just after  ramp up
            req.isIdle= true;            
        }else {
            if (mySubTree.isCompletelySolved()){
                req.isIdle = true;
                req.local_bestBound = mySubTree.solveResult.bound;
                req.local_incumbent= mySubTree.solveResult.bestKnownSolution  ;
                req.numNodesProcessed= mySubTree.solveResult.numNodesExplored  ;
                
            }else {
                req.isIdle = false;
                req.local_bestBound = mySubTree.solveResult.bound;
                req.local_incumbent= mySubTree.solveResult.bestKnownSolution  ;
                req.numNodesProcessed= mySubTree.solveResult.numNodesExplored  ;
                req.availableNodes = mySubTree.farmedLeafs;
                                 
            }
        }
        
        return req;
    }
    
    private static long  processResponse(ServerResponseObject responseFromServer  ) throws  Exception {
        logger.info(" processing Response " );
        
        long result = ZERO;
        
        if (null != responseFromServer.pruneList){
            //prune leafs for LCA nodes that were migrated to other workers   
            logger.info(" prune list size before   "+ responseFromServer.pruneList.size());
            long  prune_startTime = System.currentTimeMillis();
            mySubTree.prune(responseFromServer.pruneList);
            result = System.currentTimeMillis() - prune_startTime;
            logger.info(" prune list size after  "+ responseFromServer.pruneList.size());
        }
        if (null!=responseFromServer.assignment){
            //
            logger.info(" creating new SubTree ... " );
            
            if (mySubTree!=null) mySubTree.end();
            mySubTree = new SubTree  ( responseFromServer.assignment );
            logger.info(" SubTree_LCA created " );
            
        } 
        
        return result;
    }
    
}
