package RHEA;

import RHEA.utils.ParameterSet;


import java.util.HashMap;
import java.util.Random;
import java.util.LinkedList;
import java.util.Deque;

import enumerate.Action;
import enumerate.State;

import struct.FrameData;
import util.BaseUtil;
import struct.CharacterData;

import static util.BaseUtil.*;
import static RHEA.utils.Constants.*;

import RHEA.utils.GeneralInformation;

import RHEA.Heuristics.SimpleStateHeuristic;



class RollingHorizonPlayer {

    ParameterSet params;
    private Population population;
    int numCalls;

    Random random;

    // Action mapping
    int start_nActions;
    int continue_nActions;
    private HashMap<Integer, Action> start_action_mapping;
    private HashMap<Action, Integer> start_action_mapping_r;
    private HashMap<Integer, Action> continue_action_mapping;
    private HashMap<Action, Integer> continue_action_mapping_r;
    int[] actionDist; // action distribution
    private int lastAct = -1;
    public int MAX_ACTIONS = Action.values().length;
    private GeneralInformation gi;

    RollingHorizonPlayer(GeneralInformation gi, Random randomGen) {
        this.random = randomGen;
        this.gi = gi;

       
    }
    
    boolean IsGameOver(FrameData frameData){
    	return frameData.getRemainingFramesNumber()<=5 
    			|| frameData.getCharacter(this.gi.getMyPlayer()).getHp() ==0
    			|| frameData.getCharacter(!this.gi.getMyPlayer()).getHp() == 0;
    }
    
    

    private void initStateInfo(GeneralInformation gi) {
          start_nActions =   gi.startAvailActions.size(); // actionAir.length + actionGround.length;
          continue_nActions = gi.myStartHitActions.size();
//          actionDist = new int[nActions];  // MAX_ACTIONS = 56
          start_action_mapping = new HashMap<>();
          continue_action_mapping = new HashMap<>();
          start_action_mapping_r = new HashMap<>();
          continue_action_mapping_r = new HashMap<>();
         
          int k =0;
    	  for (Action act: gi.startAvailActions){
    		  start_action_mapping.put(k, act);
    		  start_action_mapping_r.put(act, k);
    		  k++;
    	  }
    	  k = 0;
    	  for (Action act: gi.myStartHitActions){
    		  continue_action_mapping.put(k, act);
    		  continue_action_mapping_r.put(act, k);
    		  k++;
    	  }

    	  Gene.initValidActions(start_action_mapping, continue_action_mapping);
    }


    /**
     * Initialize the player for a new game tick to update information.
     * @param stateObs - current StateObservation
     * @param agent - agent calling the player
     */
    void init(GeneralInformation gi, RHEAAgent agent) {
    	initStateInfo(gi);
        numCalls = 0;

        if (params.SHIFT_BUFFER && lastAct != -1) {
            population.shiftLeft(lastAct);
        } else {
            population = new Population(agent, this, params.MAX_FM_CALLS);
            if (params.INIT_TYPE == INIT_ONESTEP) {
                numCalls += population.initOneStep(gi, new SimpleStateHeuristic());
            }
            if (params.INIT_TYPE == INIT_MCTS) {
                numCalls += population.initMCTS(gi);
            }
        }

        numCalls += population.evaluateAll(gi, params.MAX_FM_CALLS - numCalls);

//        population.addAllToPosCellCountAllGen();

    }

    /**
     * Run this agent. Initialize then evolve.
     * @param stateObs - current StateObservation
     * @param agent - the agent calling the player
     * @return - action to be played in the game
     */
    int run(GeneralInformation gi, long budgetTime, RHEAAgent agent) {
        params = agent.getParameters();
        long start = System.nanoTime();
        init(gi, agent);
        budgetTime = budgetTime - (System.nanoTime() - start);
//        System.out.println("budgetTime:" + budgetTime);
        int nextAction = evolve(gi, agent, budgetTime);
 
        lastAct = nextAction;
        return nextAction;
    }

