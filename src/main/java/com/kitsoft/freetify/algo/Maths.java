package com.kitsoft.freetify.algo;

public final class Maths {

    public static double cosineSimilarity(int[] a, int[] b) {
        return dotProduct(a, b) / Math.sqrt(magnitudeSq(a) * magnitudeSq(b));
    }

    public static int dotProduct(int[] a, int[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }

    public static int magnitudeSq(int[] a) {
        return dotProduct(a, a);
    }

    public static double mean(int[] a) {
        int sum = 0;
        for (int x : a) sum += x;
        return (double) sum / a.length;
    }

}
