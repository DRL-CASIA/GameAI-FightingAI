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

   final static private int numInputs = 12;
   static private double learningRate = 0.00001;

   static private HashMap<Action, Integer> actToIndex;
   final static private int numOutputs = 56;
   final static private int n_epochs = 3; 

   final static private String directory = "./data/aiData/RHEA_PI";
   private static String oppName;
   private static String myName;
   private static String oppModelName;
  

   private static String lossType;

  
   public DeepModel(String myName, String oppName, String lossType){
   	      this.myName = myName;
   	      this.oppName = oppName;
          this.lossType = lossType;
          this.oppModelName = "oppModel.zip";
          this.oppModelName = this.lossType + "-" + this.oppModelName;
          // network learning model
          System.out.println("Create " + this.lossType + " Model!");


		   MultiLayerConfiguration conf = getConfig();
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
   private MultiLayerConfiguration getConfig(){
	   
	   if (lossType.toLowerCase()== "pi"){
		   return new NeuralNetConfiguration.Builder()
				   .seed(seed)
				   .weightInit(WeightInit.XAVIER)
				   .gradientNormalization(GradientNormalization.ClipL2PerLayer)
				   .gradientNormalizationThreshold(1.0)
				   .updater(new Adam(learningRate, 0.8, 0.999, 0.00000008)) //
				   .list()
				   .layer(0,new OutputLayer.Builder(new PILoss())  // best performance: PILoss()
						   .activation(Activation.SOFTMAX)   // pi: softmax, q: identity
						   .nIn(numInputs).nOut(numOutputs).build())
				   .build();

	   }
	   else if (lossType.toLowerCase() == "sl"){
		   return new NeuralNetConfiguration.Builder()
				   .seed(seed)
				   .weightInit(WeightInit.XAVIER)
				   .gradientNormalization(GradientNormalization.ClipL2PerLayer)
				   .gradientNormalizationThreshold(1.0)
				   .updater(new Adam(learningRate, 0.8, 0.999, 0.00000008)) //
				   .list()
				   .layer(0,new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)  // best performance: PILoss()
						   .activation(Activation.SOFTMAX)   // pi: softmax, q: identity
						   .nIn(numInputs).nOut(numOutputs).build())
				   .build();

	    }
      System.err.printf("Not right loss:%s \n", lossType);
      return null;

   }
   

   
   public void train(float[][] input_info, float[] real_actions){
	   int batch_size = real_actions.length;
	   INDArray actions = Nd4j.create(real_actions, new int[]{batch_size, 1});
	   INDArray nd;

	   nd = Nd4j.create(input_info);

	   if (lossType == "sl"){
	   	   int [] labels = new int[real_actions.length];
	   	   for (int i=0; i<labels.length; i++)
	   	   	   labels[i] = (int)(real_actions[i]);
	   	   for (int i=0; i<n_epochs; i++){
	   	   		net.fit(nd, labels);
	   	   		System.out.println("Loss:" + net.score());
	   	   }
	   }
	   else{
		   float discount = 0.99f;
		   float [] targets_f = new float[batch_size];
		   float rewards = 0;
		   targets_f[batch_size-1] = input_info[batch_size-1][5] >= input_info[batch_size-1][0] ? 1:-1;
		   
		   for (int i=batch_size-2; i>=0; i--){
			   rewards = ( input_info[i+1][5]  - input_info[i+1][0]); 
			   targets_f[i] =  rewards + discount* targets_f[i+1] ;
		   }
		   
		   // Normalize reward part
		   for (int i=0; i<batch_size-1;i++){
			   targets_f[i] = targets_f[i]/ batch_size;
		   }
 
		   INDArray target_r = Nd4j.create(targets_f, new int[] {batch_size, 1});
           
		   
		   // train repeatedly
		   for (int i=0; i<n_epochs; i++){
				   net.setMask(actions);
				   net.fit(nd, target_r);
				   System.out.println("Loss:" + net.score());
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
