package cn.centipede.numpy;

import cn.centipede.Config;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

class ArrayHelper {

    static Object mergeArray(int[] a, int[] b) {
        int[] ret = new int[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = a[i];
        }
        for (int i = a.length; i < ret.length; i++) {
            ret[i] = b[i-a.length];
        }
        return ret;
    }

    static Object mergeArray(double[] a, double[] b) {
        double[] ret = new double[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = a[i];
        }
        for (int i = a.length; i < ret.length; i++) {
            ret[i] = b[i-a.length];
        }
        return ret;
    }

    /**
     * axis = 0, do -> a append b
     */
    static Object mergeArray(Object a, boolean aInt, Object b, boolean bInt) {
        Object ret;

        if (aInt && bInt) {
            ret = mergeArray((int[])a, (int[])b);
        } else if (aInt){
            double[] aNew = IntStream.of((int[])a).asDoubleStream().toArray();
            ret = mergeArray(aNew, (double[])b);
        } else if (bInt) {
            double[] bNew = IntStream.of((int[])b).asDoubleStream().toArray();
            ret = mergeArray((double[])a, bNew);
        } else {
            ret = mergeArray((double[])a, (double[])b);
        }
        return ret;
    }

    /**
     * @param array rows string
     * @param spaces indent array
     * @return array to string
     */
    private static String mergeRowsWithIndent(String[] array, int[] dimens, int rows, int[] spaces) {
        StringBuilder sb = new StringBuilder("array(");
        sb.append(array[0]);

        for (int i = 1; i < rows; i++) {
            boolean group = i % dimens[dimens.length-2] == 0;
            sb.append(group?"\n\n":"\n");

            int indent = spaces[0]-spaces[i];
            String fmt = "%" + indent + "s%s";
            sb.append(String.format(fmt, "", array[i]));
        }
        return sb.append(")\n").toString();
    }

    private static String getIndent(Object data, int size) {
        int maxLen = 0;
        for (int i = 0; i < size; i++) {
            String item = Array.get(data, i).toString();
            if (maxLen < item.length()) {
                maxLen = item.length();
            }
        }
        return "%-"+ maxLen +"s";
    }

    /**
     * row separated according to the last dimension
     * ex: [[2,3,4],[3,4,5]] -> [2,3,4], [3,4,5]
     * @param row which row
     * @return row string
     */
    private static String getRowString(Object data, int[] dimens, int[] idata, int row) {
        Object array = getRowData(data, dimens, idata, row);
        int length = dimens[dimens.length-1];

        int size = 1;
        for (int dimen : dimens) {
            size *= dimen;
        }

        String indent = getIndent(data, size);
        String indentWithComma = indent + ", ";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            String fmt = i < length-1? indentWithComma:indent;
            String num = String.format(fmt, Array.get(array, i));
            sb.append (num);
        }
        return sb.append("]").toString();
    }

    private static Object getRowData(Object data, int[] dimens, int[] idata, int row) {
        int cell = dimens[dimens.length-1];
        int offset = cell * row;

        boolean isIntArray = data instanceof int[];
        Object object= Array.newInstance(isIntArray?int.class:double.class, cell);

        for (int i = 0; i < cell; i++) {
            Array.set(object, i, Array.get(data, idata[i+offset]));
        }
        return object;
    }

    /**
     * (2,3,4,5) -> (24, 12, 4)
     * check row index % 4, index % 12, index % 24
     * @param data array data
     * @param dimens array dimens
     * @param idata array data index
     * @return array string
     */
    static String getString(Object data, int[] dimens, int[] idata) {
        if (dimens.length == 0) {
            return String.format("array(%s)", data);
        }

        int rows  = idata.length/dimens[dimens.length-1];
        String[] rowStr = new String[rows];
        for (int i = 0; i < rows; i++) {
            rowStr[i] = getRowString(data, dimens, idata, i);
        }

        int[] spaces = new int[rows];
        int step = 1;

        for (int i = dimens.length-2; i >= 0; i--) {
            step *= dimens[i];
            for (int j = 0; j < rows; j++) {
                if (step == 1) { // special
                    rowStr[j] = "[" + rowStr[j] + "]";
                    continue;
                }
                int r = j%step+1;
                if (r == 1) {
                    rowStr[j] = "[" + rowStr[j];
                    spaces[j]++; // calc indent spaces
                } else if (r == step) {
                    rowStr[j] = rowStr[j] +  "]";
                }
            }
        }
        spaces[0] += Config.ARRAY_PREFIX_LEN;
        return mergeRowsWithIndent(rowStr, dimens, rows, spaces);
    }

    /**
     * WARN: Only support int/double
     * @param data multi-dimens int/double array
     * flatten the data to one-d array
     */
    private static int _flatten(Object data, Object array, int index) {
        int length = Array.getLength(data);

        if (data instanceof int[] || data instanceof double[]) {
            System.arraycopy(data, 0, array, index, length);
            return index+length;
        }

        int offset = index;
        for (int i = 0; i < length; i++) {
            offset = _flatten(Array.get(data, i), array, offset);
        }
        return offset;
    }

    /**
     * flatten array to one dimension
     * @param data int/double array
     * @return [dimens, flatten-array]
     */
    static Object[] flatten(Object data) {
        int[] dimens = new int[20]; // max=20
        Object o = data;
        int deep = 0;

        while(!(o instanceof int[] || o instanceof double[])) {
            dimens[deep] = Array.getLength(o);
            o = Array.get(o, 0);
            deep++;
        }
        dimens[deep] = Array.getLength(o);
        dimens = Arrays.copyOfRange(dimens, 0, deep+1);

        int size = 1;
        for (int dimen : dimens) {
            size *= dimen;
        }

        Object array = o instanceof int[]?new int[size]:new double[size];
        _flatten(data, array, 0);
        return new Object[]{dimens, array};
    }

    public static int[] reverse(int[] data) {
        int[] rdata = new int[data.length];
        for (int i= data.length-1; i >=0; i--) {
            rdata[i] = data[data.length-i-1];
        }
        return rdata;
    }

    static void _struct(Object array, Object data, int offset) {
        int len = Array.getLength(array);
        Object elem = Array.get(array, 0);

        if (!elem.getClass().isArray()) {
            System.arraycopy(data, offset, array, 0, len);
            return;
        }

        int dimens = Array.getLength(elem);
        for (int i = 0; i < len; i++) {
            _struct(Array.get(array, i), data, i*dimens);
        }
    }

    static Object struct(Object data, int[] dimens) {
        boolean isDouble = data.getClass().getTypeName().contains("double");
        return struct(data, dimens, isDouble?double.class:int.class);
    }

    private static Object struct(Object data, int[] dimens, Class<?> dtype) {
        Object array = Array.newInstance(dtype, dimens);

        if (dimens.length == 1) { // vector
            System.arraycopy(data, 0, array, 0, dimens[0]);
            return array;
        }

        for (int i = 0; i < dimens[0]; i++) {
            _struct(Array.get(array, i), data, i*dimens[1]);
        }
        return array;
    }
}
