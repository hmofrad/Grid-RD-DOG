/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DLAPSO;


/*
 * Author: Anthony Sulistio  
 * Date: November 2004
 * Description: A simple program to demonstrate of how to use GridSim
 *              network extension package.
 *              This example shows how to create user and resource
 *              entities connected via a network topology, using link
 *              and router.
 *
 */
import java.io.IOException;
import java.util.*;
import java.util.Scanner;
import java.text.Format;
import gridsim.*;
import gridsim.net.*;
import gridsim.GridSim;
import gridsim.datagrid.DataGridUser;
import gridsim.net.SimpleLink;
import eduni.simjava.*;
import gridsim.index.*;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class basically creates Gridlets and submits them to a
 * particular GridResources in a network topology.
 */
class NetUser extends GridUser {

    private int myId_;      // my entity ID
    private String name_;   // my entity name
    private GridletList list_;          // list of submitted Gridlets
    private GridletList receiveList_;   // list of received Gridlets
    private int[] resourceInfoID;
    private ArrayList GISInfoList;
    private int myGISIndex_;
    private int[][] GISMap;


    /**
     * Creates a new NetUser object
     * @param name  this entity name
     * @param totalGridlet  total number of Gridlets to be created
     * @param baud_rate     bandwidth of this entity
     * @param delay         propagation delay
     * @param MTU           Maximum Transmission Unit
     * @throws Exception    This happens when name is null or haven't
     *                      initialized GridSim.
     */
    NetUser(String name, int totalGridlet, double baud_rate, double delay,
            int MTU, int totalResource, ArrayList GISList, double[] gridletLength, int[] gridletVector) throws Exception {
        super(name, new SimpleLink(name + "_link", baud_rate, delay, MTU));

        this.name_ = name;
        this.receiveList_ = new GridletList();
        this.list_ = new GridletList();


        // Gets an ID for this entity
        this.myId_ = super.getEntityId(name);
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.myId_);

        // Creates a list of Gridlets or Tasks for this grid user
        System.out.println(name + ":Creating " + totalGridlet + " Gridlets");
        this.createGridlet(myId_, totalGridlet, gridletLength, gridletVector);
        this.resourceInfoID = new int[totalResource];
        this.GISInfoList = GISList;

    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body() {

        // wait for a little while for about 3 seconds.
        // This to give a time for GridResource entities to register their
        // services to GIS (GridInformationService) entity.

        super.gridSimHold(3000.0 * 5);    // wait for time in seconds
        LinkedList resList = super.getGridResourceList();
        System.out.println(resList.size());
        System.out.println();
        int i, j, k, l;

        // initialises all the containers
        int totalResource = resList.size();
        int resourceID[] = new int[totalResource];
        String resourceName[] = new String[totalResource];
        // a loop to get all the resources available

        for (i = 0; i < totalResource; i++) {
            resourceID[i] = this.resourceInfoID[i];
            resourceName[i] = GridSim.getEntityName(resourceID[i]);
        }

        ResourceCharacteristics resChar;
        ArrayList resCharList = new ArrayList(totalResource);
        for (i = 0; i < totalResource; i++) {
            // Requests to resource entity to send its characteristics
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.myId_);
            // waiting to get a resource characteristics
            resChar = (ResourceCharacteristics) super.receiveEventObject();

            resourceName[i] = resChar.getResourceName();

            //System.out.println( " " +  " " + resChar.getMachineList().getMachine(0).getNumFreePE());
            resCharList.add(resChar);
            /*            
            System.out.println("Receiving ResourceCharacteristics from " + resourceName[i] +
            ", with id = " + resourceID[i] + ", and PE = "
            + resChar.getMachineList().getMachine(0).getNumPE());
             */
        }

        System.out.println();

        // get GIS Information form GISInformation
        int num_GIS = this.GISInfoList.size();
        ArrayList gisList = new ArrayList(this.GISInfoList.size());
        gisList = this.GISInfoList;
        int localGISIndex = this.myGISIndex_;
        GISInformation GIS_obj;
        GIS_obj = (GISInformation) gisList.get(localGISIndex);

        // Sync Network Topology
        int[][] adjacencyMatrix = this.GISMap;
        int[] GISAdjacency;

        // caclulate number of LA on each Node based on resource Type
        // [CPU] [RAM] [HDD] [OS]
        // [1 4] [5 10] [11 14] [15 16]
        // divide each range to subranges with two elements
        // {1,2},{3,4} {5,6}{7,8}{9,10} {11,12}{13,14} {15,16}

