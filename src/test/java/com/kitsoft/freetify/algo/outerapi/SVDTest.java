package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.algo.SVD;
import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SVDTest {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        new SVDTest();
    }

    private final Scanner scanner = new Scanner(System.in);

    private Data data;
    private SVD system;
    private Map<Map.Entry<Song, User>, Integer> testData;

    public SVDTest() {
        init();
        tune();
        System.out.println("Interactive testing...");
        showHelp();
        while (true) {
            System.out.print("Type here:  ");
            String command = scanner.nextLine();
            switch (command) {
                case "init" -> init();
                case "tune" -> tune();
                case "help" -> showHelp();
                case "stop" -> System.exit(0);
                default -> {
                    try {
                        String[] split = command.split(" ");
                        switch (split[0]) {
                            case "rate" -> {
                                User user = userByID(Integer.parseInt(split[1]));
                                Song song = songByID(Integer.parseInt(split[2]));
                                System.out.printf("Estimated rating: %.2f", system.estimateRating(song, user) * 0.1);
                                int actualRating = testData.getOrDefault(Map.entry(song, user), data.getRating(song, user));
                                if (actualRating != 0) System.out.printf(" [Actual rating from %s dataset: %.2f]\n",
                                        data.isRated(song, user) ? "train" : "test", actualRating * 0.1);
                                else System.out.println();
                            }
                            case "sim" -> {
                                Song first = songByID(Integer.parseInt(split[1]));
                                Song second = songByID(Integer.parseInt(split[2]));
                                System.out.printf("Estimated song similarity: %.2f\n", system.estimateSongSimilarity(first, second));
                            }
                            case "rec" -> {
                                User user = userByID(Integer.parseInt(split[1]));
                                int size = split.length > 2 ? Integer.parseInt(split[2]) : 5;
                                List<Song> recommendedSongs = system.recommendSongs(user, size);
                                System.out.println("Recommended song IDs: " + recommendedSongs.stream()
                                        .map(i -> "%d (%.2f)".formatted(i.getID(), system.estimateRating(i, user) * 0.1)).toList());
                            }
                            case "list" -> {
                                Song song = songByID(Integer.parseInt(split[1]));
                                int size = split.length > 2 ? Integer.parseInt(split[2]) : 5;
                                List<Song> similarSongs = system.listSimilarSongs(song, size);
                                System.out.println("Similar song IDs: " + similarSongs.stream()
                                        .map(i -> "%d (%.2f)".formatted(i.getID(), system.estimateSongSimilarity(song, i))).toList());
                            }
                            default -> throw new RuntimeException("Unknown command.");
                        }
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    private void showHelp() {
        System.out.println("Possible commands: \"init\", \"tune\", \"help\", \"stop\", \"rate {userID} {songID}\","
                           + " \"sim {songID} {songID}\", \"rec {userID} [size]\", \"list {songID} [size]\".");
    }

    private User userByID(int userID) {
        return data.allUsers().stream().filter(u -> u.getID() == userID).findFirst()
                .orElseThrow(() -> new RuntimeException("There is no user with such ID [" + userID + "]."));
    }

    private Song songByID(int songID) {
        return data.allSongs().stream().filter(i -> i.getID() == songID).findFirst()
                .orElseThrow(() -> new RuntimeException("There is no song with such ID [" + songID + "]."));
    }

    public void init() {
        System.out.println("Initializing...");
        System.out.print("Select dataset size (small/large):  ");
        String line = scanner.nextLine();
        String resource = line.equals("small") ? "ratings-100k.csv" : "ratings-1m.csv";
        System.out.print("Input test dataset percent (0 to 100):  ");
        double testDataPercent = Double.parseDouble(scanner.nextLine());
        timeAction("loading data", () -> loadData(resource, testDataPercent));
        System.out.printf("Unique users: %d, songs: %d, ratings: %d|%d\n",
                data.allUsers().size(), data.allSongs().size(), data.allRatings().size(), testData.size());
        System.out.println();
    }

    public void tune() {
        System.out.println("Tuning...");
        System.out.print("Input separated by a space number of features (50 to 500) + [max iterations, learning rate and regularization]:  ");
        String[] line = scanner.nextLine().split(" ");
        int features = Integer.parseInt(line[0]);
        int maxIterations = line.length > 1 ? Integer.parseInt(line[1]) : 25;
        double learningRate = line.length > 2 ? Double.parseDouble(line[2]) : 0.005;
        double regularization = line.length > 3 ? Double.parseDouble(line[3]) : 0.02;
        timeAction("system tuning", () -> tuneSystem(features, maxIterations, learningRate, regularization));
        checkTestData();
        System.out.println();
    }

    private void timeAction(String name, Runnable action) {
        long time = System.currentTimeMillis();
        action.run();
        System.out.printf("Finished %s in %.2f seconds.\n", name, (System.currentTimeMillis() - time) * 0.001);
    }

    public void loadData(String resource, double testDataPercent) {
        URL url = SVDTest.class.getClassLoader().getResource(resource);
        if (url == null) throw new RuntimeException("Resource \"" + resource + "\" not found.");
        Set<Song> allSongs = new HashSet<>();
        Set<User> allUsers = new HashSet<>();
        Map<Map.Entry<Song, User>, Integer> trainData = new HashMap<>();
        testData = new HashMap<>();
        Random random = new Random();
        int bound = (int) (1000 * testDataPercent), globalBound = 100_000;
        try {
            Files.lines(Path.of(url.toURI())).map(line -> line.split(",")).forEach(split -> {
                int userID = Integer.parseInt(split[0]), songID = Integer.parseInt(split[1]);
                Song song = new SongImpl("Song " + songID, songID);
                User user = new UserImpl("User " + userID, userID);
                allSongs.add(song);
                allUsers.add(user);
                Map.Entry<Song, User> pair = Map.entry(song, user);
                int rating = (int) (10 * Double.parseDouble(split[2]));
                if (random.nextInt(globalBound) < bound) testData.put(pair, rating);
                else trainData.put(pair, rating);
            });
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        data = new DataImpl(new ArrayList<>(allSongs), new ArrayList<>(allUsers), trainData);
    }

    public void tuneSystem(int features, int maxIterations, double learningRate, double regularization) {
        system = new SVD(data, features);
        double error = system.tune(maxIterations, learningRate, regularization);
        System.out.printf("Error on train data: %.2f\n", error * 0.1);
    }

    public void checkTestData() {
        double rmse = 0, mae = 0;
        for (Map.Entry<Map.Entry<Song, User>, Integer> e : testData.entrySet()) {
            Song song = e.getKey().getKey();
            User user = e.getKey().getValue();
            int rating = e.getValue();
            double difference = rating - system.estimateRating(song, user);
            rmse += difference * difference;
            mae += Math.abs(difference);
        }
        rmse = testData.isEmpty() ? 0 : Math.sqrt(rmse / testData.size());
        mae = testData.isEmpty() ? 0 : mae / testData.size();
        System.out.printf("Error on test data: RMSE = %.2f, MAE = %.2f\n", rmse * 0.1, mae * 0.1);
    }
}