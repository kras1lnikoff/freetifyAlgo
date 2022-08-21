package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.algo.struct.Matrix;
import com.kitsoft.freetify.algo.struct.TopPriorityQueue;

import java.util.*;

import static com.kitsoft.freetify.algo.Maths.*;

public final class BiasedSVD extends AbstractRecommender {

    private int features;

    private int maxIterations;
    private int maxIterationsOnline;
    private double learningRate;
    private double regularization;

    private double globalBias;
    private double[] userBias;
    private double[] itemBias;
    private double[][] userFeatures;
    private double[][] itemFeatures;

    private Random random;

    public BiasedSVD(Matrix data) {
        this(data, 75, 25, 1, 0.005, 0.01);
    }

    public BiasedSVD(Matrix data, int features, int maxIterations, int maxIterationsOnline, double learningRate, double regularization) {
        super(data);
        setFeatures(features);
        setParameters(maxIterations, learningRate, regularization);
    }

    public int getFeatures() {
        return features;
    }

    public void setFeatures(int features) {
        this.features = features;
        userBias = new double[users];
        itemBias = new double[items];
        userFeatures = new double[users][features];
        itemFeatures = new double[items][features];
        random = new Random();
        for (int f = 0; f < features; f++) {
            for (int u = 0; u < users; u++) userFeatures[u][f] = 0.1 * random.nextGaussian();
            for (int i = 0; i < items; i++) itemFeatures[i][f] = 0.1 * random.nextGaussian();
        }
    }

    public void setParameters(int maxIterations, double learningRate, double regularization) {
        this.maxIterations = maxIterations;
        this.learningRate = learningRate;
        this.regularization = regularization;
    }

    @Override
    public void initialize() {
        int actualSize = data.actualSize();
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int counter = 0; counter < actualSize; counter++) {
                int u = random.nextInt(users), size = data.getRow(u).actualSize();
                if (size == 0) continue;
                Map.Entry<Integer, Double> e = data.getRow(u).entryList().get(random.nextInt(size));
                update(u, e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public void updateOnline(int user, int item) {
        data.set(user, item, 1.0);
        List<Map.Entry<Integer, Double>> list = data.getRow(user).entryList();
        for (int iteration = 0; iteration < maxIterationsOnline; iteration++) {
            Collections.shuffle(list, random);
            for (Map.Entry<Integer, Double> e : list) update(user, e.getKey(), e.getValue());
        }
    }

    @Override
    public double predict(int user, int item) {
        return globalBias + userBias[user] + itemBias[item] + dotProduct(userFeatures[user], itemFeatures[item]);
    }

    @Override
    public List<Integer> similarItems(int i, int maxSize) {
        Map<Integer, Double> map = new HashMap<>();
        for (int j = 0; j < items; j++) if (i != j) map.put(j, cosineSimilarity(itemFeatures[i], itemFeatures[j]));
        return TopPriorityQueue.sortKeysByValues(map, maxSize);
    }

    @Override
    public double loss() {
        double loss = 0;
        for (int u = 0; u < users; u++) loss += magnitudeSq(userFeatures[u]);
        for (int i = 0; i < items; i++) loss += magnitudeSq(itemFeatures[i]);
        return super.loss() + regularization * loss;
    }

    private void update(int u, int i, double rating) {
        double error = rating - predict(u, i);
        globalBias += learningRate * error;
        userBias[u] += learningRate * (error - regularization * userBias[u]);
        itemBias[i] += learningRate * (error - regularization * itemBias[i]);
        for (int f = 0; f < features; f++) {
            double oldItemFeatures = itemFeatures[i][f], oldUserFeatures = userFeatures[u][f];
            userFeatures[u][f] += learningRate * (error * oldItemFeatures - regularization * oldUserFeatures);
            itemFeatures[i][f] += learningRate * (error * oldUserFeatures - regularization * oldItemFeatures);
        }
    }
}