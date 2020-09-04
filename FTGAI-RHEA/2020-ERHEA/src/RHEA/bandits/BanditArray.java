package RHEA.bandits;
import RHEA.Individual;
import RHEA.utils.Picker;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by simonmarklucas on 27/05/2016.
 */


public class BanditArray {

    static Random random = new Random();
    static double eps = 1e-6;

    int nBandits;
    int nMutations;
    public ArrayList<BanditGene> genome;

    public BanditArray(Individual[] population, int N_ACTIONS, int nBandits) {
        this.nBandits = nBandits;
        genome = new ArrayList<>();
        nMutations = 0;

        for (int i = 0; i < nBandits; i++) {
            int[] acts = new int[population.length];
            for (int j=0; j < population.length; j++) {
                acts[j] = population[j].getGene(i).getFirstAction();
            }
            genome.add(new BanditGene(i, acts, N_ACTIONS));
        }
    }

    public int[] toArray() {
        int[] a = new int[nBandits];
        int ix = 0;
        for (BanditGene gene : genome) {
            a[ix++] = gene.x;
        }
        return a;
    }

    @Override
    public String toString() {
        String s = "";
        for (BanditGene bg : genome) {
            s += bg.toString() + "\n" + bg.statusString() + "\n";
        }
        s += "----" + nMutations + "----";
        return s;
    }

    public void shiftArray(Individual[] population, int N_ACTIONS, double discount) {
        //remove first gene
        genome.remove(0);

        //apply discount to previous rewards
        if (discount < 1) {
            for (BanditGene bg : genome) {
                bg.applyDiscountToAll(discount);
            }
        }

        //add the new gene
        int i = nBandits - 1;
        int[] acts = new int[population.length];
        for (int j=0; j < population.length; j++) {
            acts[j] = population[j].getGene(i).getFirstAction();
        }

        genome.add(new BanditGene(i, acts, N_ACTIONS));
    }

    public BanditGene selectGeneToMutate() {

        nMutations++;

        Picker<BanditGene> picker = new Picker<>();

        int i = 0;
        for (BanditGene gene : genome) {
            // break ties with small random values
            // System.out.println(i++ + "\t " + gene.statusString(nEvals));
            picker.add(gene.urgency(nMutations) + eps * random.nextDouble(), gene);
        }

        // System.out.println(picker.getBestScore());
        return picker.getBest();

    }
}
