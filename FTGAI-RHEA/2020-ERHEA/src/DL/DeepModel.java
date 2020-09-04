package DL;
import java.io.File;

import org.deeplearning4j.nn.conf.layers.*;
import org.nd4j.linalg.activations.Activation;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;

import org.deeplearning4j.util.ModelSerializer;

import org.nd4j.linalg.api.ndarray.INDArray;

import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;


import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.deeplearning4j.nn.conf.GradientNormalization;

import javax.swing.*;

import enumerate.Action;

import java.util.*;

public class DeepModel {
   static private MultiLayerNetwork net;
   static private int seed = 123;  // seed is 123;

   static private double learningRate = 0.001;

   static private HashMap<Action, Integer> actToIndex;
   final static private int numOutputs = 56;
   final static private int n_epochs = 3; 

   final static private String directory = "./data/aiData/ERHEA_PI";
   private static String oppName;
   private static String myName;
   private static String oppModelName;
  

   private static String lossType;

  
   public DeepModel(int input_size, String myName, String oppName, String lossType){
   	      this.myName = myName;
   	      this.oppName = oppName;
          this.lossType = lossType;
          this.oppModelName = "oppModel.zip";
          this.oppModelName = this.lossType + "-" + this.oppModelName;
          // network learning model
          System.out.println("Create " + this.lossType + " Model!");
           

		   MultiLayerConfiguration conf = getConfig(input_size);
		   net = new MultiLayerNetwork(conf);
		   net.init();
		   net.setListeners(new ScoreIterationListener(1));

		   actToIndex = new HashMap<Action, Integer>();
		   for (int i=0; i< Action.values().length; i++){
		   	  actToIndex.put(Action.values()[i],i);
		   }

   }
   

   
// num input: my HP, my Energy, my Posx, my Posy, my state, diff posx, opp HP, opp Energy, opp Posx, opp Posy, opp State, 
           // my Last Act,opp Last Act
   private MultiLayerConfiguration getConfig(int input_size){
	   
	   if (lossType.toLowerCase()== "pi"){
		   return new NeuralNetConfiguration.Builder()
				   .seed(seed)
//				   .weightInit(WeightInit.XAVIER)
				   .weightInit(WeightInit.ONES)
				   .biasInit(0.0)
				   .gradientNormalization(GradientNormalization.ClipL2PerLayer)
				   .gradientNormalizationThreshold(1.0)
				   .updater(new Adam(learningRate, 0.99, 0.999, 0.00000001)) //
				   .list()
				   .layer(0,new OutputLayer.Builder(new PILoss())  // best performance: PILoss()
						   .activation(Activation.SOFTMAX)   // pi: softmax, q: identity
						   .nIn(input_size).nOut(numOutputs).build())
				   .build();

	   }
	   else if (lossType.toLowerCase() == "sl"){
		   return new NeuralNetConfiguration.Builder()
				   .seed(seed)
//				   .weightInit(WeightInit.XAVIER)
				   .weightInit(WeightInit.ONES)
				   .gradientNormalization(GradientNormalization.ClipL2PerLayer)
				   .gradientNormalizationThreshold(1.0)
				   .updater(new Adam(learningRate, 0.99, 0.999, 0.00000001)) //
				   .list()
				   .layer(0,new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)  // best performance: PILoss()
						   .activation(Activation.SOFTMAX)   // pi: softmax, q: identity
						   .nIn(input_size).nOut(numOutputs).build())
				   .build();

	    }
      System.err.printf("Not right loss:%s \n", lossType);
      return null;

   }
   

   
   public void train(float[][] cur_input_info,  float[][] nx_input_info, float[] sel_actions, float[] opp_sel_actions, float win_signal){
	   int batch_size = sel_actions.length;

	   INDArray sel_actsND = Nd4j.create(sel_actions, new int[]{batch_size, 1});
	   INDArray opp_sel_actsND = Nd4j.create(opp_sel_actions, new int[]{batch_size, 1});
	   INDArray cur_nd = Nd4j.create(cur_input_info);
	   
	     

	   if (lossType == "sl"){
	   	   int [] labels = new int[opp_sel_actions.length];
	   	   for (int i=0; i<labels.length; i++)
	   	   	   labels[i] = (int)(opp_sel_actions[i]);
	   	   for (int i=0; i<n_epochs; i++){
	   	   		net.fit(cur_nd, labels);
	   	   		System.out.println("Loss:" + net.score());
	   	   }
	   }
	   else{
		   float discount = 0.99f;
		   float [] targets_opp = new float[batch_size];
		   float [] targets_sel = new float[batch_size];
		   float reward_sel = 0;
		   float reward_opp = 0;
		   
		   targets_opp[batch_size - 1] = win_signal; 
		   targets_sel[batch_size - 1] = win_signal;

		   for (int i=batch_size-2; i>=0; i--){
			   
			   reward_sel = -Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f) - 0.01f;
			   reward_opp = Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f) + 0.01f;
			   
//			   v1 -- 55.9
//			   reward_sel = -Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f) + 0.01f;
//			   reward_opp = Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f) - 0.01f;
			   
//			   // my player - soso-0
//			   reward = Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f);
			   
//			   //   opp player - not so well
//			   reward = Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f);
			   
//	            opp player    --not so well                                     our player
//				reward = Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f) - Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f); 
				   

			//  no well     
//			   reward = Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f) + Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f); 
			   
			   
			   
			   // not well        
//			   reward = Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f) - Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f); 
			   
			   
			   
			   //                    our player hp diff                                   opp player hp diff
//			   reward = Math.max(nx_input_info[i][0] - nx_input_info[i+1][0], 0.f) - Math.max(nx_input_info[i][1] - nx_input_info[i+1][1], 0.f); 
			   targets_opp[i] = reward_opp + discount * targets_opp[i+1];
			   targets_sel[i] = reward_sel + discount * targets_sel[i+1];
			  
//			   targets_f[i] = reward;
			 
	
			  
			   
		   }

		   System.out.println("");
		   
