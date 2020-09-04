package AI;


import java.util.ArrayList;
import java.util.LinkedList;

import aiinterface.CommandCenter;

import enumerate.Action;
import enumerate.State;

import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.MotionData;
import util.BaseUtil;
import util.Calculator;


import RHEA.RHEAAgent;
import RHEA.utils.GeneralInformation;
 
import enumValue.CharName;


public class BaseAI {
	public GeneralInformation gi;
	boolean player;
	FrameData frameData, simFrameData;
	CommandCenter cc;
	Simulator simulator;
	GameData gd;
	Calculator calc;
	RHEAAgent rheAgent;
    
	int distance;
	double bestScore;
	CharacterData my;
	CharacterData opp;
	
	ArrayList<MotionData> myMotion;
	ArrayList<MotionData> oppMotion;
	
	LinkedList<Action> myActionsEnoughEnergy;
	LinkedList<Action> oppActionsEnoughEnergy;
	
	LinkedList<Action> myHittingActions = new LinkedList<Action>();
	
	boolean last_AIR_UB = false;
	
	MotionData lastOppAttackMotion=null;
	Action[] myacts;
	Action[] oppacts;
	Action bestact;

	CharName c_name;

	long budgetTime;
	long start;
	
		
	void setRootActions(){

		// For ZEN
		if (c_name == CharName.ZEN){
			// my state and acts
			myacts = my.getState()!=State.AIR? BaseUtil.actionGroundZEN : BaseUtil.actionAirZEN;
						

		}
		// For Garnet
		else if (c_name == CharName.GARNET){
			// my state and acts
			myacts = my.getState()!=State.AIR? BaseUtil.actionGroundGAR : BaseUtil.actionAirGAR;
			
		}
		// For LUD or others
		else{
			// my state and acts
			myacts = my.getState()!=State.AIR? BaseUtil.actionGroundOther : BaseUtil.actionAirOther;
		}

		// opp state and acts
		oppacts = opp.getState()!=State.AIR? BaseUtil.actionGround : BaseUtil.actionAir;
		
	}
	
	void setMyHittingAction(){
		bestScore = -9999;
		bestact = null;
		this.myHittingActions.clear();
		for (Action ac:myacts){
			double hpScore = calc.getHpScore(ac);
			MotionData mo = myMotion.get(ac.ordinal());
			
			if (hpScore>0 ){
				this.myHittingActions.add(ac);
				double turnScore = (double) hpScore 
						+ (mo.attackDownProp? 30.0:0.0)
						- ((double) mo.attackStartUp) * 0.01
						-  ((double) mo.cancelAbleFrame) *0.0001;
				if (turnScore>bestScore) {
					bestScore = turnScore;
					bestact = ac;
				}
			}
		}
	}
	
	
	
	public BaseAI(CharName c_name, GameData gd, boolean player, String lossType, boolean specificEnemy){

		this.player = player;
		this.frameData = new FrameData();
		this.cc = new CommandCenter();
		this.gd = gd;
		this.c_name = c_name;
		this.simulator = this.gd.getSimulator();
		this.myMotion = this.gd.getMotionData(this.player);
		this.oppMotion = this.gd.getMotionData(!this.player);
		
		this.myActionsEnoughEnergy = new LinkedList<Action>();
		this.oppActionsEnoughEnergy = new LinkedList<Action>();
		
		// budget Time
		this.budgetTime = 165 * 100000;
		
		// rolling horizon evolution
		gi = new GeneralInformation(this.frameData, this.gd, this.player, lossType, specificEnemy);
		rheAgent = new RHEAAgent(gi);
		
	}
	
	public void close(){
		// TBD
	}
	
		
	
