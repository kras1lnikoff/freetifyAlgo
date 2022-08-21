package com.kitsoft.freetify.algo.struct;

import java.util.*;

public class Vector {

    private int size;
    private Map<Integer, Double> map;

    public Vector(int size) {
        init(size, new HashMap<>());
    }

    public Vector(Vector other) {
        init(other.size, new HashMap<>(other.map));
    }

    public void init(int size, Map<Integer, Double> map) {
        this.size = size;
        this.map = map;
    }

    public int size() {
        return size;
    }

    public Map<Integer, Double> map() {
        return map;
    }

    public double get(int index) {
        return map.getOrDefault(index, 0.0D);
    }

    public void set(int index, double value) {
        if (value == 0.0D) map.remove(index);
        else map.put(index, value);
    }

    public int actualSize() {
        return map.size();
    }

    public boolean contains(int index) {
        return map.containsKey(index);
    }

    public Set<Map.Entry<Integer, Double>> entrySet() {
        return map.entrySet();
    }

    public List<Map.Entry<Integer, Double>> entryList() {
        return new ArrayList<>(map.entrySet());
    }

    public double magnitude() {
        return Math.sqrt(dot(this));
    }

    public double magnitudeSq() {
        return Math.sqrt(dot(this));
    }

    public double dot(Vector other) {
        if (map.size() > other.map.size()) return other.dot(this);
        double sum = 0;
        for (Map.Entry<Integer, Double> e : map.entrySet()) sum += e.getValue() * other.get(e.getKey());
        return sum;
    }

}
