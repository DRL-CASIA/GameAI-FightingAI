package RHEA;

import java.util.*;

import enumerate.Action;
import enumerate.State;

import static RHEA.utils.Constants.*;
import static util.BaseUtil.*;

import RHEA.utils.GeneralInformation;

public class Gene {
  
    
    private int[] macroAction;
    private Random gen;

    private static ArrayList<Integer>[][][] validMutation;
    static int start_nActions;
    static int continue_nActions;
    static HashMap<Integer, Action> start_actionMapping;
    static HashMap<Integer, Action> continue_actionMapping;

    Gene(Random randomGenerator, int macroLength) {
    
        gen = randomGenerator;
        macroAction = new int[macroLength];

        // Random action initialization
        randomActions(macroLength);
    }
    
    Gene(Random randomGenerator, int macroLength, int step_n) {
        
        gen = randomGenerator;
        macroAction = new int[macroLength];
      

        // Random action initialization
        randomActions(macroLength, step_n);
    }

    /**
     * Initialize to valid random actions
     * @param macroLength - length of one macro-action
     */
    void randomActions(int macroLength, int step_n) {
    	 
        for (int j = 0; j < macroLength; j++) {
            macroAction[j] = -1;
            setNewMacroValidRandomValue(j, step_n);
        }
    }
    
    
    /**
     * Initialize to valid random actions
     * @param macroLength - length of one macro-action
     */
    void randomActions(int macroLength) {
    	 
        for (int j = 0; j < macroLength; j++) {
            macroAction[j] = -1;
            setNewMacroValidRandomValue(j);
        }
    }
    
    static void initValidActions(HashMap<Integer, Action> startMap, HashMap<Integer, Action> continueMap){
    	start_nActions = startMap.size();
    	continue_nActions = continueMap.size();
    	start_actionMapping = startMap;
    	continue_actionMapping = continueMap;
    }
     

    private static boolean keepSame(int a, int b, HashMap<Integer, Action> actionMap, GeneralInformation gi){
    	Action action_a = actionMap.get(a);
    	Action action_b = actionMap.get(b);
 
    	if (gi.startAvailActions.contains(action_a) && gi.startAvailActions.contains(action_b))
    		return true;
    	if (gi.myStartHitActions.contains(action_a) && gi.myStartHitActions.contains(action_b))
    		return true;
//    	if (gi.oppStartHitActions.contains(action_a) && gi.oppStartHitActions.contains(action_b))
//    		return true;
    	return false;
    }
    // Is this action pair valid? Returns false if LR or UD
    private static boolean valid(int action, int neighbour, HashMap<Integer, Action> actionMap) {
        Action a1 = actionMap.get(action);
        Action a2 = actionMap.get(neighbour);
        
        return !(a1 == Action.DOWN && a2 ==  Action.JUMP || a1 == Action.JUMP && a2 == Action.DOWN)
                && !(a1 == Action.BACK_STEP && a2 == Action.FORWARD_WALK || a1 == Action.FORWARD_WALK && a2 == Action.BACK_STEP);
    }
    
    private static boolean contains(Action action, Action[] actions){
    	for (Action a:actions){
    		if (action == a)
    			return true;
    	}
    	return false;
    }
    
    

    void setAction(int singleAction, int[] macroGene, int macroIdx) {
        if (macroGene != null)
            setMacroAction(macroGene);
        else if (macroIdx >= 0 && macroIdx < macroAction.length)
            macroAction[macroIdx] = singleAction;
        else {
            System.out.println("Gene.setAction() call. IndexOutOfBounds for setting individual action in macro-action gene.");
        }
    }

    
    void setGene(Gene gene) {
        macroAction = gene.macroAction.clone();
        gen = gene.gen;
    }

    /**
     * Returns action of this gene. If a macro action, returns first action of the macro action.
     * @return
     */
    public int getFirstAction() {
        return macroAction[0];
    }

    int[] getMacroAction() {
        return macroAction;
    }

    /**
     * Uniformly random mutation by default
     * @param idxGene - the index this gene has in the individual
     * @param MUT_DIVERSITY - whether we use diversity mutation operator or not
     * @param population - the population where this gene comes from
     */
    public void mutate(Population population, boolean MUT_DIVERSITY, int DIVERSITY_TYPE, int idxGene, int idxActionToMutate) {
        mutateMacroAction(population, MUT_DIVERSITY, DIVERSITY_TYPE, idxGene, idxActionToMutate);
    }

    private void setMacroAction(int[] a) {
        macroAction = a.clone();
    }

