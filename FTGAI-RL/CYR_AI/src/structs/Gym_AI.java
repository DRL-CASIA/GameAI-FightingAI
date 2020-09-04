package structs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import enumerate.Action;
import struct.AttackData;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import java.util.ArrayList;

/**
 * Data for the RL_brain
 *
 */
public class Gym_AI {
	public int action_n;
	public double[] Input;
	public int my_state;
	public int my_remaining;
	public int opp_state;
	public int opp_remaining;
	public Deque<AttackData> attacks;
	public int input_frame;
	
	public double current_score;

	public double score;
	
	public double reward;
	
	

	public Gym_AI(GameData gameData, boolean player, ArrayList<CharacterData> my, ArrayList<CharacterData> opp,
			ArrayList<FrameData> frame, ArrayList<Action> delay_actions) {
		Input = get_obs(gameData, player, my, opp, frame, delay_actions);
	}

	public Gym_AI(double[] train_X, int action_n) {
		this.Input = train_X;
		this.action_n = action_n;
	}

	double[] get_obs(GameData gameData, boolean player, ArrayList<CharacterData> my_list,
			ArrayList<CharacterData> opp_list, ArrayList<FrameData> frame_list, ArrayList<Action> delay_actions) {

		ArrayList<Double> input_list = new ArrayList<Double>();
		int record_frame = 4;
		int delay_frame = 15;

		for (int n = 0; n < record_frame; n++) {
			if(n<my_list.size()){
				CharacterData my = my_list.get(n);
				this.my_state = my.getAction().ordinal();
				this.my_remaining = gameData.getMotionData(player).get(my.getAction().ordinal()).getFrameNumber();
				double myCenterX = (my.getLeft()+my.getRight())/2;
				double myCenterY = (my.getTop()+my.getBottom())/2;
				input_list.add((double) (myCenterX)/1000);
				input_list.add((double) (myCenterY)/600);
				input_list.add((double) (my.getEnergy())/100);
				input_list.add((double) (Math.abs(my.getHp()))/1000);
				for (int i = 0; i < 56; i++) {
					if (i == my_state) {
						input_list.add(1.0);
					} else {
						input_list.add(0.0);
					}
				}
				input_list.add((double) (my_remaining)/10);
			}else{
				input_list.add(0.0);
				input_list.add(0.0);
				input_list.add(0.0);
				input_list.add(0.0);
				for (int i = 0; i < 56; i++) {
					input_list.add(0.0);
				}
				input_list.add(0.0);
			}
		}

		for (int n = 0; n < record_frame; n++) {
			if(n<opp_list.size()){
				CharacterData opp = opp_list.get(n);
				this.opp_state = opp.getAction().ordinal();
				this.opp_remaining = gameData.getMotionData(!player).get(opp.getAction().ordinal()).getFrameNumber();
				double oppCenterX = (opp.getLeft()+opp.getRight())/2;
				double oppCenterY = (opp.getTop()+opp.getBottom())/2;
				input_list.add((double) (oppCenterX)/1000);
				input_list.add((double) (oppCenterY)/600);
				input_list.add((double) (opp.getEnergy())/100);
				input_list.add((double) (Math.abs(opp.getHp()))/1000);
				for (int i = 0; i < 56; i++) {
					if (i == opp_state) {
						input_list.add(1.0);
					} else {
						input_list.add(0.0);
					}
				}
				input_list.add((double) (opp_remaining)/10);
			}else{
				input_list.add(0.0);
				input_list.add(0.0);
				input_list.add(0.0);
				input_list.add(0.0);
				for (int i = 0; i < 56; i++) {
					input_list.add(0.0);
				}
				input_list.add(0.0);
			}
		}

		for (int l = 0; l < record_frame; l++) {
			if (l < frame_list.size()) {
				List<AttackData> myAttack = new ArrayList<AttackData>();
				List<AttackData> oppAttack = new ArrayList<AttackData>();
				myAttack.addAll(player? frame_list.get(l).getProjectilesByP1():frame_list.get(l).getProjectilesByP2());
				oppAttack.addAll(player ? frame_list.get(l).getProjectilesByP2() : frame_list.get(l).getProjectilesByP1());
					for (int n = 0; n < 2; n++) {
						if (myAttack.size() > n) {
							AttackData tmp = myAttack.get(n);
							input_list.add((double) (tmp.getHitDamage()) / 200);
							input_list.add((double) ((tmp.getCurrentHitArea().getLeft() + tmp.getCurrentHitArea().getRight()) / 2) / 1000);
							input_list.add((double) ((tmp.getCurrentHitArea().getTop() + tmp.getCurrentHitArea().getBottom()) / 2) / 600);
						} else {
							input_list.add(0.0);
							input_list.add(0.0);
							input_list.add(0.0);
						}
					}
					for (int n = 0; n < 2; n++) {
						if (oppAttack.size() > n) {
							AttackData tmp = oppAttack.get(n);
							input_list.add((double) (tmp.getHitDamage()) / 200);
							input_list.add((double) ((tmp.getCurrentHitArea().getLeft() + tmp.getCurrentHitArea().getRight()) / 2) / 1000);
							input_list.add((double) ((tmp.getCurrentHitArea().getTop() + tmp.getCurrentHitArea().getBottom()) / 2) / 600);
						} else {
							input_list.add(0.0);
							input_list.add(0.0);
							input_list.add(0.0);
						}
					}
			}else{
				// myattack
				for (int n = 0; n < 2; n++) {
					input_list.add(0.0);
					input_list.add(0.0);
					input_list.add(0.0);
				}
				// oppattack
				for (int n = 0; n < 2; n++) {
					input_list.add(0.0);
					input_list.add(0.0);
					input_list.add(0.0);
				}
			}

		}

		//the action during each of the previous 15 frames
		for (int n = 0; n < delay_frame; n++) {
			if (n < delay_actions.size()) {
				for (int i = 0; i < 56; i++) {
					if (i == delay_actions.get(n).ordinal()) {
						input_list.add(1.0);
					} else {
						input_list.add(0.0);
					}
				}
			} else {
				for (int i = 0; i < 56; i++) {
					input_list.add(0.0);
				}
			}
		}

		Input = toArr(input_list);
		return Input;
	}

	public void setAction(int action_n) {
		this.action_n = action_n;
	}

	public void setScore(double winRate) {
		this.score = winRate - current_score;
	}

	public void outputRawData(BufferedWriter tw) {
		try {
			StringBuffer strb = new StringBuffer();

			for (int i = 0; i < Input.length; i++) {
				strb.append(Input[i] + "");
				if (i != Input.length - 1) {
					strb.append(",");
				}
			}
			strb.append("," + action_n);
			tw.write(strb.toString());
			tw.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static double[] toArr(List<Double> list) {
		int l = list.size();
		double[] arr = new double[l];
		Iterator<Double> iter = list.iterator();
		for (int i = 0; i < l; i++)
			arr[i] = iter.next();
		return arr;
	}

	public String getArgTrainX() {
		StringBuffer strb = new StringBuffer();

		for (int i = 0; i < Input.length; i++) {
			strb.append(Input[i] + "");
			if (i != Input.length - 1) {
				strb.append(" ");
			}
		}

		return strb.toString();
	}

	double get_reward(GameData gameData, boolean player, ArrayList<CharacterData> my_list,
			ArrayList<CharacterData> opp_list, ArrayList<FrameData> frame_list, ArrayList<Action> delay_actions) {
		
		this.reward = 0;
		return reward;
	}


}


