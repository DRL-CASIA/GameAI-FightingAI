package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import enumerate.Action;
import enumerate.State;
import struct.MotionData;

public class BaseUtil {
	

	public static final Action[] actionAir= 
			new Action[] {
					Action.AIR_GUARD, 
					Action.AIR_A, Action.AIR_B,
					Action.AIR_FA, Action.AIR_FB, 
					Action.AIR_UA, Action.AIR_UB,
					Action.AIR_DA, Action.AIR_DB, 
					Action.AIR_D_DB_BA,Action.AIR_D_DF_FA,
					};
	
	
	public static final Action[] actionGround =
	        new Action[] { 
    		      Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.BACK_STEP, Action.DASH, Action.FORWARD_WALK, Action.STAND,
    		      Action.STAND_GUARD, Action.CROUCH_GUARD,
    		      Action.STAND_A, Action.STAND_B, Action.STAND_FA, 
    		      Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA, Action.CROUCH_FB,
    		      Action.STAND_F_D_DFA, 
    		      };
	
	public static final Action[] actionAirOther=
			new Action[] {
					Action.AIR_GUARD, 
					Action.AIR_A, Action.AIR_B,
					Action.AIR_FA, Action.AIR_FB, 
					Action.AIR_UB,Action.AIR_UA,
					Action.AIR_DA, Action.AIR_DB,
					Action.AIR_D_DB_BA,Action.AIR_D_DF_FA
					};
	