//		   // Normalize reward part
//		   for (int i=0; i<batch_size-1;i++){
//			   targets_f[i] = targets_f[i]/ batch_size;
//		   }
 
		     
		   // target values
		   INDArray target_sel = Nd4j.create(targets_sel, new int[] {batch_size, 1});
		   INDArray target_opp = Nd4j.create(targets_opp, new int[] {batch_size, 1});
	       
		   // train repeatedly
		   for (int i=0; i<n_epochs; i++){   
				   net.setMask(sel_actsND);
				   net.fit(cur_nd, target_sel);
//				   System.out.println("Loss:" + net.score());
				   
				   net.setMask(opp_sel_actsND);
				   net.fit(cur_nd, target_opp);
		   }

	   }

   }
	  
	 
	  
	 	 
   
	public int forward(float[] input_info){  
		int num_out=0;
		INDArray input;
		INDArray out;

		input = Nd4j.create(input_info);
		out = net.output(input);
		
	   
	   INDArray argmax;
	   //System.out.println("Out:" + out);
	   argmax =  Nd4j.argMax(out, 1);  
	   num_out = (int)(argmax.getInt(0));
	   

	  
	   return num_out;
   }
	
	
	public int forward(float[] input_info, LinkedList<Action> validActs){
		 
		int num_out=0;
		double maxV = -99999999.0;
		double val;
		INDArray input;
		INDArray out;
		
		input = Nd4j.create(input_info);
		out = net.output(input);
		
		double[] ds = out.toDoubleVector();
		for (Action act:validActs ){
			int i = actToIndex.get(act);
			val = ds[i];
			if (val>maxV){
				maxV = val;
				num_out = i;
			}
		}

		return num_out;

		
   }

   private String getPathName(){
   	  String myname;
   	  String oppname;
   	  myname = this.myName == ""?  "": this.myName + "-";
   	  oppname = this.oppName == ""? "": this.oppName + "-";
   	  return this.directory + "/" + myname + oppname +  this.oppModelName;
   }
	
   public void save(){
       File file = new File(this.directory);
       if (!file.exists()) { file.mkdir();}

	   String pathName;
       pathName = getPathName();
	   try{
			   System.out.println("Save Model!");
			   ModelSerializer.writeModel(net, new File(pathName), true); // false is not saved optimization parameters.
	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
	   
   }
   
   public void load(){
   	   String pathName;
   	   pathName = getPathName();
	   try{
		   File loadFile = new File(pathName);
		   if(loadFile.exists()) {
			   System.out.println("Loaded Model!");
			   net = ModelSerializer.restoreMultiLayerNetwork(loadFile);
		   }
		   else{
		   	   System.out.println("Could not find out the model!");
		   }
	   }
	   catch(Exception e){
		   e.printStackTrace();
	   }
   }



}
