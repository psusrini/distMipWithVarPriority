/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.subtree;

/**
 *
 * @author sst119
 */
public class NodeAttachment {
     
    public NodeAttachment parentNode = null;
    
    //condition used to create down branch child , up branch condition can be inferred
    public String branchingVarName = null;
    public Double upperBound = null;  
    
    public boolean amITheDownBranchChild = false;
    
}

 
