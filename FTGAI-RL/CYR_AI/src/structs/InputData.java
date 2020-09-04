package structs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import struct.AttackData;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import java.util.ArrayList;

/**
 * Input Data for the sample network
 *
 */
public class InputData {
	public int action_n;
	public double[] Input;
	public int my_state;
	public int my_action;
	public int opp_state;
	public int opp_action;
	public Deque<AttackData> attacks;
	public int input_frame;
	
	public double current_score;

	public double score;

	public InputData(GameData gameData, boolean player, CharacterData my, CharacterData opp,
			FrameData frame) {
		Input = convert(gameData, player, my, opp, frame);
	}

	public InputData(double[] train_X, int action_n) {
		this.Input = train_X;
		this.action_n = action_n;
	}

	double[] convert(GameData gameData, boolean player, CharacterData my,
			CharacterData opp, FrameData frame) {

		ArrayList<Double> input_list = new ArrayList<Double>();
		
		// my information
		double myEnergy = my.getEnergy() / 300;
		double myX = ((double) (my.getLeft() + my.getRight()) / 2 - 960/2) / (960/2);
		double myY = ((double) (my.getBottom() + my.getTop()) / 2) / 640;
		double mySpeedX = (double) my.getSpeedX() / 20;
		double mySpeedY = (double) my.getSpeedY() / 28;
		this.my_state = my.getState().ordinal();
		this.my_action = my.getAction().ordinal();
		double myRemainingFrame = (double) my.getRemainingFrame() / 70;
		// double myhp = my.getHp();
		
		// opp information
		double oppEnergy = (double) opp.getEnergy() / 300;
		double oppX = ((double) (opp.getLeft() + opp.getRight()) / 2 - 960/2) / (960/2);
		double oppY = ((double) (opp.getBottom() + opp.getTop()) / 2) / 640;
		double oppSpeedX = (double) opp.getSpeedX() / 20;
		double oppSpeedY = (double) opp.getSpeedY() / 28;
		this.opp_state = opp.getState().ordinal();
		this.opp_action = opp.getAction().ordinal();
		double oppRemainingFrame = (double) opp.getRemainingFrame() / 70;
		// double opphp = opp.getHp();
		
		// my information
		input_list.add(myEnergy);
		input_list.add(myX);
		input_list.add(myY);
		input_list.add(mySpeedX);
		input_list.add(mySpeedY);
		for (int i = 0; i < 4; i++) {
			if (i == my_state) {
				input_list.add(1.0);
			} else {
				input_list.add(0.0);
			}
		}
		for (int i = 0; i < 56; i++) {
			if (i == my_action) {
				input_list.add(1.0);
			} else {
				input_list.add(0.0);
			}
		}
		input_list.add(myRemainingFrame);
		
		// opp information
		input_list.add(oppEnergy);
		input_list.add(oppX);
		input_list.add(oppY);
		input_list.add(oppSpeedX);
		input_list.add(oppSpeedY);
		for (int i = 0; i < 4; i++) {
			if (i == opp_state) {
				input_list.add(1.0);
			} else {
				input_list.add(0.0);
			}
		}
		for (int i = 0; i < 56; i++) {
			if (i == opp_action) {
				input_list.add(1.0);
			} else {
				input_list.add(0.0);
			}
		}
		input_list.add(oppRemainingFrame);
		
		
		List<AttackData> myAttack = new ArrayList<AttackData>();
		List<AttackData> oppAttack = new ArrayList<AttackData>();
		myAttack.addAll(player ? frame.getProjectilesByP1() : frame.getProjectilesByP2());
		oppAttack.addAll(player ? frame.getProjectilesByP2() : frame.getProjectilesByP1());
		for (int n = 0; n < 2; n++) {
			if (myAttack.size() > n) {
				AttackData tmp = myAttack.get(n);
				input_list.add((double) (tmp.getHitDamage()) / 200);
				input_list.add((double) ((tmp.getCurrentHitArea().getLeft() + tmp.getCurrentHitArea().getRight()) / 2 - (opp.getLeft() + opp.getRight()) / 2) / 960);
				input_list.add((double) ((tmp.getCurrentHitArea().getTop() + tmp.getCurrentHitArea().getBottom()) / 2) / 640);
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
				input_list.add((double) ((tmp.getCurrentHitArea().getLeft() + tmp.getCurrentHitArea().getRight()) / 2 - (my.getLeft() + my.getRight()) / 2) / 960);
				input_list.add((double) ((tmp.getCurrentHitArea().getTop() + tmp.getCurrentHitArea().getBottom()) / 2) / 640);
			} else {
				input_list.add(0.0);
				input_list.add(0.0);
				input_list.add(0.0);
			}
		}
		
		// input_list.add((myhp-opphp)/100.0);


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
}

