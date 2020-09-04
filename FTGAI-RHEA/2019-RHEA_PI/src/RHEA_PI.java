import aiinterface.AIInterface;
import aiinterface.CommandCenter;


import enumValue.CharName;
import enumValue.GameMode;
import struct.FrameData;
import struct.GameData;
import struct.Key;

import org.apache.log4j.BasicConfigurator;

import AI.BaseAI;

public class RHEA_PI implements AIInterface {
    private Key inputKey;
    private boolean player;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gd;
   
    // game Character Name
    private CharName charName;
    	
	// RHEA AI
	BaseAI ai;

	
	// Set AI mode
	public void setAIMode(){
		System.out.println("Call First Get Information");
		CommandCenter c = new CommandCenter();
		c.setFrameData(frameData, player);

		String lossType = "pi";    // pi, sl
		boolean specificEnemy=true;


		// Just Set Game Node hold on.
		ai = new BaseAI(charName, gd, player, lossType, specificEnemy);

		// load AI model
		ai.gi.dlOpModel.loadModel();
	
	}
	

	
	
	@Override
	public void close() {
		System.gc();
		System.out.println("Close AI!");
	}
	
	
	
	
	

	@Override
	public void getInformation(FrameData fd) {
		 this.frameData = fd;
		 ai.getInformation(this.frameData);
		 
	}

	@Override
	public int initialize(GameData gd, boolean player) {
		System.out.println("Initialize!");
		BasicConfigurator.configure();
		this.inputKey = new Key();
		this.player = player;
		this.frameData = new FrameData();
		cc = new CommandCenter();
		this.gd = gd;
		String s_charName = this.gd.getCharacterName(this.player);
		if (s_charName.equals("ZEN")) charName = CharName.ZEN;
		else if (s_charName.equals("GARNET")) charName = CharName.GARNET;
		else charName = CharName.OTHER;
		
		// Set AI Mode
		setAIMode();
		
		return 0;
	}

	@Override
	public Key input() {
		return inputKey;
	}
	
	


	@Override
	public void processing() {
		
		if (!frameData.getEmptyFlag() && frameData.getRemainingTimeMilliseconds()>0){
			
			// keep do skill
			if (cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}
			else{
				inputKey.empty();
				cc.setFrameData(frameData, player);
				cc.skillCancel();
				String key = ai.getDoAction();
				cc.commandCall(key);
				
			}
			
		}
	}

	@Override
	public void roundEnd(int arg0, int arg1, int arg2) {
 
        System.out.println("Round End!");
        
        // train it.
        ai.gi.dlOpModel.train_batch();
        ai.gi.dlOpModel.saveModel();
        
	}

}
