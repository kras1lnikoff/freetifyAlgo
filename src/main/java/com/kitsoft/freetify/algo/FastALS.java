package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.algo.struct.Matrix;
import com.kitsoft.freetify.algo.struct.TopPriorityQueue;

import java.util.*;

import static com.kitsoft.freetify.algo.Maths.*;

public final class FastALS extends AbstractRecommender {

    private int features;

    private int maxIterations;
    private int maxIterationsOnline;
    private double regularization;

    private double[][] userFeatures;
    private double[][] itemFeatures;

    private Matrix weight;

    private double onlineWeight;
    private double coefficient;
    private double[] Wi;

    private double[][] userCache;
    private double[][] itemCache;

    double[] prediction_users, prediction_items;
    double[] rating_users, rating_items;
    double[] w_users, w_items;

    public FastALS(Matrix data) {
        this(data, 75, 25, 1, 0.01, 1.0, 50, 0.5);
    }

    public FastALS(Matrix data, int features, int maxIterations, int maxIterationsOnline,
                   double regularization, double onlineWeight, double coefficient, double alpha) {
        super(data);
        setFeatures(features);
        setParameters(maxIterations, maxIterationsOnline, regularization, onlineWeight, coefficient, alpha);
        setWeights(1.0);
        initCaches();
    }

    public void setFeatures(int features) {
        this.features = features;
        userFeatures = new double[users][features];
        itemFeatures = new double[items][features];
        Random random = new Random();
        for (int f = 0; f < features; f++) {
            for (int u = 0; u < users; u++) userFeatures[u][f] = 0.1 * random.nextGaussian();
            for (int i = 0; i < items; i++) itemFeatures[i][f] = 0.1 * random.nextGaussian();
        }
    }

    public void setParameters(int maxIterations, int maxIterationsOnline,
                              double regularization, double onlineWeight, double coefficient, double alpha) {
        this.maxIterations = maxIterations;
        this.maxIterationsOnline = maxIterationsOnline;
        this.regularization = regularization;
        this.onlineWeight = onlineWeight;
        this.coefficient = coefficient;
        double sum1 = 0, sum2 = 0;
        double[] p = new double[items];
        for (int i = 0; i < items; i++) sum1 += p[i] = data.getColumn(i).actualSize();
        for (int i = 0; i < items; i++) sum2 += p[i] = Math.pow(p[i] / sum1, alpha);
        Wi = new double[items];
        for (int i = 0; i < items; i++) Wi[i] = coefficient * p[i] / sum2;
    }

    public void setWeights(double value) {
        weight = new Matrix(users, items);
        for (int u = 0; u < users; u++) for (Map.Entry<Integer, Double> e : data.getRow(u).entrySet()) weight.set(u, e.getKey(), value);
    }

    private void initCaches() {
        prediction_users = new double[users];
        prediction_items = new double[items];
        rating_users = new double[users];
        rating_items = new double[items];
        w_users = new double[users];
        w_items = new double[items];

        userCache = new double[features][features];
        for (int f = 0; f < features; f++) {
            for (int g = 0; g <= f; g++) {
                double value = 0;
                for (int u = 0; u < users; u++) value += userFeatures[u][f] * userFeatures[u][g];
                userCache[f][g] = userCache[g][f] = value;
            }
        }
        itemCache = new double[features][features];
        for (int f = 0; f < features; f++) {
            for (int g = 0; g < features; g++) {
                double value = 0;
                for (int i = 0; i < items; i++) value += Wi[i] * itemFeatures[i][f] * itemFeatures[i][g];
                itemCache[f][g] = itemCache[g][f] = value;
            }
        }
    }

