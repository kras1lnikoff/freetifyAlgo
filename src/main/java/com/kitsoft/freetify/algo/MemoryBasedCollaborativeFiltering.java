package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.*;

import java.util.*;

public final class MemoryBasedCollaborativeFiltering extends AbstractRecommenderSystem {

    private final Map<Song, Double> cachedMean = new HashMap<>();
    private final Map<Map.Entry<Song, Song>, Double> cachedSongSimilarity = new HashMap<>();
    private final Map<Song, List<Song>> cachedMostSimilarSongs = new HashMap<>();

    public MemoryBasedCollaborativeFiltering(Data data) {
        super(data);
    }

    @Override
    public void update() {
        cachedMean.clear();
        cachedSongSimilarity.clear();
        cachedMostSimilarSongs.clear();
    }

    private double mean(Song i) {
        return cachedMean.computeIfAbsent(i, this::computeMean);
    }

    private double computeMean(Song i) {
        int sum = 0, count = 0;
        for (User u : data.allUsers()) {
            if (data.isRated(i, u)) count++;
            sum += data.getRating(i, u);
        }
        return count == 0 ? 0 : (double) sum / count;
    }

    private double computeSongSimilarity(Map.Entry<Song, Song> entry) {
        Song i = entry.getKey(), j = entry.getValue();
        double numerator = 0, magFirst = 0, magSecond = 0;
        for (User u : data.allUsers()) {
            if (!data.isRated(i, u) || !data.isRated(j, u)) continue;
            double x = data.getRating(i, u), y = data.getRating(j, u);
            numerator += x * y;
            magFirst += x * x;
            magSecond += y * y;
        }
        return magFirst == 0 || magSecond == 0 ? 0 : numerator / Math.sqrt(magFirst * magSecond);
    }

    private List<Song> mostSimilarSongs(Song i) {
        return cachedMostSimilarSongs.computeIfAbsent(i, this::computeMostSimilarSongs);
    }

    private List<Song> computeMostSimilarSongs(Song i) {
        return listSimilarSongs(i, 25);
    }

    @Override
    public double estimateRating(Song i, User u) {
        double numerator = 0, denominator = 0;
        for (Song j : mostSimilarSongs(i)) {
            if (!data.isRated(j, u)) continue;
            double sim = estimateSongSimilarity(i, j);
            numerator += sim * (data.getRating(j, u) - mean(j));
            denominator += Math.abs(sim);
        }
        return mean(i) + (denominator == 0 ? 0 : numerator / denominator);
    }

    @Override
    public double estimateSongSimilarity(Song first, Song second) {
        if (first.equals(second)) return 1;
        return cachedSongSimilarity.computeIfAbsent(Map.entry(first, second), this::computeSongSimilarity);
    }
}