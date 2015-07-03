
package RD_FLOODING;



import gridsim.*;
import gridsim.index.*;
import gridsim.net.*;
import java.util.*;
import gridsim.*;
import gridsim.index.*;
import gridsim.net.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

        



/**
 * A test driver to run multiple regional GIS in a network.
 * This test driver uses only NetUser.java file
 */
public class body
{
    /**
     * Creates main() to run this example
     * This the Automata Test Example
     */
    public static void main(String[] args)
    {
        System.out.println("Starting GIS-network examples ...");

        try
        {
            
            //////////////////////////////////////////
            // Variables that are important to this example
            int num_user = 5;        // number of grid users


            double baud_rate = 1e8;  // 100 Mbps
            double propDelay = 10;   // propagation delay in millisecond
            int mtu = 1500;          // max. transmission unit in byte
            int i = 0;               // a temp variable for a loop
            int j = 0;
            int gisIndex = 0;        // a variable to select a GIS entity
            RegionalGIS gis = null;  // a regional GIS entity
            GISInformation gisInfo;
            


            //////////////////////////////////////////
            // First step: Initialize the GridSim package. It should be called
            // before creating any entities. We can't run this example without
            // initializing GridSim first. We will get a run-time exception
            // error.
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;     // true means trace GridSim events

            // Initialize the GridSim package
            System.out.println("Initializing GridSim package");
            GridSim.init(num_user, calendar, trace_flag);
            // Read Network Topology
            int num_GIS;
            //int connectionDegree = num_GIS - 1;

            //adjacencyMatrix = generateNetworkTopology(num_GIS, connectionDegree);
            int scale = 0;
            // 0   1    2     3   4    5    6    7
            // 500 1000 1500 2000 2500 3000 4000 5000
            String topologyScale;
            String mapResource;
            switch (scale)
            {
                case 0:
                {
                    topologyScale = "T500.txt";
                    mapResource = "R500.txt";
                    num_GIS = 500;
                    break;
                }
                case 1:
                {
                    topologyScale = "T1000.txt";
                    mapResource = "R1000.txt";
                    num_GIS = 1000;
                    break;
                }
                case 2:
                {
                    topologyScale = "T1500.txt";
                    mapResource = "R1500.txt";
                    num_GIS = 1500;
                    break;
                }
                case 3:
                {
                    topologyScale = "T2000.txt";
                    mapResource = "R2000.txt";
                    num_GIS = 2000;
                    break;
                }
                case 4:
                {
                    topologyScale = "T2500.txt";
                    mapResource = "R2500.txt";
                    num_GIS = 2500;
                    break;
                }
                case 5:
                {
                    topologyScale = "T3000.txt";
                    mapResource = "R3000.txt";
                    num_GIS = 3000;
                    break;
                }
                case 6:
                {
                    topologyScale = "T4000.txt";
                    mapResource = "R4000.txt";
                    num_GIS = 4000;
                    break;
                }
                case 7:
                {
                    topologyScale = "T5000.txt";
                    mapResource = "R5000.txt";
                    num_GIS = 5000;
                    break;
                }
                default:
                {
                    topologyScale = "InvalidTopology";
                    mapResource = "InvalidMap";
                    num_GIS = 0;
                    break;
                }
            }

            int adjacencyMatrix [][]=new int[num_GIS][num_GIS];
            FileReader fr=new FileReader(topologyScale);            
            BufferedReader br=new BufferedReader(fr);
            String line;
            for (i = 0; i < num_GIS; i++) {
                line = br.readLine();
                String[] values=line.split(",");
                for (j=0;j<num_GIS;j++)
                    adjacencyMatrix[i][j]=Integer.parseInt(values[j]);
            }
            br.close();
            fr.close();
            // Resource Distribution parameters [CPU][RAM][HDD][OS]
            // int maxResource = 100;
            // int distinctType = 4;
            // int totalResource = maxResource * distinctType;
            // int[] maxFrequency = {4, 6, 4, 2}; // [1 4] [5 10] [11 14] [15 16]



            // Read Job charachteristics
            int totalGridlet = 250;
            int   [] gridletVector = new int  [totalGridlet];
            double[] gridletLength = new double [totalGridlet];

            FileReader fr0 =new FileReader("job.txt");            
            BufferedReader br0 =new BufferedReader(fr0);
            String line0;

            for (i = 0; i < 2; i++)
            {
                line0 = br0.readLine();
                String[] values0=line0.split(",");
                if (i == 0)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletVector[j]=Integer.parseInt(values0[j]);                    
                }
                else if (i == 1)
                {
                    for (j = 0; j < totalGridlet; j++)   
                        gridletLength[j]=Integer.parseInt(values0[j]);                    
                }
            }
            br0.close();
            fr0.close();

            //////////////////////////////////////////
            // Second step: Creates one or more regional GIS entities
            ArrayList gisList = new ArrayList();  // array
            ArrayList gisInformationList = new ArrayList();  // array
            
            int [] gisIDList = new int [num_GIS];
            for (i = 0; i < num_GIS; i++)
            {
                String gisName = "Regional_GIS_" + i;   // regional GIS name

                // a network link attached to this regional GIS entity
                Link link = new SimpleLink(gisName + "_link", baud_rate,propDelay, mtu);
                // create a new regional GIS entity
                gis = new RegionalGIS(gisName, link);
                //System.out.println("Creating a " + gisName + " with id = " + gis.get_id());
                gisIDList[i] = gis.get_id();
                // store extra information about GIS entity
                gisInfo = new GISInformation(i, gis.get_id(), gisName);
                // add this GIS into List
                gisInformationList.add(gisInfo);
                gisList.add(gis);
            }
                        
            // Read Resource Distribution
            int totalResource = 500;
            int [] resourceVector = new int [totalResource];
            FileReader fr1=new FileReader("resource.txt");
            BufferedReader br1=new BufferedReader(fr1);
            String line1;
            line1 = br1.readLine();
            String[] values1=line1.split(",");
            for (i = 0; i < totalResource; i++)
                resourceVector[i]=Integer.parseInt(values1[i]);
            br1.close();
            fr1.close();
            
            
            
            ArrayList resList = new ArrayList(totalResource);
            int totalMachine = 1;   // number of machines or nodes
            int rating = 1500;      // an estimation CPU power
            int [] resInfoID = new int [totalResource];
           
            
                   
            // construct resource to GIS map
            int [] gisIndexArray = new int [totalResource];
            FileReader fr2=new FileReader(mapResource);            
            BufferedReader br2=new BufferedReader(fr2);
            String line2;
            line2 = br2.readLine();
            String[] values2=line2.split(",");
            for (i = 0; i < totalResource; i++)
                gisIndexArray[i]=Integer.parseInt(values2[i]);
            br2.close();
            fr2.close();

            
            // create a typical resource
            for (i = 0; i < totalResource; i++)
            {
                GridResource res = createGridResource("Res_" + i , resourceVector[i],
                                 totalMachine, rating, baud_rate, propDelay, mtu);

                resInfoID[i] = res.get_id(); 
                // allocate this resource to a random regional GIS entity
                //gisIndex = random.nextInt(num_GIS);
                gisIndex = gisIndexArray[i];
                gis = (RegionalGIS) gisList.get(gisIndex);
                res.setRegionalGIS(gis);    // set the regional GIS entity
                // System.out.println(res.get_name() + " will register to " + gis.get_name());
                gisInfo = (GISInformation) gisInformationList.get(gisIndex);
                gisInfo.setGISResourceMember(i);
                gisInformationList.set(gisIndex, gisInfo);
                // put this resource into a list
                resList.add(res);
            }
            System.out.println();

            //////////////////////////////////////////
            // Fourth step: Creates one or more grid user entities
            Random random = new Random();   // a random generato
            ArrayList userList = new ArrayList(num_user);
            int startNode = 10; // start node can be from node # 1 to 10
            for (i = 0; i < num_user; i++)
            {
                NetUser user = new NetUser("User_" + i,totalGridlet,
                                baud_rate, propDelay, mtu, totalResource, 
                                gisInformationList, gridletLength, gridletVector);
                user.setInfo(resInfoID);
                user.setGISMap(adjacencyMatrix);
                gisIndex = random.nextInt(startNode);
                gis = (RegionalGIS) gisList.get(gisIndex);
                user.setRegionalGIS(gis);   // set the regional GIS entity
                user.setLocalGISIndex(gisIndex);
                System.out.println(user.get_name() + " will communicate to " + gis.get_name() +
                                                     " with id= " + gis.get_id());
                // put this user into a list
                userList.add(user);
            }
            //////////////////////////////////////////
            // Fifth step: Builds the network topology for all entities.

            // In this example, the topology is:
            // with Regional GIS the network topology becomes:
            // User(s) --- r1 --- r2 --- r3 --- GIS(s)
            //                    |
            //                    |---- resource(s)
            createTopology(gisList, resList, userList);

            //////////////////////////////////////////
            // Sixth step: Starts the simulation
            System.out.println();
            GridSim.startGridSimulation();

            // write network topology
   //     try 
  //      {


    //    }
    //    catch (Exception ex)
  //      {
   //         Logger.getLogger(body.class.getName()).log(Level.SEVERE, null, ex);
  //      }
            
            
            //////////////////////////////////////////
            FileOutputStream fos = new FileOutputStream("Output.txt",true);
            PrintWriter pw = new PrintWriter(fos);
            GridletList glList = null;
            NetUser user_obj = null;
            int avg,utilityCount,redCount;
            int [][] learnProcess = new int [userList.size()][totalGridlet];
            System.out.println("////////////// " + "RD_BESTNEIGHBOR " + num_GIS + " //////////////");
            pw.println("////////////// " + "RD_BESTNEIGHBOR " + num_GIS + " //////////////");
            
            for (i = 0; i < userList.size(); i++)
            {
                user_obj = (NetUser) userList.get(i);
                System.out.println("////////////// " + user_obj.getName() + " //////////////");
                pw.println("////////////// " + user_obj.getName() + " //////////////");
                
                glList = user_obj.getGridletList();
                avg = 0;
                for (j = 0; j < totalGridlet; j++)
                {
                    avg += user_obj.hopReq[j];
                }
                avg = avg/totalGridlet;
                System.out.println("Average Hop:" + avg);
                pw.println("Average Hop:" + avg);
                
                System.out.println("successfull Requests:" + user_obj.successReq);
                pw.println("successfull Requests:" + user_obj.successReq);
                                
                utilityCount = 0;
                for (j = 0; j< totalResource; j++)
                {
                    if (user_obj.resourceUtil[j] == 1)
                        utilityCount++;
                }        
                System.out.println("Resource Utilization:" + utilityCount);
                pw.println("Resource Utilization:" + utilityCount);
                                
                redCount = 0;
                for (j = 0; j< totalGridlet; j++)
                    redCount += user_obj.redundancyCounter[j];
                redCount = redCount/totalGridlet;
                
                System.out.println("Redundancy Counter:" + redCount);
                pw.println("Redundancy Counter:" + redCount);
                
                for (j = 0; j< totalGridlet; j++)
                    learnProcess[i][j] = user_obj.gridletStatus[j];
                
                //System.out.println(glList.size());
                //printGridletList(glList, user_obj.get_name(), false);
                
            }
            pw.close();
            fos.close();
            
            ///////////////////////////////
            int c = 0;
            FileOutputStream fos0 = new FileOutputStream("Process.txt");
            PrintWriter pw0 = new PrintWriter(fos0);
            for (i = 0; i < totalGridlet; i++)
            {
                for (j = 0; j< userList.size(); j++)
                {
                    if(learnProcess[j][i] == 1)
                        c++;
                }

                if (c >= Math.ceil((double) userList.size()/2))
                    pw0.println("1 ");
                else
                    pw0.println("0 ");
                c = 0;
            }
            pw0.println();
            pw0.close();
            fos0.close();

            System.out.println("\nFinish network example ...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }

    /**
     * Creates a simple network topology
     * In this example, the topology is:
     * with Regional GIS the network topology becomes:
     * User(s) --- r1 --- r2 --- r3 --- GIS(s)
     *                    |
     *                    |---- resource(s)
     */
    private static void createTopology(ArrayList gisList, ArrayList resList,
                                       ArrayList userList) throws Exception
    {
                    
        int i = 0;
        double baud_rate = 1e8;  // 100 Mbps
        double propDelay = 10;   // propagation delay in millisecond
        int mtu = 1500;          // max. transmission unit in byte

        // create the routers
        Router r1 = new RIPRouter("router1");   // router 1
        Router r2 = new RIPRouter("router2");   // router 2
        Router r3 = new RIPRouter("router3");   // router 3
        
        // connect all user entities with r1 router
        // For each host, specify which PacketScheduler entity to use.
        NetUser obj = null;
        for (i = 0; i < userList.size(); i++)
        {
            FIFOScheduler userSched = new FIFOScheduler("NetUserSched_"+i);
            obj = (NetUser) userList.get(i);
            r1.attachHost(obj, userSched);
        }

        // connect all resource entities with r2 router
        // For each host, specify which PacketScheduler entity to use.
        GridResource resObj = null;
        for (i = 0; i < resList.size(); i++)
        {

            FIFOScheduler resSched = new FIFOScheduler("GridResSched_"+i);
            resObj = (GridResource) resList.get(i);
            r2.attachHost(resObj, resSched);
        }

        // then connect r1 to r2
        // For each host, specify which PacketScheduler entity to use.
        Link link = new SimpleLink("r1_r2_link", baud_rate, propDelay, mtu);
        FIFOScheduler r1Sched = new FIFOScheduler("r1_Sched");
        FIFOScheduler r2Sched = new FIFOScheduler("r2_Sched");

        // attach r2 to r1
        r1.attachRouter(r2, link, r1Sched, r2Sched);
            
        // attach r3 to r2
        FIFOScheduler r3Sched = new FIFOScheduler("r3_Sched");
        link = new SimpleLink("r2_r3_link", baud_rate, propDelay, mtu);
        r2.attachRouter(r3, link, r2Sched, r3Sched);

        // attach regional GIS entities to r3 router
        RegionalGIS gis = null;
        for (i = 0; i < gisList.size(); i++)
        {
            FIFOScheduler gisSched = new FIFOScheduler("gis_Sched" + i);
            gis = (RegionalGIS) gisList.get(i);
            r3.attachHost(gis, gisSched);
        }
    }

    /**
     * Creates one Grid resource. A Grid resource contains one or more
     * Machines. Similarly, a Machine contains one or more PEs (Processing
     * Elements or CPUs).
     */
    private static GridResource createGridResource(String name,
                int totalPE, int totalMachine, int rating,
                double baud_rate, double delay, int MTU)
    {
        // Here are the steps needed to create a Grid resource:
        // 1. We need to create an object of MachineList to store one or more
        //    Machines
        MachineList mList = new MachineList();

        for (int i = 0; i < totalMachine; i++)
        {
            // 2. Create one Machine with its id, number of PEs and rating
            mList.add( new Machine(i, totalPE, rating) );
        }

        // 3. Create a ResourceCharacteristics object that stores the
        //    properties of a Grid resource: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/PE time unit).
        String arch = "Intel";      // system architecture
        String os = "Linux";        // operating system
        double time_zone = 10.0;    // time zone this resource located
        double cost = 3.0;          // the cost of using this resource

        ResourceCharacteristics resConfig = new ResourceCharacteristics(
                arch, os, mList, ResourceCharacteristics.SPACE_SHARED,
                time_zone, cost);

        // 4. Finally, we need to create a GridResource object.
        long seed = 11L*13*17*19*23+1;
        double peakLoad = 0.0;        // the resource load during peak hour
        double offPeakLoad = 0.0;     // the resource load during off-peak hr
        double holidayLoad = 0.0;     // the resource load during holiday

        // incorporates weekends so the grid resource is on 7 days a week
        LinkedList Weekends = new LinkedList();
        Weekends.add(new Integer(Calendar.SATURDAY));
        Weekends.add(new Integer(Calendar.SUNDAY));

        // incorporates holidays. However, no holidays are set in this example
        LinkedList Holidays = new LinkedList();
        GridResource gridRes = null;
        try
        {
            // creates a GridResource with a link
            Link link = new SimpleLink(name + "_link", baud_rate, delay, MTU);
            gridRes = new GridResource(name, link, seed, resConfig, peakLoad,
                                offPeakLoad, holidayLoad, Weekends, Holidays);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

       // System.out.println("Creating a Grid resource (name: " + name +
       //         " - id: " + gridRes.get_id() + " and PE: " + totalPE + ")");

        return gridRes;
    }
    private static void printGridletList(GridletList list, String name,boolean detail)
    {
        int size = list.size();
        Gridlet gridlet = null;

        String indent = "    ";
        System.out.println();
        System.out.println("============= OUTPUT for " + name + " ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent +
                "Resource ID" + indent + "Cost");

        // a loop to print the overall result
        int i = 0;
        for (i = 0; i < size; i++)
        {
            gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent
                    + indent);

            System.out.print( gridlet.getGridletStatusString() );

            System.out.println( indent + indent + gridlet.getResourceID() +
                    indent + indent + gridlet.getProcessingCost() );
        }

        if (detail == true)
        {
            // a loop to print each Gridlet's history
            for (i = 0; i < size; i++)
            {
                gridlet = (Gridlet) list.get(i);
                System.out.println( gridlet.getGridletHistory() );
                System.out.print("Gridlet #" + gridlet.getGridletID() );
                System.out.println(", length = " + gridlet.getGridletLength()
                        + ", finished so far = " + gridlet.getGridletFinishedSoFar() );
                System.out.println("======================================\n");
            }
        }
    }

} // end class

