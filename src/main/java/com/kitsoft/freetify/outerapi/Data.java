package com.kitsoft.freetify.outerapi;

import java.util.List;

public interface Data {

    List<User> allUsers();

    List<Song> allSongs();

    int getRating(User user, Song song);
}