        int distinctType = 5;
        int maxResource = 100;
        int distinctLA = distinctType;
        
        int maxSplit = 2;
        int[] minFrequency = {0, 20, 40, 60, 80};
        int[] maxFrequency = {19, 39, 59, 79, 99};
        int[] upperbound = {9, 19, 29, 39, 49, 59, 69, 79, 89, 99};

/*        
        int maxSplit = 4;
        int[] minFrequency = {0, 20, 40, 60, 80};
        int[] maxFrequency = {19, 39, 59, 79, 99};
        int[] upperbound = {4,9,14,19, 24,29,34,39, 44,49,54,59, 64,69,74,79, 84,89,94,99};

*/         
/*        
        int maxSplit = 10;
        int[] minFrequency = {0, 20, 40, 60, 80};
        int[] maxFrequency = {19, 39, 59, 79, 99};
        int[] upperbound = {1 ,3 , 5, 7, 9,11,13,15,17,19,
                            21,23,25,27,29,31,33,35,37,39,
                            41,43,45,47,49,51,53,55,57,59,
                            61,63,65,67,69,71,73,75,77,79,
                            81,83,85,87,89,91,93,95,97,99};
*/             

        // Initialize Learning Automata
        // construct 2D Automata Set: [resourceType] [resourceRange]
        automata automaton;
        ArrayList automataList = new ArrayList();  // array
        int adjacenceGISNumel;
        for (i = 0; i < num_GIS; i++) 
        {
            adjacenceGISNumel = 0;
            for (j = 0; j < num_GIS; j++)
            {
                if (adjacencyMatrix[i][j] == 1)
                    adjacenceGISNumel++;
            }
            automaton = new automata("automaton_" + i, adjacenceGISNumel);
            automataList.add(automaton);
        }

        // Set Automaton ActionSet


        for (i = 0; i < num_GIS; i++) 
        {
            automaton = (automata) automataList.get(i);
            for (j = 0; j < num_GIS; j++)
            {
                if (adjacencyMatrix[i][j] == 1)
                    automaton.setAutomatonActionSet(j);
            }
            automataList.set(i, automaton);
        }


        ////////////////////////////////////////////////
        // Discrete Particle Swarm Optimizer
        // Initialize PSO parameters
        // Each Node act as a dimnesion for particle
        // Each dimension associated with an automaton 
        // Number of dimensions consider as TTL value
        int fe = 0; //Fitness Evaluation
        int Ps = 30; // Population Size
        int D = 100; // Problem dimension same as maxHop 
        

        Random random = new Random();   // A random generator     
        
        // Initialize population
        int[][] p = new int[Ps][D]; // Particle Position [0 1]
        
        // Initilize population position
        for (i = 0; i < Ps; i++)
        {
            for (j = 0; j < D; j++)
            {
                p[i][j] = 0;
            }
        }
        
        // Clculate population fitness
        double [] f = new double [Ps]; // Particle Fitness
        for (i = 0; i < Ps; i++)
                f[i] = 2; // fitness  = [0,2] where 2 is the worst fitness
        
        // Calculate particle pbest
        int[][] pbest = new int[Ps][D]; // Particle Best Position
        double [] fpbest = new double [Ps]; // Particle Best Position Fitness
        for (i = 0; i < Ps; i++)
            for (j = 0; j < D; j++)
                pbest[i][j] = p[i][j];
        // Clculate particle pbest fitness
        for (i = 0; i < Ps; i++)
            fpbest[i] = f[i];
        
        // Calculate population best position (gbest)
        int[] gbest = new int [D]; // Global Best Position
        for (j = 0; j < D; j++)
            gbest[j] = p[0][j];
        double fgbest = f[0]; // Global Best Position Fitness
        
        
        // Initialize Particle Gridlet status
        // Initialize Particle Hop number
        // Initialize Successful Requests per population
        int totalGridlet = list_.size();
        int gridletStatus[][] = new int[totalGridlet][Ps];
        int[][] hopReq = new int [totalGridlet][Ps];
        int[] successReq = new int [totalGridlet];
        for (i = 0; i < totalGridlet; i++)
        {
            for (j = 0; j < Ps; j++)
            {
                gridletStatus[i][j] = 0;
                hopReq[i][j] = 0;
            }
            successReq[i]=0;
        }
        
        // Initialize Reinforcement signal
        int b[][] = new int [Ps][D];
        
