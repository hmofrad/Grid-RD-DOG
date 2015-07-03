/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RD_RANDOM;


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
import gridsim.*;
import gridsim.net.*;
import gridsim.GridSim;
import gridsim.datagrid.DataGridUser;
import gridsim.net.SimpleLink;
import eduni.simjava.*;
import gridsim.index.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * This class basically creates Gridlets and submits them to a
 * particular GridResources in a network topology.
 */
class NetUser extends GridUser
{
    private int myId_;      // my entity ID
    private String name_;   // my entity name
    private GridletList list_;          // list of submitted Gridlets
    private GridletList receiveList_;   // list of received Gridlets
    private int [] resourceInfoID;
    private ArrayList GISInfoList;
    private int myGISIndex_;
    private int [][] GISMap; 
    
    public int successReq;
    public int[] gridletStatus;
    public int[] hopReq;
    public int[] resourceUtil;
    public int[] redundancyCounter;




    


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
            int MTU,int totalResource, ArrayList GISList, double [] gridletLength, int [] gridletVector) throws Exception
    {
        super( name, new SimpleLink(name+"_link",baud_rate,delay, MTU) );

        this.name_ = name;
        this.receiveList_ = new GridletList();
        this.list_ = new GridletList();


        // Gets an ID for this entity
        this.myId_ = super.getEntityId(name);
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.myId_);

        // Creates a list of Gridlets or Tasks for this grid user
        System.out.println(name + ":Creating " + totalGridlet +" Gridlets");
        this.createGridlet(myId_, totalGridlet, gridletLength, gridletVector);
        this.resourceInfoID = new int [totalResource];
        this.GISInfoList = GISList;
        
        this.resourceUtil = new int[totalResource];
        for (int i = 0; i < totalResource; i++) {
            this.resourceUtil[i] = 0;
        }
        this.hopReq = new int[totalGridlet];
        for (int i = 0; i < totalGridlet; i++) {
            this.hopReq[i] = 0;
        }
        this.gridletStatus = new int[totalGridlet];
        for (int i = 0; i < totalGridlet; i++) {
            this.gridletStatus[i] = 0;
        this.redundancyCounter = new int [totalGridlet];
        for (i = 0; i < totalGridlet; i++)
            this.redundancyCounter[i] = 0;
        }
        this.successReq = 0;

        
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body()
    {
        
        // wait for a little while for about 3 seconds.
        // This to give a time for GridResource entities to register their
        // services to GIS (GridInformationService) entity.

        super.gridSimHold(6000.0);    // wait for time in seconds
        LinkedList resList = super.getGridResourceList();
        System.out.println(" " + resList.size());
        System.out.println();
        int i,j,k = 0;


         // initialises all the containers
        int totalResource = resList.size();
        int resourceID[] = new int[totalResource];
        String resourceName[] = new String[totalResource];
        // a loop to get all the resources available

        for (i = 0; i < totalResource; i++)
        {
            resourceID[i] = this.resourceInfoID[i];
            resourceName[i] = GridSim.getEntityName(resourceID[i]);
        }
        
        ResourceCharacteristics resChar;
        ArrayList resCharList = new ArrayList(totalResource);
        for (i = 0; i < totalResource; i++)
        {            
            // Requests to resource entity to send its characteristics
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW
                                    , GridSimTags.RESOURCE_CHARACTERISTICS,this.myId_);
            // waiting to get a resource characteristics
            resChar = (ResourceCharacteristics) super.receiveEventObject();

            resourceName[i] = resChar.getResourceName();
            
            //System.out.println( " " +  " " + resChar.getMachineList().getMachine(0).getNumFreePE());
            resCharList.add(resChar);
            System.out.println("Receiving ResourceCharacteristics from " + resourceName[i] +
                               ", with id = " + resourceID[i] + ", and PE = "
                               + resChar.getMachineList().getMachine(0).getNumPE());
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
        int [][] adjacencyMatrix = this.GISMap;
        int [] GISAdjacency;
        System.out.println();

          ////////////////////////////////////////////////
         // SUBMIT Gridlets
        // determines which GridResource to send to
       // sends all the Gridlets

        
        System.out.println();
        Gridlet gl = null;
        boolean success;
        int successCount = 0;
        int totalGridlet = list_.size();
        // int gridletStatus[] = new int[totalGridlet];
        // for (i = 0; i < totalGridlet; i++)
        //    gridletStatus[i] = 0; // set Gridlet ststus as FAIL
        int [] memberRsource;
        Random random = new Random();   // a random generator
        int nextGIS = localGISIndex;
        int isVisit [] = new int [num_GIS];
        for (i = 0; i < num_GIS; i++)
            isVisit[i] = 0;
        int count = 0;
        // int [] hop = new int [totalGridlet];
        // for (i = 0; i < totalGridlet; i++)
        //    hop[i] = 0; // set hop value to zero
        int maxHop = 100;
        int sentinel = 0;
        

        //Calendar calendar = Calendar.getInstance();
        //System.out.println("Initialize simulation time = " + calendar.getTimeInMillis() + " " + GridSim.clock());
        for (i = 0; i < totalGridlet; i++) // Process Gridlets
        {
            gl = (Gridlet) this.list_.get(i);
            // System.out.println("Gridlet #" + i + ": Resource Request with "
            //                     + gl.getNumPE() + " Processing Elements");
            // Process User local Resources
            GIS_obj = (GISInformation) gisList.get(localGISIndex);
            memberRsource = convertInt(GIS_obj.myResourcelist_);
            GISAdjacency = extractGISAdjacency(adjacencyMatrix,localGISIndex);
            isVisit [localGISIndex] = 1;
            // Process User local Resources
            for (j = 0; j < memberRsource.length; j++ )
            {
                resChar = (ResourceCharacteristics) resCharList.get(memberRsource[j]);
                if ( resChar.getNumPE() == gl.getNumPE())
                {
                    success = super.gridletSubmit(gl,resChar.getResourceID(),0.0,true);
                    // calculate resource ID
                    String str = resChar.getResourceName();
                    int indStr = str.indexOf('_') + 1;
                    String numStr = str.substring(indStr);
                    int intStr = Integer.parseInt(numStr);
                    this.resourceUtil[intStr] = 1;
                    this.gridletStatus[i] = 1; // set Gridlet ststus as SUCCESS
                    this.successReq++;
                    break;
/*                    
                    
                    
                    System.out.println(name_ + ": Perfect Match is found for Gridlet #" + i + " with " + 
                                       gl.getNumPE() + " PE on " + resChar.getResourceName() +
                                           " on GIS with index " + nextGIS);
                        success = super.gridletSubmit(gl,resChar.getResourceID(),0.0,true);
                        System.out.println(name_ + ": Sending Gridlet #" + gl.getGridletID() +
                      " with status = "    + success + " to ID " + resChar.getResourceID());
                        gridletStatus[i] = 1; // set Gridlet ststus as SUCCESS
                        c++;
                        break;
 */
                }
            }
            this.hopReq[i]++;
            while (this.gridletStatus[i] != 1 && sentinel != 1 && this.hopReq[i] <=maxHop)
            {
                nextGIS = random.nextInt(GISAdjacency.length);
                GISAdjacency = extractGISAdjacency(adjacencyMatrix, nextGIS);
                GIS_obj = (GISInformation) gisList.get(nextGIS);
                memberRsource = convertInt(GIS_obj.myResourcelist_);
                if (isVisit [nextGIS] != 1)
                {
                    isVisit [nextGIS] = 1;
                    // Process global resources
                    for (j = 0; j < memberRsource.length; j++ )
                    {
                        
                        resChar = (ResourceCharacteristics) resCharList.get(memberRsource[j]);
                        if ( resChar.getNumPE() == gl.getNumPE())
                        {
                            success = super.gridletSubmit(gl,resChar.getResourceID(),0.0,true);
                            // calculate resource ID
                            String str = resChar.getResourceName();
                            int indStr = str.indexOf('_') + 1;
                            String numStr = str.substring(indStr);
                            int intStr = Integer.parseInt(numStr);
                            this.resourceUtil[intStr] = 1;
                            this.gridletStatus[i] = 1; // set Gridlet ststus as SUCCESS
                            this.successReq++;
                            break;  
/*                                                      
                            System.out.println(name_ + ": Perfect Match is found for Gridlet #" + i + " with " + 
                                               gl.getNumPE() + " PE on " + resChar.getResourceName() +
                                                   " on GIS with index " + nextGIS);
                                success = super.gridletSubmit(gl,resChar.getResourceID(),0.0,true);
                                System.out.println(name_ + ": Sending Gridlet #" + gl.getGridletID() +
                              " with status = "    + success + " to ID " + resChar.getResourceID());
                                gridletStatus[i] = 1; // set Gridlet ststus as SUCCESS
                                c++;
                                break;
 */
                        }
                    }
                }
                else if (isVisit [nextGIS] == 1)
                {
                    this.redundancyCounter[i]++;
                }
                // check constraints
                count = 1;
                for (k = 0; k < isVisit.length; k++)
                {
                    if (isVisit[k] == 1)
                        count++;    
                    
                }
                if (count == num_GIS)
                {
                    sentinel = 1;
                    System.out.println("Resource Request is not found");
                }
             this.hopReq[i]++;   
            }
            sentinel = 0;
            for (k = 0; k < num_GIS; k++)
            {
                isVisit[k] = 0;
            }
        }
      // Process global resources  
/*        
        for (i = 0; i < totalGridlet; i++)
        {
            System.out.print(i + " " + gridletStatus[i] + " " + hop[i]);
            System.out.println();
        }
*/            
        // System.out.println("Finalize simulation time = " + calendar.getTimeInMillis() + " " + GridSim.clock());

        
        ////////////////////////////////////////////////////////
        // RECEIVES Gridlets back

        // hold for few period - few seconds since the Gridlets length are
        // quite huge for a small bandwidth
        super.gridSimHold(45);

        // receives the gridlet back
        for (i = 0; i < this.successReq; i++)
        {
            gl = (Gridlet) super.receiveEventObject();  // gets the Gridlet
            //System.out.println("cow i am here" + gl.getGridletStatusString());
            receiveList_.add(gl);   // add into the received list
            //System.out.println(name_ + ": Receiving Gridlet #" + 
            //       gl.getGridletID() + " at time = " + GridSim.clock());
        }
 // System.out.println("\t \t \t"+ c);
 


        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();

        System.out.println(this.name_ + ": sending and receiving of Gridlets" + " complete at " + GridSim.clock() );

    }

    /**
     * Gets a list of received Gridlets
     * @return a list of received/completed Gridlets
     */
    public GridletList getGridletList() {
        return receiveList_;
    }
    
    public void setInfo(int resInfoID [])
    {
        this.resourceInfoID = resInfoID; // list of Grid Resources
    }
    public void setGISMap(int adjacencyMatrix [][])
    {
        this.GISMap = adjacencyMatrix; // GIS network Topology
    }
    
    public void setLocalGISIndex(int index)
    {
        this.myGISIndex_ = index;
    }
    public int [] convertInt (ArrayList list)
    {
        int [] res = new int [list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            res[i] = ((Integer) list.get(i)).intValue();
        }
        return res;
    }
    
    public int [] extractGISAdjacency (int [][] array, int index)
    {
        int adjacenceGISNumel = 0;
        int i = 0;
        for (i = 0; i < array.length; i++)
        {
            if (array[index][i] == 1)
                adjacenceGISNumel++;
        }
        int [] adjacenceGISIndex = new int [adjacenceGISNumel];
        int c = 0;
        for (i = 0; i < array.length; i++)
        {
            if (array[index][i] == 1)
            {
                adjacenceGISIndex[c] = i;
               // System.out.print(adjacenceGISIndex[c] + " ");
                c++;
            }
        }
      //  System.out.println();
        return adjacenceGISIndex;

    }
    
    public ArrayList convertArray (int [] array)
    {
        ArrayList list = new ArrayList(array.length);
        for (int i = 0; i < array.length; i++)
            list.add(array[i]);
        return list;
            
    }
    

    

    /**
     * This method will show you how to create Gridlets
     * @param userID        owner ID of a Gridlet
     * @param numGridlet    number of Gridlet to be created
     */
    private void createGridlet(int userID, int numGridlet, double [] gridletLength, int [] gridletPE)
    {
        double length;
        long file_size = 300;
        long output_size = 300;
        for (int i = 0; i < numGridlet; i++) // i as Gridlet ID
        {
            // Creates a Gridlet
            length = gridletLength[i];
            Gridlet gl = new Gridlet(i,length , file_size, output_size);
            gl.setNumPE(gridletPE[i]);
            gl.setUserID(userID);
            // add this gridlet into a list
            this.list_.add(gl);

        }

    }
    
    private void printArray(String msg, Object[] globalArray)
    {
        // if array is empty
        if (globalArray == null)
        {

            System.out.println(super.get_name() + ": number of "+ msg + " = 0.");
            return;
        }

        System.out.println(super.get_name() + ": number of " + msg + " = " + globalArray.length);

        for (int i = 0; i < globalArray.length; i++)
        {
            Integer num = (Integer) globalArray[i];
            System.out.println(super.get_name() + ": receiving info about " +
                msg + ", name = " + GridSim.getEntityName(num.intValue()) + " (id: " + num + ")");
        }
        System.out.println();
    }



} // end class

