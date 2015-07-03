/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RD_FLOODING;


import RD_RANDOM.*;
import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList.*;


/**
 *
 * @author MORBiD
 */
public class GISInformation
{
    public int index_;        // my entity index 
    public int myId_;         // my entity ID
    public String myName_;    // my entity name
    public ArrayList myResourcelist_;          // list of submitted Gridlets

    
    
    

    /**
     * Creates a new GISInformation object
     * @param index this GIS index
     * @param id    this GIS id
     * @param name  this GIS name
     * @param Resource List  total number of Resources to be assigned
     * 
     */
    
    public GISInformation  (int index, int id, String name)
    {
        this.index_ = index;
        this.myId_ = id;
        this.myName_ = name;
        this.myResourcelist_ = new ArrayList ();
    }
    
    public int getGISIndex()
    {
        return this.index_;
    }
    public int getGISID()
    {
        return this.myId_;
    }
    public ArrayList getGISResourceList()
    {
        return this.myResourcelist_;
    }

    public void setGISResourceMember(int memberResource)
    {
        this.myResourcelist_.add(memberResource);
    }

}