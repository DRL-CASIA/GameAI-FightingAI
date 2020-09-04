package cn.centipede.model;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class LossFunction {
    public static NDArray MES_loss(NDArray y_pre, NDArray y) {
        // loss = np.sum((y_pre - y) ** 2)
        NDArray loss = np.abs(y_pre.subtract(y));
        return loss;
    }

    public static NDArray Cross_loss(NDArray y_pre, NDArray y) {
        // NDArray loss = -np.sum(y*np.log(y_pre)+(1-y)*np.log(1-y_pre));
        NDArray loss = y.divide(y_pre).add(y.negative().add(1)).divide(y_pre.negative().add(1));
        return loss;
    }

    public static NDArray Softmax_cross_loss(NDArray y_pre, NDArray y) {
        NDArray softmax = np.exp(y_pre).divide(np.sum(np.exp(y_pre), 0));
        //loss = - np.sum(y * np.log(softmax))
        NDArray loss = softmax.subtract(y);
        return loss;
    }
}