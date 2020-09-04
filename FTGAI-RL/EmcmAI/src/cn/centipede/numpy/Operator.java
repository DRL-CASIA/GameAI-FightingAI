package cn.centipede.numpy;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;


public class Operator {

    static double[] divide(int[] src, int[] data) {
        int length = data.length;
        double[] ret = new double[src.length];

        for (int i = 0; i < src.length; i++) {
            Integer left = src[i];
            Integer right = data[i%length];
            ret[i] = (double)left/right;
        }
        return ret;
    }

    static int[] doOp(int[] src, int[] data, BinaryOperator<Integer> op) {
        int length = data.length;
        int[] ret = new int[src.length];

        for (int i = 0; i < src.length; i++) {
            Integer left = src[i];
            Integer right = data[i%length];
            ret[i] = (Integer)op.apply(left, right);
        }
        return ret;
    }

    static double[] doOp(double[] src, double[] data, BinaryOperator<Double> op) {
        int length = data.length;
        double[] ret = new double[src.length];

        for (int i = 0; i < src.length; i++) {
            Double left = src[i];
            Double right = data[i%length];
            ret[i] = (Double)op.apply(left, right);
        }
        return ret;
    }

    static double[] doOp(int[] src, double[] data, BiFunction<Integer, Double, Double> op) {
        int length = data.length;
        double[] ret = new double[src.length];

        for (int i = 0; i < src.length; i++) {
            Integer left = src[i];
            Double right = data[i%length];
            ret[i] = op.apply(left, right);
        }
        return ret;
    }

    static double[] doOp(double[] src, int[] data, BiFunction<Double, Integer, Double> op) {
        int length = data.length;
        double[] ret = new double[src.length];

        for (int i = 0; i < src.length; i++) {
            Double left = src[i];
            Integer right = data[i%length];
            ret[i] = op.apply(left, right);
        }
        return ret;
    }

    static int[] binaryOp(int[] src, int[] dat, String op) {
        int[] ret;
        switch(op) {
        case "+" : ret = Operator.doOp(src, dat, (a,b)->a+b); break;
        case "-" : ret = Operator.doOp(src, dat, (a,b)->a-b); break;
        case "*" : ret = Operator.doOp(src, dat, (a,b)->a*b); break;
        case "//" : ret = Operator.doOp(src, dat, (a,b)->a/b); break;
        case "**": ret = Operator.doOp(src, dat, (a,b)->{while(b-->0)a*=a;return a;}); break;
        default: throw new RuntimeException("Not support operator!");
        }
        return ret;
    }

    static double[] binaryOp(double[] src, int[] dat, String op) {
        double[] ret;
        switch(op) {
        case "+" : ret = Operator.doOp(src, dat, (a,b)->a+b); break;
        case "-" : ret = Operator.doOp(src, dat, (a,b)->a-b); break;
        case "*" : ret = Operator.doOp(src, dat, (a,b)->a*b); break;
        case "/" : ret = Operator.doOp(src, dat, (a,b)->a/b); break;
        case "**": ret = Operator.doOp(src, dat, (a,b)->Math.pow(a,b)); break;
        default: throw new RuntimeException("Not support operator!");
        }
        return ret;
    }

    static double[] binaryOp(int[] src, double[] dat, String op) {
        double[] ret;
        switch(op) {
        case "+" : ret = Operator.doOp(src, dat, (a,b)->a+b); break;
        case "-" : ret = Operator.doOp(src, dat, (a,b)->a-b); break;
        case "*" : ret = Operator.doOp(src, dat, (a,b)->a*b); break;
        case "/" : ret = Operator.doOp(src, dat, (a,b)->a/b); break;
        case "**": ret = Operator.doOp(src, dat, (a,b)->Math.pow(a,b)); break;
        default: throw new RuntimeException("Not support operator!");
        }
        return ret;
    }

