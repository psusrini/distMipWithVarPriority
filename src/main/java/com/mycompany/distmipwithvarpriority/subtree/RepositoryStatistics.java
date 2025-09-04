/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distmipwithvarpriority.subtree;

import static com.mycompany.distmipwithvarpriority.Constants.ZERO;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sst119
 */
public class RepositoryStatistics {
        
    public   Set<String> varsInRepository = new HashSet <String> ();
    public int numRepoHits = ZERO;
    public int numRepoMisses = ZERO;
}
