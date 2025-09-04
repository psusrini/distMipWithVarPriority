/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.subtree;

import static com.mycompany.distmipwithvarpriority.Constants.ONE;
import java.io.Serializable;

/**
 *
 * @author sst119
 */
public class BranchingCondition implements Serializable {
    public String varName = null;
    public double bound = -ONE;     
    public boolean isBranchDirectionDown = true;
    
    public String toString (){
        return "("+ varName + ", "+ bound + ", "+ isBranchDirectionDown+")";
    }
}