        // Initialize Path travered by each particle 
        //ArrayList gisTrace = new ArrayList();  // array
        //ArrayList actionTrace = new ArrayList();  // array
        ArrayList tmpGisTrace = new ArrayList();  // array
        ArrayList tmpActionTrace = new ArrayList();  // array
        ArrayList[] gisTrace = new ArrayList[Ps];  // array
        ArrayList[] actionTrace = new ArrayList[Ps];  // array
        
        
/*        
        ////////////////////////////////////////////////
        // Learning Automata Initialization
        // LA are placed in each dimension
        // Their role is to forward the resource request
        // to the next Peer as the Velocity of particles
        double alpha = 0.1; // reward signal
        double beta = 0.1; // penalty signal 
        automata automaton;
        ArrayList automataList = new ArrayList();  // array
        int adjacenceGISNumel;
        for (i = 0; i < Ps; i++) 
        {
            for (j = 0; j < Ps; j++)
            {
                automaton = new automata(i + "_" + j + "_");
                automataList.add(automaton);
            }
        }
   
*/
                
        ////////////////////////////////////////////////
        // SUBMIT Gridlets
        // determines which GridResource to send to
        // sends all the Gridlets


        System.out.println();
        Gridlet gl = null;
        boolean success;
        int successCount = 0;
        double[] avgHop = new double [totalGridlet];

        int[] memberRsource;
        int nextGIS;
        int action = -1;
        int previousGIS;
        int isVisit[] = new int[num_GIS];
        for (i = 0; i < num_GIS; i++)
            isVisit[i] = 0;
       
        int enableAutomaton[] = new int[num_GIS];
        for (i = 0; i < num_GIS; i++)
            enableAutomaton[i] = 0;        
        