	public void getInformation(FrameData frameData){
		this.start = System.nanoTime();
		
		if (frameData.getFramesNumber() >= 0){
			MotionData oppmo = oppMotion.get(frameData.getCharacter(!player).getAction().ordinal());
			if (oppmo.getAttackHitDamage()>0) this.lastOppAttackMotion = oppmo;
			
		 
			if (frameData.getFramesNumber()  < 14){
				simFrameData = new FrameData(frameData);
			}
			else{
				simFrameData = simulator.simulate(frameData, this.player, null, null, 14);
			}
			
			cc.setFrameData(simFrameData, player);  
			distance = simFrameData.getDistanceX();
			my = simFrameData.getCharacter(player);
			opp = simFrameData.getCharacter(!player);
			
			calc = new Calculator(simFrameData, gd, player, Calculator.NONACT);
			setRootActions();
			this.myActionsEnoughEnergy = calc.getEnoughEnergyActions(this.player, myacts);
			this.oppActionsEnoughEnergy = calc.getEnoughEnergyActions(!this.player, oppacts);
			setMyHittingAction();
		}
		
	}
	
	Action rheaProcessing(FrameData simFrame, LinkedList<Action> startAvailActions){
		gi.updateInfos(simFrame, startAvailActions, myActionsEnoughEnergy, oppActionsEnoughEnergy);
		return rheAgent.act(gi, this.budgetTime - (System.nanoTime() - this.start));
	}
	
	

	
	public String getDoAction(){

		// Detect I'm down
		Action myAct = my.getAction();	
		// attack type is 1.high 2.middle 3.low 4.throw
	   
		if ((myAct == Action.DOWN || myAct == Action.RISE || myAct == Action.CHANGE_DOWN) 
				&& opp.getState() != State.AIR){
			if (this.lastOppAttackMotion.attackType==2)	
				return "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4"; 
			else
				return "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1"; // crouch guard
		}
		
		
		
		// For Zen--------------------------------------------------------------------
		if (this.c_name == CharName.ZEN){
			// Fighting huge beat!
			if (   	opp.getState().equals(State.DOWN)&&	
					distance <= 200 && 
					calc.IsInHitArea(Action.STAND_D_DF_FC)
					&& my.getEnergy()>=150)
				return Action.STAND_D_DF_FC.name();
			
			// The amount of Energy larger than 200
			if (!opp.getState().equals(State.AIR) && 
					distance <= 200 &&
					my.getEnergy()>=190)
				 return Action.STAND_D_DB_BB.name();
		}
		// For Garnet---------------------------------------------------------------------
		else if (this.c_name == CharName.GARNET){
			
			// Fighting Huge Beat!
			if ( distance<=300 
				&& calc.IsInHitArea(Action.STAND_D_DF_FC) 
				&& opp.getState()!= State.AIR){
				return Action.STAND_D_DF_FC.name();
			}
					
		
			LinkedList<Action> oppMove = calc.getEnoughEnergyActions(!this.player, Action.NEUTRAL, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB);
			if (my.getState() == State.AIR
			    && calc.getMinHpScore(Action.AIR_UB, oppMove)>0){
				last_AIR_UB = true;
				return Action.AIR_UB.name();
			}
			if (bestact != null 
				&& calc.getMinHpScore(bestact, oppMove)>0 
				&& last_AIR_UB
			)
				return bestact.name();
			else
				last_AIR_UB = false;	
		}
			
		
		// RHEA based method--------------------------------------------------------------
		if (myHittingActions.size() >0) return rheaProcessing(simFrameData, myHittingActions).name();
		else{
			
			// From Thunder
			LinkedList<Action> moveActs=
					(distance<300)?
							calc.getEnoughEnergyActions(true,
									Action.FOR_JUMP,
									Action.FORWARD_WALK,
									Action.JUMP,
									Action.BACK_JUMP,
									Action.BACK_STEP
									):
										calc.getEnoughEnergyActions(true,
												Action.FORWARD_WALK,
												Action.FOR_JUMP,
												Action.JUMP,
												Action.BACK_JUMP,
												Action.BACK_STEP
												);

			return(calc.getMinMaxIfHadouken(moveActs).name());						
		}
				
	}
		
	
	
}
