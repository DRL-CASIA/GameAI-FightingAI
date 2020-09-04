package cn.centipede.numpy;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Numpy extends NumpyBase{
    public static final int ALL = -999_999_999;
    public static final int newaxis = -999_999_998; // np.newaxis

    // tricky impl as python numpy
    public class np extends Numpy{}
    public class random extends Random{}

    public static NDArray zeros(int... dimens) {
        return zeros(dimens, double.class);
    }

    public static NDArray zeros(int[] dimens, Class<?> dtype) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        Object array = Array.newInstance(dtype, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, 0);
        }
        return new NDArray(array, dimens);
    }

    public static NDArray ones(int... dimens) {
        return ones(dimens, double.class);
    }

    public static NDArray ones(int[] dimens, Class<?> dtype) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        Object array = Array.newInstance(dtype, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, 1);
        }
        return new NDArray(array, dimens);
    }

    /**
     * arange(9) = [0,1,2,3,4,5,6,7,8]
     * @param top = 9
     * @param dtype int/double
     * @return
     */
    public static NDArray arange(int top, Class<?> dtype) {
        Object array = Array.newInstance(dtype, top);
        for (int i = 0; i < top; i++) {
            Array.set(array, i, i);
        }

        return new NDArray(array, top);
    }

    public static NDArray linspace(double start, double top, int count) {
        double[] array = new double[count];
        double distance = top - start;
        double step = distance/(count-1);

        for (int i = 0; i < count; i++) {
            array[i] = start + step*i;
        }

        return new NDArray(array);
    }

    public static NDArray binaryOp(NDArray src, Object dat, String op) {
        Object srcData = getArrayData(src);
        Object datArray = flattenDat(dat);
        Object ret = Operator.binaryOp(srcData, datArray, op);
        return new NDArray(ret, src.shape());
    }

    public static NDArray add(NDArray src, Object dat) {
        return binaryOp(src, dat, "+");
    }

    public static NDArray subtract(NDArray src, Object dat) {
        return binaryOp(src, dat, "-");
    }

    public static NDArray multiply(NDArray src, Object dat) {
        return binaryOp(src, dat, "*");
    }

    public static NDArray divide(NDArray src, Object dat) {
        return binaryOp(src, dat, "/");
    }

    public static NDArray pow(NDArray src, int pow) {
        return binaryOp(src, pow, "**");
    }

    public static NDArray negate(NDArray src) {
        Object srcData = getArrayData(src);

        if (src.isInt()) {
            srcData = IntStream.of((int[])srcData).map(n->-n).toArray();
        } else {
            srcData = DoubleStream.of((double[])srcData).map(n->-n).toArray();
        }

        return new NDArray(srcData,  src.shape());
    }

    public static NDArray exp(NDArray src) {
        Object srcData = getArrayData(src);

        if (src.isInt()) {
            srcData = IntStream.of((int[])srcData).mapToDouble(n->Math.exp(n)).toArray();
        } else {
            srcData = DoubleStream.of((double[])srcData).map(n->Math.exp(n)).toArray();
        }

        return new NDArray(srcData,  src.shape());
    }

    public static NDArray log(NDArray src) {
        Object srcData = getArrayData(src);

        if (src.isInt()) {
            srcData = IntStream.of((int[])srcData).mapToDouble(n->Math.log(n)).toArray();
        } else {
            srcData = DoubleStream.of((double[])srcData).map(n->Math.log(n)).toArray();
        }
        return new NDArray(srcData,  src.shape());
    }

    public static NDArray abs(NDArray src) {
        Object srcData = getArrayData(src);

        if (src.isInt()) {
            srcData = IntStream.of((int[])srcData).map(n->n>=0?n:-n).toArray();
        } else {
            srcData = DoubleStream.of((double[])srcData).map(n->n>=0?n:-n).toArray();
        }
        return new NDArray(srcData,  src.shape());
    }

    /**
     * @return average of dat array
     */
    public static double mean(NDArray src) {
        Object srcData   = getArrayData(src);

        if (src.isInt()) {
            return IntStream.of((int[])srcData).average().getAsDouble();
        } else {
            return DoubleStream.of((double[])srcData).average().getAsDouble();
        }
    }

    /**
     * @return 1/src
     */
    public static NDArray reciprocal(NDArray src) {
        Object srcData = getArrayData(src);

        if (src.isInt()) {
            srcData = IntStream.of((int[])srcData).mapToDouble(n->1/n).toArray();
        } else {
            srcData = DoubleStream.of((double[])srcData).map(n->1/n).toArray();
        }
        return new NDArray(srcData,  src.shape());
    }

    /**
     * Now only support 1-2 dimens
     * @param a number/vector/matrix
     * @param b number/vector/matrix
     * @return NDArray
     */
    public static NDArray dot(NDArray a, NDArray b) {
        int[] aDim = a.shape(), bDim = b.shape();
        Object aData = a.data(), bData = b.data();

        /** a is number */
        if (aDim.length == 0) {
            if (aData instanceof Integer)return dot(b, (int)aData);
            else return dot(b, (double)aData);
        }

        /** b is number */
        if (bDim.length == 0) {
            if (bData instanceof Integer)return dot(a, (int)bData);
            else return dot(a, (double)bData);
        }

        /** a is array but not a vector */
        if (aDim.length == 1) {
            aDim = new int[]{1, aDim[0]};
        }

        int[] cDim;

        /** b is array but not a vector */
        if (bDim.length == 1) {
            bDim = new int[]{bDim[0], 1};
            cDim = new int[]{aDim[0]};
        } else {
            cDim = new int[]{aDim[0], bDim[1]};
        }

        aData = getArrayData(a);
        bData = getArrayData(b);

        /** normal dot operation */
        Object cData = Operator.dot(aData, aDim, bData, bDim);
        return new NDArray(cData, cDim);
    }

    public static NDArray dot(NDArray a, int b) {
        Object data = getArrayData(a);
        if (data instanceof int[]) {
            data = IntStream.of((int[])data).map(n->n*b).toArray();
        } else {
            data = DoubleStream.of((double[]) data).map(n->n*b).toArray();
        }
        return Numpy.array(data, a.shape());
    }

    public static NDArray dot(NDArray a, double b) {
        Object data = getArrayData(a);
        if (data instanceof int[]) {
            data = IntStream.of((int[])data).mapToDouble(n->n*b).toArray();
        } else {
            data = DoubleStream.of((double[]) data).map(n->n*b).toArray();
        }
        return Numpy.array(data, a.shape());
    }

    /**
     * src & dst share the same data buffer,
     * but own idata separately
     * @param idataSrc src data index
     * @param dimens dimens array
     * @return transpose data index
     */
    public static int[] transpose(int[] idataSrc, int[] dimens) {
        int[] index = new int[dimens.length];
        int[] idataRet = new int[idataSrc.length];
        int[][] dimensRet = new int[][]{dimens, ArrayHelper.reverse(dimens)};
        doTranspose(idataSrc, idataRet, index, 0, 0, dimensRet);
        return idataRet;
    }

    public static void set(NDArray array, Object data, int... index) {
        array.set(data, index);
    }

    public static NDArray arange(int top) {
        return arange(top, int.class);
    }

    /**
     * [[2,3,4],[4,5,6]] -> flatten
     * -> obj{[2,3,4,4,5,6], int[] dimens}
     * @param dimens multi-dimens
     * @return NDArray
     */
    public static NDArray array(Object data, int... dimens) {
        if (dimens.length > 0) {
            return new NDArray(data, dimens);
        }

        boolean isArray = data.getClass().isArray();
        if (!isArray) {
            return new NDArray(data);
        }

        Object[] object = ArrayHelper.flatten(data);
        return new NDArray(object[1], (int[]) object[0]);
    }

    /**
     * Matrix is special NDArray
     */
    public static Matrix matrix(Object data) {
        NDArray array = array(data);
        return new Matrix(array);
    }

    /**
     * Now only support axis = 0, two operands !!
     * @param a - 1xn
     * @param b - 1xn
     * @return merge a & b to be a 2xn
     */
    public static NDArray concatenate(NDArray a, NDArray b) {
        return concatenate(a, b, 0);
    }

    public static NDArray concatenate(NDArray a, NDArray b, int axis) {
        if (axis != 0) {
            return mergeArray(a, b, axis);
        }

        int[] adim = a.shape();
        int[] bdim = b.shape();

        if (adim.length > 1 && bdim.length == 1) {
            bdim = new int[]{1, bdim[0]};
        }

        int[] ndim;
        if (adim.length == 1) {
            ndim = new int[]{adim[0]+bdim[0]};
        } else {
            ndim = Arrays.copyOf(adim, adim.length);
            ndim[0] = adim[0] + bdim[0];
        }

        Object aArray = getArrayData(a);
        Object bArray = getArrayData(b);
        Object cArray = ArrayHelper.mergeArray(aArray, a.isInt(), bArray, b.isInt());
        return new NDArray(cArray, ndim);
    }

    public static NDArray stack(int axis, NDArray... arrays) {
        return stack(arrays, axis);
    }

    public static NDArray stack(NDArray[] arrays, int axis) {
        int[] dimens_o = arrays[0].shape();
        if (dimens_o.length == 1 && axis == 1) {
            //Stream.of(arrays).forEach(it->it.reshape(ALL, np.newaxis));
            for (int i = 0; i < arrays.length; i++) arrays[i] = arrays[i].reshape(ALL, np.newaxis);
        }

        NDArray ret = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            ret = concatenate(ret, arrays[i], axis);
        }

        int[] dimens_d = new int[dimens_o.length+1];
        for (int i = 0; i < axis; i++) {
            dimens_d[i] = dimens_o[i];
        }

        dimens_d[axis]=arrays.length;
        for (int i = axis; i < dimens_o.length; i++) {
            dimens_d[i+1] = dimens_o[i];
        }

        return ret.reshape(dimens_d);
    }

    public static NDArray hstack(NDArray a, NDArray b) {
        return concatenate(a, b, a.shape().length>1?1:0);
    }

    public static NDArray vstack(NDArray a, NDArray b) {
        NDArray ret = concatenate(a, b, 0);
        int[] dimens = a.shape();
        return dimens.length>1?ret:ret.reshape(2, dimens[0]);
    }

    /**
     * WANING: axis != 0, a concatenate b
     * internal use only.
     */
    static NDArray mergeArray(NDArray a, NDArray b, int axis) {
        int[] adim = a.shape();
        NDArray[] rows = new NDArray[adim[0]];
        for (int i = 0; i < adim[0]; i++) {
            rows[i] = concatenate(a.row(i), b.row(i), axis-1);
        }

        NDArray ret = rows[0];

        if (adim[0] > 1) {
            //Stream.of(rows).map(it->it=it.reshape(np.newaxis, ALL));
            for (int i = 0; i < rows.length; i++) rows[i] = rows[i].reshape(np.newaxis, ALL);
            ret = Stream.of(rows).reduce(Numpy::concatenate).get();
        }

        if (adim[0] == 1 && axis > 0) {
            ret = ret.reshape(np.newaxis, ALL);
        }
        return ret;
    }

    public static double sum(NDArray array) {
        Object dat = getArrayData(array);
        if (array.isInt()) {
            return IntStream.of((int[])dat).sum();
        } else {
            return DoubleStream.of((double[])dat).sum();
        }
    }

    public static NDArray sum(NDArray array, int axis) {
        int[] index = new int[++axis];
        for (int i = 0; i < axis-1; i++) {
            index[i] = ALL;
        }

        int[] dimens = array.shape();

        NDArray ret = array.get(index);
        for (int i = 1; i < dimens[axis-1]; i++) {
            index[axis-1] = i;
            ret=ret.add(array.get(index));
        }
        return ret;
    }

    public static NDArray sum(NDArray array, int[] axies) {
        Arrays.sort(axies);

        for (int i = axies.length-1; i >= 0; i--) {
            array = sum(array, axies[i]);
        }
        return array;
    }

    public static int sumInt(NDArray array) {
        Object dat = getArrayData(array);
        return IntStream.of((int[])dat).sum();
    }

    public static boolean compare(NDArray src, NDArray dst) {
        if (!Arrays.equals(src.shape(), dst.shape())) {
            return false;
        }
        Object srcData = getArrayData(src);
        Object dstData = getArrayData(dst);
        if (!srcData.getClass().equals(dstData.getClass())) {
            return false;
        }

        if (srcData instanceof int[]) {
            return Arrays.equals((int[])srcData, (int[])dstData);
        } else {
            return Arrays.equals((double[])srcData, (double[])dstData);
        }
    }

    public static boolean same(NDArray src, NDArray dst, double slope) {
        if (!Arrays.equals(src.shape(), dst.shape())) {
            return false;
        }
        Object srcData = getArrayData(src);
        Object dstData = getArrayData(dst);
        if (!srcData.getClass().equals(dstData.getClass())) {
            return false;
        }

        if (srcData instanceof int[]) {
            int[] a = (int[])srcData, b = (int[])dstData;
            for (int i = 0; i < a.length; i++) if (Math.abs(a[i] - b[i]) > slope) return false;
            return true;
        } else {
            double[] a = (double[])srcData, b = (double[])dstData;
            for (int i = 0; i < a.length; i++) if (Math.abs(a[i] - b[i]) > slope) return false;
            return true;
        }
    }

    private static double parseDouble(String dat) {
        try {return Double.parseDouble(dat);
        } catch(NumberFormatException e){}
        return 0;
    }

    public static NDArray loadtxt(String fname, final String delimiter) {
        try (Stream<String> lines = Files.lines(Paths.get(fname))) {
            double[][] array = lines.skip(0)
                .map(s -> s.split(delimiter))
                .map(s->Arrays.stream(s).mapToDouble(Numpy::parseDouble).toArray())
                .toArray(double[][]::new);
            return np.array(array);
        } catch(IOException e) {
            System.err.println(e);
        }
        return null;
    }

    private static Object padData(NDArray array, int left, int right) {
        int len = array.size();
        Object data;
        if (array.isInt()) {
            data = new int[left+len+right];
        } else {
            data = new double[left+len+right];
        }
        System.arraycopy(np.getArrayData(array), 0, data, left, len);
        return data;
    }

    private static NDArray _pad(NDArray array, int[] pads) {
        int left = pads[0], right = pads[1];
        int[] dimens = array.shape();

        if (dimens.length == 1) {
            Object data = padData(array, left, right);
            return np.array(data, dimens[0]+left+right);
        }

        int cell_size = array.row(0).size();
        int[] pad_dimens = Arrays.copyOf(dimens, dimens.length);

        if (left != 0) {
            pad_dimens[0] = left;
            NDArray zeroLeft = np.zeros(new int[]{cell_size*left}, int.class).reshape(pad_dimens);
            array = np.vstack(zeroLeft, array);
        }

        if (right != 0) {
            pad_dimens[0] = right;
            NDArray zeroRight = np.zeros(new int[]{cell_size*right}, int.class).reshape(pad_dimens);
            array = np.vstack(array, zeroRight);
        }

        pads = Arrays.copyOfRange(pads, 2, pads.length);
        NDArray first = _pad(array.row(0), pads);

        for (int i = 1; i < array.shape()[0]; i++) {
            NDArray row = array.row(i);
            row = _pad(row, pads);
            first = vstack(first, row);
        }
        return first;
    }

    public static NDArray pad(NDArray array, int[][] pads) {
        int[] pads_ = new int[pads.length*2];
        for (int i = 0; i < pads.length; i++) {
            pads_[i*2] = pads[i][0];
            pads_[i*2+1] = pads[i][1];
        }

        int[] dimens = array.shape();
        NDArray ret = _pad(array, pads_);

        for (int i=0; i < dimens.length; i++) {
            dimens[i] += pads_[i*2];
            dimens[i] += pads_[i*2+1];
        }
        return ret.reshape(dimens);
    }

    public static NDArray pad(NDArray array, int[] pads) {
        int[] dimens = array.shape();
        int[] pads_ = Arrays.copyOfRange(pads, 0, dimens.length * 2);
        if (pads.length == 1) {
            pads_[1] = pads_[0];
        }

        for (int i = 2; i < pads_.length; i+=2) {
            pads_[i] = pads_[0];
            pads_[i+1] = pads_[1];
        }

        NDArray ret = _pad(array, pads_);
        for (int i=0; i < dimens.length; i++) {
            dimens[i] += pads_[i*2];
            dimens[i] += pads_[i*2+1];
        }
        return ret.reshape(dimens);
    }

    public static boolean next_dimen(int[] dimens, int[] iter) {
        boolean flag = true;
        int i=iter.length-1;

        while (i >= 0) {
            if (flag) iter[i]++;
            flag = iter[i] >= dimens[i];
            if (!flag) break;
            iter[i--] = 0;
        }
        return i >= 0;
    }

    /**
     * for swap axis, do swap data
     */
    private static int[] getOffset(int[] dimens, int[] iter, int axis1, int axis2) {
        int[] dimens_dst = Arrays.copyOf(iter, iter.length);
        int a1 = dataOffset(dimens, iter);

        int t = dimens_dst[axis1];
        dimens_dst[axis1] = dimens_dst[axis2];
        dimens_dst[axis2] = t;

        int[] dimens2 = Arrays.copyOf(dimens, dimens.length);
        t = dimens2[axis1];
        dimens2[axis1] = dimens2[axis2];
        dimens2[axis2] = t;
        int a2 = dataOffset(dimens2, dimens_dst);
        return new int[]{a1, a2};
    }

    private static void doSwap(int[] data, int[] data_dst, int[] dimens, int[] iter, int axis1, int axis2) {
        do {
            int[] offsets = getOffset(dimens, iter, axis1, axis2);
            ((int[])data_dst)[offsets[1]] = ((int[])data)[offsets[0]];
        } while(next_dimen(dimens, iter));
    }

    private static void doSwap(double[] data, double[] data_dst, int[] dimens, int[] iter, int axis1, int axis2) {
        do {
            int[] offsets = getOffset(dimens, iter, axis1, axis2);
            ((double[])data_dst)[offsets[1]] = ((double[])data)[offsets[0]];
        } while(next_dimen(dimens, iter));
    }

    private static void doSwap(Object data, Object data_dst, boolean isInt, int[] dimens, int[] iter, int axis1, int axis2) {
        if (isInt) {
            doSwap((int[])data, (int[])data_dst, dimens, iter, axis1, axis2);
        } else {
            doSwap((double[])data, (double[])data_dst, dimens, iter, axis1, axis2);
        }
    }

    public static NDArray swapaxes(NDArray a, int axis1, int axis2) {
        if (axis1 == 0 && axis2 == 1) {
            return a.T();
        }

        int[] dimens = a.shape();
        int[] iter = new int[dimens.length];
        Object data = getArrayData(a);
        Object data_dst = Array.newInstance(a.isInt()?int.class:double.class, a.size());
        doSwap(data, data_dst, a.isInt(), dimens, iter, axis1, axis2);

        int t = dimens[axis1];
        dimens[axis1] = dimens[axis2];
        dimens[axis2] = t;

        return np.array(data_dst, dimens);
    }

    public static NDArray flipud(NDArray a) {
        int[] dimens = a.shape();
        NDArray ret = a.row(dimens[0]-1);
        for (int i = dimens[0]-2; i >= 0; i--) {
            ret = concatenate(ret, a.row(i));
        }
        return ret.reshape(dimens);
    }

    public static NDArray rot90(NDArray a, int times, int[] axies) {
        NDArray ret = swapaxes(a, axies[0], axies[1]);
        return flipud(ret);
    }

    public static NDArray rot90(NDArray a, int times) {
        if (times < 0) {
            times += 4;
        }
        NDArray ret = a;
        while (times-->0) {
            ret=rot90(ret);
        }
        return ret;
    }

    public static NDArray rot90(NDArray a) {
        int[] dimens = a.shape();
        int[][] range = new int[2][];
        range[0] = new int[]{ALL};
        range[1] = new int[1];

        NDArray ret = a.get(range);
        for (int i = 1; i < dimens[1]; i++) {
            range[1][0] = i;
            ret = np.concatenate(a.get(range), ret);
        }

        if (a.shape().length == 2) {
            return ret.reshape(dimens[1], dimens[0]);
        }

        int temp = dimens[0];
        dimens[0] = dimens[1];
        dimens[1] = temp;

        return ret.reshape(dimens);
    }

    public static NDArray repeat(NDArray a, int repeats) {
        return repeat(a, new int[]{repeats}, 1, a.size()*repeats);
    }

    public static NDArray repeat(NDArray a, int repeats, int axis) {
        return repeat(a, new int[]{repeats}, axis);
    }

    private static NDArray repeat(NDArray a, int[] repeats, int block, int size) {
        Object data = a.isInt()? new int[size] :new double[size];
        Object src = getArrayData(a);
        int count = a.size()/block;
        int offset = 0;

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < repeats[i%repeats.length]; j++) {
                System.arraycopy(src, i*block, data, offset, block);
                offset += block;
            }
        }
        return array(data);
    }

    public static NDArray repeat(NDArray a, int[] repeats, int axis) {
        int[] dimens = Arrays.copyOf(a.shape(), a.shape().length);
        int[] size = new int[dimens.length];

        int index = dimens.length-1;
        size[index] = 1;

        while (--index >= 0) {
            size[index] = size[index+1]*dimens[index+1];
        }

        if (repeats.length == 1) {
            dimens[axis] = dimens[axis]*repeats[0];
        } else {
            dimens[axis] = IntStream.of(repeats).sum();
        }

        int length = 1;
        for (int i = 0; i < dimens.length; i++) {
            length *= dimens[i];
        }

        NDArray ret =repeat(a, repeats, size[axis], length);
        return ret.reshape(dimens);
    }

    private static int argmaxInt(NDArray a) {
        Object dat = getArrayData(a);
        int index = 0;
        int temp = ((int[])dat)[0];

        for (int i = 1; i < ((int[])dat).length; i++) {
            if (temp < ((int[])dat)[i]) {
                index = i;
                temp = ((int[])dat)[i];
            }
        }
        return index;
    }

    private static int argmaxDouble(NDArray a) {
        Object dat = getArrayData(a);
        int index = 0;
        double temp = ((double[])dat)[0];

        for (int i = 1; i < ((double[])dat).length; i++) {
            if (temp < ((double[])dat)[i]) {
                index = i;
                temp = ((double[])dat)[i];
            }
        }
        return index;
    }

    public static Object max(NDArray a) {
        Object dat = getArrayData(a);
        if (a.isInt()) {
            return IntStream.of((int[])dat).max().getAsInt();
        }
        return DoubleStream.of((double[])dat).max().getAsDouble();
    }

    public static int argmax(NDArray a) {
        return a.isInt()?argmaxInt(a):argmaxDouble(a);
    }

    private static NDArray max(NDArray a, int axis, boolean argmax) {
        int[] dimens = a.shape();
        int retSize = 1;

        for (int i = 0; i < dimens.length; i++) {
            if (i == axis) continue;
            retSize *= dimens[i];
        }

        int[] retDimens = new int[dimens.length-1];
        for (int i = 0, j = 0; i < dimens.length; i++) {
            if (i == axis) continue;
            retDimens[j++] = dimens[i];
        }

        int[] dimensEx = new int[dimens.length+1];
        System.arraycopy(dimens, 0, dimensEx, 1, dimens.length);
        dimensEx[0] = 1;

        int rows = dimensEx[axis+1];
        int blocks = 1;
        for (int i = 0; i <= axis; i++) {
            blocks *= dimensEx[i];
        }

        int bsize = 1;
        for (int i = axis+2; i < dimensEx.length; i++) {
            bsize *= dimensEx[i];
        }

        Object retData = a.isInt()||argmax ? new int[retSize]:new double[retSize];
        Object data = getArrayData(a);

        for (int b = 0; b < blocks; b++) {
            int retOffset = b*bsize;
            int blockOffset = retOffset*rows;
            System.arraycopy(data, blockOffset, retData, b*bsize, bsize);

            for (int i = 1; i < rows; i++) {
                int rowOffset = blockOffset+i*bsize;
                if (a.isInt()) {
                    for (int j = 0; j < bsize; j++) {
                        int retj = retOffset+j;
                        int rowj = rowOffset+j;
                        if (((int[])retData)[retj] < ((int[])data)[rowj]) {
                            ((int[])retData)[retj] = argmax?i:((int[])data)[rowj];
                        }
                    }
                } else {
                    for (int j = 0; j < bsize; j++) {
                        int retj = retOffset+j;
                        int rowj = rowOffset+j;
                        if (((double[])retData)[retj] < ((double[])data)[rowj]) {
                            if (argmax) ((int[])retData)[retj] = i;
                            else ((double[])retData)[retj] = ((double[])data)[rowj];
                        }
                    }
                }
            }
        }

        NDArray ret = np.array(retData);
        return retDimens.length>1?ret.reshape(retDimens):ret;
    }

    public static NDArray max(NDArray a, int axis) {
        return max(a, axis, false);
    }

    public static NDArray argmax(NDArray a, int axis) {
        return max(a, axis, true);
    }

    public static NDArray maximum(NDArray a, int v){
        int[] idata = a.dataIndex();
        int[] data = (int[])a.data();

        for (int i = 0; i < a.size(); i++) {
            if (data[idata[i]] < v) data[idata[i]] = v;
        }
        return a;
    }

    public static NDArray maximum(NDArray a, double v){
        int[] idata = a.dataIndex();
        double[] data = (double[])a.data();

        for (int i = 0; i < a.size(); i++) {
            if (data[idata[i]] < v) data[idata[i]] = v;
        }
        return a;
    }

    public static NDArray checkset(NDArray a, NDArray x, DoublePredicate check, double value) {
        int[] iadata = a.dataIndex();
        int[] ixdata = x.dataIndex();

        double[] adata = (double[])a.data();
        double[] xdata = (double[])x.data();

        for (int i = 0; i < a.size(); i++) {
            if (check.test(xdata[ixdata[i]])) adata[iadata[i]] = value;
        }
        return a;
    }
    public static NDArray checkset(NDArray a, NDArray x, IntPredicate check, int value) {
        int[] iadata = a.dataIndex();
        int[] ixdata = x.dataIndex();

        int[] adata = (int[])a.data();
        int[] xdata = (int[])x.data();

        for (int i = 0; i < a.size(); i++) {
            if (check.test(xdata[ixdata[i]])) adata[iadata[i]] = value;
        }
        return a;
    }
}