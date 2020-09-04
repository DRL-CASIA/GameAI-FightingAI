package RHEA;

import java.util.*;
import static RHEA.utils.Constants.*;

// RHEA utils
import RHEA.utils.ParameterSet;
import RHEA.utils.GeneralInformation;
import RHEA.Heuristics.*;
import RHEA.bandits.BanditArray;
import RHEA.utils.Operations;

// Structure Data
import struct.FrameData;

// Enumerate action
import enumerate.Action;

// MCTS Node
import RHEA.sampleOLMCTS.SingleTreeNode;

public class Population {

	private Individual[] population;
	private StateHeuristic heuristic;
	public int numGenerations;

	private RollingHorizonPlayer player;
	private RHEAAgent agent;
	private ParameterSet params;

	// Bandits
	private BanditArray bandits; // bandits for each gene

	private TreeNode statsTree;

	private HashMap<Integer, Integer>[] actionCountAllGen; // action:count array
															// for all
															// generations
	private HashMap<Integer, Integer> posCellCountAllGen; // pos/cell:count
															// array for all
															// generations
	private static int noCells, noCellsW, noCellsH, gridCellSize;
	static double noGridCellW, noGridCellH; // number grid cells per pos cell
											// (smallest unit)

	/**
	 * Constructor to initialize population
	 * 
	 * @param stateObs
	 *            - StateObservation of current game tick
	 */
	Population(RHEAAgent agent, RollingHorizonPlayer player, int budget) {
		this.agent = agent;
		params = agent.getParameters();
		heuristic = new WinScoreHeuristic();
		this.player = player;
		numGenerations = 0;

		// New tree
		if (params.TREE) {
			statsTree = new TreeNode(null, MAX_ACTIONS, 0, -1, player.random);
		}

		// New action count for all generations
		if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
			actionCountAllGen = new HashMap[params.SIMULATION_DEPTH * params.INNER_MACRO_ACTION_LENGTH];
			for (int i = 0; i < actionCountAllGen.length; i++) {
				actionCountAllGen[i] = new HashMap<>();
				Set<Integer> actionSpace;
				if (i == 0)
					actionSpace = player.getStartActionMapping().keySet();
				else
					actionSpace = player.getContinueActionMapping().keySet();
				for (int a = 0; a < actionSpace.size(); a++) {
					actionCountAllGen[i].put(a, 0);
				}
			}
		}

		// // New position cell count for all generations
		// if (params.POP_DIVERSITY && params.DIVERSITY_TYPE ==
		// DIVERSITY_PHENOTYPE) {
		// ArrayList<FrameData>[][] obsGrid = stateObs.getObservationGrid();
		// if (params.SIMULATION_DEPTH > 0) {
		// noCellsH = (int) Math.ceil(1.0 * obsGrid.length /
		// (params.SIMULATION_DEPTH / 2));
		// noCellsW = (int) Math.ceil(1.0 * obsGrid[0].length /
		// (params.SIMULATION_DEPTH / 2));
		// noGridCellH = 1.0 * obsGrid.length / noCellsH;
		// noGridCellW = 1.0 * obsGrid[0].length / noCellsW;
		// gridCellSize = stateObs.getBlockSize();
		// noCells = noCellsH * noCellsW;
		//
		// posCellCountAllGen = new HashMap<>();
		// for (int i = 0; i < noCells; i++) {
		// posCellCountAllGen.put(i, 0);
		// }
		// }
		// }

		// New population
		population = new Individual[params.POPULATION_SIZE];
		// System.out.println("Population");
		for (int i = 0; i < params.POPULATION_SIZE; i++) {
			population[i] = new Individual(player.start_nActions, player.random, heuristic, player, agent);
			if (params.INIT_TYPE == INIT_RANDOM) {
				addToActionCountAllGen(population[i]);
			}
		}

