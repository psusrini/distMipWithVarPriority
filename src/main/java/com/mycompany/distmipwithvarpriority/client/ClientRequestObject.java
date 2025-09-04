/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.client;

 
import static com.mycompany.distmipwithvarpriority.Constants.*;
import com.mycompany.distmipwithvarpriority.subtree.FarmedLeaf;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author sst119
 */
public class ClientRequestObject {
       
    public String clientname = null;
    
    //key is lp relax, value is a list of available leaf nodes for migration
    //Farmed leaf contains machine name
    public Set<FarmedLeaf> availableNodes=null;
     
    public Boolean isIdle=null ;
    
    public double local_bestBound =BILLION;
    public double local_incumbent =BILLION;
    public long numNodesProcessed = ZERO;
    
    //check how many repo hits
    public int numBranchesMade = ZERO;
    public int numBranchesMade_fromRepo = ZERO;
}
