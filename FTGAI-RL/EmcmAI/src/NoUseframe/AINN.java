package NoUseframe;

import java.util.ArrayList;

import java.util.Vector;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;
import enumerate.Action;


public class AINN {
	public ArrayList<NDArray> nnWeights;
	public ArrayList<ArrayList<NDArray>> mixNnWeights;
	public static final String MID = "399"; //中间
	public AINN(String mode) {
		this.nnWeights = getWeight(mode);
	}

	public static ArrayList<NDArray> getWeight(String name) {
		NDArray weight;
		NDArray bias;
		ArrayList<NDArray> nn = new ArrayList<NDArray>();
		weight = np.loadtxt("./data/aiData/EmcmAi/W0_" + name + ".csv", ",");
		bias = np.loadtxt("./data/aiData/EmcmAi/b0_" + name + ".csv", ",");
		nn.add(weight);
		nn.add(bias);
		weight = np.loadtxt("./data/aiData/EmcmAi/W1_" + name + ".csv", ",");
		bias = np.loadtxt("./data/aiData/EmcmAi/b1_" + name + ".csv", ",");
		nn.add(weight);
		nn.add(bias);
		weight = np.loadtxt("./data/aiData/EmcmAi/W2_" + name + ".csv", ",");
		bias = np.loadtxt("./data/aiData/EmcmAi/b2_" + name + ".csv", ",");
		nn.add(weight);
		nn.add(bias);
		return nn;
	}

	public static final Action[] ACTIONS = new Action[] { Action.AIR_A, Action.AIR_B, Action.AIR_D_DB_BA,
			Action.AIR_D_DB_BB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_DA, Action.AIR_DB,
			Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB,
			Action.BACK_JUMP, Action.BACK_STEP, Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA, Action.CROUCH_FB,
			Action.CROUCH_GUARD, Action.DASH, Action.FOR_JUMP, Action.FORWARD_WALK, Action.JUMP, Action.NEUTRAL,
			Action.STAND_A, Action.STAND_B, Action.STAND_D_DB_BA, Action.STAND_D_DB_BB, Action.STAND_D_DF_FA,
			Action.STAND_D_DF_FB, Action.STAND_D_DF_FC, Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_FA,
			Action.STAND_FB, Action.STAND_GUARD, Action.THROW_A, Action.THROW_B };

	public int get_policy(NDArray obs, Vector<Boolean> ownMasks) {
		int n_layers = this.nnWeights.size() / 2;
		NDArray h = obs;
		NDArray y;
		NDArray weight;
		NDArray bias;
		for (int i = 0; i < n_layers - 1; i++) {
			weight = this.nnWeights.get(i * 2);
			bias = this.nnWeights.get(i * 2 + 1);

			y = np.dot(h, weight).add(bias);
			h = np.maximum(y, 0.0);
		}
		weight = this.nnWeights.get((n_layers - 1) * 2);
		bias = this.nnWeights.get((n_layers - 1) * 2 + 1);
		y = np.dot(h, weight).add(bias);

		//向量转成1维
		y = y.get(0);
		
		//将不可能向量降到0
		for (int i = 0; i < ownMasks.size(); i++) {
			if (ownMasks.get(i) == false) {
				y.set(Float.MIN_VALUE, i);
			}
		}

		// softmax
		y = np.exp(y).divide(np.sum(np.exp(y), 0));

		// random select
		java.util.Random random = new java.util.Random(System.currentTimeMillis());
		double prob = random.nextDouble();
		double start_prob = 0;
		int sel_act = 0;
		for (int i = 0; i < ownMasks.size(); i++) {
			start_prob = start_prob + y.asDouble(i);
			if (start_prob > prob) {
				sel_act = i;
				break;
			}
		}

		return sel_act;
	}

}