	public static final Action[] actionGroundOther =
	        new Action[] { 
                   Action.JUMP,Action.FOR_JUMP,Action.BACK_JUMP,Action.DASH, Action.BACK_STEP, Action.FORWARD_WALK, Action.STAND,
                   Action.STAND_GUARD, Action.CROUCH_GUARD,
	        	   Action.STAND_A, Action.STAND_B,
	        	   Action.CROUCH_A, Action.CROUCH_B,
                   Action.STAND_F_D_DFA,
	        	   };
	
	
	//	For GARNET	
	public static final Action[] actionAirGAR=
			new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
					Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB};
	
	public static final Action[] actionGroundGAR =
	        new Action[] { 
	        		Action.FORWARD_WALK, Action.DASH,
	                Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.BACK_STEP,
	                Action.STAND_GUARD, Action.CROUCH_GUARD, 
	                Action.STAND_A, Action.STAND_B, 
	                Action.CROUCH_A, Action.CROUCH_B,  Action.CROUCH_FA, Action.CROUCH_FB,
	                Action.STAND_F_D_DFA ,
	                };
	
	
	// For ZEN
	public static final Action[] actionGroundZEN =
	        new Action[] { 
	       			Action.JUMP,
	    		    Action.STAND_GUARD, Action.CROUCH_GUARD,
	    		    Action.STAND_A, Action.STAND_B, Action.STAND_FA, 
	    		    Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA, Action.CROUCH_FB,
	    		    Action.STAND_F_D_DFA, 
	    		    };
	
	public static final Action[] actionAirZEN= 
			new Action[] {
					Action.AIR_GUARD, 
					Action.AIR_A, Action.AIR_B,
					Action.AIR_FA, Action.AIR_FB, 
					Action.AIR_UA, Action.AIR_UB,
					Action.AIR_DA, Action.AIR_DB, 
					};
	
	

	public static final Action[] actionAirNonEnergy =
            new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DB,
                Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
                };
	
	
	public static final Action[] actionGroundNonEnergy =
            new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
                Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
                Action.CROUCH_GUARD, Action.STAND_A, Action.STAND_B,
                Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
                Action.CROUCH_FB,  Action.STAND_F_D_DFA,
                 Action.STAND_D_DB_BB};



	/****ZEN***/
	public static final Action[] ZenFinalActionAir=//20 3.3759618E7
			new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_DA, Action.AIR_F_D_DFB};
	public static final Action[] ZenFinalActionGround =
	        new Action[] {Action.BACK_STEP, Action.DASH, Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.STAND_D_DB_BA, Action.CROUCH_A, Action.CROUCH_B,
	        		Action.FORWARD_WALK, Action.THROW_A, Action.CROUCH_FB, Action.STAND_F_D_DFA, Action.STAND_D_DB_BB,};

	public static final Action[] ZenFinalActionHame =
	        new Action[] {  Action.FORWARD_WALK,Action.STAND,Action.CROUCH_FB,Action.STAND_B,Action.STAND_D_DF_FC};

	public static final Action[] ZenFinalActionNear =
	        new Action[] {Action.FORWARD_WALK,Action.DASH,Action.STAND_D_DB_BB,Action.STAND_B,Action.STAND_D_DF_FC };

	public static final Action[] ZenFinalActionFar =
			 new Action[] { Action.FORWARD_WALK,Action.DASH,Action.STAND_D_DB_BB,Action.FOR_JUMP,Action.STAND_D_DF_FC };


	public static final Action[] ZenMustActionAir=
			new Action[] {};


	public static final Action[] ZenMustActionGround =
	        new Action[] { Action.DASH, Action.FOR_JUMP,Action.STAND_D_DB_BA, Action.CROUCH_FB, Action.STAND_D_DF_FC };

	public static final Action[] ZenMustActionHame =
	        new Action[] { Action.STAND_D_DB_BA, Action.CROUCH_FB, Action.STAND_D_DF_FC };
	public static final Action[] ZenMustActionFar =
	        new Action[] { Action.FOR_JUMP, Action.STAND_D_DB_BA, Action.STAND_D_DF_FC };

	/****Garnet***/

	public static final Action[] NormalGarnetFinalActionAir=
			new Action[] {Action.AIR_GUARD, Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_D_DF_FB, Action.AIR_F_D_DFB,
 };

	public static final Action[] NormalGarnetFinalActionGround =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.BACK_STEP, Action.JUMP, Action.THROW_A, Action.CROUCH_B, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
 };

	public static final Action[] NormalGarnetFinalActionHame =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.STAND_D_DB_BA, Action.FORWARD_WALK, Action.CROUCH_GUARD, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB,
};

	public static final Action[] NormalGarnetFinalActionNear =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.STAND_D_DB_BA, Action.BACK_JUMP, Action.STAND_GUARD, Action.STAND_A, Action.STAND_B, Action.CROUCH_B, Action.STAND_FB, Action.CROUCH_FA, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFB,
};

	public static final Action[] NormalGarnetFinalActionFar =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.BACK_STEP, Action.FORWARD_WALK, Action.JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_F_D_DFB,
};



	public static final Action[] SpeedGarnetFinalActionAir=
			new Action[] {Action.AIR_DA, Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BB,
 };

	public static final Action[] SpeedGarnetFinalActionGround =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.BACK_STEP, Action.FORWARD_WALK, Action.JUMP, Action.CROUCH_GUARD, Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.STAND_F_D_DFB,
 };

	public static final Action[] SpeedGarnetFinalActionHame =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.STAND_D_DB_BA, Action.FORWARD_WALK, Action.CROUCH_GUARD, Action.THROW_B, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DB_BB,
};

	public static final Action[] SpeedGarnetFinalActionNear =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.JUMP, Action.BACK_JUMP, Action.CROUCH_GUARD, Action.THROW_B, Action.STAND_A, Action.CROUCH_A, Action.CROUCH_B, Action.CROUCH_FA, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
};

	public static final Action[] SpeedGarnetFinalActionFar =
			new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC, Action.BACK_STEP, Action.STAND_GUARD, Action.THROW_B, Action.STAND_B, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.STAND_D_DF_FA,
};


	public static final Action[] GarnetMustActionAir=
			new Action[] {};



	public static final Action[] GarnetMustActionGround =
	        new Action[] {Action.DASH, Action.FOR_JUMP, Action.STAND_D_DF_FC};



}