		// New bandits
		if (params.BANDIT_MUTATION) {
			bandits = new BanditArray(population, player.start_nActions, params.SIMULATION_DEPTH);
		}

	}

	private void addToActionCountAllGen(Individual ind) {
		int[] actionSequence = ind.getActions();
		if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
			for (int i = 0; i < actionSequence.length; i++) {
				int act = actionSequence[i];
				actionCountAllGen[i].put(act, actionCountAllGen[i].get(act) + 1);
			}
		}

	}

	void addAllToPosCellCountAllGen() {
		if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
			for (Individual i : population) {
				addToPosCellCountAllGen(i);
			}
		}
	}

	private void addToPosCellCountAllGen(Individual ind) {
		if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
			int[] actionSequence = ind.getActions();
			for (int i = 0; i < actionSequence.length; i++) {
				int idxGene = i / player.params.INNER_MACRO_ACTION_LENGTH;
				int idxAction = i % player.params.INNER_MACRO_ACTION_LENGTH;

			}
		}
	}

	public HashMap<Integer, Integer>[] getActionCountAllGen() {
		return actionCountAllGen;
	}

	public HashMap<Integer, Integer> getPosCellCountAllGen() {
		return posCellCountAllGen;
	}

	public TreeNode getStatsTree() {
		return statsTree;
	}

	/**
	 * One Step Look Ahead initialization. For the first individual, roll the
	 * state with best action at each step Rest of the individuals are mutations
	 * of the first
	 * 
	 * @param stateObs
	 *            - current StateObservation
	 * @param heuristic
	 *            - Heuristic used by the 1SLA
	 * @return - number of FM calls used in this method
	 */
	int initOneStep(GeneralInformation gi, StateHeuristic heuristic) {
		int nCalls = 0;
		for (int i = 0; i < params.POPULATION_SIZE; i++) {
			if (i > 0) {
				population[i] = population[0].copy();
				mutation(population[i]);
			} else {
				Individual ind = population[i];
				FrameData o_fd = gi.getFrameData();
				Action bestMyAction;
				Action bestOppAction;
				double maxQ;
				double minQ;
				for (int k = 0; k < params.SIMULATION_DEPTH; k++) {
					for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) {
						// Not over
						if (!gi.IsGameOver()) {
							bestMyAction = null;
							maxQ = Double.NEGATIVE_INFINITY;

							for (Action myAction : gi.startAvailActions) {
								gi.advanceOneStep(myAction, null);
								double Q = heuristic.evaluateState(gi);
								Q = Operations.noise(Q, epsilon, player.random.nextDouble());
								if (Q >= maxQ) {
									maxQ = Q;
									bestMyAction = myAction;
								}
								gi.setFrameData(o_fd);
							}

							// Do Best Action
							ind.setGene(k, player.getReversedStartActionMapping(bestMyAction), m);
							gi.advanceOneStep(bestMyAction, null);
						}
					}
				}
				gi.setFrameData(o_fd);

				nCalls += params.SIMULATION_DEPTH;
			}
			addToActionCountAllGen(population[i]);
		}
		return nCalls;
	}

	/**
	 * Monte Carlo Tree Search initialization. For the first individual, use
	 * MCTS with half budget to find solution Rest of the individuals are
	 * mutations of the first
	 * 
	 * @param stateObs
	 *            - current StateObservation
	 * @return - number of FM calls used in this method
	 */
	int initMCTS(GeneralInformation gi) {
		int nCalls = 0;
		int MCTS_BUDGET = (int) (params.MAX_FM_CALLS * 0.5);

		for (int i = 0; i < params.POPULATION_SIZE; i++) {
			if (i > 0) {
				population[i] = population[0].copy();
				mutation(population[i]);
			} else {
				Action[] actions = new Action[player.start_nActions];
				for (int j = 0; j < player.start_nActions; j++)
					actions[j] = player.getStartActionMapping(j);
				SingleTreeNode m_root = new SingleTreeNode(player.random, player.start_nActions, actions);
				m_root.rootGI = gi;// Do the search within the
									// available time.
				m_root.mctsSearchCalls(MCTS_BUDGET, 10);

				// Seed only first gene
				// population[i].actions[0] = m_root.mostVisitedAction();

				// Seed N relevant genes
				ArrayList<Integer> ind = m_root.mostVisitedActions(m_root);
				int limit = ind.size() < params.SIMULATION_DEPTH ? ind.size() : params.SIMULATION_DEPTH;
				for (int j = 0; j < limit / params.INNER_MACRO_ACTION_LENGTH; j++) {
					for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) {
						population[i].setGene(j, ind.get(j), m);
					}
				}

				nCalls += MCTS_BUDGET;
			}
			addToActionCountAllGen(population[i]);

		}
		return nCalls;
	}

	/**
	 * Shift buffer. Shift population to the left (and trees and bandits), add
	 * random action at the end of all individuals
	 * 
	 * @param lastAct
	 *            - the action that was played in previous game tick
	 */
	void shiftLeft(int lastAct) {
		numGenerations = 0; // reset gens

		// Remove first action of all individuals and add a new random one at
		// the end
		for (int i = 0; i < params.POPULATION_SIZE; i++) {
			for (int j = 0; j < params.SIMULATION_DEPTH - 1; j++) {
				if (params.isInnerMacro()) {
					for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) { // shift
																					// macros
						Gene next = population[i].getGene(j + 1);
						population[i].setGene(j, next);
					}
				} else {
					int next = population[i].getGene(j + 1).getFirstAction();
					if (j == 0)
						population[i].setGene(j,
								(next < player.start_nActions) ? next : player.random.nextInt(player.start_nActions),
								0);
					else
						population[i].setGene(j, (next < player.continue_nActions) ? next
								: player.random.nextInt(player.continue_nActions), 0);

				}
			}
			population[i].setGene(params.SIMULATION_DEPTH - 1); // set last
																// action as new
																// random one
			population[i].resetValue();

		}
		if (params.TREE) {
			// Cut the tree to the node that was chosen
			statsTree.shiftTree(lastAct, params.SHIFT_DISCOUNT);
		}
		if (params.BANDIT_MUTATION) {
			bandits.shiftArray(population, player.start_nActions, params.SHIFT_DISCOUNT);
		}

		// Shift overall pop stats
		if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
			for (int i = 0; i < actionCountAllGen.length; i++) {
				if (i == actionCountAllGen.length - 1) {
					actionCountAllGen[i] = new HashMap<>();
					Set<Integer> actionSpace;
					if (i == 0)
						actionSpace = player.getStartActionMapping().keySet();
					else
						actionSpace = player.getContinueActionMapping().keySet();

					for (int a = 0; a < actionSpace.size(); a++) {
						actionCountAllGen[i].put(a, 0);
					}
				} else {
					actionCountAllGen[i] = actionCountAllGen[i + 1];
				}
			}
		}
	}

	/**
	 * Returns the next action to play in the game
	 * 
	 * @return - by default, first action of best (first) individual
	 */
	int getNextAction() {
		if (params.TREE) {
			// return statsTree.getBestChild();
			return statsTree.getMostVisitedChild();
		}

		// remove diversity measure for recommendation policy
		if (params.ADD_DIVERSITY_FIT){
			Arrays.sort(population);
		}
		else{
			double aux = params.D;
			params.D = 0;
			Arrays.sort(population);
			params.D = aux;
		}
		

		return population[0].getGene(0).getFirstAction();
	}

	/**
	 * Returns the best fitness value in the population
	 * 
	 * @return - fitness value of best (first) individual in the population
	 */
	double getBestFitness() {
		return population[0].getValue();
	}

	Individual[] getPopulation() {
		return population;
	}

	static int n = 0;
	static double totalDiversity = 0;

	/**
	 * Move to the next generation through crossover and mutation
	 * 
	 * @param stateObs
	 *            - StateObservation of current game tick
	 * @return - number of FM calls during this call of the method
	 */
	int nextGeneration(GeneralInformation gi) {
		numGenerations++;
       
		int nCalls = 0;
		Individual[] nextGenome = new Individual[params.POPULATION_SIZE];
//		Individual[] nextGenome = population.clone();
		
		Arrays.sort(population); // sort population

		for (int i = 0; i < population.length; i++) {
			if (i < params.ELITISM && !params.isRMHC()) { // Individuals
															// promoted through
															// elitism are
															// copied directly
				//保存最好的几个
				nextGenome[i] = population[i].copy();
			} else {
				// System.out.println("pop size:"+population.length);
				//找一个容器
				Individual newInd = population[0].copy();

				// Crossover
				if (params.canCrossover()) {
					newInd = crossover();
				}

				// Mutation
				mutation(newInd);

				// Evaluate new individual
				nCalls += evaluate(newInd, gi, false);

				// Insert new individual into population
				//如果大于1个就赋值
				if (!params.isRMHC())
					nextGenome[i] = newInd;
				else {
					// Only 1 individual in the population, replace only if
					// better
					if (population[i].getValue() < newInd.getValue()) {
						nextGenome[i] = newInd.copy();
					}
				}

				// Add newInd actions to record of actions over all generations
				addToActionCountAllGen(newInd);
				// addToPosCellCountAllGen(newInd);
			}
			
		}

		// Assign new population
		population = nextGenome;

		// Evaluate diversity for all individuals in new population
		evaluateDiversity(gi);

		// Sort the new population according to individual fitness values
		try {
			Arrays.sort(population);
		} catch (NullPointerException e) {
			System.out.println(
					"Population.nextGeneration() call. Null values in population. This should not be happening.");
			e.printStackTrace();
		}

		// Debug print of evolutionary process during one game tick
		// double overallDiversity = 0;
		// for (Individual g : population) {
		// overallDiversity += g.getDiversityScore();
		// }
		// totalDiversity += overallDiversity;
		// n++;
		// System.out.println(String.format("%.2f", overallDiversity) + ": " +
		// population[0]);

		return nCalls;
	}

	/**
	 * Evaluate a certain individual in the population, passed through index
	 * 
	 * @param idx
	 *            - index of individual to be evaluated
	 * @param stateObs
	 *            - StateObservation of current game tick
	 * @return - FM calls used up in this method call
	 */
	int evaluate(int idx, GeneralInformation gi, boolean evaluateAll) {
		return evaluate(population[idx], gi, evaluateAll);
	}

	private int evaluate(Individual newind, GeneralInformation gi, boolean evaluateAll) {
		return newind.evaluate(gi, params, statsTree, bandits, player.actionDist);
	}

	private int evaluateDiversity(GeneralInformation gi) {
		int nCalls = 0;

		if (params.POP_DIVERSITY) {
			// compute diversity score for all individuals in the population
			for (int i = 0; i < population.length; i++) {
				Individual aGenome = population[i];
				double diversityScore = 0;
				// compare this individual to all the others in the population
				diversityScore += aGenome.diversityDiff(this);
				// update diversity score
//				int f_score =  evaluate(aGenome, gi, false);
//				System.out.println("f_score:" + f_score + ", diversity:" + diversityScore);
//				diversityScore = (1 - params.D) * f_score + params.D * diversityScore;
				diversityScore /= population.length - 1;
				//更新分数
				System.out.println("totalDiver:" +  diversityScore);
			
				aGenome.updateDiversityScore(diversityScore);
			}
		}

		return nCalls;
	}

	/**
	 * Evaluate all individuals in the population
	 * 
	 * @param stateObs
	 *            - StateObservation of current game tick
	 * @return - FM calls used up in this method call
	 */
	int evaluateAll(GeneralInformation gi, int budget) {
		int nCalls = 0;

		// Evaluate all individuals in the population
		for (int i = 0; i < params.POPULATION_SIZE; i++) {
			if (nCalls + params.SIMULATION_DEPTH <= budget) {
				nCalls += evaluate(i, gi, true);
			}
		}

		// Sort the new population according to individual fitness values
		if (params.POPULATION_SIZE > 1) {
			try {
				Arrays.sort(population);
			} catch (NullPointerException e) {
				System.out.println(
						"Population.evaluateAll() call. Null values in population. This should not be happening.");
				e.printStackTrace();
			}
		}

		return nCalls;
	}

	/**
	 * Mutates an individual using the correct mutation oeprator
	 * 
	 * @param newInd
	 *            - individual to be mutated (if null, the first in the
	 *            population)
	 * @return - budget left after the mutation and evaluation of new individual
	 */
	private void mutation(Individual newInd) {
		if (params.BANDIT_MUTATION) {
			newInd.banditMutate(bandits);
		} else {
			newInd.mutate(this);
		}
	}

	/**
	 * Performs crossover throug tournament between individuals in the
	 * population
	 * 
	 * @return - new individual resulting from crossover
	 */
	private Individual crossover() {

		Individual newInd = new Individual(player.start_nActions, player.random, heuristic, player, agent);
		Individual[] parents = new Individual[params.NO_PARENTS];
		Random rand = new Random();
		// Get parents for crossover. Tournament if possible.选父母
		//这里可以调？？？
		if (params.canTournament()) {
			int index = 0;
			Arrays.sort(population);
			if(params.ELITISM == 1) {
				parents[0] = population[0].copy();
			}
			else {
				index = rand.nextInt(params.ELITISM);
				parents[0] = population[index].copy();
			}
			index = rand.nextInt(params.POPULATION_SIZE - params.ELITISM);
			index = index + params.ELITISM;
			parents[1] = population[index].copy();
//			Individual[] tournament = new Individual[params.TOURNAMENT_SIZE];
//			ArrayList<Individual> list = new ArrayList<>();
//			list.addAll(Arrays.asList(population).subList(0, params.POPULATION_SIZE));
//			Collections.shuffle(list);
//			for (int i = 0; i < params.TOURNAMENT_SIZE; i++) {
//				tournament[i] = list.get(i);
//			}
//			try {
//				Arrays.sort(tournament);
//			} catch (NullPointerException e) {
//				System.out.println(
//						"Population.crossover() call. Null values in population. This should not be happening.");
//				e.printStackTrace();
//			}
//			parents[0] = tournament[0];
//			parents[1] = tournament[1];
		} else {
			parents[0] = population[0];
			parents[1] = population[1];
		}

		// Perform crossover, return resulting individual
		//随机进行交叉配对
		if (params.CROSSOVER_TYPE == POINT1_CROSS) {
			// 1-point
			int p = player.random.nextInt(params.SIMULATION_DEPTH - 3) + 1;
			for (int i = 0; i < params.SIMULATION_DEPTH; i++) {
				if (i < p)
					newInd.setGene(i, parents[0].getGene(i));
				else
					newInd.setGene(i, parents[1].getGene(i));
			}
		} else if (params.CROSSOVER_TYPE == UNIFORM_CROSS) {
			// uniform
			for (int i = 0; i < params.SIMULATION_DEPTH; i++) {
				newInd.setGene(i, parents[player.random.nextInt(params.NO_PARENTS)].getGene(i));
			}
		}

		return newInd;
	}

	@Override
	public String toString() {
		return "Pop: " + Arrays.toString(population) + "\n";
	}

}
