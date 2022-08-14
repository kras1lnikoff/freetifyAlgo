package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.util.List;
import java.util.Map;

public class DataImpl implements Data {

    private final List<Song> allSongs;
    private final List<User> allUsers;
    private final Map<Map.Entry<Song, User>, Integer> allRatings;

    public DataImpl(List<Song> allSongs, List<User> allUsers, Map<Map.Entry<Song, User>, Integer> allRatings) {
        this.allSongs = allSongs;
        this.allUsers = allUsers;
        this.allRatings = allRatings;
    }

    @Override
    public List<Song> allSongs() {
        return allSongs;
    }

    @Override
    public List<User> allUsers() {
        return allUsers;
    }

    @Override
    public Map<Map.Entry<Song, User>, Integer> allRatings() {
        return allRatings;
    }

    @Override
    public int getRating(Song song, User user) {
        return allRatings.getOrDefault(Map.entry(song, user), 0);
    }

    @Override
    public boolean isRated(Song song, User user) {
        return getRating(song, user) != 0;
    }
}
