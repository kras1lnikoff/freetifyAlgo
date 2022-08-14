package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public abstract class AbstractRecommenderSystem implements RecommenderSystem {

    protected final Data data;

    public AbstractRecommenderSystem(Data data) {
        this.data = data;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void update() {

    }

    @Override
    public abstract double estimateRating(Song song, User user);

    @Override
    public abstract double estimateSongSimilarity(Song first, Song second);

    @Override
    public List<Song> recommendSongs(User user, int size) {
        return sortSongs(song -> !data.isRated(song, user), song -> estimateRating(song, user), size);
    }

    @Override
    public List<Song> listSimilarSongs(Song song, int size) {
        return sortSongs(other -> !other.equals(song), other -> estimateSongSimilarity(song, other), size);
    }

    private List<Song> sortSongs(Predicate<Song> filter, ToDoubleFunction<Song> sorter, int size) {
        Comparator<Song> comparator = Comparator.comparingDouble(song -> -sorter.applyAsDouble(song));
        return data.allSongs().stream().filter(filter).sorted(comparator).limit(size).toList();
    }
}