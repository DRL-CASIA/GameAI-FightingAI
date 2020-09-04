package cn.centipede.model.activation;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

/**
 *  y = (e^x - e^-x)/(e^x + e^-x)
 *  dy/dx = 1 - y^2
 */
public class Tanh implements Activation{
    private NDArray x;

    @Override
    public NDArray forward(NDArray x) {
        NDArray ex = np.exp(x);
        NDArray ex_ = np.exp(x.negative());

        this.x = ex.subtract(ex_).divide(ex.add(ex_));
        return this.x;
    }

    /**
     * tanh`=1−𝑡𝑎𝑛ℎ(𝑥)^2
     */
    @Override
    public NDArray backward(NDArray delta) {
        return delta.multiply(np.pow(this.x,2).negative().add(1));
    }
}