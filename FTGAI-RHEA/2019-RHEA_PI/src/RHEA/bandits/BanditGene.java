package RHEA.bandits;
import RHEA.utils.Picker;

import java.util.Random;

/**
 * Created by simonmarklucas on 27/05/2016.
 *
 *
 *  Idea is to keep track of which changes lead to an improvement
 *
 */
public class BanditGene {

    private static Random random = new Random();

    private static double eps = 0.01;
    private int nArms;

    // double[] rewards = new double[nArms];
    private double[] deltaRewards;
    private int[] armPulls;
    private int nPulls;

    public int x;

    // start all at one to avoid div zero
    private int nMutations = 1;
    private static double k = Math.sqrt(2);

    private int xPrevious;

    public int index;

    BanditGene(int idx, int[] acts, int arms) {
        nArms = arms;
        armPulls = new int[nArms];
        deltaRewards = new double[nArms];
        for (int i = 0; i < acts.length; i++) {
            x = acts[i];
            armPulls[x]++;
            nPulls++;
        }
        index = idx;
    }

    public void banditMutate() {
        // having chosen this bandit, only the UCB terms
        // within this bandit are relevant (not the total across all bandits)
        Picker<Integer> picker = new Picker<>(Picker.MAX_FIRST);

        for (int i = 0; i < nArms; i++) {
            // never choose the current value of x
            // that would not be a mutation!!!
            if (i != x) {
                double exploit = exploit(i);
                double explore = explore(nPulls, armPulls[i]);
                // small random numbers: break ties in unexpanded nodes
                double noise = random.nextDouble() * eps;
//                 System.out.format("%d\t %.2f\t %.2f\n", i, exploit, explore);
                picker.add(exploit + explore + noise, i);
            }
        }
        xPrevious = x;
        x = picker.getBest();
        armPulls[x]++;
        nPulls++;
        nMutations++;
    }

    private double maxDelta() {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : deltaRewards)  max = max<=d? d: max;
        return max;
    }

    double urgency(int n) {
        return rescue() + explore(n, nMutations);
    }


    // in bandit terms this would normally be called the exploit term
    // but in an EA we need to use it in the opposite sense
    // since we need to stick with values that are already thought to be
    // good and instead modify ones that need to be rescued
    private double rescue() {
        return -maxDelta() / nMutations;
    }

    // standard UCB Explore term
    // consider modifying a value that's not been changed much yet
    private double explore(int n, int nA) {
        return k * Math.sqrt(Math.log(n) / (nA));
    }
    private double exploit(int i) {
        return deltaRewards[i];
    }


    public void applyReward(double delta) {
        if (x != xPrevious)
            deltaRewards[x] += delta;
    }

    void applyDiscountToAll(double discount) {
        for (int i = 0; i < nArms; i++) {
            deltaRewards[i] *= discount;
        }
    }

    // returns true if reverting to old value
    public boolean revertOrKeep(double delta) {
        if (delta < 0) {
            x = xPrevious;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Gene:" + nArms + ":" + nPulls + ":" + nMutations + ":" + x + ":" + armPulls[x] + ":" + deltaRewards[x];
    }

    String statusString() {
        return String.format("%d\t rescue: %.2f\t explore: %.2f\t urgency: %.2f",
                x, rescue(), explore(nPulls, nMutations), urgency(nPulls));
    }
}
