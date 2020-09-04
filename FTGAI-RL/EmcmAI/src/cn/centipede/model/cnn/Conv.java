package cn.centipede.model.cnn;

import java.util.ArrayList;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class Conv {
    NDArray x;
    int pad = 0;
    int stride = 0;
    NDArray k;
    NDArray b;
    NDArray k_gradient;
    NDArray b_gradient;
    ArrayList<NDArray> image_col;

    public Conv(int[] kshape, int stride, int pad) {
        init(kshape, stride, pad);
    }

    public Conv(int[] kshape) {
        init(kshape, 1, 0);
    }

    private void init(int[] kshape, int stride, int pad) {
        int width = kshape[0], height = kshape[1];
        int in_channel = kshape[2], out_channel = kshape[3];
        this.stride = stride;
        this.pad = pad;

        double scale = Math.sqrt(3*in_channel*width*height/out_channel);
        this.k = np.random.standard_normal(kshape).divide(scale);
        this.b = np.random.standard_normal(out_channel).divide(scale);
        this.k_gradient = np.zeros(kshape);
        this.b_gradient = np.zeros(out_channel);

        this.image_col = new ArrayList<>();
    }

    public NDArray forward(NDArray X) {
        this.x = X;
        if (this.pad != 0) {
            int[][] pads = {{0,0},{this.pad,this.pad},{this.pad,this.pad},{0,0}};
            this.x = np.pad(this.x,pads);
        }

        int[] xshape = this.x.shape();
        int bx = xshape[0];
        int wx = xshape[1];

        int[] kshape = k.shape();
        int wk = kshape[0];
        int nk = kshape[3];

        int feature_w = (wx - wk) / this.stride + 1;
        NDArray feature = np.zeros(bx, feature_w, feature_w, nk);
        this.image_col.clear();

        NDArray kernel = this.k.reshape(-1, nk);
        for (int i = 0; i < bx; i++) {
            NDArray image_col = img2col(this.x.row(i), wk, this.stride);
            NDArray ifeature = (np.dot(image_col, kernel).add(this.b)).reshape(feature_w,feature_w,nk);
            feature.set(ifeature, i);
            this.image_col.add(image_col);
        }
        return feature;
    }

    public NDArray backward(NDArray delta, double learning_rate) {
        int[] xshape = this.x.shape(); // batch,14,14,inchannel
        int bx = xshape[0], wx = xshape[1], hx = xshape[2];
        int[] kshape = this.k.shape(); // 5,5,inChannel,outChannel
        int wk = kshape[0], hk = kshape[1], ck = kshape[2];
        int[] dshape = delta.shape();  // batch,10,10,outChannel
        int bd = dshape[0], hd = dshape[2], cd = dshape[3];

        // self.k_gradient,self.b_gradient
        NDArray delta_col = delta.reshape(bd, -1, cd);
        for (int i = 0; i < bx; i++) {
            this.k_gradient.add(np.dot(this.image_col.get(i).T(), delta_col.row(i)).reshape(this.k.shape()));
        }

        this.k_gradient = this.k_gradient.divide(bx);
        this.b_gradient = this.b_gradient.add(np.sum(delta_col, new int[]{0, 1}));
        this.b_gradient = this.b_gradient.divide(bx);

        // delta_backward
        NDArray delta_backward = np.zeros(xshape);
        NDArray k_180 = np.rot90(this.k, 2, new int[]{0,1});
        k_180 = np.swapaxes(k_180, 2, 3);
        NDArray k_180_col = k_180.reshape(-1, ck);

        NDArray pad_delta;
        if (hd-hk+1 != hx) {
            int pad = (hx-hd+hk-1) / 2;
            int[][] pads = {{0,0},{pad, pad},{pad, pad},{0,0}};
            pad_delta = np.pad(delta, pads);
        } else {
            pad_delta = delta;
        }

        for (int i = 0; i < bx; i++) {
            NDArray pad_delta_col = img2col(pad_delta.row(i), wk, this.stride);
            delta_backward.set(np.dot(pad_delta_col, k_180_col).reshape(wx,hx,ck), i);
        }

        this.k = this.k.subtract(this.k_gradient.multiply(learning_rate));
        this.b = this.b.subtract(this.b_gradient.multiply(learning_rate));
        return delta_backward;
    }

    public static NDArray img2col(NDArray x, int ksize, int stride) {
        int[] shape = x.shape();
        int wx = shape[0], cx = shape[2];
        int feature_w = (wx - ksize) / stride + 1;

        NDArray image_col = np.zeros(feature_w*feature_w, ksize*ksize*cx);
        int num = 0;

        for (int i = 0; i < feature_w; i++) {
            for (int j = 0; j < feature_w; j++) {
                int[][] range = {{i*stride,i*stride+ksize}, {j*stride,j*stride+ksize}};
                NDArray get = x.get(range).reshape(-1);
                np.set(image_col, get, num++);
            }
        }
        return image_col;
    }
}