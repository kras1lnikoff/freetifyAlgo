package com.kitsoft.freetify.outerapi;

import java.util.List;
import java.util.Map;

public interface Data {

    List<Song> allSongs();

    List<User> allUsers();

    Map<Map.Entry<Song, User>, Integer> allRatings();

    int getRating(Song song, User user);

    boolean isRated(Song song, User user);
}
