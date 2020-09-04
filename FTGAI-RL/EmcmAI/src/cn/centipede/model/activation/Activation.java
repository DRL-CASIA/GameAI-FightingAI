package cn.centipede.model.activation;

import cn.centipede.numpy.NDArray;

public interface Activation {
    NDArray forward(NDArray x);
    NDArray backward(NDArray delta);
}