package util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import enumerate.Action;
import enumerate.State;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.HitArea;
import struct.MotionData;

public class Calculator {
	public final static int SIMULATE_LIMIT = 60;
	public final static Action NONACT=Action.NEUTRAL;
	public FrameData nonActionFrame;
	FrameData motoFrame;
	HashMap<List,FrameData>map;
	GameData gd;
	Simulator simlator;
	boolean player;
	private ArrayList<MotionData> myMotion;
	private ArrayList<MotionData> oppMotion;
	LinkedList<Action> hadoukenActsAir;
	LinkedList<Action> hadoukenActsGround;
	
	
	public Calculator( FrameData motoFrame,GameData gd,boolean player,Action preAct) {
		this.motoFrame = motoFrame;
		this.gd=gd;
		this.simlator= gd.getSimulator();
		this.player=player;
		this.myMotion=gd.getMotionData(this.player);
		this.oppMotion = gd.getMotionData(!this.player);


		this.hadoukenActsAir=new LinkedList<Action>();
		this.hadoukenActsGround=new LinkedList<Action>();
		for(Action ac:BaseUtil.actionAir){
			MotionData mo=myMotion.get(ac.ordinal());
			if(mo.getAttackSpeedX()!=0||mo.getAttackSpeedY()!=0)hadoukenActsAir.add(ac);
		}
		for(Action ac:BaseUtil.actionGround){
			MotionData mo=myMotion.get(ac.ordinal());
			if(mo.getAttackSpeedX()!=0||mo.getAttackSpeedY()!=0)hadoukenActsGround.add(ac);
		}


		map=new HashMap<List,FrameData>();
		this.nonActionFrame=getFrame(preAct,NONACT);


	}
	
	
	public boolean isEnoughEnergy(Action act,boolean player){
		ArrayList<MotionData> mos=(player?myMotion:oppMotion);
		CharacterData ch=(player?this.motoFrame.getCharacter(this.player):this.motoFrame.getCharacter(!this.player));
		
		return (mos.get(act.ordinal()).getAttackStartAddEnergy()+ch.getEnergy()>=0);

	}
	
	public LinkedList<Action> getEnoughEnergyActions(boolean player,Action... acts){
		LinkedList<Action> moveActs=new LinkedList<Action>();
		for(Action tac:acts){
			if(isEnoughEnergy(tac, player))moveActs.add(tac);
		}

		return moveActs;
	}
	
	public FrameData getFrame(Action myact,Action opact){
		Action tmyact,topact;
		if(isEnoughEnergy(myact,true)){
			tmyact=myact;
		}else{
			tmyact=NONACT;
		}
		if(isEnoughEnergy(opact,false)){
			topact=opact;
		}else{
			topact=NONACT;
		}
		List<Action>key=new ArrayList<Action>();
		key.add(tmyact);key.add(topact);


		if(!map.containsKey(key)){
			Deque<Action>mAction=new LinkedList<Action>();
			mAction.add(tmyact);
			Deque<Action>opAction=new LinkedList<Action>();
			opAction.add(topact);

			FrameData value=this.simlator.simulate(motoFrame, player, mAction, opAction, SIMULATE_LIMIT);
			map.put(key, value);
		}

		return map.get(key);
	}

	public FrameData getMyFrame(Action myact){
		return getFrame(myact,NONACT);
	}
	public double getHpScore(Action myact){return getHpScore(myact,NONACT);}
	public double getHpScore(Action myact,Action opact){
		FrameData fd=getFrame(myact,opact);
		double gapMyHp=fd.getCharacter(player).getHp()-nonActionFrame.getCharacter(player).getHp();
		double gapOpHp=fd.getCharacter(!player).getHp()-nonActionFrame.getCharacter(!player).getHp();
	 
		return gapMyHp-gapOpHp;
	}


	public double getMinHpScoreIfHadouken(Action myact){
		double min=9999;
		for(Action opact:this.hadoukenActsGround){
			double score=getHpScore(myact,opact);
			if(score<min)min=score;
		}
		return min;
	}

	public Action getMinMaxIfHadouken(List<Action> acs){
		double max=-9999;
		Action maxact=Action.FORWARD_WALK;
		for(Action myact:acs){
			double score=getMinHpScoreIfHadouken(myact);
			if(score>max){max=score;maxact=myact;}
		}
		return maxact;
	}

	
	public double getMinHpScore(Action myact,List<Action> opAcs){
		double min=9999;

		for(Action opact:opAcs){
			double score=getHpScore(myact,opact);
			if(score<min){min=score;}
		}

		return min;
	}

	
	public Action getMinMaxHp(List<Action> myAcs,List<Action> opAcs){
		double alpha=-9999;
		Action maxact=Action.FORWARD_WALK;
		for(Action myact:myAcs){
			double min=9999;

			for(Action opact:opAcs){
				double score=getHpScore(myact,opact);
				if(score<min){min=score;if(min<alpha)break;}
			}
			if(min>alpha){alpha=min;maxact=myact;}
		}
		return maxact;
	}


	public  boolean IsInHitArea(Action ac){
		int left,right,top,bottom;
		CharacterData mych= this.motoFrame.getCharacter(player);
		CharacterData opch= this.motoFrame.getCharacter(!player);
		if(!this.isEnoughEnergy(ac, true))return false;

		MotionData mo=this.myMotion.get(ac.ordinal());
		HitArea hi=mo.attackHitArea;

		// getY is the most top-left y
		top=mych.getY()+hi.getTop();
		bottom=mych.getY()+hi.getBottom();

		bottom+=mo.getAttackStartUp()*mo.getSpeedY();
		top+=mo.getAttackStartUp()*mo.getSpeedY();

		if(mo.getAttackSpeedY()>0){
			bottom+=mo.attackActive*mo.getAttackSpeedY();
		}else{
			top+=mo.attackActive*mo.getAttackSpeedY();
		}
		if(mo.getSpeedY()>0){
			bottom+=mo.attackActive*mo.getSpeedY();
		}else{
			top+=mo.attackActive*mo.getSpeedY();
		}

		int frontfugou=1;
        
		// getX is get most top-left x
		if(mych.isFront()){
			left=mych.getX()+hi.getLeft();
			right=mych.getX()+hi.getRight();
		}else{
			frontfugou=-1;
			left=mych.getX()+mych.getGraphicSizeX()-hi.getRight();
			right=mych.getX()+mych.getGraphicSizeX()-hi.getLeft();
		}

		left+=mo.getAttackStartUp()*mo.getSpeedX()*frontfugou;
		right+=mo.getAttackStartUp()*mo.getSpeedX()*frontfugou;

		if(mo.getAttackSpeedX()*frontfugou>0){
			right+=mo.attackActive*mo.getAttackSpeedX()*frontfugou;
		}else{
			left+=mo.attackActive*mo.getAttackSpeedX()*frontfugou;
		}
		if(mo.getSpeedX()*frontfugou>0){
			right+=mo.attackActive*mo.getSpeedX()*frontfugou;
		}else{
			left+=mo.attackActive*mo.getSpeedX()*frontfugou;
		}

		int or=opch.getRight();
		int ol=opch.getLeft();
		int ot=opch.getTop();
		int ob=opch.getBottom();


		if(right<ol){return false;}
		if(or<left){return false;}
		if(bottom<ot){return false;}
		if(ob<top){return false;}

	//	System.out.println("-----------");

		return true;

	}

}
