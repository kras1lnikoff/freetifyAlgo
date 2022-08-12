package com.kitsoft.freetify.algo.outerapi;

import com.kitsoft.freetify.outerapi.Data;
import com.kitsoft.freetify.outerapi.Song;
import com.kitsoft.freetify.outerapi.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataImpl implements Data {

    private final List<Song> allSongs;
    private final List<User> allUsers;
    private final Map<Pair, Integer> ratings;

    public DataImpl(List<Song> allSongs, List<User> allUsers, Map<Pair, Integer> ratings) {
        this.allSongs = allSongs;
        this.allUsers = allUsers;
        this.ratings = ratings;
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
    public int getRating(Song song, User user) {
        return ratings.get(new Pair(song, user));
    }


    public static class Pair {

        private final Song song;
        private final User user;

        public Pair(Song song, User user) {
            this.song = song;
            this.user = user;
        }

        public Song getSong() {
            return song;
        }

        public User getUser() {
            return user;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(song, pair.song) && Objects.equals(user, pair.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(song, user);
        }

        @Override
        public String toString() {
            return "Pair{" +
                   "song=" + song +
                   ", user=" + user +
                   '}';
        }
    }
}
