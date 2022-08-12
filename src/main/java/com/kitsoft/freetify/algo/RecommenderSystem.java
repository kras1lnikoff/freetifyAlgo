package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.*;

import java.util.List;

public interface RecommenderSystem {

    Data getData();

    void update();

    double predictRating(Song song, User user);

    List<Song> recommendSongs(User user, int size);

    List<Song> listSimilarSongs(Song song, int size);
}
