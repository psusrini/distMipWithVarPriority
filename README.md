This document decribes the distributed implementation of CPLEX which uses a pseudo-Cost repository.
The code is organized as a main package, and then a collection of sub packages each of which serves a specific purpose.

Under the main package, there are these 2 files:

Constants:
----------
This file includes the following definitions:

REALTIVE_MIP_GAP_THRESHOLD: a CPLEX parameter which is used to set the relative MIP gap. See CPLEX documentaion for details.

MIPROOT_NODE_ID: used to identify the CPLEX search tree root node. Note that every leaf node (and its ancestors) maintain a pointer to their parent. This pointer is used to infer the branching conditions which lead to a leaf.

SOLUTION_CYCLE_TIME_MINUTES: Map cycle time. Set to 30 minutes. All the workers report to the master every 30 minutes.

Parameters:
-----------

MIP_NAME: Mip being solved

WELL_KNOWN_OPTIMAL_VALUE: if set to less than a BILLION, is used to set an initial cutoff. When set to the well known optimal solution for the MIP, the problem reduces to proving that no better solution exists.

RANDOM_SEED: CPLEX random seed

CPX_PARAM_MIPEMPHASIS: CPLEX emphasis

USE_VAR_PRIORITY_LIST, MAX_PRIORITY_VARS: whether or not to use the variable priority list, and the maximum size of this list.

PRIORITY_LIST_FILENAME: file that contains the variable priorty order that CPLEX must use for branching.

NUM_WORKERS, SERVER_NAME, SERVER_PORT_NUMBER: used to create the workers and help them connect to their master to start the computation. 

MAX_FARMED_LEAVES_COUNT: How many leaves each worker must make available for load balancing

CPX_PARAM_VARSEL , CPX_PARAM_NODEFILEIND, CPX_PARAM_HEURFREQ , CPX_PARAM_MIPSEARCH, MAX_THREADS: configuration for CPLEX. refer to CPLEX documentation for definitions.

CPlexUtils:
------------
Includes methods for configuring an using CPLEX

getVariables(): given a CPLEX instance that has read a MIP file, returns the variables in that model as a Hashmap. This hashMap is used to retrive the CPLEX variable corresponding to a given variable name.

updateVariableBounds() : used to update variable bounds for a given variable

applyVarFixings(): invokes updateVariableBounds() to create a node , given the branching conditions that lead to the node.  

getCPlex() : create a CPLEX instance that has read the MIP, and applies variable fixings needed to arrive at the subtree root node. Applies variable priority order if so instructed.

areAllObjectiveCoeffsIntgeral() : Checks if all objective coefficients are intgeral, in which case (for example) a known solution of value 5 is provably optimal the moment CPLEX's best bound exceeds 4.

Sai
