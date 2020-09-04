package RHEA.utils;

import static RHEA.utils.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class ParameterSet {

    public boolean DEBUG = false;
    public boolean EVOLVE = true; // set to false if no evolution required (ie Random Search)
    

    // variable
    public int POPULATION_SIZE = 4; //try 5
    public int SIMULATION_DEPTH = 3; //try 6
    public int INIT_TYPE = INIT_RANDOM;  // INIT_RANDOM, INIT_ONESTEP, INIT_MCTS
    public int BUDGET_TYPE = HALF_BUDGET;
    public int MAX_FM_CALLS = 900;  // 900 origin
    public int HEURISTIC_TYPE = HEURISTIC_WINSCORE;
    public int MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH MACRO-ACTION
    public int INNER_MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH INNER MACRO-ACTION

    public boolean BANDIT_MUTATION = false; //if false - random; if true - bandit
    public boolean MUT_BIAS = false;
    public boolean MUT_DIVERSITY = false;
    public int CROSSOVER_TYPE = UNIFORM_CROSS; // 0 - 1point; 1 - uniform

    public boolean TREE = false;
    public boolean SHIFT_BUFFER = false;

    public boolean ROLLOUTS = false;
    public int ROLLOUT_LENGTH = SIMULATION_DEPTH / 2;
    public int REPEAT_ROLLOUT = 5;
    
    public boolean ADD_DIVERSITY_FIT = false;
    public boolean POP_DIVERSITY = false;
    public int DIVERSITY_TYPE = DIVERSITY_GENOTYPE;  // DIVERSITY_GENOTYPE, DIVERSITY_PHENOTYPE
    public double D = 0.5; // weight given to diversity in fitness, values in range [0,1]

    // set
    public boolean REEVALUATE = false;
    public int MUTATION = 1;
    public int TOURNAMENT_SIZE = 2;
    public int NO_PARENTS = 2;
    public int RESAMPLE = 1; //try 1,2,3
    public int ELITISM = 2;
    public double DISCOUNT = 1; //0.99;
    public double SHIFT_DISCOUNT = 0.99;


    public boolean canCrossover() {
        return POPULATION_SIZE > 1;
    }

    public boolean canTournament() {
        return POPULATION_SIZE > TOURNAMENT_SIZE;
    }

    public boolean isRMHC() { return POPULATION_SIZE == 1; }

    public boolean isInnerMacro() { return INNER_MACRO_ACTION_LENGTH > 1; }

    public String[] getParams() {
        String[] s = new String[]{"Population size", "Simulation depth", "Init type", "Budget type", "Budget (FM calls)",
        "Heuristic type", "Macro-action length", "Inner macro-action length", "Bandit mutation", "Softmax bias in mutation",
        "Diversity in mutation", "Crossover type", "Statistical tree", "Stat tree recommend", "Shift buffer", "Rollouts",
        "Rollout length", "Repeat rollout N", "Population diversity", "Diversity type", "Diversity weight", "Reevaluate individuals",
        "N genes mutated", "Tournament size", "N parents", "Resample rate", "Elitism", "Discount reward", "Discount shift buffer"};
        return s;
    }

    public String[] getDefaultValues() {
        String[] s = new String[]{""+POPULATION_SIZE, ""+SIMULATION_DEPTH, ""+INIT_TYPE, ""+BUDGET_TYPE, ""+MAX_FM_CALLS,
        ""+HEURISTIC_TYPE, ""+MACRO_ACTION_LENGTH, ""+INNER_MACRO_ACTION_LENGTH, ""+BANDIT_MUTATION, ""+MUT_BIAS, ""+MUT_DIVERSITY,
        ""+CROSSOVER_TYPE, ""+TREE, ""+SHIFT_BUFFER, ""+ROLLOUTS, ""+ROLLOUT_LENGTH, ""+REPEAT_ROLLOUT,
        ""+POP_DIVERSITY, ""+DIVERSITY_TYPE, ""+D, ""+REEVALUATE, ""+MUTATION, ""+TOURNAMENT_SIZE, ""+NO_PARENTS, ""+RESAMPLE, ""+ELITISM,
        ""+DISCOUNT, ""+SHIFT_DISCOUNT};
        return s;
    }

    public Object[][] getValueOptions() {
        return new Object[][] {new Integer[0],new Integer[0],new String[]{"Random", "OneStep", "MCTS"},
                new String[]{"Full budget evo", "Budget include init"},new Integer[0],new String[]{"WinScore", "SimpleState"},
                new Integer[0],new Integer[0],new Boolean[]{false, true},new Boolean[]{false, true},new Boolean[]{false, true},
                new String[]{"1-Point", "Uniform"},new Boolean[]{false, true},new Boolean[]{false, true},
                new Boolean[]{false, true},new Boolean[]{false, true},new Integer[0],new Integer[0],new Boolean[]{false, true},
                new String[]{"Genotypic", "Phenotypic"},new Double[0],new Boolean[]{false, true},
                new Integer[0],new Integer[0],new Integer[0],new Integer[0],new Integer[0],new Double[0],new Double[0]};
    }

    @Override
    public String toString() {
        String s = "";

        String init = "none";
        if (INIT_TYPE == INIT_RANDOM) init = "random";
        else if (INIT_TYPE == INIT_ONESTEP) init = "OneStep";
        else if (INIT_TYPE == INIT_MCTS) init = "MCTS";

        String bud = "none";
        if (BUDGET_TYPE == FULL_BUDGET) bud = "full budget";
        else if (BUDGET_TYPE == HALF_BUDGET) bud = "half budget";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        String cross = "none";
        if (CROSSOVER_TYPE == UNIFORM_CROSS) cross = "uniform";
        else if (CROSSOVER_TYPE == POINT1_CROSS) cross = "1-Point";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Population size") + ": " + POPULATION_SIZE + "\n";
        s += String.format("%1$-20s", "Individual length") + ": " + SIMULATION_DEPTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Initialization type") + ": " + init + "\n";
        s += String.format("%1$-20s", "Budget type") + ": " + bud + "\n";
        s += String.format("%1$-20s", "Budget") + ": " + MAX_FM_CALLS + "\n";
        s += String.format("%1$-20s", "Resampling") + ": " + RESAMPLE + "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT + "\n";
        s += String.format("%1$-20s", "Elitism") + ": " + ELITISM + "\n";
        s += String.format("%1$-20s", "Reevaluate?") + ": " + REEVALUATE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Macro Action Length") + ": " + MACRO_ACTION_LENGTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Bandit mutation?") + ": " + BANDIT_MUTATION + "\n";
        s += String.format("%1$-20s", "Genes mutated") + ": " + MUTATION + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Tournament size") + ": " + TOURNAMENT_SIZE + "\n";
        s += String.format("%1$-20s", "Crossover type") + ": " + cross + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Stats tree?") + ": " + TREE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Shift buffer?") + ": " + SHIFT_BUFFER + "\n";
        s += String.format("%1$-20s", "Shift discount?") + ": " + SHIFT_DISCOUNT + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Rollouts?") + ": " + ROLLOUTS + "\n";
        s += String.format("%1$-20s", "Rollout length") + ": " + ROLLOUT_LENGTH + "\n";
        s += String.format("%1$-20s", "Repeat rollouts") + ": " + REPEAT_ROLLOUT + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }
}
