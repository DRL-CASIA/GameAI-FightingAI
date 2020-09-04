package RHEA.Heuristics;

import struct.CharacterData;

import RHEA.utils.GeneralInformation;
 
public class WinScoreHeuristic extends StateHeuristic {

    private static final double HUGE_NEGATIVE = -1;
    private static final double HUGE_POSITIVE =  1000000.0;
  

    public WinScoreHeuristic( ) {
          
    }

    public double evaluateState(GeneralInformation gi) {   	
    	boolean player = gi.getMyPlayer();
    	boolean gameOver = gi.IsGameOver();
    	int max_hp = gi.getGamedata().getMaxHP(player);
    	int max_energy = gi.getGamedata().getMaxEnergy(player);
    	CharacterData my = gi.getFrameData().getCharacter(player);
    	CharacterData opp = gi.getFrameData().getCharacter(!player);
        boolean win = gi.IsWin();
        double myHp =  Math.max((double)(my.getHp()), 0);
        double oppHp =  Math.max((double)(opp.getHp()), 0);
        
        CharacterData lastmy = gi.getLastFrameData().getCharacter(player);
        CharacterData lastopp = gi.getLastFrameData().getCharacter(!player);
        double lastmyHp =  Math.max((double)(lastmy.getHp()), 0);
        double lastoppHp =  Math.max((double)(lastopp.getHp()), 0);
        
        // absolute hp----------------------------------------------
        double rawScore = (double)(myHp - oppHp + my.getHitCount() - opp.getHitCount());


        return rawScore;
        
        
        
      
        
        
        
    }


}


