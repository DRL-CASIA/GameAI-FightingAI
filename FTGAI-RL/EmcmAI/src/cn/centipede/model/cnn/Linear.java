package cn.centipede.model.cnn;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class Linear {
    NDArray x;
    NDArray W;
    NDArray b;
    NDArray W_gradient;
    NDArray b_gradient;

    public Linear(int inChannel, int outChannel) {
        init(inChannel, outChannel);
    }

    private void init(int inChannel, int outChannel) {
        double scale = Math.sqrt(inChannel/2);
        this.W = np.random.standard_normal(inChannel, outChannel).divide(scale);
        this.b = np.random.standard_normal(outChannel).divide(scale);
        this.W_gradient = np.zeros(inChannel, outChannel);
        this.b_gradient = np.zeros(outChannel);
    }

    public NDArray forward(NDArray x) {
        this.x = x;
        NDArray x_forward = np.dot(this.x, this.W).add(this.b);
        return x_forward;
    }

    public NDArray backward(NDArray delta, double learning_rate) {
        // 梯度计算
        int batch_size = this.x.shape()[0];
        this.W_gradient = np.dot(this.x.T(), delta).divide(batch_size); // bxin bxout
        this.b_gradient = np.sum(delta, 0).divide(batch_size);
        NDArray delta_backward = np.dot(delta, this.W.T()); // bxout inxout

        // 反向传播
        this.W = this.W.subtract(W_gradient.multiply(learning_rate));
        this.b = this.b.subtract(this.b_gradient.multiply(learning_rate));

        return delta_backward;
    }
}