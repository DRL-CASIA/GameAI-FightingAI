package RHEA.utils;

/**
 * Created by rdgain on 3/20/2017.
 */
public class Constants {
    private static final long BREAK_MS = 5;
    public static final int MAX_ACTIONS = 56;  // ALL Action is 56
    public static final double epsilon = 1e-6;

    public static final int POINT1_CROSS = 0;
    public static final int UNIFORM_CROSS = 1;
    public static final int INIT_RANDOM = 0;
    public static final int INIT_ONESTEP = 1;
    public static final int INIT_MCTS = 2;

    public static final int HEURISTIC_WINSCORE = 0;
    public static final int HEURISTIC_SIMPLESTATE = 1;

    public static final int FULL_BUDGET = 0;
    public static final int HALF_BUDGET = 1;

    public static final int DRAW_EXPLORATION = 0;
    public static final int DRAW_THINKING = 1;
    public static final int DRAW_ET = 2; //both

    public static final double HUGE_NEGATIVE = -1000000.0;
    public static final double HUGE_POSITIVE =  1000000.0;

    public static final int DIVERSITY_GENOTYPE = 0;
    public static final int DIVERSITY_PHENOTYPE = 1;
}
