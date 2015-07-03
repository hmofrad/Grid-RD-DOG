/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DLAPSO;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList.*;


public class automata {
    
    public String index_;
    public int actionNumel_;
    public ArrayList actionSet_;
    public double[] actionProbability_;
    private double alpha;
    private double beta;
    
    
    
        /**
     * Creates a new Learning Automaton object
     * @param index this automaton index 
     * @param actionNumel_ this automaton action cardinality
     * @param actionSet_    this automaton action set
     * @param actionProbability_  this automaton probability vector
     * @param alpha     reward signal
     * @param beta      penalty signal 
     */
    
    public automata (String index, int adjacenceGISNumel)
    {
        int i,j = 0;
        this.index_ = index;
        this.actionNumel_ = adjacenceGISNumel;
        this.actionProbability_ = new double [adjacenceGISNumel];
        this.actionSet_ = new ArrayList();
        this.alpha = 0.1;
        //.beta = 0.1;
        int p = 1;
        for(i = 0; i < adjacenceGISNumel; i++ )
            this.actionProbability_[i] = (double) p/adjacenceGISNumel;
    }


    public void setAutomatonActionSet(int memberAction)
    {
        this.actionSet_.add(memberAction);
    }


    
    public int actionSelection ()
    {
        // roulette wheel selection
        int i,j = 0; 
        //int adjacenceGISNumel = this.actionNumel_;
        int probabilityNumel;
        int selectedAction;
        int precession = 10000;
        //double[] actionProbability_ = this.actionProbability_;
        int[] actionName = new int[this.actionNumel_];
        for (i = 0; i < this.actionNumel_; i++)
            actionName[i] = i;
        Random random = new Random();   // a random generator
        ArrayList probabilitySrc = new ArrayList();
        
        for (i = 0; i < this.actionNumel_; i++)
        {
            probabilityNumel = (int) Math.rint(this.actionProbability_[i]*precession);

            if (probabilityNumel == 0)
                 probabilityNumel = 1;
            for (j = 0; j < probabilityNumel; j++)
                probabilitySrc.add(actionName[i]);
        }
        
        selectedAction = ((Integer) probabilitySrc.get(random.nextInt(probabilitySrc.size()))).intValue();   
        
        if (selectedAction > this.actionNumel_)
        {
            System.out.println("Alert");
            System.out.println(selectedAction +" " + this.actionNumel_);
            
        }
            
        return selectedAction;
        
        
    }
    
    public void probablityUpdate(int selectedAction, int signal)
    {
        int i;
        if (signal == 0)
        {
            // reward the selected action    
            this.actionProbability_[selectedAction] = this.actionProbability_[selectedAction] +
                                         this.alpha * (1 - this.actionProbability_[selectedAction]);
            // penalty the other actions
            for (i =0; i < this.actionNumel_; i++)
            {
                if (this.actionProbability_[i] != this.actionProbability_[selectedAction])
                    this.actionProbability_[i]  = ( 1 - this.alpha) * this.actionProbability_[i];
            }
        }
        else if ( signal == 1)
        {
            // penalty the selected action
            this.actionProbability_[selectedAction] = (1 - this.beta) * this.actionProbability_[selectedAction];

            // reward the other actions
            for (i = 0; i < this.actionNumel_; i++)
            {
             if (this.actionProbability_[i] != this.actionProbability_[selectedAction])
                 this.actionProbability_[i] = (this.beta/(this.actionNumel_ - 1)) + (1 - beta) * this.actionProbability_[i];
            }
        }
    }
}
