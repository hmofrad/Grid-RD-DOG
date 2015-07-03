/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package VDLAPSO;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList.*;


public class automata {
    
    public String index_;
    public int tempIndex_;
    public int actionNumel_;
    public int tempActionNumel_;
    public ArrayList actionSet_;
    public ArrayList tempActionSet_;
    public ArrayList actionProbability_;
    public ArrayList tempActionProbability_;
    public ArrayList removedActionProbability_;
    public ArrayList removedAction_;
    private double alpha;
    private double beta;
    
    
    
    /**
     * this Class create Distributed Learning Automata
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
        this.index_ = index;
        this.actionNumel_ = adjacenceGISNumel;
        this.tempActionNumel_ = adjacenceGISNumel;
        this.actionProbability_ = new ArrayList();
        
        this.actionSet_ = new ArrayList();
        this.tempActionSet_ = new ArrayList();
        this.removedAction_ = new ArrayList();
        this.removedActionProbability_ = new ArrayList();
        
        
        //this.actionSet_ = actionList;
        this.alpha = 0.1;
        this.beta = 0.1;
        int p = 1;
        for(int i = 0; i < adjacenceGISNumel; i++ )
        {
            this.actionProbability_.add((double) p/adjacenceGISNumel);
            // System.out.println(this.actionProbability_.get(i));
        }

    }
    
    public void setAutomatonActionSet(int memberAction)
    {
        this.actionSet_.add(memberAction);
        //this.tempActionSet_.add(memberAction);
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
            probabilityNumel = (int) Math.rint(((Double) this.actionProbability_.get(i)).doubleValue() * precession);
            if (probabilityNumel == 0)
                 probabilityNumel = 1;
            for (j = 0; j < probabilityNumel; j++)
                probabilitySrc.add(actionName[i]);
        }
        
        selectedAction = ((Integer) probabilitySrc.get(random.nextInt(probabilitySrc.size()))).intValue();   
 
        // Active corresponded LA to selectedAction and Deactive the previous LA
        
        //System.out.println(selectedAction);
        return selectedAction;
        
    }
    
    public void probablityUpdate(int selectedAction, int signal)
    {
        int i;
        if (signal == 0)
        {
            // reward the selected action  
            this.actionProbability_.set(selectedAction, ((Double) this.actionProbability_.get(selectedAction)).doubleValue() +
                                           alpha * (1 - ((Double) this.actionProbability_.get(selectedAction)).doubleValue()));

            // penalty the other actions
            for (i =0; i < this.actionNumel_; i++)
            {
                if (this.actionProbability_.get(i) != this.actionProbability_.get(selectedAction))
                    this.actionProbability_.set(i, ( 1 - alpha) * ((Double) this.actionProbability_.get(i)).doubleValue());
            }
        }
        else
        {
            if ( signal == 1)
            {
                // penalty the selected action
                this.actionProbability_.set(selectedAction, ( 1 - beta) * 
                             ((Double) this.actionProbability_.get(selectedAction)).doubleValue());

                // reward the other actions
                for (i = 0; i < this.actionNumel_; i++)
                {
                 if (this.actionProbability_.get(i) != this.actionProbability_.get(selectedAction))
                     this.actionProbability_.set(i, (beta/(this.actionNumel_ - 1)) +
                             (1 - beta) * ((Double) this.actionProbability_.get(i)).doubleValue());
                }
                
            }
        }
        double sum = 0.0;
        for (i = 0; i < this.actionProbability_.size(); i++)
            sum += ((Double) this.actionProbability_.get(i)).doubleValue();
        System.out.println(sum);
    }
    public void modify (int actionName)
    {
        int i;
        int tf = 0; // True/ False flag
        int selectedActionName = -1; // action Name in this action set
        double selectedActionNameProb = 0.0;
        int selectedAction = -1; // selected action of the automaton
        
        for (i = 0; i < this.actionNumel_; i++)
        {
            selectedActionName = ((Integer) this.actionSet_.get(i)).intValue();
            if (selectedActionName == actionName)
            {
                tf = 1;
         //       System.out.println("automaton_" + this.index_ + ": Remove action " + i +
           //             " with Name = " + selectedActionName + " action numel = " + this.actionNumel_);
                selectedAction = i;
                break;
            }
        }
        // if the selected action is in the action set then ...

        if (tf == 1)
        {
            /*
            for (i = 0; i < this.actionNumel_; i++)
            {
                selectedActionName = ((Integer) this.actionSet_.get(i)).intValue();
                selectedActionNameProb = ((Double) this.actionProbability_.get(i)).doubleValue();
                System.out.println(" action = " + i + " Name = " + selectedActionName + " Prob = " + selectedActionNameProb);
            }
             */
            selectedActionName = ((Integer) this.actionSet_.get(selectedAction)).intValue();
            selectedActionNameProb = ((Double) this.actionProbability_.get(selectedAction)).doubleValue();
            
