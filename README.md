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

MAX_FARMED_LEAVES_COUNT: How many leaves each worker must make available for load balancing; set to W -1 where W is the number of workers in the cluster

CPX_PARAM_VARSEL , CPX_PARAM_NODEFILEIND, CPX_PARAM_HEURFREQ , CPX_PARAM_MIPSEARCH, MAX_THREADS: configuration for CPLEX. Refer to CPLEX documentation for definitions.

-------------------------------------------------------
The following files are in their own packages
-------------------------------------------------------

-------------------------
PACKAGE: utilities
-------------------------

CPlexUtils:
------------
Includes methods for configuring and using CPLEX

getVariables(): given a CPLEX instance that has read a MIP file, returns the variables in that model as a Hashmap. This hashMap is used to retrive the CPLEX variable corresponding to a given variable name.

updateVariableBounds() : used to update variable bounds for a given variable

applyVarFixings(): invokes updateVariableBounds() to create a node , given the branching conditions that lead to the node.  

getCPlex() : create a CPLEX instance that has read the MIP, and applies variable fixings needed to arrive at the subtree root node. Applies variable priority order if so instructed.

areAllObjectiveCoeffsIntgeral() : Checks if all objective coefficients are intgeral, in which case (for example) a known solution of value 5 is provably optimal the moment CPLEX's best bound exceeds 4.

-------------------------------------------------------

-------------------------
PACKAGE: subtree
-------------------------

BranchingCondition:
-------------------
A variables name, bound, and direction

FarmedLeaf:
--------------
A leaf that will be sent to the server as a potential migration candidate, identified by the worker machine which offered it, its node ID in the subtree on that machine, its LP relaxed objective, and the variable fixings needed to arrive at it

NodeAttachment:
--------------
attached to every CPLEX search tree node. Has a link to the parent node, and whether this node is the down branch child. The branching variable and bound are also included.
 
SizeConstrainedMap:
------------------
A TreeMap that saves at most W-1 leaves for sending to the server as migration candidates.
It has these methods:

removeLeaf (): removes the largest LP relaxed objective leaf from the bag. This is done to insert another migration candidate which has a lower LP relaxed objective.

add() : adds a new migration candidate leaf node to the bag

getFarmedLeafs() : used by the server to collect all the migration candidate leaf nodes sent by a given worker

SolveResult:
-------------
Results from a CPLEX search tree, including the bestKnownSolution, the dual bound, number of leaf nodes in the tree, number of nodes explored so far, and the relative MIP gap

SubTree:
----------------
A CPLEX search tree, which is a subtree of the original MIP search tree.

Constructor() : creates the subtree given the variable bounds needed to arrive at this subtree

isCompletelySolved(): Whether this subtree has been solved to optimality or infeasibility

end() : important to destroy the tree if once it is completely solved, in order to reclaim computer memory

farm() : uses the farming callback to farm for upto W-1 leaves which are later made available to the server as migration candidates. Done in single threaded mode.

prune() : uses the prune callback to prune leaf nodes that the server has accepted for migration to other workers. Done in single threaded mode.

sequentialSolve () : solves the CPLEX subtree for 30 minutes, and returns a SolveResult object. This is the method that does the "heavy lifting" at the worker by actually using CPLEX to solve a MIP subtree for 30 minutes.

isWithinMIpGapThreshold(): checks if the subtree has been solved to optimality, infeasibility, or the distributed MIP gap is within the selected MIP Gap treshold. See the paper text for the definition of the distributed MIP gap.

-------------------------
PACKAGE: rampup
-------------------------

RampupNodeCallback:
---------------------
main(): prepares a tree with W leaf nodes, where W is the number of workers in the cluster. Makes these leaves available as a list.

Rampup:
----------
doRampUp(): returns the W leaf nodes prepared by the ramp up node callback.

-------------------------
PACKAGE: callbacks
-------------------------

These are CPLEX node and branch callbacks used 1) during ramp up, 2) for solution, 3) for farming leaf nodes for migration, and 4) for pruning migrated leaf nodes.

BranchHandler:
---------------
main() : accepts a list of nodes for pruning. At the begiining of a MAP cycle, prunes these nodes. If there are no leaf nodes left for pruning, records the branching condition for both child nodes in the search tree by attaching a node attachment into each node. 

PruneNodecallback:
------------------
main(): takes the ID of the node to be pruned, and selects it so that it can be pruned.

FarmNodecallback:
-----------------

main(): loops through all the leaf nodes of a subtree and adds them one by one to a size constrained Map. If the map already has W leaves, then a leaf already in the map having the largest LP relaxed objective is relaced with this leaf. In effect, this method collects upto 5 lowest LP relaxed objective leaf nodes from this subtree to send to the server as migration candidates.

-------------------------
PACKAGE: client
-------------------------

Each Client is a worker that reports to the master server

ClientRequestObject:
---------------------
includes the worker name, the leaf nodes this worker is making available for assignment to idle workers, whether this worker is idle,  the local incumbent , and the number of nodes processed by the current subtree

Client:
--------
main(): connects to the server, and repeatedly executes these steps

    1) Sends a ClientRequestObject to the server, informing it of this worker's status. Note that, at the begininning and end of the computation, all workers report an idle status. 
    
    2) recieves a response back from the server. This response contains an updated incumbent value which is used to update the worker's cutoff. The response may also contain a set of leaf nodes to prune form the current search tree. An idle client will recieve a new subtree root node to start exploring.
    
    3) process the response and solve the subtree for 30 minutes 
    
    4) after solving, farm leaf nodes to send to the server and include them in the ClientRequestObject
    
    5) if work is complete before 30 minutes, idle until 30 minutes have elapsed since the last time the server was contacted.
    
    6) go to step 1)

processResponse(): prune leaf nodes and create a new subtree if idle

prepareRequest():   populate the ClientRequestObject that will be sent to the server  

Sai
