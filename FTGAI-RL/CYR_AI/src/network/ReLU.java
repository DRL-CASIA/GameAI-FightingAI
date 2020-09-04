package network;

public class ReLU extends ActivationFunction{
	public double func(double outputs){
		return Math.max(0,outputs);
	}
}

