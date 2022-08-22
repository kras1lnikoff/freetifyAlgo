package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.algo.struct.Matrix;
import com.kitsoft.freetify.algo.struct.TopPriorityQueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemKNN extends AbstractRecommender {

    private int neighbours;

    private Matrix similarity;

    private double[] magnitudeCache;

    public ItemKNN(Matrix data) {
        this(data, 100);
    }

    public ItemKNN(Matrix data, int neighbours) {
        super(data);
        setParameters(neighbours);
    }

    public void setParameters(int neighbours) {
        this.neighbours = neighbours;
    }

    @Override
    public void init() {
        similarity = new Matrix(items, items);
        magnitudeCache = new double[items];
        for (int i = 0; i < items; i++) magnitudeCache[i] = data.getColumn(i).magnitude();
    }

    @Override
    public void build() {
        for (int i = 0; i < items; i++) {
            Map<Integer, Double> map = buildSimilarityMap(i);
            if (neighbours <= 0) similarity.getRow(i).init(items, map);
            else for (Map.Entry<Integer, Double> e : TopPriorityQueue.fromValues(map, neighbours)) similarity.set(i, e.getKey(), e.getValue());
        }
    }

    @Override
    public void update(int user, int item) {
        data.set(user, item, 1.0);
        build();
    }

    @Override
    public double predict(int user, int item) {
        return data.getRow(user).dot(similarity.getRow(item));
    }

    @Override
    public List<Integer> similarItems(int item, int maxSize) {
        Map<Integer, Double> map = maxSize > neighbours && neighbours > 0 ? buildSimilarityMap(item) : similarity.getRow(item).map();
        return TopPriorityQueue.sortKeysByValues(map, neighbours);
    }

    private Map<Integer, Double> buildSimilarityMap(int i) {
        Map<Integer, Double> map = new HashMap<>();
        for (int j = 0; j < items; j++) {
            if (i == j || magnitudeCache[i] == 0 || magnitudeCache[j] == 0) continue;
            double dot = data.getColumn(i).dot(data.getColumn(j));
            if (dot != 0) map.put(j, dot / (magnitudeCache[i] * magnitudeCache[j]));
        }
        return map;
    }
}