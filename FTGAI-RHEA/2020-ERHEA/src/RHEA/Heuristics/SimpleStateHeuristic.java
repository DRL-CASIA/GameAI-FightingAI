package RHEA.Heuristics;

import java.util.ArrayList;
import java.util.HashMap;

import RHEA.utils.GeneralInformation;

import struct.FrameData;


public class SimpleStateHeuristic extends StateHeuristic {

    double initialNpcCounter = 0;

    public SimpleStateHeuristic() {

    }
    
    

    public double evaluateState(GeneralInformation gi) {
        
        double won = 0;
        if (gi.IsWin() && gi.IsGameOver()) {
            won = 1000000000;
        } else if (!gi.IsWin() && gi.IsGameOver()) {
            return -999999999;
        }


//        double minDistance = Double.POSITIVE_INFINITY;  
//        double minDistancePortal = Double.POSITIVE_INFINITY;
      
     
        double score = 0;
       
        score = gi.getHPScore() + won*100000000;
       

        return score;
    }
    
    public double evaluateState(GeneralInformation gi, boolean diffPlayer) {
        
        double won = 0;
        if (gi.IsWin() && gi.IsGameOver()) {
            won = 1000000000;
        } else if (!gi.IsWin() && gi.IsGameOver()) {
            return -999999999;
        }


//        double minDistance = Double.POSITIVE_INFINITY;  
//        double minDistancePortal = Double.POSITIVE_INFINITY;
      
     
        double score = 0;
       
        score = gi.getHPScore(diffPlayer) + won*100000000;
       

        return score;
    }


}
