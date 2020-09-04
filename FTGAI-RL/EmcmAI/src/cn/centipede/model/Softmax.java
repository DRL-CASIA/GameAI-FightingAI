package cn.centipede.model;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class Softmax {
    private NDArray softmax;

    public NDArray loss(NDArray predict, NDArray label) {
        int[] pdimens = predict.shape();
        int batchsize = pdimens[0];

        predict(predict);

        NDArray delta = np.zeros(pdimens);
        double loss = 0;

        for (int i=0; i < batchsize; i++) {
            NDArray label_i = label.row(i);
            NDArray softmanx_i = this.softmax.row(i);
            delta.set(softmanx_i.subtract(label_i), i);
            loss -= np.sum(np.log(softmanx_i).multiply(label_i));
        }

        loss /= batchsize;
        System.out.printf("Softmax: loss=%f\n", loss);
        return delta;
    }

    public NDArray predict(NDArray predict) {
        int[] pdimens = predict.shape();
        int batchsize = pdimens[0];

        softmax = np.zeros(predict.shape());

        for (int  i=0; i < batchsize; i++) {
            NDArray predict_tmp = predict.row(i).subtract(np.max(predict.row(i)));
            predict_tmp = np.exp(predict_tmp);
            softmax.set(predict_tmp.divide(np.sum(predict_tmp)), i);
        }
        return softmax;
    }
}