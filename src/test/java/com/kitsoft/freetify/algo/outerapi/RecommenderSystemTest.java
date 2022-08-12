package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.algo.MemoryBasedCollaborativeFiltering;
import com.kitsoft.freetify.algo.RecommenderSystem;
import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.util.*;

public class RecommenderSystemTest {

    public static void main(String[] args) {
        int n = 5, m = 5;
        List<Song> allSongs = new ArrayList<>();
        List<User> allUsers = new ArrayList<>();
        for (int i = 0; i < n; i++) allSongs.add(new SongImpl("Song " + i, i));
        for (int i = 0; i < m; i++) allUsers.add(new UserImpl("User " + i, i));
        int[][] table = {
                {0, 3, 4, 5, 2},
                {3, 5, 2, 2, 5},
                {5, 3, 0, 4, 3},
                {5, 5, 5, 0, 4},
                {2, 3, 0, 2, 2}
        };
        Map<DataImpl.Pair, Integer> ratings = new HashMap<>();
        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) ratings.put(new DataImpl.Pair(allSongs.get(i), allUsers.get(j)), table[j][i]);
        Data data = new DataImpl(allSongs, allUsers, ratings);
        RecommenderSystem system = new MemoryBasedCollaborativeFiltering(data);
        system.update();
        Song song = allSongs.get(0);
        User user = allUsers.get(0);
        double prediction = system.predictRating(song, user);
        System.out.println("prediction = " + prediction);
    }


}