        int count = 0;
        int [] hop = new int [totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            hop[i] = 0; // set hop value to zero
        int maxHop = 100; 
        int sentinel = 0;
        adjacenceGISNumel = 0;


        int pathLength;
        int currentAtomaton;
        int currentAction;
        int rienforcementSignal;
        int currentAutomataType = -1;
        int currentAutomataRange = -1;
        int currentRangeBound = 0;
        int rangeBreak = 0;
        int r = 0;
        double rc = 0; // redundant Counter

        // count resource utilization
        int [] resourceUtil = new int [totalResource];
        for (i = 0; i < totalResource; i ++)
            resourceUtil [i] = 0;


        //Calendar calendar = Calendar.getInstance();
        //System.out.println("Initialize simulation time = " + calendar.getTimeInMillis() + " " + GridSim.clock());
        for (i = 0; i < totalGridlet; i++) // Process Gridlets
        {
            // Patch Gridlet
            gl = (Gridlet) this.list_.get(i);
            
            // Do the search for resource

            k = 0; // Dimension Index
             // for all particles in parallel mode
            for (j = 0; j < Ps; j++)
            {
                previousGIS = localGISIndex;
                for (k = 0; k < D; k++)
//                while (this.gridletStatus[i] != 1 && sentinel != 1 && this.hopReq[i] <= maxHop)
                {
                    rc  = random.nextDouble();
                    if (isVisit[previousGIS] != 1 && rc> 0.0001)
                    {
                        GIS_obj = (GISInformation) gisList.get(previousGIS);
                        memberRsource = convertInt(GIS_obj.myResourcelist_);

                        // Process request in local resources

                        for (l = 0; l < memberRsource.length; l++) 
                        {
                            resChar = (ResourceCharacteristics) resCharList.get(memberRsource[l]);
                            //System.out.println(gl.getNumPE() +" ? " + resChar.getNumPE() + " !" + l);
                            if (resChar.getNumPE() == gl.getNumPE())
                            {
                                // = super.gridletSubmit(gl, resChar.getResourceID(), 0.0, true);
                                // calculate resource ID
                                String str = resChar.getResourceName();
                                int indStr = str.indexOf('_') + 1;
                                String numStr = str.substring(indStr);
                                int intStr = Integer.parseInt(numStr);
                                resourceUtil[intStr] = 1;
                                gridletStatus[i][j] = 1; // Set Gridlet ststus as SUCCESS
                                successReq[i]++; // Increase successul requests counter
                                //System.out.println("Particle_" + j  + " | Gridlet_" + gl.getGridletID() + " | hop = " + hopReq[i][j]);
                                break; // Skip other resources
                            }
                        }


                        // if resource is found leave the loop
                        if (gridletStatus[i][j] == 1)
                            break;
                        // if resource is not found increase th Hop number
                        else  
                            hopReq[i][j]++;
                    }

                    // check constraints
                    // check if all nodes are not visit
                    count = 0;
                    for (l = 0; l < isVisit.length; l++)
                    {
                        if (isVisit[l] == 1)
                            count++;
                    }
                    // Automaton select action if all nodes are not meet
                    if (count != num_GIS) 
                    {
                        automaton = (automata) automataList.get(previousGIS);
                        action = automaton.actionSelection();
                        nextGIS = ((Integer) automaton.actionSet_.get(action)).intValue();
                        isVisit[previousGIS] = 1;
                        tmpGisTrace.add(previousGIS);
                        tmpActionTrace.add(action);
                        //enableAutomaton [GIS_obj.index_] = 1;
                        //gisTrace.add(previousGIS); // keep GIS Trace
                        //actionTrace.add(action);   // add action Trace
/*                       
                        if (isVisit[previousGIS] == 0)
                        {
                        isVisit[previousGIS] = 1;
                        gisTrace[j].add(previousGIS); // keep GIS Trace
                        actionTrace[j].add(action);   // add action Trace
                        }
*/                        

 
                        
                        previousGIS = nextGIS;
                        p[j][k] = nextGIS;
                    }
                } // end resource checking of current node
                
                
                if (k < maxHop)
                {
                    for (l = k+1; l < D; l++) // fill empty dimensions
                        p[j][l] = 0;                
                }
                
                // save path
                gisTrace[j] = (ArrayList) tmpGisTrace.clone();
                actionTrace[j] = (ArrayList) tmpActionTrace.clone(); 
                tmpGisTrace.clear();
                tmpActionTrace.clear();
            }
            
             // for all particles in Parallel mode
            for (j = 0; j < Ps; j++)
            {
                
                // Calculate particle fitness
                f[j] = (gridletStatus[i][j] * ((double) Math.pow(hopReq[i][j] / maxHop,maxHop))) +
                        (((1 - gridletStatus[i][j]) * (1 + (double) hopReq[i][j] / maxHop)));
                
                fe++;


                // Calculate pbest and gbest

                if (f[j] < fpbest[j])
                {
                    for (k = 0; k < hopReq[i][j]; k++)
                        pbest[j][k] = p[j][k];
                    fpbest[j]=f[j];
                }
                if (fpbest[j] < fgbest)
                {
                    if (hopReq[i][j] == 0)
                    {
                        for (k = 0; k < maxHop; k++)
                            gbest[k] = 0;
                    }
                    else
                    {
                        for (k = 0; k < hopReq[i][j]; k++)
                            gbest[k] = pbest[j][k];
                        for (l = k+1; l < maxHop; l++)
                            gbest[l] = 0;
                    }
                    fgbest = fpbest[j];
                }

                // Calculate Reinforcement Signal
                for (k = 0; k < hopReq[i][j]; k++)
                {                    
                    //if (gridletStatus[i][j] == 1)
                    if(pbest[j][k] == p[j][k])
                    //if(gbest[k] == pbest[j][k] && pbest[j][k] == p[j][k])    
                        b[j][k] = 0; // reward the Request Path
                    else 
                        b[j][k] = 1; // penalty the Request Path
                }

                // Deploy Reinforcement Signal
                for (k = 0; k < hopReq[i][j]; k++)
                {
                    currentAtomaton = ((Integer) gisTrace[j].get(k)).intValue();
                    currentAction = ((Integer) actionTrace[j].get(k)).intValue();
                    automaton = (automata) automataList.get(currentAtomaton);
                    GIS_obj = (GISInformation) gisList.get(currentAtomaton);
                    automaton.probablityUpdate(currentAction, b[j][k]);
                    automataList.set(currentAtomaton, automaton);  
                }    
                // Clear visited nodes    
                for (k = 0; k < num_GIS; k++) 
                {
                    isVisit[k] = 0;
                }

                //Clear path;
                gisTrace[j].clear();
                actionTrace[j].clear();
                                
            }
            
            // Print current Gridlet statistics
            for (j = 0; j < Ps; j++)
                avgHop[i] += hopReq[i][j];
            avgHop[i] = (double) avgHop[i]/Ps;

            System.out.println("Gridlet_" + gl.getGridletID() + " | gbest =  " + fgbest + " | Times = " +  successReq[i] + " " + " | Hop = " + avgHop[i]);
            //System.out.println(successReq[i]);
            // Clear hop counter
            for (j = 0; j < Ps; j++)
                hopReq[i][j] = 0;
            
        } // end of processing this Gridlet
  
        super.gridSimHold(45);
        System.out.println("/////////////////////////////////");
        // Calculate results
        double totalHop = 0.0; // total number of hops
        double totalHit = 0.0; // total number of hits
        for (i = 0; i < totalGridlet; i++)
        {
            totalHop += avgHop[i];
            totalHit += successReq[i];
        }
        totalHop = (double) totalHop/totalGridlet;
        totalHit = (double) totalHit/(totalGridlet * Ps);
        
        int totalRes = 0;
        for (i = 0; i < totalResource; i++)
        {
        if (resourceUtil[i] == 1)
        totalRes++;
        }    
        

/*
        // show gbest
        for (i = 0; i < D; i++)
            System.out.print(gbest[i] + " ");

        System.out.println();
*/        
        
        

        /// Calculate Entropy
        double P = 0;
        double PP = 0;
        double[] entropy = new double[num_GIS];
        for (i = 0; i < num_GIS; i++)
            entropy[i]=0.0;
        for (i = 0; i < num_GIS; i++)
        {
            automaton = (automata) automataList.get(i);
            for (j = 0; j < automaton.actionNumel_; j++)
            {
                P = automaton.actionProbability_[j];
                PP = (double) 1/automaton.actionNumel_;
                if (P != 0 && P != 1)
                    //entropy[i] -= P * (Math.log(P) / Math.log(2));
                    entropy[i] -= P * (Math.log(P) / Math.log(2));
                PP -= PP * (Math.log(PP)/ Math.log(2));
            }
            //System.out.println(entropy[i]);
        }
        //entropy = (entropy/PP)/num_GIS;
        double S = 0.0;
        for (i = 0; i < num_GIS; i++)
            S += entropy[i];
        S = (double) S/num_GIS;
        //System.out.println(S);  
        //System.out.println();
        System.out.println("Total Hop = " + totalHop + " Total Hit = " + totalHit + 
                " Total Res = " + totalRes + " Total Entropy = " + S);

        



        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();


    }

