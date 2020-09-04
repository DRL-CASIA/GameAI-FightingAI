package cn.centipede.numpy;

import Jama.QRDecomposition;
import Jama.SingularValueDecomposition;

/**
 * only support double
 */
public class Matrix extends NDArray {
    private Jama.Matrix handler;

    public Matrix(NDArray array) {
        super(array);
        handler = new Jama.Matrix((double[][]) Numpy.getArray(this));
    }

    public NDArray I() {
        double[][] array = handler.inverse().getArray();
        return Numpy.array(array);
    }

    public Matrix[] QR() {
        QRDecomposition qr = handler.qr();
        Matrix q = Numpy.matrix(qr.getQ().getArray());
        Matrix r = Numpy.matrix(qr.getR().getArray());
        Matrix h = Numpy.matrix(qr.getH().getArray());
        return new Matrix[]{q, r, h};
    }

    public Matrix[] svd() {
        SingularValueDecomposition svd = handler.svd();
        Matrix s = Numpy.matrix(svd.getS().getArray());
        Matrix u = Numpy.matrix(svd.getU().getArray());
        Matrix v = Numpy.matrix(svd.getV().getArray());
        return new Matrix[]{s, u, v};
    }

    /** Solve A*X = B
     @param B    right hand side
     @return     solution if A is square, least squares solution otherwise
     */
    public Matrix solve(Matrix B) {
        Jama.Matrix b = new Jama.Matrix((double[][]) Numpy.getArray(B));
        Jama.Matrix r = handler.solve(b);
        return Numpy.matrix(r.getArray());
    }

    public static Matrix random(int m, int n) {
        return Numpy.matrix(Jama.Matrix.random(m, n).getArray());
    }

    public double norm1() {
        return handler.norm1();
    }

    public double norm2() {
        return handler.norm2();
    }

    public int rank() {
        return handler.rank();
    }

    public double det() {
        return handler.det();
    }

    public double cond() {
        return handler.cond();
    }
}
