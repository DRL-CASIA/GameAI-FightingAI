package cn.centipede.model.data;

import java.io.IOException;
import java.io.InputStream;

import cn.centipede.numpy.NDArray;
import cn.centipede.numpy.Numpy.np;

public class MNIST {
    public static NDArray[] numpy(boolean train) {
        String[][] data = {
            {"data/train-images.idx3-ubyte", "data/train-labels.idx1-ubyte"},
            {"data/t10k-images.idx3-ubyte", "data/t10k-labels.idx1-ubyte"}
        };

        NDArray[] arrays = null;
        try {
            arrays = loadMnistData(data[train ? 0 : 1][0], data[train ? 0 : 1][1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrays;
    }

    private static NDArray[] loadMnistData(String images, String labels) throws IOException {
        MnistDataReader reader = new MnistDataReader();
        InputStream image= ClassLoader.getSystemClassLoader().getResourceAsStream(images);
        InputStream label = ClassLoader.getSystemClassLoader().getResourceAsStream(labels);
        return toNDArray(reader.readData(image, label));
    }

    /**
     * Asume image have same size
     * @param mnistMatrix
     * @return MNIST NDArray: data, target
     */
    private static NDArray[] toNDArray(MnistMatrix[] mnistMatrix) {
        int[] labels = new int[mnistMatrix.length];

        int rows = mnistMatrix[0].getNumberOfRows();
        int cols = mnistMatrix[0].getNumberOfColumns();
        int[][][] dataBf = new int[mnistMatrix.length][rows][cols];

        for (int i = 0; i < mnistMatrix.length; i++) {
            int[][] data = mnistMatrix[i].getData();
            labels[i] = mnistMatrix[i].getLabel();
            dataBf[i] = data;
        }

        NDArray data = np.array(dataBf);
        NDArray target = np.array(labels);
        return new NDArray[]{data, target};
    }
}