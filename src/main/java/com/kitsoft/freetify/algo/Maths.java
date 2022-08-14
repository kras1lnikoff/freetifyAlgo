package com.kitsoft.freetify.algo;

public final class Maths {

    public static double cosineSimilarity(double[] a, double[] b) {
        return dotProduct(a, b) / Math.sqrt(magnitudeSq(a) * magnitudeSq(b));
    }

    public static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

    public static double magnitudeSq(double[] a) {
        return dotProduct(a, a);
    }
}
