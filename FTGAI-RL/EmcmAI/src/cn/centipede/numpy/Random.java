package cn.centipede.numpy;

import java.math.BigDecimal;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import cn.centipede.Config;

/***
 * random inner class similar to numpy.random
 * @author simbaba
 */
public class Random {
    static java.util.Random random = new java.util.Random(System.currentTimeMillis());

    /** set random seed, we can reproduce the output */
    public static void seed(long r) {
        random.setSeed(r);
    }

    public static NDArray uniform(double start, double end, int...dimens) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        DoubleStream ds = random.doubles(start, end);
        double[] array = ds.limit(size).toArray();
        return new NDArray(array, dimens);
    }

    public static NDArray standard_normal(int... dimens) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        DoubleStream ds = DoubleStream.generate(random::nextGaussian);
        double[] array = ds.limit(size).toArray();
        return new NDArray(array, dimens);
    }

    /**
     * N[0,1] - N(m, r^2) => x= ry+m
     */
    public static NDArray standard_normal(double start, double end, int... dimens) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        DoubleStream ds = DoubleStream.generate(()->Math.sqrt(start)*random.nextGaussian()+end);
        double[] array = ds.limit(size).toArray();
        return new NDArray(array, dimens);
    }

    /**
     * @param top
     * @return [0, top)
     */
    public static int randint(int top) {
        return random.nextInt(top);
    }

    /**
     * random ints between [0, top)
     * @param top
     * @param size
     * @return randome choices
     */
    public static int[] choice(int top, int size) {
        return random.ints(0, top).limit(size).toArray();
    }

    /**
     * shuffle number list in place
     * @param numbers
     */
    public static int[] shuffle(int[] numbers) {
        for (int i = numbers.length; i > 0; i--) {
            int r = random.nextInt(i);
            int temp = numbers[i - 1];
            numbers[i - 1] = numbers[r];
            numbers[r] = temp;
        }
        return numbers;
    }

    /** generate NDArray according dimens */
    public static NDArray rand(int... dimens) {
        int size = IntStream.of(dimens).reduce(1, (a, b) -> a * b);
        double[] array = new double[size];

        for (int i = 0; i < array.length; i++) {
            double d = random.nextDouble();
            BigDecimal bd=new BigDecimal(d);
            double d1=bd.setScale(Config.ROUND_LIMIT, BigDecimal.ROUND_HALF_UP).doubleValue();
            array[i] = d1;
        }
        return new NDArray(array, dimens);
    }
}