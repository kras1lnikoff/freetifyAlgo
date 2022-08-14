package com.kitsoft.freetify.algo;

import com.kitsoft.freetify.outerapi.*;

import java.util.List;

public interface RecommenderSystem {

    Data getData();

    void update();

    double estimateRating(Song song, User user);

    double estimateSongSimilarity(Song first, Song second);

    List<Song> recommendSongs(User user, int size);

    List<Song> listSimilarSongs(Song song, int size);
}
