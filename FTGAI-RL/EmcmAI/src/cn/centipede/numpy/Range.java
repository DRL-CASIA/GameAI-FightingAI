package cn.centipede.numpy;

public class Range {
    public static double[] arange(double start, double stop, double step) {
        int qty = (int)((stop - start)/step) + 1;
        double[] series = new double[qty];
        for (int i = 0; i < qty; i++) {
            series[i] = start + i * step;
        }
        return series;
    }

    public static double[] arange(double start, double stop) {
        return arange(start, stop, 1);
    }

    public static void main(String[] args) {
        double[] series = Range.arange(1, 10, 20);
        for (double d: series) {
            System.out.printf("%f,", d);
        }
    }
}