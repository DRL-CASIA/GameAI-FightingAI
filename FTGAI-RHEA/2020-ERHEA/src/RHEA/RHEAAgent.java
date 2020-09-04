package RHEA;

import java.util.Random;

import enumerate.Action;

import RHEA.utils.ParameterSet;
import RHEA.utils.GeneralInformation;

public class RHEAAgent{
	
    // Rolling Horizon Player
    private RollingHorizonPlayer rhPlayer;
    
    // parameters set
    private ParameterSet parameterSet = new ParameterSet();
    

     

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     */
    public RHEAAgent(GeneralInformation gi) {
   
        // Set up random generator
        Random randomGenerator = new Random();

        // Set up algorithm to get action.
        rhPlayer = new RollingHorizonPlayer(gi, randomGenerator);
    }

    /**
     * Act method of agent, called at every game tick.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due (not used).
     * @return - action to play in the game
     */  
    public Action act(GeneralInformation gi, long budgetTime) {

 
//        // Find action to play
        int nextAction =  rhPlayer.run(gi, budgetTime, this);
//        nextAction = rhPlayer.random.nextInt(gi.startAvailActions.size());
       
         // Action
         Action actionToPlay = rhPlayer.getStartActionMapping(nextAction);
        
//        System.out.println("actionToPlay:" + actionToPlay);
        return actionToPlay;
    }
    
    public ParameterSet getParameters(){
    	return parameterSet;
    }

    

   

   

}
