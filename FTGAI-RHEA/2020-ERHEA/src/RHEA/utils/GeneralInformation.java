package RHEA.utils;

import java.util.Deque;
import java.util.LinkedList;

import simulator.Simulator;
import struct.GameData;
import struct.FrameData;
import struct.CharacterData;
import enumerate.State;
import enumerate.Action;

import util.BaseUtil;
import util.Calculator;

// opponent Model
import Opponent.DLOpponentModel;

import java.util.Random;

public class GeneralInformation {

	private boolean selfPlayer;
	private FrameData frameData;
	private FrameData lastFrameData;
	private FrameData recordLastFrameData;
	private GameData gamedata;
	private Simulator simulator;
	
	public LinkedList<Action> startAvailActions;
	public LinkedList<Action> myStartHitActions;
	public LinkedList<Action> oppStartHitActions;
	
	
	public DLOpponentModel dlOpModel;
	private Action lastOppAct;
	private Action lastMyAct;
	
 
	private Action predAct;
	public Random random;
	 
	
	
	public GeneralInformation(GameData gd, boolean selfplayer, String lossType, boolean specificEnemy) {
		this.gamedata = gd;
		this.simulator = gd.getSimulator();
		this.frameData = null;
		this.lastFrameData = null;
		this.recordLastFrameData = null;
		this.selfPlayer = selfplayer;
		this.startAvailActions = new LinkedList<Action>();
		this.myStartHitActions = new LinkedList<Action>();
		this.oppStartHitActions = new LinkedList<Action>();
		this.lastOppAct = null;
		this.lastMyAct = null;
		this.predAct = Action.STAND;
		this.random = new Random();

		String myName;
		String oppName;

		myName = gd.getAiName(selfplayer);
		oppName = gd.getAiName(!selfplayer);
		if (!specificEnemy)
			oppName = "";
		
		// Use Deep Opponent Model
		dlOpModel = new DLOpponentModel(gd, frameData, myName, oppName, lossType);
			
		 
	}


	public void resetInfo(){
		this.lastOppAct = null;
		this.lastMyAct = null;
		this.recordLastFrameData = null;
	}


	// record pair
	public void recordPair(FrameData cur_frame, FrameData nx_frame, Action sel_act){
		Action opp_sel_act = cur_frame.getCharacter(!this.selfPlayer).getAction();
		// record infos
		this.dlOpModel.record(this.gamedata, cur_frame, nx_frame, this.selfPlayer, sel_act.ordinal(), opp_sel_act.ordinal());

	}
	

	public void updateInfos(FrameData frameData, LinkedList<Action> startAvailActions, LinkedList<Action> myStart, LinkedList<Action> oppStart) {
		this.frameData = frameData;
		this.startAvailActions = startAvailActions;
		this.myStartHitActions = myStart;
		this.oppStartHitActions = oppStart;
		

		// Action myAct = this.frameData.getCharacter(this.selfPlayer).getAction();
		// Action oppAct = this.frameData.getCharacter(!this.selfPlayer).getAction();
		
// //		System.out.println("record act:" + oppAct);
// 		if (lastOppAct != null && !lastOppAct.equals(oppAct)){
// 			// CharacterData my_cd = this.recordLastFrameData.getCharacter(this.selfPlayer);
// 			// CharacterData opp_cd = this.recordLastFrameData.getCharacter(!this.selfPlayer);
// 			// System.out.println("cur my_hp:"+my_cd.getHp() + " opp_hp:" + opp_cd.getHp());

// 			this.dlOpModel.record(this.gamedata, this.recordLastFrameData, this.frameData, this.selfPlayer, lastOppAct.ordinal());
// //			System.out.println("record act:" + oppAct);
			
// 		}
//         // last opp Act
// 		this.recordLastFrameData = new FrameData(this.frameData);
// 		this.lastOppAct = oppAct;
// 		this.lastMyAct = myAct;
		
	}
	
	public Action getNextAction(){
		
		this.predAct = dlOpModel.predict(this.gamedata, this.frameData, this.selfPlayer, this.oppStartHitActions);
//		// random part action
//		int size = this.oppStartHitActions.size();
//		int ind = this.random.nextInt(size);
//		predAct = this.oppStartHitActions.get(ind);	

		return this.predAct; 
	}
 

	public boolean IsGameOver() {
		if (  this.frameData.getCharacter(this.selfPlayer).getHp() == 0 || this.frameData.getCharacter(!this.selfPlayer).getHp() == 0)
			return true;
		return false;

	}

	public boolean IsWin() {
		return this.frameData.getCharacter(this.selfPlayer).getHp() > this.frameData.getCharacter(!this.selfPlayer)
				.getHp();
	}

	public double getHPScore() {
		double my = this.frameData.getCharacter(this.selfPlayer).getHp();
		double opp = this.frameData.getCharacter(!this.selfPlayer).getHp();
		return my - opp;
	}

	public double getHPScore(boolean player) {
		double my = this.frameData.getCharacter(player).getHp();
		double opp = this.frameData.getCharacter(!player).getHp();
		return my - opp;
	}


	public void advanceOneStep(Action myact, Action oppact) {
		Deque<Action> myActs = new LinkedList<Action>();
		Deque<Action> oppActs = new LinkedList<Action>();
		if (myact != null)
			myActs.add(myact);
		if (oppact != null)
			oppActs.add(oppact);
		
		int simulate_time =  80;  // -1
		this.lastFrameData = new FrameData(this.frameData);
		this.frameData = this.simulator.simulate(this.frameData, this.selfPlayer, myActs, oppActs, simulate_time);
	}
	
	// null oppActs is better than random oppActs
	public void advanceWholeStep(Deque<Action> myActs, Deque<Action> oppActs){
		int simulate_time =  80; // -1
		this.lastFrameData = new FrameData(this.frameData);
		this.frameData = this.simulator.simulate(this.frameData, this.selfPlayer, myActs, oppActs, simulate_time);
		
	}
	
	
	public LinkedList<Action> getMySelectedActions(){
		CharacterData myDat = this.frameData.getCharacter(this.selfPlayer);
		Action[] myActs = myDat.getState() != State.AIR ? BaseUtil.actionGround : BaseUtil.actionAir;
		Calculator calc = new Calculator(this.frameData, this.gamedata, this.selfPlayer, Calculator.NONACT);
		return calc.getEnoughEnergyActions(true, myActs);
		
	}
	
	public LinkedList<Action> getOppSelectedActions(){
		CharacterData oppDat = this.frameData.getCharacter(!this.selfPlayer);
		Action[] oppActs = oppDat.getState()!=State.AIR? BaseUtil.actionGround : BaseUtil.actionAir;
		Calculator calc = new Calculator(this.frameData, this.gamedata, this.selfPlayer, Calculator.NONACT);
		return calc.getEnoughEnergyActions( false, oppActs);
		
	}
	
	public void setFrameData(FrameData fd) {this.frameData = fd;}
	public GameData getGamedata(){ return this.gamedata;}
	public boolean getMyPlayer(){ return this.selfPlayer;}
	public FrameData getFrameData() {return this.frameData;}
	public FrameData getLastFrameData() {return this.lastFrameData;}
	public DLOpponentModel getDLopModel() {return this.dlOpModel;}


	 

}