    static double[] binaryOp(double[] src, double[] dat, String op) {
        double[] ret;
        switch(op) {
        case "+" : ret = Operator.doOp(src, dat, (a,b)->a+b); break;
        case "-" : ret = Operator.doOp(src, dat, (a,b)->a-b); break;
        case "*" : ret = Operator.doOp(src, dat, (a,b)->a*b); break;
        case "/" : ret = Operator.doOp(src, dat, (a,b)->a/b); break;
        case "**": ret = Operator.doOp(src, dat, (a,b)->Math.pow(a,b)); break;
        default: throw new RuntimeException("Not support operator!");
        }
        return ret;
    }

    static Object binaryOp(double[] src, Object dat, String op) {
        if (dat instanceof int[]) {
            return binaryOp(src, (int[])dat, op);
        } else {
            return binaryOp(src, (double[])dat, op);
        }
    }

    static Object binaryOp(int[] src, Object dat, String op) {
        if (dat instanceof int[]) {
            return op.equals("/") ? divide(src, (int[])dat) : binaryOp(src, (int[])dat, op);
        } else {
            return binaryOp(src, (double[])dat, op);
        }
    }

    static Object binaryOp(Object src, Object dat, String op) {
        if (src instanceof int[]) {
            return binaryOp((int[])src, dat, op);
        } else {
            return Operator.binaryOp((double[])src, dat, op);
        }
    }

    static double[] dot(double[] aData, int[] aDim, double[] bData, int[] bDim) {
        double[] result = new double[aDim[0]*bDim[1]];
        for (int i = 0; i < aDim[0]; i++) {
            for (int j = 0; j < bDim[1]; j++) {
                for (int k = 0; k < aDim[1]; k++) {
                    result[bDim[1]*i+j] += aData[i*aDim[1]+k] * bData[k*bDim[1]+j];
                }
            }
        }
        return result;
    }

    static double[] dot(int[] aData, int[] aDim, double[] bData, int[] bDim) {
        double[] result = new double[aDim[0]*bDim[1]];
        for (int i = 0; i < aDim[0]; i++) {
            for (int j = 0; j < bDim[1]; j++) {
                for (int k = 0; k < aDim[1]; k++) {
                    result[bDim[1]*i+j] += aData[i*aDim[1]+k] * bData[k*bDim[1]+j];
                }
            }
        }
        return result;
    }

    static double[] dot(double[] aData, int[] aDim, int[] bData, int[] bDim) {
        double[] result = new double[aDim[0]*bDim[1]];
        for (int i = 0; i < aDim[0]; i++) {
            for (int j = 0; j < bDim[1]; j++) {
                for (int k = 0; k < aDim[1]; k++) {
                    result[bDim[1]*i+j] += aData[i*aDim[1]+k] * bData[k*bDim[1]+j];
                }
            }
        }
        return result;
    }

    static int[] dot(int[] aData, int[] aDim, int[] bData, int[] bDim) {
        int[] result = new int[aDim[0]*bDim[1]];
        for (int i = 0; i < aDim[0]; i++) {
            for (int j = 0; j < bDim[1]; j++) {
                for (int k = 0; k < aDim[1]; k++) {
                    result[bDim[1]*i+j] += aData[i*aDim[1]+k] * bData[k*bDim[1]+j];
                }
            }
        }
        return result;
    }

    static Object dot(int[] aData, int[] aDim, Object bData, int[] bDim) {
        if (bData instanceof int[]) {
            return dot(aData, aDim, (int[])bData, bDim);
        } else {
            return dot(aData, aDim, (double[])bData, bDim);
        }
    }

    static Object dot(double[] aData, int[] aDim, Object bData, int[] bDim) {
        if (bData instanceof int[]) {
            return dot(aData, aDim, (int[])bData, bDim);
        } else {
            return dot(aData, aDim, (double[])bData, bDim);
        }
    }

    static Object dot(Object aData, int[] aDim, Object bData, int[] bDim) {
        if (aData instanceof int[]) {
            return dot((int[])aData, aDim, bData, bDim);
        } else {
            return dot((double[])aData, aDim, bData, bDim);
        }
    }
}