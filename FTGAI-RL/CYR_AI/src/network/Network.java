package network;

import java.util.ArrayList;

public class Network {
	ArrayList<Layer> model;
	public Network(){
		model = new ArrayList<Layer>();
	}
	/**
	 * add layer
	 * @param layer
	 */
	public void addLayer(Layer layer){
		model.add(layer);
	}
	/**
	 * forward propagation
	 * @param inputs
	 * @return outputs
	 */
	public double[][] forward(double[][] inputs){
		double[][] outputs = null;
		
		for(int i=0; i<model.size(); i++){
			Layer layer = model.get(i);
			if(i==0){
				layer.setInputs(inputs);
			}
			layer.forward();
			outputs = layer.getOutputs_T();
			
			//sets the next layer input
			if(model.size()>i+1){
				model.get(i+1).setInputs(outputs);
			}
		}
		
		return outputs;
	}
}
