package Opponent;

import java.util.*;


import DL.DeepModel;

import enumerate.Action;

import struct.FrameData;
import struct.CharacterData;
import struct.GameData;
import struct.AttackData;

public class DLOpponentModel{

	private DeepModel dlModel; 
	
	private Vector cur_batch_feature;
	private Vector nx_batch_feature;
	private Vector batch_sel_act_label;
	private Vector batch_opp_sel_act_label;
	
	private float[] last_feature;
	static private int batch_size=100;
	static private int batch_pool_size=1000;

	LinkedList<Float> input_data;


	// config maximum of values
	float max_hp;
	float max_energy;
	float stage_height;
	float stage_width;
	Deque<AttackData> myAttack;
	Deque<AttackData> oppAttack;
	
	float [] my_CurAttack;
	float [] opp_CurAttack;
	

	public DLOpponentModel(GameData gd,  FrameData fd, String myName, String oppName, String lossType){
		this.input_data = new LinkedList<Float>();
		this.last_feature = new float[26];
		for (int i=0; i<this.last_feature.length;i++) { 
			this.last_feature[i]=0;
		}
		int input_size = this.last_feature.length;
		
		dlModel = new DeepModel(input_size, myName, oppName, lossType);
		// batch features and labels
		cur_batch_feature = new Vector();
		nx_batch_feature = new Vector();
		batch_sel_act_label = new Vector();
		batch_opp_sel_act_label = new Vector();
	
		

		// sets 
		max_hp = (float)(gd.getMaxHP(true));
		max_energy = (float)(gd.getMaxEnergy(true));
		stage_height = (float)(gd.getStageHeight());
		stage_width = (float)(gd.getStageWidth());
	   
	}
	
	

	private float[] preprocess(GameData gd, FrameData fd, boolean player){
		this.input_data.clear();
		
		
		// current state. 
		CharacterData my = fd.getCharacter(player);
		CharacterData opp = fd.getCharacter(!player);
		
		// my part
		float myHp = (float)(my.getHp());
		float myEnergy = (float)(my.getEnergy());
		float myPosX = (float)(my.getCenterX());
		float myPosY = (float)(my.getCenterY());
		float mySpeedX = (float)(my.getSpeedX());
		float mySpeedY = (float)(my.getSpeedY());
		float myHits = (float)(my.getHitCount());
		float myRemainFrame = (float)(my.getRemainingFrame());
		int myState = my.getState().ordinal();
		

		// opp part
		float oppHp = (float)(opp.getHp());
		float oppEnergy = (float)(opp.getEnergy());
		float oppPosX = (float)(opp.getCenterX());
		float oppPosY = (float)(opp.getCenterY());
		float oppSpeedX = (float)(opp.getSpeedX());
		float oppSpeedY = (float)(opp.getSpeedY());
		float oppHits = (float)(opp.getHitCount());
		float oppRemainFrame = (float)(opp.getRemainingFrame());
		int oppState = opp.getState().ordinal();
		

		// dist x, y
		float distX = (float)(fd.getDistanceX());
		float distY = (float)(fd.getDistanceY());
		int stateLen = (my.getState().values().length);  
		float maxHitCount = 10;
		
		if (player){
			myAttack = fd.getProjectilesByP1();
			oppAttack = fd.getProjectilesByP2();
		}
		else{
			myAttack = fd.getProjectilesByP2();
			oppAttack = fd.getProjectilesByP1();
		}


		
		// my- 7
		myHp = myHp / this.max_hp;
		myEnergy = myEnergy / this.max_energy;
		myPosX = (myPosX - this.stage_width /2) / (this.stage_width / 2);
		myPosY = (myPosY / this.stage_height);
		mySpeedX = mySpeedX / 20;
		mySpeedY = mySpeedY / 28;
		myRemainFrame = myRemainFrame / 70;
		
		

		
		// opp- 7
		oppHp = oppHp / this.max_hp;
		oppEnergy = oppEnergy / this.max_energy;
		oppPosX = (oppPosX - this.stage_width / 2) / (this.stage_width / 2);
		oppPosY = (oppPosY / this.stage_height);
		oppSpeedX = oppSpeedX / 20;
		oppSpeedY = oppSpeedY / 28;
		oppRemainFrame = oppRemainFrame / 70;
		

		// 2
		distX = (distX - this.stage_width / 2) / (this.stage_width / 2) ;
		distY = (distY / this.stage_height);

		
		// 2
		myHits = Math.min(myHits, maxHitCount) / maxHitCount;
		oppHits = Math.min(oppHits, maxHitCount) / maxHitCount;

		// append data
		// hp part
		this.input_data.add(myHp);
		this.input_data.add(oppHp);
        
		// my part
		this.input_data.add(myEnergy);
		this.input_data.add(myPosX);
		this.input_data.add(myPosY);
		this.input_data.add(mySpeedX);
		this.input_data.add(mySpeedY);
		this.input_data.add(myRemainFrame);
		this.input_data.add(myHits);
		for (int i=0; i<stateLen; i++){
			if (myState == i){
				this.input_data.add(1.f);
			}
			else{
				this.input_data.add(0.f);
			}
		}
		// opp part
		this.input_data.add(oppEnergy);
		this.input_data.add(oppPosX);
		this.input_data.add(oppPosY);
		this.input_data.add(oppSpeedX);
		this.input_data.add(oppSpeedY);
		this.input_data.add(oppRemainFrame);
		this.input_data.add(oppHits);
		for (int i=0; i<stateLen; i++){
			if (oppState == i){
				this.input_data.add(1.f);
			}
			else{
				this.input_data.add(0.f);
			}
		}
		
		// dist
		this.input_data.add(distX);
		this.input_data.add(distY);
		
		int len_inputs = this.input_data.size();
		float [] outs = new float[len_inputs];
		for (int i=0; i<len_inputs; i++){
			outs[i] = this.input_data.get(i);
		}
		
		
		return outs;
	}

