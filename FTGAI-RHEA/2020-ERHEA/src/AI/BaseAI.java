package AI;


import java.util.ArrayList;
import java.util.LinkedList;


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
	FrameData simFrameData;
	FrameData frameData;

	Simulator simulator;
	GameData gd;
	Calculator calc;
	RHEAAgent rheAgent;
    
	int distance;
	int distance_threshold;
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
	boolean start_run = false;

	long budgetTime;
	long start;
	
		
	void setRootActions(){
		
		

		 // For ZEN
		 if (c_name == CharName.ZEN){
		 	// my state and acts
		 	myacts = my.getState()!=State.AIR? BaseUtil.actionGroundZEN : BaseUtil.actionAirZEN;	
		 	// opp state and acts
			 oppacts = opp.getState()!=State.AIR? BaseUtil.actionGroundTH : BaseUtil.actionAirTH;
		 }
		 // For GARNET
		 else if (c_name == CharName.GARNET){
		 	// my state and acts
		 	myacts = my.getState()!=State.AIR? BaseUtil.actionGroundGAR : BaseUtil.actionAirGAR;
		 	// opp state and acts
			oppacts = opp.getState()!=State.AIR? BaseUtil.actionGroundTH : BaseUtil.actionAirTH;
		 }
		 // For LUD or others
		 else{
		 	// my state and acts
		 	myacts = my.getState()!=State.AIR? BaseUtil.actionGroundOther : BaseUtil.actionAirOther;
		 	// opp state and acts
		 	oppacts = opp.getState()!=State.AIR? BaseUtil.actionGroundTH : BaseUtil.actionAirTH;
		 }

		 
		
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
		// init properties
		this.start_run = false;
		this.player = player;
		this.gd = gd;
		this.c_name = c_name;
		this.distance_threshold = 300;

		// init simulator and motion data.
		this.simulator = this.gd.getSimulator();
		this.myMotion = this.gd.getMotionData(this.player);
		this.oppMotion = this.gd.getMotionData(!this.player);
		
		this.myActionsEnoughEnergy = new LinkedList<Action>();
		this.oppActionsEnoughEnergy = new LinkedList<Action>();
		
		// budget Time
		this.budgetTime =  165 * 100000;
		
		// rolling horizon evolution
		gi = new GeneralInformation(this.gd, this.player, lossType, specificEnemy);
		rheAgent = new RHEAAgent(gi);
		
		
	}
	
	public void reset_info(){
		gi.resetInfo();
		this.lastOppAttackMotion = null;
	}
	
		
	
	public void getInformation(FrameData frameData){
		this.start = System.nanoTime();
		
		if (frameData.getFramesNumber() >= 0){
			MotionData oppmo = oppMotion.get(frameData.getCharacter(!player).getAction().ordinal());
			if (oppmo.getAttackHitDamage()>0) this.lastOppAttackMotion = oppmo;
			
			
			this.frameData = frameData;
			this.simFrameData = simulator.simulate(frameData, this.player, null, null, 14);
		
			
			
			distance = this.simFrameData.getDistanceX();
			my = simFrameData.getCharacter(player);
			opp = simFrameData.getCharacter(!player);
			calc = new Calculator(simFrameData, gd, player, Calculator.NONACT);

			
			
//			distance = this.frameData.getDistanceX();
//			my = this.frameData.getCharacter(player);
//			opp = this.frameData.getCharacter(!player);
//			calc = new Calculator(frameData, gd, player, Calculator.NONACT);
		
			
//			System.out.println("oppActs:" + opp.getAction().name());
			
			
//			distance = this.frameData.getDistanceX();
//			my = this.frameData.getCharacter(player);
//			opp = this.frameData.getCharacter(!player);
//			calc = new Calculator(simFrameData, gd, player, Calculator.NONACT);
			
			
//			distance = this.simFrameData.getDistanceX();
//			my = this.simFrameData.getCharacter(player);
//			opp = this.simFrameData.getCharacter(!player);
//			calc = new Calculator(frameData, gd, player, Calculator.NONACT);
			
			setRootActions();
			this.myActionsEnoughEnergy = calc.getEnoughEnergyActions(this.player, myacts);
			this.oppActionsEnoughEnergy = calc.getEnoughEnergyActions(!this.player, oppacts);
			setMyHittingAction();
			
		}
		
	}
	
	Action rheaProcessing(FrameData simFrame, LinkedList<Action> startAvailActions){
		 // preparing opponet's
		 gi.updateInfos(simFrame, startAvailActions, myActionsEnoughEnergy, oppActionsEnoughEnergy);
		 // record pair
		 gi.recordPair(simFrame, simFrame, gi.getNextAction());
		return rheAgent.act(gi, this.budgetTime - (System.nanoTime() - this.start));
	}
	
	private boolean whetherUseBigFight(int min_dist, int max_dist, Action skill){
		if(my.getState()!=State.AIR){
			
			LinkedList<Action> jumpactions=new LinkedList<Action>();
			if(opp.getState()==State.AIR) {
				jumpactions.add(Action.NEUTRAL);
				jumpactions.add(Action.AIR_UA);

			}else {

				jumpactions.add(Action.JUMP);
				jumpactions.add(Action.BACK_JUMP);
				jumpactions.add(Action.FOR_JUMP);
		 		 
			}
			boolean can_hit = calc.canHitFromNow(skill, this.player);
			double min_hp_score = calc.getMinHpScore(skill, jumpactions);
			
			
			if(can_hit&& min_hp_score>0 ){ 
						if(min_dist<=distance&&distance<max_dist) {return true;}
			}
		}	
		return false;
	}
	
	

	
	public String getDoAction(){
		
        
		
		// Detect I'm down
		Action myAct = my.getAction();	
		// attack type is 1.high 2.middle 3.low 4.throw
	   
		if ((myAct == Action.DOWN || myAct == Action.RISE || myAct == Action.CHANGE_DOWN) 
				&& opp.getState() != State.AIR){
			// GARNET
			if (this.c_name == CharName.GARNET){
				if (this.lastOppAttackMotion.attackType==2 || this.lastOppAttackMotion.attackType == 1){
					// stand guard
					return "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4"; 
				}
				else{
					// crouch guard
					return "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
				}
			}
			// ZEN
			else if (this.c_name == CharName.ZEN){
//				if (this.lastOppAttackMotion.attackType==2 )	
//					return Action.STAND_GUARD.name();   // stand guard
//				else
					return "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1"; // crouch guard
					
			}
			// other
			else{
//				if (this.lastOppAttackMotion.attackType==2)	
//					return "4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4"; 
//				else
					return "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1"; // crouch guard
				
			}
		}
		
		
		
		// For ZEN--------------------------------------------------------------------
		if (this.c_name == CharName.ZEN){
			
			// v1.2----------------------------------------------------------------------------------------
			// Fighting huge beat!
			if (   	opp.getState().equals(State.DOWN)&&	
					(distance <= 190 &&  distance >=50) &&
					calc.IsInHitArea(Action.STAND_D_DF_FC) &&
					calc.canHitFromNow(Action.STAND_D_DF_FC, this.player) && 
					my.getEnergy()>=150)
				return Action.STAND_D_DF_FC.name();
			
			
			// The amount of Energy larger than 200
			if ( (opp.getState().equals(State.STAND) || opp.getState().equals(State.CROUCH)) && 
					(distance >=20 && distance <= 200)
					&& (my.getEnergy()>=190 || (opp.getHp() <=70 && my.getEnergy()>=40))
					)
				return Action.STAND_D_DB_BB.name();
			
			
		
			
			
			// Crouch FB
			LinkedList<Action> opActs = calc.getEnoughEnergyActions(!this.player, Action.NEUTRAL, Action.JUMP);
			if (calc.getMinHpScore(Action.CROUCH_FB, opActs)>0 
					&& distance <= 180){
				return Action.CROUCH_FB.name();
			}
			
			
			
			
			//----------------------------------------------------------------------------------------------
			
			
			
			
			
			
			
			
			
			
			
			
//			// Fighting huge beat! -- win RHEA_PI----------------------------------------------------------
//		
//			// Fighting huge beat!
//			if (   	opp.getState().equals(State.DOWN)&&	
//					(distance <= 190 &&  distance >=50) &&
//					calc.IsInHitArea(Action.STAND_D_DF_FC) &&
//					calc.canHitFromNow(Action.STAND_D_DF_FC, this.player) && 
//					my.getEnergy()>=150)
//				return Action.STAND_D_DF_FC.name();
//			
//			// The amount of Energy larger than 200
//			if ( (opp.getState().equals(State.STAND) || opp.getState().equals(State.CROUCH)) && 
//					(distance >=20 && distance <= 200)
//					&& (my.getEnergy()>=190 || (opp.getHp() <=60 && my.getEnergy()>=40))
//				)
//				 return Action.STAND_D_DB_BB.name();
//			

//				
//				
//			
//			//---------------------------------------------------------------------------------------
			
			
			

			
			
			// in case of distance to opponent
		}
		
		// For Garnet---------------------------------------------------------------------
		else if (this.c_name == CharName.GARNET){
			
			if (whetherUseBigFight(0, 300, Action.STAND_D_DF_FC)){
				return Action.STAND_D_DF_FC.name();
			}
			
			
//			// Fighting Huge Beat!
//			if ( distance<=300 
//				&& calc.IsInHitArea(Action.STAND_D_DF_FC) 
//				&& opp.getState()!= State.AIR){
//				return Action.STAND_D_DF_FC.name();
//			}
					
		   // knock down skill.
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
		
		// For Other Character Distance Threshold
		else{
			
			// Whether Use Big Fight!
			if (whetherUseBigFight(0, 195, Action.STAND_D_DF_FC) && my.getEnergy()>=150){
				return Action.STAND_D_DF_FC.name();
			}
			
			
		
		}
			
		
		// RHEA based method--------------------------------------------------------------
		if (myHittingActions.size() >0) return rheaProcessing(simFrameData, myHittingActions).name();
		else{
			// From Thunder
			LinkedList<Action> moveActs=
					(distance< this.distance_threshold)?
							calc.getEnoughEnergyActions(this.player,
									Action.FOR_JUMP,
									Action.FORWARD_WALK,
									Action.NEUTRAL,
									Action.JUMP,
									Action.BACK_JUMP,
									Action.BACK_STEP
									):
										calc.getEnoughEnergyActions(this.player,
												Action.FORWARD_WALK,
												Action.FOR_JUMP,
												Action.NEUTRAL,
												Action.JUMP,
												Action.BACK_JUMP,
												Action.BACK_STEP
												);

			return(calc.getMinMaxIfHadouken(moveActs).name());						
		}
				
	}
		
	
	
}
