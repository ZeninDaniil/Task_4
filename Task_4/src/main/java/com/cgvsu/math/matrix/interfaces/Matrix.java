package com.cgvsu.math.matrix.interfaces;

public interface Matrix {
    int getRows();
    int getCols();
    float get(int row, int col);
    Matrix multiply(Matrix other);
    float[] toArray();
}