    /**
     * Gets a list of received Gridlets
     * @return a list of received/completed Gridlets
     */
    public GridletList getGridletList() {
        return receiveList_;
    }

    public void setInfo(int resInfoID[]) {
        this.resourceInfoID = resInfoID; // list of Grid Resources
    }

    public void setGISMap(int adjacencyMatrix[][]) {
        this.GISMap = adjacencyMatrix; // GIS network Topology
    }

    public void setLocalGISIndex(int index) {
        this.myGISIndex_ = index;
    }

    public int[] convertInt(ArrayList list) {
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = ((Integer) list.get(i)).intValue();
        }
        return res;
    }

    public int[] extractGISAdjacency(int[][] array, int index) {
        int adjacenceGISNumel = 0;
        int i = 0;
        for (i = 0; i < array.length; i++) {
            if (array[index][i] == 1) {
                adjacenceGISNumel++;
            }
        }
        int[] adjacenceGISIndex = new int[adjacenceGISNumel];
        int c = 0;
        for (i = 0; i < array.length; i++) {
            if (array[index][i] == 1) {
                adjacenceGISIndex[c] = i;
                // System.out.print(adjacenceGISIndex[c] + " ");
                c++;
            }
        }
        //  System.out.println();
        return adjacenceGISIndex;

    }

    public ArrayList convertArray(int[] array) {
        ArrayList list = new ArrayList(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;

    }


    /**
     * This method will show you how to create Gridlets
     * @param userID        owner ID of a Gridlet
     * @param numGridlet    number of Gridlet to be created
     */
    private void createGridlet(int userID, int numGridlet, double[] gridletLength, int[] gridletPE) {
        double length;
        long file_size = 300;
        long output_size = 300;
        for (int i = 0; i < numGridlet; i++) // i as Gridlet ID
        {
            // Creates a Gridlet
            length = gridletLength[i];
            Gridlet gl = new Gridlet(i, length, file_size, output_size);
            // set gridlet component as PE in order to execute in gridsim
            gl.setNumPE(gridletPE[i]);
            gl.setUserID(userID);
            // add this gridlet into a list
            this.list_.add(gl);

        }

    }

    private void printArray(String msg, Object[] globalArray) {
        // if array is empty
        if (globalArray == null) {

            System.out.println(super.get_name() + ": number of " + msg + " = 0.");
            return;
        }

        System.out.println(super.get_name() + ": number of " + msg + " = " + globalArray.length);

        for (int i = 0; i < globalArray.length; i++) {
            Integer num = (Integer) globalArray[i];
            System.out.println(super.get_name() + ": receiving info about "
                    + msg + ", name = " + GridSim.getEntityName(num.intValue()) + " (id: " + num + ")");
        }
        System.out.println();
    }
} // end class

