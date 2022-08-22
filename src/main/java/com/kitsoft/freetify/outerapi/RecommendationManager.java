package com.kitsoft.freetify.outerapi;

import com.kitsoft.freetify.algo.AbstractRecommender;
import com.kitsoft.freetify.algo.struct.Matrix;

import java.util.*;
import java.util.function.Function;

public class RecommendationManager<U, I> {

    private final List<U> users;
    private final List<I> items;

    private final Map<U, Integer> userIndices;
    private final Map<I, Integer> itemIndices;

    private final AbstractRecommender recommender;
    private Matrix data;

    public RecommendationManager(List<U> users, List<I> items, List<Rating<U, I>> ratings, Function<Matrix, AbstractRecommender> generator) {
        this.users = new ArrayList<>(users);
        this.items = new ArrayList<>(items);
        this.userIndices = new HashMap<>();
        this.itemIndices = new HashMap<>();
        updateIndexMaps();
        initData(ratings);
        this.recommender = generator.apply(data);
        recommender.setData(data);
        recommender.init();
    }

    private void updateIndexMaps() {
        userIndices.clear();
        itemIndices.clear();
        for (int index = 0; index < users.size(); index++) userIndices.put(users.get(index), index);
        for (int index = 0; index < items.size(); index++) itemIndices.put(items.get(index), index);
    }

    private void initData(List<Rating<U, I>> ratings) {
        data = new Matrix(users.size(), items.size());
        for (Rating<U, I> rating : ratings) data.set(getUserID(rating.getUser()), getItemID(rating.getItem()), rating.getScore());
    }

    private int getUserID(U user) {
        Integer u = userIndices.get(user);
        if (u == null) throw new RuntimeException("For user: " + user);
        return u;
    }

    private int getItemID(I item) {
        Integer i = itemIndices.get(item);
        if (i == null) throw new RuntimeException("For item: " + item);
        return i;
    }

    public void initialize() {
        recommender.build();
    }

    public void putRating(U user, I item, double rating) {
        data.set(getUserID(user), getItemID(item), rating);
    }

    public void onInteraction(U user, I item) {
        recommender.update(getUserID(user), getItemID(item));
    }

    public void addUser(U user) {
        userIndices.put(user, users.size());
        users.add(user);
        updateData(data.grow(1, 0));
    }

    public void addItem(I item) {
        itemIndices.put(item, items.size());
        items.add(item);
        updateData(data.grow(0, 1));
    }

    private void updateData(Matrix newData) {
        this.data = newData;
        recommender.setData(newData);
        recommender.init();
    }

    public List<I> recommendItems(U user, int maxSize, boolean ignoreKnown) {
        return toItemList(recommender.recommendItems(getUserID(user), maxSize, ignoreKnown));
    }

    public List<I> similarItems(I item, int maxSize) {
        return toItemList(recommender.similarItems(getItemID(item), maxSize));
    }

    private List<I> toItemList(List<Integer> indices) {
        List<I> list = new ArrayList<>(indices.size());
        for (int index : indices) list.add(items.get(index));
        return list;
    }

}