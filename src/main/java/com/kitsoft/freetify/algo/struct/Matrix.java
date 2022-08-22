package com.kitsoft.freetify.algo.struct;

public class Matrix {

    private int m;
    private int n;

    private Vector[] rows;
    private Vector[] columns;

    public Matrix(int m, int n) {
        init(m, n);
        for (int i = 0; i < m; i++) rows[i] = new Vector(n);
        for (int j = 0; j < n; j++) columns[j] = new Vector(m);
    }

    public Matrix(Matrix other) {
        init(other.m, other.n);
        for (int i = 0; i < m; i++) rows[i] = new Vector(other.rows[i]);
        for (int j = 0; j < n; j++) columns[j] = new Vector(other.columns[j]);
    }

    private void init(int m, int n) {
        this.m = m;
        this.n = n;
        rows = new Vector[m];
        columns = new Vector[n];
    }

    public int rows() {
        return m;
    }

    public int columns() {
        return n;
    }

    public double get(int i, int j) {
        return rows[i].get(j);
    }

    public void set(int i, int j, double value) {
        rows[i].set(j, value);
        columns[j].set(i, value);
    }

    public Vector getRow(int i) {
        return rows[i];
    }

    public Vector getColumn(int j) {
        return columns[j];
    }

    public int actualSize() {
        int sum = 0;
        if (m < n) for (int i = 0; i < m; i++) sum += rows[i].actualSize();
        else for (int j = 0; j < n; j++) sum += columns[j].actualSize();
        return sum;
    }

    public Matrix grow(int dm, int dn) {
        Matrix matrix = new Matrix(m + dm, n + dn);
        for (int i = 0; i < m; i++) matrix.rows[i].init(matrix.n, rows[i].map());
        for (int j = 0; j < n; j++) matrix.columns[j].init(matrix.m, columns[j].map());
        return matrix;
    }

}