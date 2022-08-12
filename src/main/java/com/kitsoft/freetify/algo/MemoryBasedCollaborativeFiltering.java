package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class MemoryBasedCollaborativeFiltering implements RecommenderSystem {

    private final Data data;

    private int n;
    private int[][] rating;
    private double[][] similarity;
    private double[] mean;

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
        int m = users.size();
        rating = new int[n = songs.size()][m];
        for (int i = 0; i < n; i++) for (int u = 0; u < m; u++) rating[i][u] = data.getRating(songs.get(i), users.get(u));
        similarity = new double[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) similarity[i][j] = Maths.cosineSimilarity(rating[i], rating[j]);
        mean = new double[n];
        for (int i = 0; i < n; i++) mean[i] = Maths.mean(rating[i]);
        songIndices = new HashMap<>();
        for (int i = 0; i < n; i++) songIndices.put(songs.get(i), i);
        userIndices = new HashMap<>();
        for (int u = 0; u < m; u++) userIndices.put(users.get(u), u);
    }

    private double predict(int i, int u) {
        double numerator = 0, denominator = 0;
        for (int j = 0; j < n; j++) {
            if (rating[j][u] == 0) continue;
            numerator += similarity[i][j] * (rating[u][i] - mean[j]);
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