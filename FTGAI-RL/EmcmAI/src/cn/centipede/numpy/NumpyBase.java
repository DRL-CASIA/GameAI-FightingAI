package cn.centipede.numpy;

import java.lang.reflect.Array;
import java.util.stream.IntStream;


public class NumpyBase {

    /**
     * Get the offset of the index
     * NDArray use a one-dimens array
     */
    protected static int dataOffset(int[] dim, int[] index) {
        int len = dim.length;
        int s = 1, pos = 0;

        for (int i = len-1; i > 0; i--) {
            s *= dim[i];
            pos += s * index[i-1];
        }

        pos += index[len-1];
        return pos;
    }

    /**
     * data: NDArray, number, int[][][], double[][][]
     * genertic
     * convenrt to int[] or double[]
     */
    static Object flattenDat(Object data) {
        Object array;

        if (data instanceof NDArray) {
            array = getArrayData((NDArray) data);
        } else if (data.getClass().isArray()) {
            array = ArrayHelper.flatten(data)[1];
        } else {
            array = data instanceof Integer?new int[]{(int) data}:new double[]{(double)data};
        }
       return array;
    }

    /**
     * Get a real array from NDArray
     * NDArray uses a data buffer not a muti-dim array
     */
    public static Object getArray(NDArray array) {
        Object real = getArrayData(array);
        int[] dim = array.shape();
        return ArrayHelper.struct(real, dim);
    }

    /**
     * NDArray maybe a sparse array with idata
     * share data with other var
     * @param array
     * @return standalone data
     */
    public static Object getArrayData(NDArray array) {
        Object data = array.data();
        int[] index = array.dataIndex();

        if (data instanceof int[]) {
            return IntStream.of(index).map(i-> (int) Array.get(data, i)).toArray();
        } else if (data instanceof Integer) {
            return new int[]{(int)data};
        } else if (data instanceof Double) {
            return new double[]{(double)data};
        } else {
            return IntStream.of(index).mapToDouble(i-> (double) Array.get(data, i)).toArray();
        }
    }

    /**
     * transpose the index array
     * @param idataSrc src index array
     * @param idataDst dst index array
     * @param index data index
     * @param offset index offset
     * @param deep current dimens pos
     * @param dimens dimens array
     */
    static void doTranspose(int[] idataSrc, int[] idataDst, int[] index, int offset, int deep, int[][] dimens) {
        if (deep == dimens[0].length) { // do transpose
            int p1 = dataOffset(dimens[0], index);
            int p2 = dataOffset(dimens[1], ArrayHelper.reverse(index));
            idataDst[p2] = idataSrc[p1];
            return;
        }

        for (int i = 0; i < dimens[0][deep]; i++) {
            index[offset] = i;
            doTranspose(idataSrc, idataDst, index, offset+1, deep+1, dimens);
        }
    }

}

