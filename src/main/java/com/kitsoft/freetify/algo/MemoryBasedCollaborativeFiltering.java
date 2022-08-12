package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.*;

import java.util.*;
import java.util.function.ToDoubleFunction;

public final class MemoryBasedCollaborativeFiltering implements RecommenderSystem {

    private final Data data;

    private int n, m;
    private int[][] rating;
    private double[] mean;
    private double[][] similarity;

    private Map<Song, Integer> songIndices;
    private Map<User, Integer> userIndices;

    public MemoryBasedCollaborativeFiltering(Data data) {
        this.data = data;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void update() {
        List<Song> songs = data.allSongs();
        List<User> users = data.allUsers();
        rating = new int[n = songs.size()][m = users.size()];
        for (int i = 0; i < n; i++) for (int u = 0; u < m; u++) rating[i][u] = data.getRating(songs.get(i), users.get(u));
        mean = new double[n];
        for (int i = 0; i < n; i++) mean[i] = computeMean(i);
        similarity = new double[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) similarity[i][j] = computeCorrelationCoefficient(i, j);
        songIndices = new HashMap<>();
        for (int i = 0; i < n; i++) songIndices.put(songs.get(i), i);
        userIndices = new HashMap<>();
        for (int u = 0; u < m; u++) userIndices.put(users.get(u), u);
    }

    private double computeMean(int i) {
        int sum = 0, count = 0;
        for (int u = 0; u < m; u++) {
            if (rating[i][u] != 0) count++;
            sum += rating[i][u];
        }
        return (double) sum / count;
    }

    private double computeCorrelationCoefficient(int i, int j) {
        double numerator = 0, magFirst = 0, magSecond = 0;
        for (int u = 0; u < m; u++) {
            if (rating[i][u] == 0 || rating[j][u] == 0) continue;
            numerator += (rating[i][u] - mean[i]) * (rating[j][u] - mean[j]);
            magFirst += (rating[i][u] - mean[i]) * (rating[i][u] - mean[i]);
            magSecond += (rating[j][u] - mean[j]) * (rating[j][u] - mean[j]);
        }
        return numerator / Math.sqrt(magFirst * magSecond);
    }

    private double predict(int i, int u) {
        double numerator = 0, denominator = 0;
        for (int j = 0; j < n; j++) {
            if (rating[j][u] == 0) continue;
            numerator += similarity[i][j] * (rating[j][u] - mean[j]);
            denominator += similarity[i][j];
        }
        return mean[i] + numerator / denominator;
    }

    private double getSongSimilarity(Song a, Song b) {
        Integer i = songIndices.get(a), j = songIndices.get(b);
        return i == null || j == null ? 0 : similarity[i][j];
    }

    @Override
    public double predictRating(Song song, User user) {
        Integer i = songIndices.get(song), u = userIndices.get(user);
        return i == null || u == null ? 0 : predict(i, u);
    }

    @Override
    public List<Song> recommendSongs(User user, int size) {
        return sortSongs(song -> predictRating(song, user), size);
    }

    @Override
    public List<Song> listSimilarSongs(Song song, int size) {
        return sortSongs(s -> getSongSimilarity(song, s), size);
    }

    private List<Song> sortSongs(ToDoubleFunction<Song> extractor, int size) {
        Comparator<Song> comparator = Comparator.comparingDouble(song -> -extractor.applyAsDouble(song));
        return data.allSongs().stream().sorted(comparator).limit(size).toList();
    }
}