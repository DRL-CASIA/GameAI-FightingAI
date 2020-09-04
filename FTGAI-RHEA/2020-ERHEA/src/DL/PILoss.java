package DL;

import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nd4j.linalg.api.instrumentation.InMemoryInstrumentation;


public class PILoss implements ILossFunction{
		

   private static double eps = 0.0000000001;

  // for mask the selective actions
   private static INDArray getInMask(INDArray masks, int out_size){
	   int batch = (int)masks.shape()[0];
	   float[][] mask_code = new float[batch][out_size];
	   int [] sel_arg = masks.toIntVector();
	  
	  
	   // initialize mask_code
	   for (int i=0; i<batch; i++){
		   for (int j=0; j<out_size; j++) {
			   mask_code[i][j] = 0;
		   }
		  
		   mask_code[i][sel_arg[i]] = 1;	   
	   }
	   	   
	   INDArray inMask = Nd4j.create(mask_code);
	  
	   return inMask;
   }
   
   private static INDArray safeLog(INDArray probs, INDArray inMask){
//	   System.out.println("probs:" + probs);
//	   System.out.println("max probs:"+ probs.max(1));
	   INDArray results = probs.mul(inMask).sum(1);
	   double [] logs = results.toDoubleVector();
	   int length = logs.length;
//	   System.out.print("log:");
	   for (int i=0; i<length; i++){
//		   System.out.print(logs[i] + ", ");
		   if ( Math.abs(logs[i])>=eps){
			   logs[i] = Math.log(logs[i]);
		   }
		   else{
			   logs[i] = 0;
		   }
	   }
//	   System.out.println("");
	   INDArray indLOGS = Nd4j.create(logs, new int []{length, 1});
	   return indLOGS;
   }

   
   // here 
   // labels = rewards, masks = actions
   
   // Score Array - DIY------------------------------------------
   private INDArray scoreArray(INDArray rewards, INDArray preOutput, IActivation actFn, INDArray actions){
	   // action Mask
	   int out_size =  (int)(preOutput.shape()[1]);
	   INDArray inMask = getInMask(actions, out_size);
	   INDArray output = actFn.getActivation(preOutput.dup(), true);
	   
	
	   INDArray logvals = safeLog(output, inMask);
	   INDArray PiLoss = rewards.mul(-1).mul(logvals).repeat(1, out_size).mul(inMask);
			  
	   

	   //scoreArr.addi(yMinusyHat); // regulization part
	   
	   return PiLoss;
   }
   
   @Override
   public INDArray computeGradient(INDArray rewards, INDArray preOutput, IActivation actFn, INDArray actions){   
	   int out_size =  (int)(preOutput.shape()[1]);
	   INDArray inMask = getInMask(actions, out_size);
	   
	   INDArray output = actFn.getActivation(preOutput.dup(), true);
	 
	   INDArray p_ = output.dup().add(1e-10); //output.add(eps);
	   
	   INDArray piGD = rewards.mul(-1).repeat(1, out_size).div(p_).mul(inMask);
	  
//	   System.out.println("piGD:" + piGD);
//	   System.out.println("rewards:" + rewards);
//	   System.out.println("inMask:" + inMask);
	   //.sub(Transforms.sign(yMinusyHat)); //regularization part
	   
	   	   
	   // everything below remain the same.
	   INDArray dLdPreout = actFn.backprop(preOutput.dup(), piGD).getFirst();
 
	   return dLdPreout;
   }
   
   // DIY-------------------------------------------------------
   
   
   
   @Override
   public double computeScore(INDArray labels, INDArray preOutput, IActivation actFn, INDArray mask, boolean average){
	   INDArray scoreArr = scoreArray(labels, preOutput, actFn, mask);
	   double score = scoreArr.sumNumber().doubleValue();
	   if (average)
		   score = score / scoreArr.size(0);
	   return score;
   }
   
   @Override
   public INDArray computeScoreArray(INDArray labels, INDArray preOutput, IActivation actFn, INDArray mask){
	   INDArray scoreArr = scoreArray(labels, preOutput, actFn, mask);
	   return scoreArr.sum(1);
   }

	@Override
	public Pair<Double, INDArray> computeGradientAndScore(INDArray labels, INDArray preOutput, IActivation actFn, INDArray mask,
			boolean average) {
		
		return new Pair<>(
				computeScore(labels, preOutput, actFn, mask,average),
				computeGradient(labels, preOutput, actFn, mask)
				);
	}
	
	@Override
	public String name() {
		return "RewardLoss";
		
	}

	public boolean equals(Object o) {
	    if (o == this) return true;
	    if (!(o instanceof PILoss)) return false;
	    final PILoss other = (PILoss) o;
	    if (!other.canEqual((Object) this)) return false;
	    return true;
	}
	
	public int hashCode() {
	    int result = 1;
	    return result;
	}
	
	protected boolean canEqual(Object other) {
	    return other instanceof PILoss;
	}
   
}
