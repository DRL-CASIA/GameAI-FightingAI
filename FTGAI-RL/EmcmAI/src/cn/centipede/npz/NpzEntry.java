package cn.centipede.npz;

import java.util.Arrays;
import java.util.StringJoiner;

public class NpzEntry {
    private String name;
    private int[] shape;
    private Class<?> type;

    public NpzEntry(String name, Class<?> type, int[] shape) {
        this.name = name;
        this.shape = shape;
        this.type = type;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "NpzEntry{", "}");
        sj.add("name=" + name)
          .add("type=" + type)
          .add("shape=" + Arrays.toString(shape));
        return sj.toString();
    }
}