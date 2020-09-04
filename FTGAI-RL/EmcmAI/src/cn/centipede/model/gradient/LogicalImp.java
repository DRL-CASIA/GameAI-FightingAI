package cn.centipede.model.gradient;

import cn.centipede.model.activation.Activation;
import cn.centipede.model.activation.Sigmoid;
import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class LogicalImp {

    public static double loss(NDArray x, NDArray y, NDArray w) {
    int m = x.shape()[0];
    Activation sigmoid = new Sigmoid();
    NDArray h = sigmoid.forward(np.dot(x, w));

    // np.sum(yMat.T * np.log(hypothesis) + (1 - yMat).T * np.log(1 - hypothesis))
    // yMat.T * np.log(hypothesis)
    NDArray left = y.T().dot(np.log(h));
    NDArray right = (y.negative().add(1)).T().multiply(np.log(h.negative().add(1)));
    NDArray ret = left.add(right);

    double cost = (-1.0 / m) * np.sum(ret);
        return cost;
    }

    public static NDArray BGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        // TODO Auto-generated method stub
        return null;
    }

    public static NDArray SGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        // TODO Auto-generated method stub
        return null;
    }

    public static NDArray MBGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        // TODO Auto-generated method stub
        return null;
    }
}