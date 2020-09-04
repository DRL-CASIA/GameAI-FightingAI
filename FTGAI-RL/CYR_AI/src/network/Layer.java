package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import util.Matrix2D;

public class Layer{
	double[][] inputs;
	double[][] weights;
	double[][] outputs;
	double[][] biass;
	ActivationFunction actF;
	/**
	 * Constructor
	 * @param input		Number of Input unit
	 * @param output	Number of Output unit
	 * @param actF		activation Function (in this sample ReLU or None)
	 */
	public Layer(int input,int output,ActivationFunction actF){
		inputs=new double[input][1];
		weights= new double[input][output];
		outputs=new double[output][1];
		biass=new double[output][1];
		this.actF=actF;
	}
	/**
	 * forward propagation
	 */
	public void forward(){
		Matrix2D m_inputs=new Matrix2D(inputs);
		Matrix2D m_weights=new Matrix2D(weights);
		Matrix2D m_biass = new Matrix2D(biass);
		Matrix2D m_outputs;
		
		
		m_outputs = Matrix2D.mult(m_inputs.T(),m_weights);
		m_outputs = Matrix2D.add(m_outputs, m_biass.T());
		this.outputs = m_outputs.getArrays();
		this.outputs = actF.runFunc(outputs);
	}
	
	/**
	 * Load Bias file 
	 * @param filename file path
	 */
	public Layer loadBias(String filename){
		File file = new File(filename);
		BufferedReader br=null;
		
		String str;
		try {
			br = new BufferedReader(new FileReader(file));
			while((str = br.readLine()) != null) {
				String[] tmp = str.split(",");
				System.out.println("LoadBiasFile:"+tmp.length);
				if(tmp.length==outputs.length){
					for (int i = 0; i < outputs.length; i++) {
						biass[i][0]=Double.valueOf(tmp[i]);
					}
				}else{
					System.out.println("ERROR: Bias file size is not match");
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return this;
	}
	/**
	 * Load Weight file 
	 * @param filename file path
	 */
	public Layer loadWeight(String filename){
		File file = new File(filename);
		BufferedReader br=null;
		
		String str;
		int count=0;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((str = br.readLine()) != null) {
				String[] tmp = str.split(",");
				System.out.println("LoadWeightFile:"+tmp.length+","+count);
				
				if(tmp.length==inputs.length){
					for (int i = 0; i < inputs.length; i++) {
						weights[i][count]=Double.valueOf(tmp[i]);
					}
				}else if(tmp.length==outputs.length){
					for (int i = 0; i < outputs.length; i++) {
						weights[count][i]=Double.valueOf(tmp[i]);
					}
				}else{
					System.out.println("ERROR: Weight file size is not match");
				}
				count++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return this;
	}
	
	/**
	 * @return transposed matrix of output
	 */
	public double[][] getOutputs_T(){
		return new Matrix2D(this.outputs).T().getArrays();
	}
	
	/**
	 * @return outputs
	 */
	public double[][] getOutputs(){
		return this.outputs;
	}
	
	public void setOutputs(double[][] outputs){
		this.outputs=outputs;
	}
	/**
	 * Set inputs
	 * @param inputs
	 */
	public void setInputs(double[][] inputs){
		this.inputs=inputs;
	}
	
}