    /**
     * Run evolution
     * @param stateObs - StateObservation of current game tick
     * @param agent - Agent calling this player
     * @param budget - Budget for evolution in FM calls
     * @return - action to play in the game
     */
    int evolve(GeneralInformation gi, RHEAAgent agent, long budget) {
        params = agent.getParameters();
        long start = System.nanoTime();
        // Keep track of algorithm inner workings during this game tick
        population.numGenerations = 0;
     
        if (!(gi.IsGameOver()) && (System.nanoTime() - start) <= budget && params.EVOLVE) {
            do {
                // If we should reevaluate individuals promoted through elitism, average fitness
           
                if (params.REEVALUATE) {
                    for (int i = 0; i < params.ELITISM; i++) {
                        population.evaluate(i, gi, false);
                    }
                }

                // Move to next generation
                population.nextGeneration(gi);
                
                budget -= (System.nanoTime() - start);
                start = System.nanoTime();

            } while ((System.nanoTime() - start) <= budget); // While we can still evaluate one more generation
        }

//        System.out.println(population.numGenerations + " " + numCalls + "/" + budget);

        if (params.DEBUG)
            debugPrints();

        int nextAction = population.getNextAction();

   

        return nextAction;
    }

    // Access to action mapping
    Action getStartActionMapping(int action) {return start_action_mapping.get(action);}
    Action getContinueActionMapping(int action){return continue_action_mapping.get(action);}
    HashMap<Integer, Action> getStartActionMapping() {return start_action_mapping;}
    HashMap<Integer, Action> getContinueActionMapping(){return continue_action_mapping;}
    int getReversedStartActionMapping(Action action) { return start_action_mapping_r.get(action);}
    int getReversedContinueActionMapping(Action action) {return continue_action_mapping_r.get(action);}
    HashMap<Action, Integer> getReversedStartActionMapping() {return start_action_mapping_r;}
    HashMap<Action, Integer> getReversedContinueActionMapping() {return continue_action_mapping_r;}

    /**
     * Helper method to advance state, used by both the simple and macro agent
     * @param state - starting StateObservation
     * @param act - action to advance the state with
     * @param agent - agent calling this method
     */
     boolean advanceState(GeneralInformation gi, Action myAct, RHEAAgent agent) {
        int i = 0;
        boolean ok = gi.IsGameOver();
        boolean end = gi.IsGameOver();
        LinkedList<Action> oppActions = gi.oppStartHitActions;
        while(!end)
        {
         
//            if (myAct != null && !gi.startAvailActions.contains(myAct) && !gi.myStartHitActions.contains(myAct)) myAct = null;
            int oppAct = random.nextInt(oppActions.size());
            gi.advanceOneStep(myAct, oppActions.get(oppAct));
            end = (++i >= agent.getParameters().MACRO_ACTION_LENGTH) ||gi.IsGameOver();
        }
        return !ok;
    }
     
     void advanceState(GeneralInformation gi, Deque<Action> myActs, Action oppLastAction){
    	 Deque<Action> oppAct = new LinkedList<Action>();
    	 Deque<Action> myAct = new LinkedList<Action>();

    	 
    	 for(int i = 0 ; i <params.SIMULATION_DEPTH;i++) {
    		 Action act = myActs.pollFirst();
    		 myAct.add(act);
    		 oppAct.add(gi.getNextAction());
    		 gi.advanceWholeStep(myAct, oppAct);	
    		 myAct.pop();
    		 oppAct.pop();
    	 }
    	 
     }
     
    
     boolean advanceState(GeneralInformation gi, Action myAct, Action lastAct, RHEAAgent agent) {
         int i = 0;
         boolean ok = gi.IsGameOver();
         boolean end = gi.IsGameOver();
         while(!end)
         {
            
             if (myAct != null && !gi.getMySelectedActions().contains(myAct)) myAct = Action.NEUTRAL;
             gi.advanceOneStep(myAct, lastAct); // null is a one selection
             end = (++i >= agent.getParameters().MACRO_ACTION_LENGTH) ||gi.IsGameOver();
         }
         return !ok;
     }

    public Population getPopulation() {return population;}

    /**
     * Print debugging messages
     */
    private void debugPrints() {
        System.out.println("Debug true. Printing debug messages after evolution: ");
        System.out.println("Number of generations: " + population.numGenerations);
        System.out.println("Pop: ");
        for (Individual i : population.getPopulation()) {
            System.out.println(i);
        }
        System.out.println();
    }
}
