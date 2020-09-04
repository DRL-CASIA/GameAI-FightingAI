package cn.centipede.model.gradient;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class LinearImp {

    public static double loss(NDArray x, NDArray y, NDArray w) {
        NDArray diff = x.dot(w).subtract(y);
        double cost = np.dot(diff.T(), diff).asDouble();
        return cost/(x.shape()[0]*2);
    }

    public static NDArray BGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        int m = x.shape()[0];
        int n = x.shape()[1];
        NDArray w = np.ones(n);

        for (int i = 0; i < epochs; i++) {
            NDArray diff = np.dot(x, w).subtract(y);
            NDArray gradient = (np.dot(x.T(), diff)).divide(m);
            w = w.subtract(gradient.multiply(alpha));
        }
        return w;
    }

    public static NDArray SGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        int m = x.shape()[0];
        int n = x.shape()[1];
        NDArray w = np.ones(n);
        //int[] choice = np.random.choice(m, m);

        for (int i = 0; i < epochs; i++) {
            int idex = np.random.randint(m); // choice[i%m];
            NDArray row_x = x.row(idex).V();
            NDArray row_y = y.row(idex);
            NDArray diff = np.dot(row_x, w).subtract(row_y);
            NDArray gradient = np.dot(row_x.T(), diff);
            w = w.subtract(gradient.multiply(alpha));
            //if(i%100==0)System.out.println(loss(x, y, w));
        }
        return w;
    }

    public static NDArray MBGD(NDArray x, NDArray y, double alpha, int epochs, double epsilon) {
        int m = x.shape()[0];
        int n = x.shape()[1];
        NDArray w = np.ones(n);

        int batch_size = 5;

        for (int i = 0; i < epochs; i++) {
            int[] idex = np.random.choice(m, batch_size);
            NDArray rand = x.choice(idex);
            NDArray diff = np.dot(rand, w).subtract(y.choice(idex));
            NDArray gradient = np.dot(rand.T(), diff).divide(batch_size);
            w = w.subtract(gradient.multiply(alpha));
        }
        return w;
    }
}