import java.util.ArrayList;
import java.util.Random;


import aiinterface.CommandCenter;
import enumerate.Action;
import aiinterface.AIInterface;
import network.*;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import structs.InputData;
import util.Matrix2D;

public class CYR_AI implements AIInterface {
	
	Network network;
	ArrayList<FrameData> f_list = new ArrayList<FrameData>();
	ArrayList<CharacterData> mc_list = new ArrayList<CharacterData>();
	ArrayList<CharacterData> oc_list = new ArrayList<CharacterData>();
	ArrayList<Action> a_list = new ArrayList<Action>();
	private Key key;
	private CommandCenter commandCenter;
	private boolean playerNumber;
	private boolean isControl;
	private GameData gameData;

	private FrameData frameData;
	private CharacterData myCharacter;
	private CharacterData oppCharacter;
	private Random policy_r = new Random();
	
	int[] action_count = new int[56];
	private Action[] myaction;
	private String input_action;
	private String ourCharacter;
	private boolean speedmode = false;

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getInformation(FrameData frameData, boolean isControl) {
		this.frameData = frameData;
		this.isControl = isControl;
		this.commandCenter.setFrameData(this.frameData, this.playerNumber);
		
		if(frameData.getEmptyFlag()) {
			return;
		}
		else {
			myCharacter = frameData.getCharacter(playerNumber);
			oppCharacter = frameData.getCharacter(!playerNumber);
		}
	}

	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		// TODO Auto-generated method stub
		this.playerNumber = playerNumber;
		this.gameData = gameData;
		
		this.key = new Key();
		this.frameData = new FrameData();
		this.commandCenter = new CommandCenter();
		// initializes the network
		// defines the network architecture
		this.network = new Network();
		this.ourCharacter = gameData.getCharacterName(this.playerNumber);
		this.speedmode = (gameData.getAiName(!this.playerNumber).equals("MctsAi"));
		
		myaction = new Action[] { Action.AIR_A, Action.AIR_B, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB,
				Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_DA, Action.AIR_DB, Action.AIR_F_D_DFA,
				Action.AIR_F_D_DFB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, 
				Action.BACK_JUMP, Action.BACK_STEP, Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA,
				Action.CROUCH_FB, Action.CROUCH_GUARD, Action.DASH, Action.FOR_JUMP, Action.FORWARD_WALK,
				Action.JUMP, Action.AIR_GUARD, Action.STAND_A, Action.STAND_B, Action.STAND_D_DB_BA,
				Action.STAND_D_DB_BB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_D_DF_FC,
				Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_FA, Action.STAND_FB,
				Action.STAND_GUARD, Action.THROW_A, Action.THROW_B, };

		String root = "data/aiData/CYR_AI/"+this.ourCharacter;
		if(this.speedmode) {
			root = root + "_speedmode";
			System.out.println("Speedmode£¡");
		}
		Layer layer_1 = new Layer(144, 200, new ReLU());
		layer_1.loadWeight(root+"_weights_0.csv");
		layer_1.loadBias(root+"_weights_1.csv");
		network.addLayer(layer_1);
		Layer layer_2 = new Layer(200, 200, new ReLU());
		layer_2.loadWeight(root+"_weights_2.csv");
		layer_2.loadBias(root+"_weights_3.csv");
		network.addLayer(layer_2);
		Layer layer_3 = new Layer(200, 40, new None());
		layer_3.loadWeight(root+"_weights_4.csv");
		layer_3.loadBias(root+"_weights_5.csv");
		network.addLayer(layer_3);
		

		
		
		return 0;
	}

	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return key;
	}

	@Override
	public void processing() {
		// double time = System.currentTimeMillis();
		if(canProcessing()) {
			// This part is for the input representation.
			if (commandCenter.getSkillFlag()) {
				key = commandCenter.getSkillKey();
				
				// This is for the input representation.
				a_list.add(Action.STAND);
			} else if(this.isControl) {
				key.empty();
				commandCenter.skillCancel();
				
				// creates the input from NowFrameData and other data
				InputData temp = new InputData(gameData, playerNumber, myCharacter, oppCharacter, frameData);
				double[][] inputs = new Matrix2D(temp.Input).getArrays();
				
				// forward propagation
				double[][] outputs = this.network.forward(inputs);

				// chooses the highest evaluation action from the outputs
				int action_n = choose_action(outputs);
				System.out.println("Choice:" + myaction[action_n].name() + "(No." + action_n + ")");
				
				// This part is for the input representation..
				input_action = myaction[action_n].name();
				a_list.add(myaction[action_n]);
				action_count[action_n]++;
				
//				if(this.ourCharacter.equals("GARNET")) {
//					if(input_action.equals("CROUCH_GUARD")){
//						input_action = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
//					}
//					else if(input_action.equals("STAND_GUARD")) {
//						input_action = "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4";
//					}
//					else if(input_action.equals("AIR_GUARD")){
//						input_action = "7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7";
//					}
//				}
//				else {
//					if(input_action.equals("CROUCH_GUARD")){
//						input_action = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
//					}
//					else if(input_action.equals("STAND_GUARD")) {
//						input_action = "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4";
//					}
//					else if(input_action.equals("AIR_GUARD")){
//						input_action = "7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7";
//					}
//				}
				if(input_action.equals("CROUCH_GUARD")){
					input_action = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
				}
				else if(input_action.equals("STAND_GUARD")) {
					input_action = "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4";
				}
				else if(input_action.equals("AIR_GUARD")){
					input_action = "7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7";
				}
				
				// returns an action to Framework
				commandCenter.commandCall(input_action);
				key = commandCenter.getSkillKey();
				// prints required time to return an action  
				// System.out.println("Required time to return action:" + (System.currentTimeMillis()-time)+"ms");
			}
			
		}
	}

	@Override
	public void roundEnd(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub	
		key.empty();
		commandCenter.skillCancel();
	}
	
	public boolean canProcessing() {
		return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
	}
	
	
	private int choose_action(double[][] policy_output) {
		int action_n = 0;
		int count = 0;
		double max_a = -100;
		int action_max = 0;
        double d = policy_r.nextDouble();
        double sum_p = 0;
        double sum_temp = 0;
        for (int i = 0; i < policy_output.length; i++) {
        	sum_temp += Math.exp(policy_output[i][0]-10);
        	if(max_a < policy_output[i][0]) {
        		action_max = i;
        		max_a = policy_output[i][0];
        	}
		}
		for (int i = 0; i < policy_output.length; i++) {
			if(sum_p < d){
				action_n=i;
				sum_p += Math.exp(policy_output[i][0]-10)/sum_temp;
			}
			else if(count<3) {
				if(sum_p < 0.5) {
					count=3;
					action_n = action_max;
					break;
				}
			}
		}
//		for (int i = 0; i < policy_output.length; i++) {
//			if(max_a < policy_output[i][0]) {
//				max_a = policy_output[i][0];
//				action_n = i;
//			}
//		}
		return action_n;
	}

}
