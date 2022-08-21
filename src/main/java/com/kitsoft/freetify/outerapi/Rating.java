package com.kitsoft.freetify.outerapi;

public class Rating<U, I> {

    private final U user;
    private final I item;
    private final double score;

    public Rating(U user, I item, double score) {
        this.user = user;
        this.item = item;
        this.score = score;
    }

    public U getUser() {
        return user;
    }

    public I getItem() {
        return item;
    }

    public double getScore() {
        return score;
    }

}
