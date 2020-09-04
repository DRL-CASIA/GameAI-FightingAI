package network;

public class ActivationFunction{
	public ActivationFunction(){
		
	}
	public double[][] runFunc(double[][] _outputs){
		double[][] outputs=_outputs.clone();
		
		for(int i=0; i<outputs.length; i++){
			for(int j=0;j<outputs[0].length; j++){
				outputs[i][j] = func(outputs[i][j]);
			}
		}
		
		return outputs;
	}
	public double func(double output){
		return output;
	}
}
