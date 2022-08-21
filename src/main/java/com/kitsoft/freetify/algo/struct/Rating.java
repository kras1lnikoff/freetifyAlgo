package com.kitsoft.freetify.algo.struct;

public class Rating {

    private final int user;
    private final int item;
    private final double score;

    public Rating(int user, int item, double score) {
        this.user = user;
        this.item = item;
        this.score = score;
    }

    public int getUser() {
        return user;
    }

    public int getItem() {
        return item;
    }

    public double getScore() {
        return score;
    }
}
