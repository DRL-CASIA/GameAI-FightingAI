package Opponent;

import java.util.*;


import DL.DeepModel;

import enumerate.Action;

import struct.FrameData;
import struct.CharacterData;
import struct.GameData;

import java.util.Vector;

public class DLOpponentModel{

	private DeepModel dlModel; 
	private Random random;
	private Vector batch_feature;
	private Vector batch_label;
	private int epoch;
	private float[] last_feature;
	static private int batch_size=100;
	static private int batch_pool_size=1000;
	private boolean a;
	public DLOpponentModel(String myName, String oppName, String lossType){
		random = new Random();
		dlModel = new DeepModel(myName, oppName, lossType);
		batch_feature = new Vector();
		batch_label = new Vector();
		last_feature = new float[12];
		for (int i=0; i<last_feature.length;i++) last_feature[i]=0;
		epoch = 0;
		a = true;
	}
	
	

	private float[] preprocess(GameData gd, FrameData fd, boolean player){
		// current state. 
		CharacterData my = fd.getCharacter(player);
		CharacterData opp = fd.getCharacter(!player);
		float myMaxHp = gd.getMaxHP(player);
		float oppMaxHp = gd.getMaxHP(!player);
		float stageWidth = gd.getStageWidth();
		float stageHeight = gd.getStageHeight();
		float myMaxEnergy = gd.getMaxEnergy(player);
		float oppMaxEnergy = gd.getMaxEnergy(!player);
		float myHp = my.getHp();
		float oppHp = opp.getHp();
		float myEnergy = my.getEnergy();
		float oppEnergy = opp.getEnergy();
		float myPosX = my.getCenterX();
		float oppPosX = opp.getCenterX();
		float myPosY = my.getCenterY();
		float oppPosY = opp.getCenterY();
		float distX = fd.getDistanceX();
		float distY = fd.getDistanceY();
		float myState = my.getState().ordinal();
		float oppState = opp.getState().ordinal();
		float stateLen = (float)(my.getState().values().length);  
		float relative_x = myPosX - oppPosX;
		float relative_y = myPosY - oppPosY;
		float maxHitCount = 10;
		float myHits = (float)(my.getHitCount());
		float oppHits = (float)(opp.getHitCount());


		// 5
		myHp = myHp/ myMaxHp;
		myEnergy = myEnergy / myMaxEnergy;
		myPosX = myPosX / stageWidth;
		myPosY = myPosY / stageHeight;
		myState = myState / stateLen;

		// 5
		oppHp = oppHp / oppMaxHp;
		oppEnergy = oppEnergy / oppMaxEnergy;
		oppPosX = oppPosX / stageWidth;
		oppPosY = oppPosY / stageHeight;
		oppState = oppState / stateLen;

		// 2
		distX = distX / stageWidth;
		distY = distY / stageHeight;

		// 2
		relative_x = relative_x / stageWidth;
		relative_y = relative_y / stageHeight;

		// 2
		myHits = Math.min(myHits, maxHitCount) / maxHitCount;
		oppHits = Math.min(oppHits, maxHitCount) / maxHitCount;

		float[] inputs = {myHp, myEnergy, myPosX, myPosY, myState, oppHp, oppEnergy, oppPosX, oppPosY, oppState, distX, distY};

		return inputs;
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
	
	public void record(GameData gd, FrameData fd, boolean player, int target){
		float[] inputs = preprocess(gd, fd, player);
		
		for (int i=0; i<inputs.length; i++){
			if (inputs[i] != last_feature[i]){
				batch_feature.addElement(inputs);
				batch_label.addElement((float)(target));
				break;
			}
		}
		last_feature = inputs;
		
	}
	
	public void train_batch(){
		int size = batch_label.size();
		if (size>0){
			float [][] input_features = new float[size][];
			float [] input_labels = new float[size];
			for (int i=0;i<size;i++) {
				input_labels[i] = (float) batch_label.get(i);
				input_features[i] = (float[])batch_feature.get(i);
			}
			dlModel.train(input_features, input_labels);
			batch_feature.clear();
			batch_label.clear();
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