import aiinterface.AIInterface;
import aiinterface.CommandCenter;


import enumValue.CharName;
import enumValue.GameMode;
import struct.FrameData;
import struct.GameData;
import struct.Key;

import org.apache.log4j.BasicConfigurator;

import AI.BaseAI;

public class ERHEA_PI implements AIInterface {
    private Key inputKey;
    private boolean player;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gd;
   
    // game Character Name
    private CharName charName;
    	
	// RHEA AI
	BaseAI ai;
	
	private float win1=0, win2=0;
	private float win=0;
	private int round = 0;
	private int frozenFrames=0;

	
	// Set AI mode
	public void setAIMode(){
		System.out.println("Call First Get Information");
		// CommandCenter c = new CommandCenter();
		// c.setFrameData(frameData, player);

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
	public void getInformation(FrameData fd, boolean control) {
		 this.frameData = fd;
		 ai.getInformation(this.frameData);
		 this.cc.setFrameData(frameData, this.player);
		 
		 
	}

	@Override
	public int initialize(GameData gd, boolean player) {
		System.out.println("Initialize!");
		BasicConfigurator.configure();
		this.inputKey = new Key();
		this.player = player;
		this.frameData = new FrameData();
		this.cc = new CommandCenter();
		this.gd = gd;
		String s_charName = this.gd.getCharacterName(this.player);
		if (s_charName.equals("ZEN")) charName = CharName.ZEN;
		else if (s_charName.equals("GARNET")) charName = CharName.GARNET;
		else charName = CharName.OTHER;
		
		// Frozen Frames
		this.frozenFrames = 0;
		
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
		
		// can process
		if (!frameData.getEmptyFlag() && frameData.getRemainingFramesNumber()>0){
			
			// keep do skill
			if (cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}
			else{
				inputKey.empty();
				cc.skillCancel();
				String key = ai.getDoAction();
				cc.commandCall(key);
				
			}
			
		}
	}

	@Override
	public void roundEnd(int p1_hp, int p2_hp, int frames) {
		round = round + 1;
        System.out.println("Round End!");
		float win_signal = 1;
		if (this.player && p1_hp < p2_hp){
			win_signal = -1;
		}
		else if (!this.player && p1_hp >p2_hp){
			win_signal = -1;
		}
        
        // train it.
        ai.gi.dlOpModel.train_batch(win_signal);
        ai.gi.dlOpModel.saveModel();

		// reset info
		ai.gi.resetInfo();
		
		// print statistics
		win1 = p1_hp > p2_hp ? win1+1 : win1;
		win2 = p1_hp < p2_hp ? win2+1 : win2;
		
		win = win1 + win2;
		if (win !=0){
			win = this.player? win1 / win: win2 / win;
		}
		String winstr=("Round:\t" + round +"\t" + (win_signal>0?"WIN":"LOSE"));
		System.err.println("I'm:\t"+this.gd.getAiName(this.player));
		System.err.println("Opp:\t"+this.gd.getAiName(!this.player));
		System.err.println("Chara:\t"+this.gd.getCharacterName(this.player));
		System.err.println(winstr+"\twinrate\t"+win);
		System.err.flush();
		
		// round end clean
		this.inputKey.empty();
		this.cc.skillCancel();
		System.gc();
		
		
        
	}

}
