package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.algo.struct.Matrix;
import com.kitsoft.freetify.algo.struct.TopPriorityQueue;

import java.util.*;

import static com.kitsoft.freetify.algo.Maths.*;

public final class FastALS extends AbstractRecommender {

    private int factors;
    private int maxIterations;
    private int maxIterationsOnline;
    private double regularization;
    private double updateWeight;
    private double coefficient;
    private double power;

    private double[][] userFeatures;
    private double[][] itemFeatures;

    private Matrix weight;
    private double[] itemWeight;

    private double[][] userCache;
    private double[][] itemCache;

    double[] cachedUserPredictions, cachedItemPredictions;
    double[] cachedUserRatings, cachedItemRatings;
    double[] cachedUserWeights, cachedItemWeights;

    public FastALS(Matrix data) {
        this(data, 75, 25, 1, 0.01, 1.0, 50, 0.5);
    }

    public FastALS(Matrix data, int factors, int maxIterations, int maxIterationsOnline,
                   double regularization, double updateWeight, double coefficient, double power) {
        super(data);
        setParameters(factors, maxIterations, maxIterationsOnline, regularization, updateWeight, coefficient, power);
    }

    public void setParameters(int factors, int maxIterations, int maxIterationsOnline,
                              double regularization, double updateWeight, double coefficient, double power) {
        this.factors = factors;
        this.maxIterations = maxIterations;
        this.maxIterationsOnline = maxIterationsOnline;
        this.regularization = regularization;
        this.updateWeight = updateWeight;
        this.coefficient = coefficient;
        this.power = power;
    }

    @Override
    public void init() {
        userFeatures = new double[users][factors];
        itemFeatures = new double[items][factors];
        Random random = new Random();
        for (int f = 0; f < factors; f++) {
            for (int u = 0; u < users; u++) userFeatures[u][f] = 0.1 * random.nextGaussian();
            for (int i = 0; i < items; i++) itemFeatures[i][f] = 0.1 * random.nextGaussian();
        }
        weight = new Matrix(users, items);
        for (int u = 0; u < users; u++) for (Map.Entry<Integer, Double> e : data.getRow(u).entrySet()) weight.set(u, e.getKey(), 1);
        double sum1 = 0, sum2 = 0;
        double[] p = new double[items];
        for (int i = 0; i < items; i++) sum1 += p[i] = data.getColumn(i).actualSize();
        for (int i = 0; i < items; i++) sum2 += p[i] = Math.pow(p[i] / sum1, power);
        itemWeight = new double[items];
        for (int i = 0; i < items; i++) itemWeight[i] = coefficient * p[i] / sum2;
        initCaches();
    }

    private void initCaches() {
        cachedUserPredictions = new double[users];
        cachedItemPredictions = new double[items];
        cachedUserRatings = new double[users];
        cachedItemRatings = new double[items];
        cachedUserWeights = new double[users];
        cachedItemWeights = new double[items];

        userCache = new double[factors][factors];
        for (int f = 0; f < factors; f++) {
            for (int g = 0; g <= f; g++) {
                double value = 0;
                for (int u = 0; u < users; u++) value += userFeatures[u][f] * userFeatures[u][g];
                userCache[f][g] = userCache[g][f] = value;
            }
        }
        itemCache = new double[factors][factors];
        for (int f = 0; f < factors; f++) {
            for (int g = 0; g < factors; g++) {
                double value = 0;
                for (int i = 0; i < items; i++) value += itemWeight[i] * itemFeatures[i][f] * itemFeatures[i][g];
                itemCache[f][g] = itemCache[g][f] = value;
            }
        }
    }

    @Override
    public void build() {
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int u = 0; u < users; u++) updateUser(u);
            for (int i = 0; i < items; i++) updateItem(i);
        }
    }

    @Override
    public void update(int u, int i) {
        data.set(u, i, 1);
        weight.set(u, i, updateWeight);
        if (itemWeight[i] == 0) {
            itemWeight[i] = coefficient / items;
            for (int f = 0; f < factors; f++) {
                for (int g = 0; g <= f; g++) {
                    double value = itemCache[f][g] + itemWeight[i] * itemFeatures[i][f] * itemFeatures[i][g];
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
                loss -= itemWeight[i] * prediction * prediction;
            }
            double[] product = new double[factors];
            for (int f = 0; f < factors; f++) product[f] = dotProduct(itemCache[f], userFeatures[u]);
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
            cachedItemPredictions[i] = predict(u, i);
            cachedItemRatings[i] = e.getValue();
            cachedItemWeights[i] = weight.get(u, i);
            list.add(i);
        }
        double[] oldFeatures = Arrays.copyOf(userFeatures[u], factors);
        for (int f = 0; f < factors; f++) {
            double numerator = 0, denominator = 0;
            for (int g = 0; g < factors; g++) if (g != f) numerator -= userFeatures[u][g] * itemCache[g][f];
            for (int i : list) {
                cachedItemPredictions[i] -= userFeatures[u][f] * itemFeatures[i][f];
                numerator += (cachedItemWeights[i] * cachedItemRatings[i]
                              - (cachedItemWeights[i] - itemWeight[i]) * cachedItemPredictions[i]) * itemFeatures[i][f];
                denominator += (cachedItemWeights[i] - itemWeight[i]) * itemFeatures[i][f] * itemFeatures[i][f];
            }
            denominator += itemCache[f][f] + regularization;
            userFeatures[u][f] = numerator / denominator;
            for (int i : list) cachedItemPredictions[i] += userFeatures[u][f] * itemFeatures[i][f];
        }
        for (int f = 0; f < factors; f++) {
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
            cachedUserPredictions[u] = predict(u, i);
            cachedUserRatings[u] = e.getValue();
            cachedUserWeights[u] = weight.get(u, i);
            list.add(i);
        }
        double[] oldFeatures = Arrays.copyOf(itemFeatures[i], factors);
        for (int f = 0; f < factors; f++) {
            double numerator = 0, denominator = 0;
            for (int g = 0; g < factors; g++) if (g != f) numerator -= itemFeatures[i][g] * userCache[g][f];
            numerator *= itemWeight[i];
            for (int u : list) {
                cachedUserPredictions[u] -= userFeatures[u][f] * itemFeatures[i][f];
                numerator += (cachedUserWeights[u] * cachedUserRatings[u]
                              - (cachedUserWeights[u] - itemWeight[i]) * cachedUserPredictions[u]) * userFeatures[u][f];
                denominator += (cachedUserWeights[u] - itemWeight[i]) * userFeatures[u][f] * userFeatures[u][f];
            }
            denominator += itemWeight[i] * userCache[f][f] + regularization;
            itemFeatures[i][f] = numerator / denominator;
            for (int u : list) cachedUserPredictions[u] += userFeatures[u][f] * itemFeatures[i][f];
        }
        for (int f = 0; f < factors; f++) {
            for (int g = 0; g <= f; g++) {
                double value = itemCache[f][g] + (itemFeatures[i][f] * itemFeatures[i][g]
                                                  - oldFeatures[f] * oldFeatures[g]) * itemWeight[i];
                itemCache[f][g] = itemCache[g][f] = value;
            }
        }
    }
}