package cn.centipede.model.cnn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import cn.centipede.model.activation.Relu;
import cn.centipede.model.Softmax;
import cn.centipede.model.data.MNIST;
import cn.centipede.npz.NpzFile;
import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

/**
 * Yes, I want to implement CCN oops, a little complex for me :)
 * input->hidden->output
 * 
 * @author simbaba
 * @version 0.0
 */
public class CNN {
    private Conv conv1 = new Conv(new int[]{5, 5, 1, 6});
    private Conv conv2 = new Conv(new int[]{5, 5, 6, 16});
    private Relu relu1 = new Relu();
    private Relu relu2 = new Relu();

    private Pool pool1 = new Pool();
    private Pool pool2 = new Pool();
    private Linear nn  = new Linear(256, 10);
    private Softmax softmax = new Softmax();


    /** let's any exception just go! */
    public void loadNpz(URL npzURL) {
        NpzFile npz;

        try (InputStream stream = npzURL.openStream()){
            npz = new NpzFile(stream);
        } catch (IOException e) {
           throw new RuntimeException(e.getMessage());
        }

        conv1.k = npz.get("k1");
        conv1.b = npz.get("b1");
        conv2.k = npz.get("k2");
        conv2.b = npz.get("b2");
        nn.W = npz.get("w3");
        nn.b = npz.get("b3");
    }

    public int predict(NDArray X) {
        X = X.reshape(np.newaxis, np.ALL);

        NDArray predict = conv1.forward(X);
        predict = relu1.forward(predict);
        predict = pool1.forward(predict);
        predict = conv2.forward(predict);
        predict = relu2.forward(predict);
        predict = pool2.forward(predict);
        predict = predict.reshape(1, -1);
        predict = nn.forward(predict);

        predict = softmax.predict(predict);
        return np.argmax(predict);
    }

    public NDArray onehot(NDArray targets, int num) {
        NDArray result = np.zeros(num, 10);
        for(int i = 0; i < num; i++) {
            result.set(1, i, targets.row(i).asInt());
        }
        return result;
    }

    public void train() {
        NDArray[] mnist = MNIST.numpy(true); // train=true
        NDArray targets = mnist[1];
        NDArray data = mnist[0].reshape(60000, 28, 28, 1).divide(255);
        targets = onehot(targets, 60000);

        conv1 = new Conv(new int[]{5,5,1,6});   // 24x24x6
        relu1 = new Relu();
        pool1 = new Pool();                     // 12x12x6
        conv2 = new Conv(new int[]{5,5,6,16});  // 8x8x16
        relu2 = new Relu();
        pool2 = new Pool();                     // 4x4x16
        nn = new Linear(256, 10);
        softmax = new Softmax();

        double lr = 0.01;
        int batch = 3;

        for (int epoch =0; epoch < 1; epoch++) {
            for (int i = 0; i < 6000; i+=batch) {
                NDArray X = data.get(new int[][]{{i, i+batch}});
                NDArray Y = targets.get(new int[][]{{i, i+batch}});

                NDArray predict = conv1.forward(X);
                predict = relu1.forward(predict);
                predict = pool1.forward(predict);
                predict = conv2.forward(predict);
                predict = relu2.forward(predict);
                predict = pool2.forward(predict);
                predict = predict.reshape(batch, -1);
                predict = nn.forward(predict);

                NDArray delta = softmax.loss(predict, Y);
                delta = nn.backward(delta, lr);
                delta = delta.reshape(batch, 4, 4, 16);
                delta = pool2.backward(delta);
                delta = relu2.backward(delta);
                delta = conv2.backward(delta, lr);
                delta = pool1.backward(delta);
                delta = relu1.backward(delta);
                conv1.backward(delta, lr);
            }
            lr *= Math.pow(0.95, epoch+1);
        }
    }

    public void eval() {
        NDArray[] mnist = MNIST.numpy(false); // train=false
        mnist[1].row(1).dump(); // lable

        NDArray test = mnist[0].reshape(10000, 28, 28, 1);
        // System.out.println(test.shape());
        // test.index(1).reshape(28,28).dump();

        NDArray img2col = Conv.img2col(test.get(1), 5, 1);
        System.out.println(img2col.shape());
    }

    public static void main(String[] args) {
        CNN cnn = new CNN();
        cnn.train();
    }

}