	public Action predict(GameData gd, FrameData fd, boolean player){
		float[] inputs = preprocess(gd, fd, player);
		int actionIdx = dlModel.forward(inputs);
		Action act = Action.values()[actionIdx];
		return act;
	}
	
	public Action predict(GameData gd, FrameData fd, boolean player, LinkedList<Action> validAction){
		float[] inputs = preprocess(gd, fd, player);
		int actionIdx = dlModel.forward(inputs, validAction);
		Action act = Action.values()[actionIdx];
		return act;
	}
	
	public void record(GameData gd, FrameData cur_fd, FrameData nx_fd, boolean player, int sel_act, int opp_sel_act){
		float[] cur_inputs = preprocess(gd, cur_fd, player);
		float[] nx_inputs = preprocess(gd, nx_fd, player);
		
		for (int i=0; i<cur_inputs.length; i++){
			if (cur_inputs[i] != this.last_feature[i]){
				cur_batch_feature.addElement(cur_inputs);
				nx_batch_feature.addElement(nx_inputs);
				batch_sel_act_label.addElement((float)(sel_act));
				batch_opp_sel_act_label.addElement((float)(opp_sel_act));
				
				this.last_feature = cur_inputs;
				break;
			}
		}
		
		
	}
	
	public void train_batch(float win_signal){
		int size = cur_batch_feature.size();
		if (size>0){
			float [][] cur_input_features = new float[size][];
			float [][] nx_input_features = new float[size][];
			float [] sel_act_labels = new float[size];
			float [] opp_sel_act_labels = new float[size];
			for (int i=0;i<size;i++) {
				cur_input_features[i] = (float[])cur_batch_feature.get(i);
				nx_input_features[i] = (float[]) nx_batch_feature.get(i);
				sel_act_labels[i] = (float) batch_sel_act_label.get(i);
				opp_sel_act_labels[i] = (float) batch_opp_sel_act_label.get(i);
				
			}
			dlModel.train(cur_input_features, nx_input_features, sel_act_labels, opp_sel_act_labels, win_signal);
			cur_batch_feature.clear();
			nx_batch_feature.clear();
			batch_sel_act_label.clear();
			batch_opp_sel_act_label.clear();
			
		}
		
		
	    // clear last feature.
		for (int i=0; i<this.last_feature.length;i++) { 
			this.last_feature[i]=0;
		}
		System.out.println("Finish Training Batch!");
	}
	

	
	public void saveModel(){
		dlModel.save();
	}
	
	public void loadModel(){
		dlModel.load();
	}


	
	
}