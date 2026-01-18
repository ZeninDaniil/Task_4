package com.cgvsu.math.vector.interfaces;

public interface Vector {

    Vector createVector(float... data);

    float getValueByIndex(int index);

    int getDimension();
}