    /**
     * Uniformly random mutation of macro action
     * Avoid LR and UD action blocks
     * @param idxGene - the index this gene has in the individual
     * @param MUT_DIVERSITY - whether we use diversity mutation operator or not
     * @param pop - the population where this gene comes from
     */
    private void mutateMacroAction(Population pop, boolean MUT_DIVERSITY, int DIVERSITY_TYPE, int idxGene, int idxActionToMutate) {
        if (MUT_DIVERSITY && DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
            //get the valid values this action can take
            HashSet<Integer> validValues = getValidActionSpace(idxActionToMutate);
            int newValue = 0;

            //pick new value that appears the least in the population
            HashMap<Integer, Integer>[] popStats = pop.getActionCountAllGen();
            int minCount = (int) HUGE_POSITIVE;
            for (int i : validValues) {
                int countValue = popStats[idxGene].get(i);
                if (countValue < minCount) {
                    minCount = countValue;
                    newValue = i;
                }
            }

            //set action to new value
            macroAction[idxActionToMutate] = newValue;
        } else {
            // Choose one random action in the macro-action to mutate and change it to a new valid random action
//            System.out.println("macroAction length:" + macroAction.length);
        	idxActionToMutate = gen.nextInt(macroAction.length);
        	
            setNewMacroValidRandomValue(idxActionToMutate, idxGene);
        }
    }
    
    private HashSet<Integer> getValidActionSpace(int idx, int step_n){
    	HashSet<Integer> validValues = new HashSet<>();
    	int nextIdx = idx + 1;
    	int prevIdx = idx - 1;
    	if (nextIdx >= macroAction.length)
    		nextIdx = macroAction.length - 1;
    	if (prevIdx <0)
    		prevIdx = 0;
    	
    	int lastAction = macroAction[idx];
    	
    	if (lastAction == -1 && step_n==0)
    		validValues.addAll(start_actionMapping.keySet());
    	else if (lastAction == -1 && step_n>0)
    		validValues.addAll(continue_actionMapping.keySet());
    	else if (step_n ==0){
    		validValues.addAll(start_actionMapping.keySet());
    		validValues.remove(lastAction);
    	}
    	else if (step_n > 0){
    		validValues.addAll(continue_actionMapping.keySet());
    		validValues.remove(lastAction);
    	}
    	return validValues;
    }
    
    
    private HashSet<Integer> getValidActionSpace(int idx) {
    	
        // Get list of valid values
        HashSet<Integer> validValues = new HashSet<>();

        int nextIdx = idx + 1;
        int prevIdx = idx - 1;
        if (nextIdx >= macroAction.length)
            nextIdx = macroAction.length - 1;
        if (prevIdx < 0)
            prevIdx = 0;
        
        if (macroAction[idx] == -1)
            validValues.addAll(start_actionMapping.keySet()); // All actions okay
        else {
            // Handle edge cases separately
            if (idx == 0){
                for (int i = 0; i < validMutation.length; i++) {
                    validValues.addAll(validMutation[macroAction[idx]][i][macroAction[nextIdx]]);
                }
               
            }
            else if (idx == macroAction.length - 1)
                for (int i = 0; i < validMutation.length; i++) {
                    validValues.addAll(validMutation[macroAction[idx]][macroAction[prevIdx]][i]);
                }
            else
                validValues.addAll(validMutation[macroAction[idx]][macroAction[prevIdx]][macroAction[nextIdx]]);
        }
        
    
        

        return validValues;
    }
    
    
    private void setNewMacroValidRandomValue(int idx, int step_n){
    	HashSet<Integer> validValues = getValidActionSpace(idx, step_n);
    	int newValueIdx = gen.nextInt(validValues.size());
    	int i=0;
    	
    	 for(Integer k : validValues){
             if (i == newValueIdx) {
                 // Assign new value
                 macroAction[idx] = k;
                 break;
             }
             i++;
         }
    	
    }
    
    
    private void setNewMacroValidRandomValue(int idx) {
        HashSet<Integer> validValues = getValidActionSpace(idx);
        // Get random new value from the list of valid values
        int newValueIdx = gen.nextInt(validValues.size());  
//        System.out.println("newValueIdx: "+newValueIdx);
        int i = 0;
        
        for(Integer k : validValues)
        {
            if (i == newValueIdx) {
                // Assign new value
                macroAction[idx] = k;
                break;
            }
            i++;
        }


    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Gene))
            return false;

        for (int i = 0; i < macroAction.length; i++) {
            if (macroAction[i] != ((Gene) o).macroAction[i]) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(macroAction);
    }

    public Gene copy() {
        Gene g = new Gene(this.gen, this.macroAction.length);
        g.macroAction = macroAction.clone();
        g.gen = gen;
        return g;
    }
}