            // modify actionNumel
            this.actionNumel_ --;
            // add the selected action into a list
            this.removedAction_.add(selectedActionName);
            this.removedActionProbability_.add(selectedActionNameProb);
            

            // reconfigure automaton action set
            this.actionSet_.remove(selectedAction); // remove selected action form action list
            // System.out.println("automaton_" + this.index_ + ": removed action = " + selectedActionName);
            // reconfigure automaton probability vector
            double activeActProbSum = (1 - selectedActionNameProb);
            this.actionProbability_.remove(selectedAction);
        //    System.out.println(this.actionNumel_ + " " +  this.actionProbability_.size());
            for (i = 0; i < this.actionNumel_; i++)
            {
                this.actionProbability_.set(i, ((Double) this.actionProbability_.get(i)).doubleValue()/activeActProbSum);
            }
/*            
            double sum = 0.0;
            for (i = 0; i < this.actionNumel_; i++)
            {
                sum += ((Double) this.actionProbability_.get(i)).doubleValue();
                
            }
            System.out.println(sum);
 */

       //     System.out.println(" action numel = " + this.actionNumel_);
/*            
            System.out.println("automaton_" + this.index_ + ": " + this.actionNumel_); 
            for (i = 0; i < this.actionNumel_; i++)
            {
                System.out.print(this.actionSet_.get(i) + " " + this.actionProbability_.get(i));
                System.out.println();
            }            
*/            

        }            

    }

    public void restore ()
    {
/*        
       
    //    System.out.println("BEFORE: automaton_ " + this.index_ + ": action set size = " + this.actionNumel_ +
    //            " and temp action set size = " + this.tempActionNumel_);

        this.actionNumel_ = this.tempActionNumel_;
        this.actionProbability_.clear();
        this.actionProbability_ = (ArrayList) this.tempActionProbability_.clone();
        this.actionSet_.clear();
        this.actionSet_ = (ArrayList) this.tempActionSet_.clone();
        this.removedAction_.clear();
        this.removedActionProbability_.clear();
  //      System.out.println("AFTER: automaton_ " + this.index_ + ": action set size = " + this.actionNumel_ +
    //            " and temp action set size = " + this.tempActionNumel_);
        
*/                
        
        // map action probability from remove order
        int i = 0;
        int j = 0;
        int k = 0;
/*        
        double activeActSumProb = 0.0;
        for (i = 0; i < this.actionProbability_.size(); i++)
            activeActSumProb += ((Double) this.actionProbability_.get(i)).doubleValue();
        for (i = 0; i < this.actionProbability_.size(); i++)
            this.actionProbability_.set(i,((Double) this.actionProbability_.get(i)).doubleValue() * activeActSumProb);
*/        
        // reconfigure probability vector
        int recoveredActionLength = this.removedAction_.size();
        int recoveredAction = 0;
        double recoveredProb = 0.0;
        int [] recoveredActArray = new int [recoveredActionLength];
        double [] recoveredProbArray = new double [recoveredActionLength];
        int actionTemp = 0;
        double probabilityTemp = 0.0;
/*        
        for (i = 0; i < recoveredActionLength; i++)
        {
            System.out.print(((Integer) this.removedAction_.get(i)).intValue() + " ");
        }
        System.out.println();
        
        for (i = 0; i < recoveredActionLength; i++)
        {
            System.out.print(((Double) this.removedActionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
 */
/*
        // sort action probabiliies
        for (i = 0; i < recoveredActionLength; i++)
        {
            recoveredActArray [i] = ((Integer) this.removedAction_.get(i)).intValue();
            recoveredProbArray[i] = ((Double) this.removedActionProbability_.get(i)).doubleValue();
            
        }       
        
        for (i = 0; i < recoveredActArray.length; i++)
        {
            for (j = i+1; j <recoveredActArray.length; j++)
            {
                if (recoveredActArray[j] < recoveredActArray[i])
                {

                    actionTemp = recoveredActArray[i];
                    recoveredActArray[i] = recoveredActArray[j];
                    recoveredActArray[j] = actionTemp;
                    
                    probabilityTemp = recoveredProbArray[i];
                    recoveredProbArray[i] = recoveredProbArray[j];
                    recoveredProbArray[j] = probabilityTemp;
                }               
            }
        }
        this.removedAction_.clear();
        this.removedActionProbability_.clear();
        
        for (i = 0; i < recoveredActionLength; i++)
        {
            this.removedAction_.add(recoveredActArray[i]);
            this.removedActionProbability_.add(recoveredProbArray[i]);            
        }
        
*/        
 /*
        for (i = 0; i < recoveredActionLength; i++)
        {
            System.out.print(((Integer) this.removedAction_.get(i)).intValue() + " ");
        }
        System.out.println();
        
        System.out.print("All Actions: ");
        for (i = 0; i < this.tempActionNumel_; i++)
        {
            System.out.print(((Integer) this.tempActionSet_.get(i)).intValue() + " ");
        }
        System.out.println();
        
        System.out.print("All Probs: ");
        for (i = 0; i < this.tempActionNumel_; i++)
        {
            System.out.print(((Double) this.tempActionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
        
        System.out.print("Removed Actions: ");
        for (i = 0; i < this.removedAction_.size(); i++)
        {
            System.out.print(((Integer) this.removedAction_.get(i)).intValue() + " ");
        }
        System.out.println();
        
        System.out.print("Removed Probs: ");
        for (i = 0; i < this.removedActionProbability_.size(); i++)
        {
            System.out.print(((Double) this.removedActionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
        
        System.out.print("Remained Actions: ");
        for (i = 0; i < this.actionSet_.size(); i++)
        {
            System.out.print(((Integer) this.actionSet_.get(i)).intValue() + " ");
        }
        System.out.println();
        
        System.out.print("Remained Probs: ");
        for (i = 0; i < this.actionProbability_.size(); i++)
        {
            System.out.print(((Double) this.actionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
*/
        this.actionNumel_ = this.tempActionNumel_;
        //this.actionSet_.clear();
       // this.actionSet_ = (ArrayList) this.tempActionSet_.clone();
        double activeActSumProb = 0.0;
        for (i = recoveredActionLength-1; i >= 0; i--)
        {
            //System.out.println("Insert: " + )
            recoveredAction = ((Integer) this.removedAction_.get(i)).intValue();
            recoveredProb   = ((Double) this.removedActionProbability_.get(i)).doubleValue();
            
            for (j = 0; j < this.actionNumel_; j++)
            {
                if (((Integer) this.tempActionSet_.get(j)).intValue() ==  recoveredAction)
                {
                    // map the other active action probabilities
                    activeActSumProb = 1 - recoveredProb;
                    for (k = 0; k < this.actionProbability_.size(); k++)
                        this.actionProbability_.set(k,((Double) this.actionProbability_.get(k)).doubleValue() * activeActSumProb);
                     // place the corresponding probability
                    this.actionSet_.add(recoveredAction); //
                    this.actionProbability_.add(recoveredProb); // ?????
                    break;
                }
                
            }
        }
/*        
        double S = 0.0;
        for (i = 0; i < this.actionNumel_; i++)
            S += ((Double) this.actionProbability_.get(i)).doubleValue();
        System.out.println(S);

*/
/*        
        for (i = 0; i < this.actionNumel_; i++)
        {
            System.out.print(((Integer) this.actionSet_.get(i)).intValue() + " ");
        
        }
        System.out.println();
        for (i = 0; i < this.actionNumel_; i++)
        {
                System.out.print(((Double) this.actionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
*/        
       // sort action probabiliies
        int [] actionArray = new int [this.actionNumel_];
        double [] probabilityArray = new double [this.actionNumel_];
        for (i = 0; i < this.actionNumel_; i++)
        {
            actionArray [i] = ((Integer) this.actionSet_.get(i)).intValue();
            probabilityArray[i] = ((Double) this.actionProbability_.get(i)).doubleValue();            
        }       
        
        for (i = 0; i < this.actionNumel_; i++)
        {
            for (j = i+1; j <this.actionNumel_; j++)
            {
                if (actionArray[j] < actionArray[i])
                {

                    actionTemp = actionArray[i];
                    actionArray[i] = actionArray[j];
                    actionArray[j] = actionTemp;
                    
                    probabilityTemp = probabilityArray[i];
                    probabilityArray[i] = probabilityArray[j];
                    probabilityArray[j] = probabilityTemp;
                }               
            }
        }
        this.actionSet_.clear();
        this.actionProbability_.clear();
        
        for (i = 0; i < this.actionNumel_; i++)
        {
            this.actionSet_.add(actionArray[i]);
            this.actionProbability_.add(probabilityArray[i]);            
        }
/*        
        System.out.println();
        for (i = 0; i < this.actionNumel_; i++)
        {
            System.out.print(((Integer) this.actionSet_.get(i)).intValue() + " ");
        }
        System.out.println();
        for (i = 0; i < this.actionNumel_; i++)
        {
            System.out.print(((Double) this.actionProbability_.get(i)).doubleValue() + " ");
        }        
        System.out.println();
        // clear or Restore session Arrays
        //this.actionProbability_.clear();
        //this.actionProbability_ = (ArrayList) this.tempActionProbability_.clone();
*/

        this.removedAction_.clear();
        this.removedActionProbability_.clear();
        System.out.println();
/*        
        System.out.println("////////////");
        for (i = 0; i < this.actionNumel_; i++)
        {
            System.out.print(this.actionSet_.get(i) + " " + this.actionProbability_.get(i));
            System.out.println();
        }
*/
    }
    
    public void save ()
    {
        this.tempActionProbability_ = (ArrayList) this.actionProbability_.clone();
    }
    public void updateInactive ()
    {
        int i = 0;
        int j = 0;
        int k = 0;
        int removedActionLength = this.removedAction_.size();
        int recoveredAction = 0;
        double recoveredProb = 0.0;
        int    actionTemp = 0;
        double probabilityTemp = 0.0;
        double activeActSumProb = 0.0;


        recoveredAction = ((Integer) this.removedAction_.get(removedActionLength - 1)).intValue();
        recoveredProb   = ((Double) this.removedActionProbability_.get(removedActionLength - 1)).doubleValue();
        //System.out.println("automaton_" + this.index_ + ": restore action = " + recoveredAction);
        // map the other active action probabilities
        activeActSumProb = 1 - recoveredProb;
        for (k = 0; k < this.actionNumel_; k++)
            this.actionProbability_.set(k,((Double) this.actionProbability_.get(k)).doubleValue() * activeActSumProb);

        // place the corresponding probability
        this.actionSet_.add(recoveredAction);
        this.actionProbability_.add(recoveredProb);
        //System.out.println(this.actionSet_.size() + " " + this.actionNumel_ + " " + this.tempActionNuml);
        this.actionNumel_++;
        ///////////////////////////////////
        /// sort action probabiliies
        int [] actionArray = new int [this.actionNumel_];
        double [] probabilityArray = new double [this.actionNumel_];
       // System.out.println(this.actionSet_.size() + " " + this.actionProbability_.size() + " " + this.actionNumel_);
        for (i = 0; i < this.actionNumel_; i++)
        {

            actionArray [i] = ((Integer) this.actionSet_.get(i)).intValue();
            probabilityArray[i] = ((Double) this.actionProbability_.get(i)).doubleValue();            
        }       

        for (i = 0; i < this.actionNumel_; i++)
        {
            for (j = i+1; j <this.actionNumel_; j++)
            {
                if (actionArray[j] < actionArray[i])
                {

                    actionTemp = actionArray[i];
                    actionArray[i] = actionArray[j];
                    actionArray[j] = actionTemp;

                    probabilityTemp = probabilityArray[i];
                    probabilityArray[i] = probabilityArray[j];
                    probabilityArray[j] = probabilityTemp;
                }               
            }
        }
        this.actionSet_.clear();
        this.actionProbability_.clear();

        for (i = 0; i < this.actionNumel_; i++)
        {
            this.actionSet_.add(actionArray[i]);
            this.actionProbability_.add(probabilityArray[i]);            
        }
        this.removedAction_.remove(removedActionLength - 1);
        this.removedActionProbability_.remove(removedActionLength - 1);
        
/*        
        double S = 0.0;
        for (i = 0; i < this.actionNumel_; i++)
            S += ((Double) this.actionProbability_.get(i)).doubleValue();
        System.out.println(S);
*/

    }
    public void updateActive(int selectedAction, int signal)
    {
        int i,j,k;
/*        
        for (i = 0; i < this.actionNumel_; i++)
        {
            System.out.println(i + " " + ((Integer) this.actionSet_.get(i)).intValue());
            
     //       if (((Integer) this.actionSet_.get(i)).intValue() == selectedAction)
    //        {
    //            System.out.println("COW");
      //      }
        }
 */
        for(i =0; i < this.actionNumel_; i++)
        {
            if(((Integer) this.actionSet_.get(i)).intValue() == selectedAction)
                selectedAction = i;
        }
/*        
        System.out.println(" Before");
        for(i =0; i < this.actionNumel_; i++)
        {
            System.out.print(((Double) this.actionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
 */
        if (signal == 0)
        {
            // reward the selected action  

            this.actionProbability_.set(selectedAction, ((Double) this.actionProbability_.get(selectedAction)).doubleValue() +
                                           alpha * (1 - ((Double) this.actionProbability_.get(selectedAction)).doubleValue()));

            // penalty the other actions
            for (i =0; i < this.actionNumel_; i++)
            {
                if (this.actionProbability_.get(i) != this.actionProbability_.get(selectedAction))
                    this.actionProbability_.set(i, ( 1 - alpha) * ((Double) this.actionProbability_.get(i)).doubleValue());
            }
        }
        else if ( signal == 1)
        {
            // penalty the selected action
            this.actionProbability_.set(selectedAction, ( 1 - beta) * 
                         ((Double) this.actionProbability_.get(selectedAction)).doubleValue());

            // reward the other actions
            for (i = 0; i < this.actionNumel_; i++)
            {
             if (this.actionProbability_.get(i) != this.actionProbability_.get(selectedAction))
                 this.actionProbability_.set(i, (this.beta/(this.actionNumel_ - 1)) +
                         (1 - this.beta) * ((Double) this.actionProbability_.get(i)).doubleValue());
            }
        }
/*        
        System.out.println("After");
        for(i =0; i < this.actionNumel_; i++)
        {
            System.out.print(((Double) this.actionProbability_.get(i)).doubleValue() + " ");
        }
        System.out.println();
*/
        
/*        
        double sum = 0.0;
        for (i = 0; i < this.actionProbability_.size(); i++)
            sum += ((Double) this.actionProbability_.get(i)).doubleValue();
        System.out.println(sum);        
*/        
    }
            
}