    @Override
    public void initialize() {
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int u = 0; u < users; u++) updateUser(u);
            for (int i = 0; i < items; i++) updateItem(i);
        }
    }

    @Override
    public void updateOnline(int u, int i) {
        data.set(u, i, 1);
        weight.set(u, i, onlineWeight);
        if (Wi[i] == 0) {
            Wi[i] = coefficient / items;
            for (int f = 0; f < features; f++) {
                for (int g = 0; g <= f; g++) {
                    double value = itemCache[f][g] + Wi[i] * itemFeatures[i][f] * itemFeatures[i][g];
                    itemCache[f][g] = itemCache[g][f] = value;
                }
            }
        }
        for (int iteration = 0; iteration < maxIterationsOnline; iteration++) {
            updateUser(u);
            updateItem(i);
        }
    }

    @Override
    public double predict(int user, int item) {
        return dotProduct(userFeatures[user], itemFeatures[item]);
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
        loss *= regularization;
        for (int u = 0; u < users; u++) {
            for (Map.Entry<Integer, Double> e : data.getRow(u).entrySet()) {
                int i = e.getKey();
                double prediction = predict(u, i), difference = prediction - e.getValue();
                loss += weight.get(u, i) * difference * difference;
                loss -= Wi[i] * prediction * prediction;
            }
            double[] product = new double[features];
            for (int f = 0; f < features; f++) product[f] = dotProduct(itemCache[f], userFeatures[u]);
            loss += dotProduct(product, userFeatures[u]);
        }
        return loss;
    }

    private void updateUser(int u) {
        Set<Map.Entry<Integer, Double>> entries = data.getRow(u).entrySet();
        if (entries.isEmpty()) return;
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : entries) {
            int i = e.getKey();
            prediction_items[i] = predict(u, i);
            rating_items[i] = e.getValue();
            w_items[i] = weight.get(u, i);
            list.add(i);
        }
        double[] oldFeatures = Arrays.copyOf(userFeatures[u], features);
        for (int f = 0; f < features; f++) {
            double numerator = 0, denominator = 0;
            for (int g = 0; g < features; g++) if (g != f) numerator -= userFeatures[u][g] * itemCache[g][f];
            for (int i : list) {
                prediction_items[i] -= userFeatures[u][f] * itemFeatures[i][f];
                numerator += (w_items[i] * rating_items[i] - (w_items[i] - Wi[i]) * prediction_items[i]) * itemFeatures[i][f];
                denominator += (w_items[i] - Wi[i]) * itemFeatures[i][f] * itemFeatures[i][f];
            }
            denominator += itemCache[f][f] + regularization;
            userFeatures[u][f] = numerator / denominator;
            for (int i : list) prediction_items[i] += userFeatures[u][f] * itemFeatures[i][f];
        }
        for (int f = 0; f < features; f++) {
            for (int g = 0; g <= f; g++) {
                double value = userCache[f][g] + userFeatures[u][f] * userFeatures[u][g] - oldFeatures[f] * oldFeatures[g];
                userCache[f][g] = userCache[g][f] = value;
            }
        }
    }

    private void updateItem(int i) {
        Set<Map.Entry<Integer, Double>> entries = data.getColumn(i).entrySet();
        if (entries.isEmpty()) return;
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : entries) {
            int u = e.getKey();
            prediction_users[u] = predict(u, i);
            rating_users[u] = e.getValue();
            w_users[u] = weight.get(u, i);
            list.add(i);
        }
        double[] oldFeatures = Arrays.copyOf(itemFeatures[i], features);
        for (int f = 0; f < features; f++) {
            double numerator = 0, denominator = 0;
            for (int g = 0; g < features; g++) if (g != f) numerator -= itemFeatures[i][g] * userCache[g][f];
            numerator *= Wi[i];
            for (int u : list) {
                prediction_users[u] -= userFeatures[u][f] * itemFeatures[i][f];
                numerator += (w_users[u] * rating_users[u] - (w_users[u] - Wi[i]) * prediction_users[u]) * userFeatures[u][f];
                denominator += (w_users[u] - Wi[i]) * userFeatures[u][f] * userFeatures[u][f];
            }
            denominator += Wi[i] * userCache[f][f] + regularization;
            itemFeatures[i][f] = numerator / denominator;
            for (int u : list) prediction_users[u] += userFeatures[u][f] * itemFeatures[i][f];
        }
        for (int f = 0; f < features; f++) {
            for (int g = 0; g <= f; g++) {
                double value = itemCache[f][g] + Wi[i] * (itemFeatures[i][f] * itemFeatures[i][g] - oldFeatures[f] * oldFeatures[g]);
                itemCache[f][g] = itemCache[g][f] = value;
            }
        }
    }
}