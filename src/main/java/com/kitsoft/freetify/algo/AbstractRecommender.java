package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.algo.struct.Matrix;
import com.kitsoft.freetify.algo.struct.Rating;
import com.kitsoft.freetify.algo.struct.TopPriorityQueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractRecommender {

    protected int users;
    protected int items;
    protected Matrix data;

    public AbstractRecommender(Matrix data) {
        setData(data);
    }

    public Matrix getData() {
        return data;
    }

    public void setData(Matrix data) {
        this.data = data;
        this.users = data.rows();
        this.items = data.columns();
    }

    public abstract void init();

    public abstract void build();

    public abstract void update(int user, int item);

    public abstract double predict(int user, int item);

    public abstract List<Integer> similarItems(int item, int maxSize);

    public List<Integer> recommendItems(int user, int maxSize, boolean ignoreKnown) {
        Map<Integer, Double> map = new HashMap<>();
        for (int i = 0; i < items; i++) map.put(i, predict(user, i));
        return TopPriorityQueue.sortKeysByValues(map, maxSize, ignoreKnownFilter(user, ignoreKnown));
    }

    public double loss() {
        double loss = 0;
        for (int u = 0; u < users; u++) {
            for (Map.Entry<Integer, Double> e : data.getRow(u).entrySet()) {
                double difference = predict(u, e.getKey()) - e.getValue();
                loss += difference * difference;
            }
        }
        return loss;
    }

    public boolean isHit(int user, int item, int maxSize, boolean ignoreKnown) {
        Map<Integer, Double> map = new HashMap<>();
        double maxScore = predict(user, item);
        int count = 0;
        for (int i = 0; i < items; i++) {
            double score = predict(user, i);
            map.put(i, predict(user, i));
            if (score > maxScore) count++;
            if (count > maxSize) return false;
        }
        return TopPriorityQueue.fromValues(map, maxSize, ignoreKnownFilter(user, ignoreKnown)).contains(Map.entry(item, maxScore));
    }

    private Predicate<Map.Entry<Integer, Double>> ignoreKnownFilter(int user, boolean ignoreKnown) {
        return ignoreKnown ? e -> !data.getRow(user).contains(e.getKey()) : null;
    }

    public double computeHitRate(List<Rating> ratings, int maxSize, boolean ignoreKnown) {
        int hits = 0;
        for (Rating rating : ratings) if (isHit(rating.getUser(), rating.getItem(), maxSize, ignoreKnown)) hits++;
        return (double) hits / ratings.size();
    }

    public double computeError(List<Rating> ratings) {
        double error = 0;
        for (Rating rating : ratings) {
            double difference = predict(rating.getUser(), rating.getItem()) - rating.getScore();
            error += difference * difference;
        }
        return Math.sqrt(error / ratings.size());
    }

}