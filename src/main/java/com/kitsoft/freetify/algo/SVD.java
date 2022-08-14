package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.util.*;

import static com.kitsoft.freetify.algo.Maths.*;

public final class SVD extends AbstractRecommenderSystem {

    private final int features;

    private Map<Song, Integer> songIndices;
    private Map<User, Integer> userIndices;

    private double globalBias;
    private double[] songBias;
    private double[] userBias;
    private double[][] songFeatures;
    private double[][] userFeatures;

    public SVD(Data data, int features) {
        super(data);
        this.features = features;
        setup();
    }

    @Override
    public void update() {
        setup();
        tune();
    }

    private void setup() {
        List<Song> songs = data.allSongs();
        List<User> users = data.allUsers();
        int n = songs.size(), m = users.size();
        songIndices = new HashMap<>(n);
        for (int i = 0; i < n; i++) songIndices.put(songs.get(i), i);
        userIndices = new HashMap<>(m);
        for (int u = 0; u < m; u++) userIndices.put(users.get(u), u);
        songBias = new double[n];
        userBias = new double[m];
        songFeatures = new double[n][features];
        userFeatures = new double[m][features];
        Random random = new Random();
        for (int f = 0; f < features; f++) {
            for (int i = 0; i < n; i++) songFeatures[i][f] = 0.1 * random.nextDouble();
            for (int u = 0; u < m; u++) userFeatures[u][f] = 0.1 * random.nextDouble();
        }
    }

    public double tune() {
        return tune(25, 0.005, 0.02);
    }

    public double tune(int maxIterations, double learningRate, double regularization) {
        List<Integer> songs = new ArrayList<>(), users = new ArrayList<>(), ratings = new ArrayList<>();
        for (Map.Entry<Map.Entry<Song, User>, Integer> e : data.allRatings().entrySet()) {
            Integer i = songIndices.get(e.getKey().getKey()), u = userIndices.get(e.getKey().getValue());
            int rating = e.getValue();
            if (i == null || u == null || rating == 0) continue;
            songs.add(i);
            users.add(u);
            ratings.add(rating);
        }
        if (ratings.size() == 0) return 0;
        double globalError = 0;
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            globalError = 0;
            for (int index = 0; index < ratings.size(); index++) {
                int i = songs.get(index), u = users.get(index), rating = ratings.get(index);
                double error = rating - estimate(i, u);
                globalError += error * error;
                globalBias += learningRate * error;
                songBias[i] += learningRate * (error - regularization * songBias[i]);
                userBias[u] += learningRate * (error - regularization * userBias[u]);
                for (int f = 0; f < features; f++) {
                    double oldSongFeatures = songFeatures[i][f], oldUserFeatures = userFeatures[u][f];
                    songFeatures[i][f] += learningRate * (error * oldUserFeatures - regularization * oldSongFeatures);
                    userFeatures[u][f] += learningRate * (error * oldSongFeatures - regularization * oldUserFeatures);
                }
            }
        }
        return Math.sqrt(globalError / ratings.size());
    }

    private double estimate(int i, int u) {
        return globalBias + songBias[i] + userBias[u] + dotProduct(songFeatures[i], userFeatures[u]);
    }

    @Override
    public double estimateRating(Song song, User user) {
        Integer i = songIndices.get(song), u = userIndices.get(user);
        double estimation = globalBias;
        if (i != null) estimation += songBias[i];
        if (u != null) estimation += userBias[u];
        if (i != null && u != null) estimation += dotProduct(songFeatures[i], userFeatures[u]);
        return estimation;
    }

    @Override
    public double estimateSongSimilarity(Song first, Song second) {
        Integer i = songIndices.get(first), j = songIndices.get(second);
        return i == null || j == null ? 0 : cosineSimilarity(songFeatures[i], songFeatures[j]);
    }
}